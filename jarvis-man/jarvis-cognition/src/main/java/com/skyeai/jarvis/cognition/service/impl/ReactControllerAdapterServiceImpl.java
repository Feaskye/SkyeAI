package com.skyeai.jarvis.cognition.service.impl;

import com.skyeai.jarvis.cognition.service.ReactControllerAdapterService;
import com.skyeai.jarvis.cognition.service.ReactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * React控制器适配器服务实现类
 */
@Service
public class ReactControllerAdapterServiceImpl implements ReactControllerAdapterService {
    
    @Autowired
    private ReactService reactService;
    
    /**
     * 执行ReAct决策流程
     */
    @Override
    public String executeReact(String query) {
        try {
            // 将查询作为目标，空观察列表作为初始观察
            List<String> observations = new ArrayList<>();
            Map<String, Object> context = new HashMap<>();
            
            // 执行ReAct决策过程
            ReactService.ReactResult result = reactService.executeReact(query, observations, context);
            
            // 返回最终答案
            if (result.isSuccess() && result.getFinalAnswer() != null) {
                return result.getFinalAnswer();
            } else {
                // 如果没有成功的最终答案，返回错误信息
                return "抱歉，执行ReAct决策过程失败: " + (result.getErrorMessage() != null ? result.getErrorMessage() : "未知错误");
            }
        } catch (Exception e) {
            // 捕获所有异常，确保服务不会崩溃
            return "抱歉，执行ReAct决策过程时发生错误: " + e.getMessage();
        }
    }
    
    /**
     * 获取工具调用提示
     */
    @Override
    public String getToolCallPrompt(String query) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是贾维斯，一个强大的AI助手。你可以使用以下工具来帮助用户解决问题:\n\n");
        
        // 系统工具
        prompt.append("工具名称: browser\n");
        prompt.append("描述: 控制无头浏览器，抓取/操作网页\n");
        prompt.append("类型: go\n\n");
        
        prompt.append("工具名称: sandbox\n");
        prompt.append("描述: 在安全沙箱中执行代码\n");
        prompt.append("类型: go\n\n");
        
        // 网络工具
        prompt.append("工具名称: http_client\n");
        prompt.append("描述: 发送HTTP请求，获取网页内容或API数据\n");
        prompt.append("类型: java\n\n");
        
        // 数据处理工具
        prompt.append("工具名称: json_processor\n");
        prompt.append("描述: 处理JSON数据，包括解析、验证和转换\n");
        prompt.append("类型: java\n\n");
        
        prompt.append("工具名称: csv_processor\n");
        prompt.append("描述: 处理CSV文件，包括读取、写入和转换\n");
        prompt.append("类型: java\n\n");
        
        // AI增强工具
        prompt.append("工具名称: image_analyzer\n");
        prompt.append("描述: 分析图像内容，识别物体、场景和文本\n");
        prompt.append("类型: java\n\n");
        
        prompt.append("工具名称: speech_processor\n");
        prompt.append("描述: 处理语音数据，包括语音识别和语音合成\n");
        prompt.append("类型: java\n\n");
        
        // 开发工具
        prompt.append("工具名称: code_analyzer\n");
        prompt.append("描述: 分析代码，包括语法检查、风格检查和复杂度分析\n");
        prompt.append("类型: java\n\n");
        
        prompt.append("工具名称: regex_tool\n");
        prompt.append("描述: 使用正则表达式进行文本匹配和提取\n");
        prompt.append("类型: java\n\n");
        
        // 实用工具
        prompt.append("工具名称: calculator\n");
        prompt.append("描述: 执行数学计算，包括基本运算和复杂函数\n");
        prompt.append("类型: java\n\n");
        
        prompt.append("工具名称: date_processor\n");
        prompt.append("描述: 处理日期和时间，包括格式化、计算和转换\n");
        prompt.append("类型: java\n\n");
        
        prompt.append("用户问题: " + query + "\n\n");
        prompt.append("请根据用户的问题，决定是否需要使用工具。如果需要，请以JSON格式返回，包含tool(工具名称)和parameters(参数)字段。\n");
        prompt.append("如果不需要使用工具，可以直接返回答案。\n");
        prompt.append("输出格式示例: {\"tool\": \"browser\", \"parameters\": {\"url\": \"https://github.com\"}}\n");
        
        return prompt.toString();
    }
}
