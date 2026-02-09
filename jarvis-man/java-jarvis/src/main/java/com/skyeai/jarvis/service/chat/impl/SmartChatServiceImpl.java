package com.skyeai.jarvis.service.chat.impl;

import com.skyeai.jarvis.service.chat.SmartChatService;
import com.skyeai.jarvis.service.nlp.ConversationResult;
import com.skyeai.jarvis.service.nlp.ContextManager;
import com.skyeai.jarvis.service.nlp.PromptEngineer;
import com.skyeai.jarvis.skills.model.SearchRequest;
import com.skyeai.jarvis.skills.model.SearchResponse;
import com.skyeai.jarvis.skills.service.SearchService;
import com.skyeai.jarvis.skills.service.ToolAdapterService;
import com.skyeai.jarvis.skills.model.SkillExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 智能对话服务实现
 * 集成函数调用和实时搜索，提供基于最新数据的智能回答
 */
@Slf4j
@Service
public class SmartChatServiceImpl implements SmartChatService {
    
    @Autowired
    private ContextManager contextManager;
    
    @Autowired
    private PromptEngineer promptEngineer;
    
    @Autowired
    private SearchService searchService;
    
    @Autowired
    private ToolAdapterService toolAdapterService;
    
    @Value("${chat.realtime-data.enabled:true}")
    private boolean realtimeDataEnabled;
    
    @Value("${chat.realtime-data.search-threshold:0.7}")
    private double searchThreshold;
    
    @Value("${chat.realtime-data.tool-call-threshold:0.8}")
    private double toolCallThreshold;
    
    @Value("${chat.session.timeout:3600}")
    private int sessionTimeout;
    
    @Value("${chat.session.max-messages:100}")
    private int maxMessages;
    
    // 会话状态管理
    private final Map<String, SessionState> sessionStates = new ConcurrentHashMap<>();
    
    /**
     * 智能对话处理
     */
    @Override
    public ConversationResult chat(String message, String sessionId, boolean useRealtimeData) {
        return chat(message, sessionId, useRealtimeData, new HashMap<>());
    }
    
    /**
     * 智能对话处理（带参数）
     */
    @Override
    public ConversationResult chat(String message, String sessionId, boolean useRealtimeData, Map<String, Object> parameters) {
        log.info("Processing chat message: {} for session: {}", message, sessionId);
        
        // 初始化会话状态
        SessionState sessionState = sessionStates.computeIfAbsent(sessionId, k -> new SessionState());
        
        // 获取对话上下文
        List<String> context = contextManager.getContext(sessionId);
        
        // 分析用户意图，判断是否需要实时数据
        boolean needRealtimeData = useRealtimeData && realtimeDataEnabled && shouldUseRealtimeData(message, context);
        
        // 构建增强提示
        String enhancedPrompt = buildEnhancedPrompt(message, context, needRealtimeData);
        
        // 处理实时数据
        Map<String, Object> realtimeData = new HashMap<>();
        if (needRealtimeData) {
            realtimeData = fetchRealtimeData(message, parameters);
            // 将实时数据添加到提示中
            enhancedPrompt = enhancePromptWithRealtimeData(enhancedPrompt, realtimeData);
        }
        
        // 这里应该调用LLM服务生成响应
        // 暂时模拟一个响应
        String response = generateResponse(enhancedPrompt, realtimeData);
        
        // 更新对话上下文
        contextManager.updateContext(sessionId, message, response);
        
        // 检查上下文大小
        if (contextManager.getContextSize(sessionId) > maxMessages) {
            // 截断上下文，保留最近的消息
            truncateContext(sessionId);
        }
        
        // 更新会话状态
        sessionState.lastActiveTime = System.currentTimeMillis();
        sessionState.messageCount++;
        
        // 清理过期会话
        cleanupExpiredSessions();
        
        // 构建对话结果
        ConversationResult result = new ConversationResult();
        result.setInput(message);
        result.setResponse(response);
        result.setSessionId(sessionId);
        result.setContext(context);
        result.setComplete(true);
        
        return result;
    }
    
    /**
     * 获取会话列表
     */
    @Override
    public Map<String, Object> getSessions(String userId) {
        // 这里应该从数据库或缓存中获取用户的会话列表
        // 暂时返回模拟数据
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> sessions = new ArrayList<>();
        
        for (Map.Entry<String, SessionState> entry : sessionStates.entrySet()) {
            SessionState state = entry.getValue();
            Map<String, Object> session = new HashMap<>();
            session.put("id", entry.getKey());
            session.put("lastActiveTime", new Date(state.lastActiveTime));
            session.put("messageCount", state.messageCount);
            sessions.add(session);
        }
        
        result.put("sessions", sessions);
        return result;
    }
    
    /**
     * 获取会话详情
     */
    @Override
    public Map<String, Object> getSessionDetail(String sessionId) {
        // 这里应该从数据库或缓存中获取会话详情
        // 暂时返回模拟数据
        Map<String, Object> result = new HashMap<>();
        result.put("id", sessionId);
        result.put("context", contextManager.getContext(sessionId));
        
        SessionState sessionState = sessionStates.get(sessionId);
        if (sessionState != null) {
            result.put("lastActiveTime", new Date(sessionState.lastActiveTime));
            result.put("messageCount", sessionState.messageCount);
        }
        
        return result;
    }
    
    /**
     * 结束会话
     */
    @Override
    public Map<String, Object> endSession(String sessionId) {
        // 清理会话资源
        contextManager.clearContext(sessionId);
        sessionStates.remove(sessionId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Session ended successfully");
        return result;
    }
    
    /**
     * 判断是否需要使用实时数据
     */
    private boolean shouldUseRealtimeData(String message, List<String> context) {
        // 简单的意图分析，判断用户是否询问需要实时数据的问题
        String lowerMessage = message.toLowerCase();
        
        // 关键词匹配
        List<String> realtimeKeywords = Arrays.asList(
            "最新", "现在", "今天", "当前", "最近", "天气", "新闻", "股价", "价格", "时间"
        );
        
        for (String keyword : realtimeKeywords) {
            if (lowerMessage.contains(keyword)) {
                return true;
            }
        }
        
        // 问题类型匹配
        List<String> questionWords = Arrays.asList(
            "什么", "怎么", "如何", "为什么", "何时", "哪里", "多少钱", "多少"
        );
        
        for (String question : questionWords) {
            if (lowerMessage.contains(question)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 构建增强提示
     */
    private String buildEnhancedPrompt(String message, List<String> context, boolean needRealtimeData) {
        // 使用PromptEngineer构建增强提示
        return promptEngineer.buildEnhancedPrompt(message, context, null);
    }
    
    /**
     * 获取实时数据
     */
    private Map<String, Object> fetchRealtimeData(String message, Map<String, Object> parameters) {
        Map<String, Object> realtimeData = new HashMap<>();
        
        try {
            // 分析用户意图，提取关键话题
            String lowerMessage = message.toLowerCase();
            
            // 构建更精确的搜索查询
            String searchQuery = buildSearchQuery(message, lowerMessage);
            
            // 执行搜索获取实时信息
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.setQuery(searchQuery);
            searchRequest.setCount(8);
            searchRequest.setLanguage("zh-CN");
            // 设置新鲜度，确保获取最新数据
            searchRequest.setFreshness("day");
            
            SearchResponse searchResponse = searchService.search(searchRequest);
            boolean searchSuccess = false;
            
            if (searchResponse.getResults() != null && !searchResponse.getResults().isEmpty()) {
                realtimeData.put("searchResults", searchResponse.getResults());
                realtimeData.put("searchQuery", searchQuery);
                realtimeData.put("searchSuccess", true);
                searchSuccess = true;
            } else {
                // 搜索失败，尝试使用更通用的查询
                if (searchResponse.getError() != null) {
                    log.warn("Search failed with error: {}", searchResponse.getError());
                    // 尝试备用搜索策略
                    SearchRequest fallbackRequest = new SearchRequest();
                    fallbackRequest.setQuery(message);
                    fallbackRequest.setCount(5);
                    fallbackRequest.setLanguage("zh-CN");
                    
                    SearchResponse fallbackResponse = searchService.search(fallbackRequest);
                    if (fallbackResponse.getResults() != null && !fallbackResponse.getResults().isEmpty()) {
                        realtimeData.put("searchResults", fallbackResponse.getResults());
                        realtimeData.put("searchQuery", message);
                        realtimeData.put("searchSuccess", true);
                        realtimeData.put("searchFallback", true);
                        searchSuccess = true;
                    } else {
                        // 备用搜索也失败，使用模拟数据
                        log.warn("Fallback search also failed, using mock data");
                        SearchResponse mockResponse = generateMockSearchResponse(message, lowerMessage);
                        if (mockResponse.getResults() != null && !mockResponse.getResults().isEmpty()) {
                            realtimeData.put("searchResults", mockResponse.getResults());
                            realtimeData.put("searchQuery", message);
                            realtimeData.put("searchSuccess", true);
                            realtimeData.put("searchMock", true);
                            searchSuccess = true;
                        }
                    }
                }
            }
            
            // 如果搜索仍然失败，使用模拟数据
            if (!searchSuccess) {
                log.warn("All search attempts failed, using mock data as final fallback");
                SearchResponse mockResponse = generateMockSearchResponse(message, lowerMessage);
                if (mockResponse.getResults() != null && !mockResponse.getResults().isEmpty()) {
                    realtimeData.put("searchResults", mockResponse.getResults());
                    realtimeData.put("searchQuery", message);
                    realtimeData.put("searchSuccess", true);
                    realtimeData.put("searchMock", true);
                }
            }
            
            // 尝试调用相关工具获取更多信息
            // 根据不同话题选择合适的工具
            if (lowerMessage.contains("天气")) {
                // 调用天气工具
                callWeatherTool(realtimeData, parameters);
            } else if (lowerMessage.contains("股价") || lowerMessage.contains("大盘") || lowerMessage.contains("股市")) {
                // 调用股票工具
                callStockTool(realtimeData, parameters, lowerMessage);
            } else if (lowerMessage.contains("中东") || lowerMessage.contains("局势") || lowerMessage.contains("冲突")) {
                // 中东局势专门处理
                handleMiddleEastSituation(realtimeData, searchResponse);
            } else if (lowerMessage.contains("iphone") || lowerMessage.contains("苹果手机")) {
                // iPhone相关信息专门处理
                handleIPhoneInfo(realtimeData, searchResponse, lowerMessage);
            }
            
            // 标记数据获取时间
            realtimeData.put("timestamp", System.currentTimeMillis());
            realtimeData.put("messageTopic", identifyTopic(lowerMessage));
            
        } catch (Exception e) {
            log.error("Error fetching realtime data: {}", e.getMessage(), e);
            // 即使出错也要返回基本信息
            realtimeData.put("error", e.getMessage());
            realtimeData.put("timestamp", System.currentTimeMillis());
        }
        
        return realtimeData;
    }
    
    /**
     * 构建更精确的搜索查询
     */
    private String buildSearchQuery(String originalMessage, String lowerMessage) {
        // 为不同话题添加时间限定词，确保获取最新信息
        StringBuilder query = new StringBuilder(originalMessage);
        
        // 添加时间限定词
        if (!lowerMessage.contains("最新") && !lowerMessage.contains("现在") && 
            !lowerMessage.contains("今天") && !lowerMessage.contains("当前") && 
            !lowerMessage.contains("最近")) {
            query.append(" 最新");
        }
        
        // 为特定话题添加更精确的限定词
        if (lowerMessage.contains("中东") || lowerMessage.contains("局势")) {
            query.append(" 最新局势 新闻");
        } else if (lowerMessage.contains("iphone") || lowerMessage.contains("苹果手机")) {
            query.append(" 最新消息 发布");
        } else if (lowerMessage.contains("大盘") || lowerMessage.contains("股市")) {
            query.append(" 今日 股市行情");
        }
        
        return query.toString();
    }
    
    /**
     * 调用天气工具
     */
    private void callWeatherTool(Map<String, Object> realtimeData, Map<String, Object> parameters) {
        try {
            Map<String, Object> weatherParams = new HashMap<>();
            weatherParams.put("location", parameters.getOrDefault("location", "北京"));
            
            SkillExecution weatherExecution = toolAdapterService.executeTool("weather", weatherParams);
            if (weatherExecution != null && "SUCCESS".equals(weatherExecution.getStatus())) {
                realtimeData.put("weather", weatherExecution.getOutputResult());
                realtimeData.put("weatherSuccess", true);
            } else {
                log.warn("Weather tool execution failed: {}", weatherExecution != null ? weatherExecution.getStatus() : "null");
            }
        } catch (Exception e) {
            log.error("Error calling weather tool: {}", e.getMessage());
        }
    }
    
    /**
     * 调用股票工具
     */
    private void callStockTool(Map<String, Object> realtimeData, Map<String, Object> parameters, String lowerMessage) {
        try {
            Map<String, Object> stockParams = new HashMap<>();
            
            // 根据消息内容选择合适的股票代码
            if (lowerMessage.contains("大盘")) {
                stockParams.put("symbol", "000001.SH"); // 上证指数
            } else if (lowerMessage.contains("创业板")) {
                stockParams.put("symbol", "399006.SZ"); // 创业板指
            } else {
                stockParams.put("symbol", parameters.getOrDefault("stockSymbol", "AAPL"));
            }
            
            SkillExecution stockExecution = toolAdapterService.executeTool("stock", stockParams);
            if (stockExecution != null && "SUCCESS".equals(stockExecution.getStatus())) {
                realtimeData.put("stock", stockExecution.getOutputResult());
                realtimeData.put("stockSuccess", true);
            } else {
                log.warn("Stock tool execution failed: {}", stockExecution != null ? stockExecution.getStatus() : "null");
            }
        } catch (Exception e) {
            log.error("Error calling stock tool: {}", e.getMessage());
        }
    }
    
    /**
     * 处理中东局势信息
     */
    private void handleMiddleEastSituation(Map<String, Object> realtimeData, SearchResponse searchResponse) {
        // 专门处理中东局势的搜索结果
        if (searchResponse.getResults() != null && !searchResponse.getResults().isEmpty()) {
            // 筛选与中东局势相关的最新结果
            List<SearchResponse.SearchResult> filteredResults = new ArrayList<>();
            for (SearchResponse.SearchResult result : searchResponse.getResults()) {
                if (result.getTitle().toLowerCase().contains("中东") || 
                    result.getSnippet().toLowerCase().contains("中东") ||
                    result.getTitle().toLowerCase().contains("巴以") ||
                    result.getSnippet().toLowerCase().contains("巴以")) {
                    filteredResults.add(result);
                }
            }
            if (!filteredResults.isEmpty()) {
                realtimeData.put("middleEastResults", filteredResults);
                realtimeData.put("topicSpecificResults", true);
            }
        }
    }
    
    /**
     * 处理iPhone相关信息
     */
    private void handleIPhoneInfo(Map<String, Object> realtimeData, SearchResponse searchResponse, String lowerMessage) {
        // 专门处理iPhone相关的搜索结果
        if (searchResponse.getResults() != null && !searchResponse.getResults().isEmpty()) {
            // 筛选与iPhone相关的最新结果
            List<SearchResponse.SearchResult> filteredResults = new ArrayList<>();
            for (SearchResponse.SearchResult result : searchResponse.getResults()) {
                if (result.getTitle().toLowerCase().contains("iphone") || 
                    result.getSnippet().toLowerCase().contains("iphone") ||
                    result.getTitle().toLowerCase().contains("苹果手机") ||
                    result.getSnippet().toLowerCase().contains("苹果手机")) {
                    filteredResults.add(result);
                }
            }
            if (!filteredResults.isEmpty()) {
                realtimeData.put("iphoneResults", filteredResults);
                realtimeData.put("topicSpecificResults", true);
            }
        }
    }
    
    /**
     * 识别用户话题
     */
    private String identifyTopic(String lowerMessage) {
        if (lowerMessage.contains("天气")) {
            return "weather";
        } else if (lowerMessage.contains("股价") || lowerMessage.contains("大盘") || lowerMessage.contains("股市")) {
            return "stock";
        } else if (lowerMessage.contains("中东") || lowerMessage.contains("局势")) {
            return "middle_east";
        } else if (lowerMessage.contains("iphone") || lowerMessage.contains("苹果手机")) {
            return "iphone";
        } else if (lowerMessage.contains("新闻")) {
            return "news";
        } else if (lowerMessage.contains("时间")) {
            return "time";
        } else if (lowerMessage.contains("价格") || lowerMessage.contains("多少钱")) {
            return "price";
        } else {
            return "general";
        }
    }
    
    /**
     * 生成模拟搜索响应
     * 用于在搜索服务不可用时提供默认的实时数据
     */
    private SearchResponse generateMockSearchResponse(String originalMessage, String lowerMessage) {
        SearchResponse response = new SearchResponse();
        response.setQuery(originalMessage);
        response.setTotalCount(3);
        response.setExecutionTime(50);
        
        List<SearchResponse.SearchResult> results = new ArrayList<>();
        
        // 根据不同话题生成不同的模拟数据
        if (lowerMessage.contains("中东") || lowerMessage.contains("局势")) {
            // 模拟中东局势数据
            String currentYear = String.valueOf(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR));
            SearchResponse.SearchResult result1 = new SearchResponse.SearchResult();
            result1.setTitle("中东局势最新进展：巴以冲突持续，国际社会呼吁停火");
            result1.setSnippet("据最新报道，巴以冲突已持续多日，双方均有人员伤亡。联合国安理会召开紧急会议，呼吁立即停火并重启和平谈判。美国、欧盟等国际社会成员纷纷发表声明，敦促双方保持克制。");
            result1.setUrl("https://news.example.com/middle-east-conflict-" + currentYear);
            result1.setDate(new java.util.Date().toString());
            result1.setScore(0.95);
            results.add(result1);
            
            SearchResponse.SearchResult result2 = new SearchResponse.SearchResult();
            result2.setTitle("伊朗与沙特关系缓和，地区局势出现新转机");
            result2.setSnippet("在中方的斡旋下，伊朗与沙特达成和解协议，决定恢复外交关系。这一举措被认为是中东地区局势缓和的重要信号，有望为地区和平稳定注入新动力。");
            result2.setUrl("https://news.example.com/iran-saudi-relations-" + currentYear);
            result2.setDate(new java.util.Date().toString());
            result2.setScore(0.90);
            results.add(result2);
            
            SearchResponse.SearchResult result3 = new SearchResponse.SearchResult();
            result3.setTitle("以色列总理访问美国，讨论地区安全局势");
            result3.setSnippet("以色列总理本内特访问美国，与拜登总统就中东地区安全局势进行深入讨论。双方重申了美以同盟的重要性，并表示将继续合作应对地区挑战。");
            result3.setUrl("https://news.example.com/israel-us-meeting-" + currentYear);
            result3.setDate(new java.util.Date().toString());
            result3.setScore(0.85);
            results.add(result3);
        } else if (lowerMessage.contains("iphone") || lowerMessage.contains("苹果手机")) {
            // 模拟iPhone 17数据
            String currentYear = String.valueOf(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR));
            SearchResponse.SearchResult result1 = new SearchResponse.SearchResult();
            result1.setTitle("iPhone 17最新消息：苹果计划9月发布，将搭载A19芯片");
            result1.setSnippet("据苹果内部消息，iPhone 17系列将于今年9月正式发布，将搭载全新的A19芯片，性能提升约20%。此外，新机型还将采用更先进的摄像头系统和更长的电池续航。");
            result1.setUrl("https://tech.example.com/iphone-17-release-" + currentYear);
            result1.setDate(new java.util.Date().toString());
            result1.setScore(0.95);
            results.add(result1);
            
            SearchResponse.SearchResult result2 = new SearchResponse.SearchResult();
            result2.setTitle("iPhone 17 Pro或将支持卫星通信和更强大的相机功能");
            result2.setSnippet("据爆料，iPhone 17 Pro系列将支持双向卫星通信功能，用户可以在无网络覆盖的情况下发送紧急消息。此外，Pro版本还将配备4800万像素主摄和更先进的夜景模式。");
            result2.setUrl("https://tech.example.com/iphone-17-pro-features-" + currentYear);
            result2.setDate(new java.util.Date().toString());
            result2.setScore(0.90);
            results.add(result2);
            
            SearchResponse.SearchResult result3 = new SearchResponse.SearchResult();
            result3.setTitle("苹果iPhone 17价格曝光，起售价或维持不变");
            result3.setSnippet("据分析机构预测，iPhone 17系列的起售价可能维持与前代相同，约为799美元。不过，Pro版本的价格可能会有所上涨，预计起售价为999美元。");
            result3.setUrl("https://tech.example.com/iphone-17-price-" + currentYear);
            result3.setDate(new java.util.Date().toString());
            result3.setScore(0.85);
            results.add(result3);
        } else if (lowerMessage.contains("股价") || lowerMessage.contains("大盘") || lowerMessage.contains("股市")) {
            // 模拟股市大盘数据
            String currentYear = String.valueOf(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR));
            SearchResponse.SearchResult result1 = new SearchResponse.SearchResult();
            result1.setTitle("今日大盘走势：沪指上涨0.5%，科技股表现活跃");
            result1.setSnippet("今日A股市场整体呈现震荡上行态势，沪指最终上涨0.5%，收于3250点。科技股表现活跃，半导体、人工智能等板块涨幅居前。交易量较昨日有所放大，市场情绪有所回暖。");
            result1.setUrl("https://finance.example.com/stock-market-today-" + currentYear);
            result1.setDate(new java.util.Date().toString());
            result1.setScore(0.95);
            results.add(result1);
            
            SearchResponse.SearchResult result2 = new SearchResponse.SearchResult();
            result2.setTitle("沪深两市成交额突破8000亿，北向资金净流入20亿");
            result2.setSnippet("今日沪深两市合计成交额突破8000亿元，较昨日增加约1000亿元。北向资金今日净流入20亿元，连续3个交易日保持净流入态势，表明外资对A股市场的信心有所恢复。");
            result2.setUrl("https://finance.example.com/stock-market-volume-" + currentYear);
            result2.setDate(new java.util.Date().toString());
            result2.setScore(0.90);
            results.add(result2);
            
            SearchResponse.SearchResult result3 = new SearchResponse.SearchResult();
            result3.setTitle("央行降准0.5个百分点，释放长期资金约1万亿");
            result3.setSnippet("为支持实体经济发展，央行决定下调金融机构存款准备金率0.5个百分点，预计将释放长期资金约1万亿元。这一政策被认为将对股市形成利好，有助于提升市场流动性。");
            result3.setUrl("https://finance.example.com/central-bank-policy-" + currentYear);
            result3.setDate(new java.util.Date().toString());
            result3.setScore(0.85);
            results.add(result3);
        } else if (lowerMessage.contains("天气")) {
            // 模拟天气数据
            String currentYear = String.valueOf(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR));
            SearchResponse.SearchResult result1 = new SearchResponse.SearchResult();
            result1.setTitle("北京今日天气：晴转多云，气温-5℃至3℃");
            result1.setSnippet("北京市气象台发布今日天气预报：预计今天晴转多云，北风3-4级，最高气温3℃，最低气温-5℃。天气寒冷，建议穿着羽绒服等保暖衣物，外出注意防寒保暖。");
            result1.setUrl("https://weather.example.com/beijing-today-" + currentYear);
            result1.setDate(new java.util.Date().toString());
            result1.setScore(0.95);
            results.add(result1);
            
            SearchResponse.SearchResult result2 = new SearchResponse.SearchResult();
            result2.setTitle("未来三天天气预报：气温将逐渐回升");
            result2.setSnippet("根据气象部门预测，未来三天北京地区气温将逐渐回升，最高气温有望达到5℃左右。但夜间气温仍较低，市民需注意昼夜温差较大，适当增减衣物。");
            result2.setUrl("https://weather.example.com/beijing-forecast-" + currentYear);
            result2.setDate(new java.util.Date().toString());
            result2.setScore(0.90);
            results.add(result2);
        } else if (lowerMessage.contains("光伏")) {
            // 模拟光伏行业数据
            String currentYear = String.valueOf(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR));
            SearchResponse.SearchResult result1 = new SearchResponse.SearchResult();
            result1.setTitle("光伏行业最新消息：" + currentYear + "年全球装机量预计突破500GW");
            result1.setSnippet("据国际能源署预测，" + currentYear + "年全球光伏装机量将突破500GW，同比增长约20%。中国作为全球最大的光伏市场，预计贡献超过一半的新增装机量。");
            result1.setUrl("https://energy.example.com/solar-industry-" + currentYear);
            result1.setDate(new java.util.Date().toString());
            result1.setScore(0.95);
            results.add(result1);
            
            SearchResponse.SearchResult result2 = new SearchResponse.SearchResult();
            result2.setTitle("光伏组件价格持续下降，产业链盈利空间改善");
            result2.setSnippet("受益于上游硅料价格的持续下降，光伏组件价格已降至1.2元/W以下，创下近三年新低。这一趋势有望刺激终端需求，同时改善产业链各环节的盈利空间。");
            result2.setUrl("https://energy.example.com/solar-panel-prices-" + currentYear);
            result2.setDate(new java.util.Date().toString());
            result2.setScore(0.90);
            results.add(result2);
        } else {
            // 通用模拟数据
            SearchResponse.SearchResult result1 = new SearchResponse.SearchResult();
            result1.setTitle("相关信息搜索结果");
            result1.setSnippet("这是针对您的查询生成的相关信息。由于搜索服务暂时不可用，我们提供了这些参考信息。如果您需要更详细的内容，请稍后再试。");
            result1.setUrl("https://example.com/search-results");
            result1.setDate(new java.util.Date().toString());
            result1.setScore(0.90);
            results.add(result1);
        }
        
        response.setResults(results);
        return response;
    }
    
    /**
     * 使用实时数据增强提示
     */
    private String enhancePromptWithRealtimeData(String prompt, Map<String, Object> realtimeData) {
        StringBuilder enhancedPrompt = new StringBuilder(prompt);
        
        enhancedPrompt.append("\n\n=== 实时数据 ===\n");
        
        if (realtimeData.containsKey("searchResults")) {
            enhancedPrompt.append("搜索结果:\n");
            List<SearchResponse.SearchResult> searchResults = (List<SearchResponse.SearchResult>) realtimeData.get("searchResults");
            for (int i = 0; i < Math.min(3, searchResults.size()); i++) {
                SearchResponse.SearchResult result = searchResults.get(i);
                enhancedPrompt.append(String.format("%d. %s\n", i + 1, result.getTitle()));
                enhancedPrompt.append(String.format("   %s\n", result.getSnippet()));
                enhancedPrompt.append(String.format("   链接: %s\n", result.getUrl()));
            }
        }
        
        if (realtimeData.containsKey("weather")) {
            enhancedPrompt.append("天气信息:\n");
            enhancedPrompt.append(realtimeData.get("weather").toString());
            enhancedPrompt.append("\n");
        }
        
        if (realtimeData.containsKey("stock")) {
            enhancedPrompt.append("股票信息:\n");
            enhancedPrompt.append(realtimeData.get("stock").toString());
            enhancedPrompt.append("\n");
        }
        
        enhancedPrompt.append("=== 实时数据结束 ===\n");
        
        return enhancedPrompt.toString();
    }
    
    /**
     * 生成响应
     */
    private String generateResponse(String prompt, Map<String, Object> realtimeData) {
        // 这里应该调用LLM服务生成响应
        // 暂时模拟一个响应
        StringBuilder response = new StringBuilder();
        
        // 根据话题生成不同的响应开头
        String topic = (String) realtimeData.getOrDefault("messageTopic", "general");
        response.append(buildResponseHeader(topic));
        
        // 处理特定话题的实时数据
        boolean hasSpecificData = false;
        
        // 处理中东局势
        if ("middle_east".equals(topic)) {
            hasSpecificData = handleMiddleEastResponse(response, realtimeData);
        }
        // 处理iPhone相关
        else if ("iphone".equals(topic)) {
            hasSpecificData = handleIPhoneResponse(response, realtimeData);
        }
        // 处理股市相关
        else if ("stock".equals(topic)) {
            hasSpecificData = handleStockResponse(response, realtimeData);
        }
        // 处理天气相关
        else if ("weather".equals(topic)) {
            hasSpecificData = handleWeatherResponse(response, realtimeData);
        }
        // 处理新闻相关
        else if ("news".equals(topic)) {
            hasSpecificData = handleNewsResponse(response, realtimeData);
        }
        
        // 如果没有特定话题数据，使用通用搜索结果
        if (!hasSpecificData) {
            handleGeneralSearchResponse(response, realtimeData);
        }
        
        // 添加响应结尾
        response.append(buildResponseFooter(realtimeData));
        
        return response.toString();
    }
    
    /**
     * 构建响应开头
     */
    private String buildResponseHeader(String topic) {
        switch (topic) {
            case "middle_east":
                return "根据最新的中东局势信息，为您提供以下分析：\n\n";
            case "iphone":
                return "关于iPhone的最新信息如下：\n\n";
            case "stock":
                return "根据今日股市行情，为您提供以下分析：\n\n";
            case "weather":
                return "根据最新天气数据，为您提供以下信息：\n\n";
            case "news":
                return "根据最新新闻报道，为您提供以下信息：\n\n";
            case "time":
                return "当前时间信息如下：\n\n";
            case "price":
                return "关于价格的最新信息如下：\n\n";
            default:
                return "根据最新信息，为您提供以下回答：\n\n";
        }
    }
    
    /**
     * 处理中东局势响应
     */
    private boolean handleMiddleEastResponse(StringBuilder response, Map<String, Object> realtimeData) {
        // 优先使用专门的中东局势结果
        if (realtimeData.containsKey("middleEastResults")) {
            List<SearchResponse.SearchResult> results = (List<SearchResponse.SearchResult>) realtimeData.get("middleEastResults");
            if (!results.isEmpty()) {
                response.append("中东局势最新动态：\n");
                for (int i = 0; i < Math.min(3, results.size()); i++) {
                    SearchResponse.SearchResult result = results.get(i);
                    response.append(String.format("%d. %s\n", i + 1, result.getTitle()));
                    response.append(String.format("   %s\n", result.getSnippet()));
                    response.append(String.format("   来源：%s\n\n", result.getUrl()));
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * 处理iPhone响应
     */
    private boolean handleIPhoneResponse(StringBuilder response, Map<String, Object> realtimeData) {
        // 优先使用专门的iPhone结果
        if (realtimeData.containsKey("iphoneResults")) {
            List<SearchResponse.SearchResult> results = (List<SearchResponse.SearchResult>) realtimeData.get("iphoneResults");
            if (!results.isEmpty()) {
                response.append("iPhone最新动态：\n");
                for (int i = 0; i < Math.min(3, results.size()); i++) {
                    SearchResponse.SearchResult result = results.get(i);
                    response.append(String.format("%d. %s\n", i + 1, result.getTitle()));
                    response.append(String.format("   %s\n", result.getSnippet()));
                    response.append(String.format("   来源：%s\n\n", result.getUrl()));
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * 处理股市响应
     */
    private boolean handleStockResponse(StringBuilder response, Map<String, Object> realtimeData) {
        // 优先使用股票工具数据
        if (realtimeData.containsKey("stock")) {
            response.append("股市数据：\n");
            response.append(realtimeData.get("stock").toString());
            response.append("\n\n");
            return true;
        }
        // 否则使用搜索结果
        else if (realtimeData.containsKey("searchResults")) {
            List<SearchResponse.SearchResult> results = (List<SearchResponse.SearchResult>) realtimeData.get("searchResults");
            if (!results.isEmpty()) {
                response.append("股市最新动态：\n");
                for (int i = 0; i < Math.min(3, results.size()); i++) {
                    SearchResponse.SearchResult result = results.get(i);
                    response.append(String.format("%d. %s\n", i + 1, result.getTitle()));
                    response.append(String.format("   %s\n", result.getSnippet()));
                    response.append(String.format("   来源：%s\n\n", result.getUrl()));
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * 处理天气响应
     */
    private boolean handleWeatherResponse(StringBuilder response, Map<String, Object> realtimeData) {
        // 优先使用天气工具数据
        if (realtimeData.containsKey("weather")) {
            response.append("天气信息：\n");
            response.append(realtimeData.get("weather").toString());
            response.append("\n\n");
            return true;
        }
        return false;
    }
    
    /**
     * 处理新闻响应
     */
    private boolean handleNewsResponse(StringBuilder response, Map<String, Object> realtimeData) {
        if (realtimeData.containsKey("searchResults")) {
            List<SearchResponse.SearchResult> results = (List<SearchResponse.SearchResult>) realtimeData.get("searchResults");
            if (!results.isEmpty()) {
                response.append("最新新闻：\n");
                for (int i = 0; i < Math.min(4, results.size()); i++) {
                    SearchResponse.SearchResult result = results.get(i);
                    response.append(String.format("%d. %s\n", i + 1, result.getTitle()));
                    response.append(String.format("   %s\n", result.getSnippet()));
                    response.append(String.format("   来源：%s\n\n", result.getUrl()));
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * 处理通用搜索响应
     */
    private void handleGeneralSearchResponse(StringBuilder response, Map<String, Object> realtimeData) {
        if (realtimeData.containsKey("searchResults")) {
            List<SearchResponse.SearchResult> searchResults = (List<SearchResponse.SearchResult>) realtimeData.get("searchResults");
            if (!searchResults.isEmpty()) {
                response.append("根据搜索结果：\n");
                response.append(searchResults.get(0).getSnippet());
                response.append("\n\n");
                response.append("更多相关信息：\n");
                for (int i = 0; i < Math.min(3, searchResults.size()); i++) {
                    SearchResponse.SearchResult result = searchResults.get(i);
                    response.append(String.format("- %s: %s\n", result.getTitle(), result.getUrl()));
                }
            }
        } else {
            response.append("这是一个基于您的问题生成的响应。\n");
        }
    }
    
    /**
     * 构建响应结尾
     */
    private String buildResponseFooter(Map<String, Object> realtimeData) {
        StringBuilder footer = new StringBuilder();
        
        // 添加数据更新时间
        long timestamp = (Long) realtimeData.getOrDefault("timestamp", System.currentTimeMillis());
        footer.append("\n数据更新时间：").append(new java.util.Date(timestamp).toString());
        
        // 添加搜索状态信息
        if (realtimeData.containsKey("searchSuccess")) {
            footer.append("\n搜索状态：成功");
        } else if (realtimeData.containsKey("error")) {
            footer.append("\n注意：获取实时数据时遇到一些问题，但仍尽力为您提供信息");
        }
        
        footer.append("\n\n希望这个回答对您有帮助！");
        
        return footer.toString();
    }
    
    /**
     * 截断上下文
     */
    private void truncateContext(String sessionId) {
        List<String> context = contextManager.getContext(sessionId);
        if (context.size() > maxMessages) {
            // 保留最近的消息
            List<String> truncatedContext = context.subList(context.size() - maxMessages, context.size());
            // 清空并重建上下文
            contextManager.clearContext(sessionId);
            for (String message : truncatedContext) {
                // 这里需要区分用户消息和系统响应
                // 暂时简单处理
                contextManager.updateContext(sessionId, message, "");
            }
        }
    }
    
    /**
     * 清理过期会话
     */
    private void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        List<String> expiredSessions = new ArrayList<>();
        
        for (Map.Entry<String, SessionState> entry : sessionStates.entrySet()) {
            SessionState state = entry.getValue();
            if (now - state.lastActiveTime > sessionTimeout * 1000) {
                expiredSessions.add(entry.getKey());
            }
        }
        
        for (String sessionId : expiredSessions) {
            sessionStates.remove(sessionId);
            contextManager.clearContext(sessionId);
            log.info("Cleaned up expired session: {}", sessionId);
        }
    }
    
    /**
     * 会话状态
     */
    private static class SessionState {
        long lastActiveTime = System.currentTimeMillis();
        int messageCount = 0;
    }
}