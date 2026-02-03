package com.skyeai.jarvis.cognition.service.impl;

import com.skyeai.jarvis.cognition.service.ReactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ReactServiceImpl implements ReactService {

    private final RestTemplate restTemplate;

    @Value("${cognition.react.enabled:true}")
    private boolean reactEnabled;

    @Value("${cognition.react.max_steps:5}")
    private int maxSteps;

    @Value("${cognition.react.timeout_seconds:300}")
    private int timeoutSeconds;

    @Value("${cognition.react.temperature:0.7}")
    private double temperature;

    @Value("${cognition.react.model:gpt-4-turbo}")
    private String model;

    @Value("${llm.openai.api-key}")
    private String openaiApiKey;

    @Value("${llm.openai.base-url:https://api.openai.com/v1}")
    private String openaiBaseUrl;

    @Value("${jarvis.services.llm.url:http://localhost:8081/api/llm}")
    private String llmServiceUrl;

    @Autowired
    public ReactServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public ReactResult executeReact(String goal, List<String> observations) {
        return executeReact(goal, observations, new HashMap<>());
    }

    @Override
    public ReactResult executeReact(String goal, List<String> observations, Map<String, Object> context) {
        ReactResult result = new ReactResult();
        result.setGoal(goal);
        result.setSteps(new ArrayList<>());

        long startTime = System.currentTimeMillis();

        try {
            if (!reactEnabled) {
                result.setSuccess(false);
                result.setErrorMessage("ReAct is disabled");
                return result;
            }

            int stepNumber = 1;
            boolean continueProcessing = true;

            while (continueProcessing && stepNumber <= maxSteps) {
                ReactStepResult stepResult = executeReactStep(goal, observations, stepNumber, context);
                if (stepResult.getStep() != null) {
                    result.getSteps().add(stepResult.getStep());
                }
                if (!stepResult.isContinueProcessing()) {
                    continueProcessing = false;
                    if (stepResult.getStep() != null && stepResult.getStep().getAction().equals("finish")) {
                        result.setFinalAnswer(stepResult.getStep().getObservation());
                        result.setSuccess(true);
                    } else if (stepResult.getErrorMessage() != null) {
                        result.setSuccess(false);
                        result.setErrorMessage(stepResult.getErrorMessage());
                    }
                } else {
                    stepNumber++;
                }
            }

            if (stepNumber > maxSteps && !result.isSuccess()) {
                result.setSuccess(false);
                result.setErrorMessage("Max steps reached without finishing");
            }

            result.setTotalSteps(result.getSteps().size());
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage("Error executing ReAct: " + e.getMessage());
            e.printStackTrace();
        } finally {
            result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        }

        return result;
    }

    @Override
    public ReactStepResult executeReactStep(String goal, List<String> observations, int stepNumber, Map<String, Object> context) {
        ReactStepResult result = new ReactStepResult();
        result.setStepNumber(stepNumber);

        try {
            // 构建ReAct提示
            String prompt = buildReactPrompt(goal, observations, stepNumber, context);

            // 调用大模型生成思考、行动和观察
            String llmResponse = callOpenAiApi(prompt);

            // 解析大模型响应
            ReactStep step = parseReactStepResponse(llmResponse, stepNumber);
            result.setStep(step);

            // 处理行动
            if (step.getAction().equals("finish")) {
                result.setContinueProcessing(false);
            } else if (step.getAction().equals("think")) {
                // 继续思考，不需要执行具体行动
                result.setContinueProcessing(true);
            } else {
                // 执行具体行动，获取观察结果
                String observation = executeAction(step.getAction(), step.getActionInput(), context);
                step.setObservation(observation);
                result.setContinueProcessing(true);
            }

        } catch (Exception e) {
            result.setContinueProcessing(false);
            result.setErrorMessage("Error executing ReAct step: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public boolean validateReactResult(ReactResult result, String goal) {
        if (!result.isSuccess()) {
            return false;
        }

        // 简单验证：检查最终答案是否与目标相关
        String finalAnswer = result.getFinalAnswer();
        if (finalAnswer == null || finalAnswer.isEmpty()) {
            return false;
        }

        // 可以添加更复杂的验证逻辑，如使用大模型评估答案质量
        return true;
    }

    @Override
    public String optimizeReactPrompt(String goal, List<String> observations) {
        // 构建优化后的ReAct提示
        return buildReactPrompt(goal, observations, 1, new HashMap<>());
    }

    @Override
    public Map<String, Object> analyzeReactProcess(ReactResult result) {
        Map<String, Object> analysis = new HashMap<>();

        analysis.put("success", result.isSuccess());
        analysis.put("totalSteps", result.getTotalSteps());
        analysis.put("executionTimeMs", result.getExecutionTimeMs());
        analysis.put("averageStepTimeMs", result.getTotalSteps() > 0 ? 
                result.getExecutionTimeMs() / result.getTotalSteps() : 0);

        // 分析行动分布
        Map<String, Integer> actionDistribution = new HashMap<>();
        for (ReactStep step : result.getSteps()) {
            String action = step.getAction();
            actionDistribution.put(action, actionDistribution.getOrDefault(action, 0) + 1);
        }
        analysis.put("actionDistribution", actionDistribution);

        // 分析思考长度
        List<Integer> thoughtLengths = new ArrayList<>();
        for (ReactStep step : result.getSteps()) {
            if (step.getThought() != null) {
                thoughtLengths.add(step.getThought().length());
            }
        }
        analysis.put("thoughtLengths", thoughtLengths);
        if (!thoughtLengths.isEmpty()) {
            int avgThoughtLength = thoughtLengths.stream().mapToInt(Integer::intValue).sum() / thoughtLengths.size();
            analysis.put("averageThoughtLength", avgThoughtLength);
        }

        return analysis;
    }

    /**
     * 构建ReAct提示
     */
    private String buildReactPrompt(String goal, List<String> observations, int stepNumber, Map<String, Object> context) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一个使用ReAct模式解决问题的助手。\n");
        prompt.append("你的目标是：" + goal + "\n\n");

        if (!observations.isEmpty()) {
            prompt.append("初始观察：\n");
            for (String observation : observations) {
                prompt.append("- " + observation + "\n");
            }
            prompt.append("\n");
        }

        prompt.append("请按照以下格式进行每一步：\n");
        prompt.append("步骤 [步骤编号]：\n");
        prompt.append("思考：[你的推理过程]\n");
        prompt.append("行动：[think | finish | [其他行动]]\n");
        prompt.append("行动输入：[行动的输入]\n");
        prompt.append("观察：[行动的结果]\n\n");

        prompt.append("可能的行动：\n");
        prompt.append("- think：继续推理\n");
        prompt.append("- finish：提供最终答案\n");
        prompt.append("- search：搜索信息\n");
        prompt.append("- calculate：执行计算\n");
        prompt.append("- ask：请求澄清\n");
        prompt.append("- evaluate：评估解决方案\n");

        prompt.append("\n当前步骤：" + stepNumber + "\n");
        prompt.append("请按照指定格式生成你的响应，使用中文。");

        return prompt.toString();
    }

    /**
     * 调用LLM服务
     */
    private String callOpenAiApi(String prompt) throws Exception {
        try {
            // 调用jarvis-llm服务的generate/text接口
            String url = llmServiceUrl + "/generate/text";
            System.out.println("调用LLM服务: " + url);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("prompt", prompt);
            requestBody.put("systemPrompt", "你是一个使用ReAct模式解决问题的助手。请用中文回应用户的问题。");
            
            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
            String result = response.get("result").toString();
            System.out.println("LLM服务响应: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("LLM服务调用失败，使用模拟响应: " + e.getMessage());
            // 返回模拟响应
            return generateMockResponse(prompt);
        }
    }

    /**
     * 生成模拟响应
     */
    private String generateMockResponse(String prompt) {
        StringBuilder mockResponse = new StringBuilder();
        mockResponse.append("Step 1:\n");
        mockResponse.append("Thought: 分析用户问题，思考如何回答\n");
        mockResponse.append("Action: finish\n");
        mockResponse.append("Action Input: 用户查询内容\n");
        mockResponse.append("Observation: 已分析用户问题，准备提供答案\n");
        return mockResponse.toString();
    }

    /**
     * 解析ReAct步骤响应
     */
    private ReactStep parseReactStepResponse(String response, int stepNumber) {
        ReactStep step = new ReactStep();
        step.setStepNumber(stepNumber);

        long stepStartTime = System.currentTimeMillis();

        // 简单解析响应，提取思考、行动和行动输入
        String[] lines = response.split("\\n");

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Thought:") || line.startsWith("思考：") || line.startsWith("思考:")) {
                int index = Math.max(line.indexOf(":"), line.indexOf("："));
                step.setThought(line.substring(index + 1).trim());
            } else if (line.startsWith("Action:") || line.startsWith("行动：") || line.startsWith("行动:")) {
                int index = Math.max(line.indexOf(":"), line.indexOf("："));
                step.setAction(line.substring(index + 1).trim());
            } else if (line.startsWith("Action Input:") || line.startsWith("行动输入：") || line.startsWith("行动输入:")) {
                int index = Math.max(line.indexOf(":"), line.indexOf("："));
                step.setActionInput(line.substring(index + 1).trim());
            } else if (line.startsWith("Observation:") || line.startsWith("观察：") || line.startsWith("观察:")) {
                int index = Math.max(line.indexOf(":"), line.indexOf("："));
                step.setObservation(line.substring(index + 1).trim());
            }
        }

        // 设置默认值
        if (step.getThought() == null) {
            step.setThought("正在分析问题...");
        }
        if (step.getAction() == null) {
            step.setAction("think");
        }
        if (step.getActionInput() == null) {
            step.setActionInput("");
        }

        step.setStepTimeMs(System.currentTimeMillis() - stepStartTime);

        return step;
    }

    /**
     * 执行行动
     */
    private String executeAction(String action, String actionInput, Map<String, Object> context) {
        switch (action.toLowerCase()) {
            case "search":
                return "Search results for '" + actionInput + "': [Simulated search results]";
            case "calculate":
                return "Calculation result: [Simulated calculation result]";
            case "ask":
                return "Clarification: [Simulated clarification]";
            case "evaluate":
                return "Evaluation: [Simulated evaluation]";
            default:
                return "Action executed: " + action + " with input: " + actionInput;
        }
    }
}
