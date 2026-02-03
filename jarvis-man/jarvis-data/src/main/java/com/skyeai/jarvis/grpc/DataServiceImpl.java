package com.skyeai.jarvis.grpc;

import com.skyeai.jarvis.model.*;
import com.skyeai.jarvis.protobuf.*;
import com.skyeai.jarvis.service.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * gRPC数据服务实现类
 */
@GrpcService
public class DataServiceImpl extends DataServiceGrpc.DataServiceImplBase {

    private final ChatHistoryService chatHistoryService;
    private final StockPriceService stockPriceService;
    private final PriceChangeInfoService priceChangeInfoService;
    private final ScheduleEventService scheduleEventService;
    private final UserPreferenceService userPreferenceService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    public DataServiceImpl(ChatHistoryService chatHistoryService, 
                          StockPriceService stockPriceService, 
                          PriceChangeInfoService priceChangeInfoService, 
                          ScheduleEventService scheduleEventService, 
                          UserPreferenceService userPreferenceService) {
        this.chatHistoryService = chatHistoryService;
        this.stockPriceService = stockPriceService;
        this.priceChangeInfoService = priceChangeInfoService;
        this.scheduleEventService = scheduleEventService;
        this.userPreferenceService = userPreferenceService;
    }

    // 聊天历史相关方法
    @Override
    public void saveChatHistory(SaveChatHistoryRequest request, StreamObserver<SaveChatHistoryResponse> responseObserver) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setUserId(request.getUserId());
        chatHistory.setContent(request.getContent());
        chatHistory.setRole(request.getRole());
        chatHistory.setSessionId(request.getSessionId());
        chatHistory.setCreatedAt(LocalDateTime.now());
        
        // 添加多模态字段
        if (!request.getContentType().isEmpty()) {
            chatHistory.setContentType(request.getContentType());
        } else {
            chatHistory.setContentType("text");
        }
        if (!request.getMediaUrl().isEmpty()) {
            chatHistory.setMediaUrl(request.getMediaUrl());
        }
        if (!request.getMetadata().isEmpty()) {
            chatHistory.setMetadata(request.getMetadata());
        }

        ChatHistory saved = chatHistoryService.saveChatHistory(chatHistory);
        ChatHistoryProto proto = convertToChatHistoryProto(saved);
        SaveChatHistoryResponse response = SaveChatHistoryResponse.newBuilder().setChatHistory(proto).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getRecentChatHistory(GetRecentChatHistoryRequest request, StreamObserver<GetRecentChatHistoryResponse> responseObserver) {
        List<ChatHistory> chatHistories = chatHistoryService.getRecentChatHistory(request.getUserId());
        GetRecentChatHistoryResponse.Builder builder = GetRecentChatHistoryResponse.newBuilder();
        for (ChatHistory chatHistory : chatHistories) {
            builder.addChatHistories(convertToChatHistoryProto(chatHistory));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getChatHistoryBySessionId(GetChatHistoryBySessionIdRequest request, StreamObserver<GetChatHistoryBySessionIdResponse> responseObserver) {
        List<ChatHistory> chatHistories = chatHistoryService.getChatHistoryBySessionId(request.getSessionId());
        GetChatHistoryBySessionIdResponse.Builder builder = GetChatHistoryBySessionIdResponse.newBuilder();
        for (ChatHistory chatHistory : chatHistories) {
            builder.addChatHistories(convertToChatHistoryProto(chatHistory));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void extractKeyInfoFromContext(ExtractKeyInfoFromContextRequest request, StreamObserver<ExtractKeyInfoFromContextResponse> responseObserver) {
        String keyInfo = chatHistoryService.extractKeyInfoFromContext(request.getUserId());
        ExtractKeyInfoFromContextResponse response = ExtractKeyInfoFromContextResponse.newBuilder().setKeyInfo(keyInfo).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // 股票价格相关方法
    @Override
    public void saveStockPrice(SaveStockPriceRequest request, StreamObserver<SaveStockPriceResponse> responseObserver) {
        StockPriceProto proto = request.getStockPrice();
        StockPrice stockPrice = convertToStockPrice(proto);
        StockPrice saved = stockPriceService.saveStockPrice(stockPrice);
        SaveStockPriceResponse response = SaveStockPriceResponse.newBuilder().setStockPrice(convertToStockPriceProto(saved)).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getRecentStockPriceBySymbol(GetRecentStockPriceBySymbolRequest request, StreamObserver<GetRecentStockPriceBySymbolResponse> responseObserver) {
        Optional<StockPrice> optionalStockPrice = stockPriceService.getRecentStockPriceBySymbol(request.getStockSymbol());
        GetRecentStockPriceBySymbolResponse.Builder builder = GetRecentStockPriceBySymbolResponse.newBuilder();
        optionalStockPrice.ifPresent(stockPrice -> builder.setStockPrice(convertToStockPriceProto(stockPrice)));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    // 价格变动信息相关方法
    @Override
    public void savePriceChangeInfo(SavePriceChangeInfoRequest request, StreamObserver<SavePriceChangeInfoResponse> responseObserver) {
        PriceChangeInfoProto proto = request.getPriceChangeInfo();
        PriceChangeInfo priceChangeInfo = convertToPriceChangeInfo(proto);
        PriceChangeInfo saved = priceChangeInfoService.savePriceChangeInfo(priceChangeInfo);
        SavePriceChangeInfoResponse response = SavePriceChangeInfoResponse.newBuilder().setPriceChangeInfo(convertToPriceChangeInfoProto(saved)).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getRecentPriceChangeInfo(GetRecentPriceChangeInfoRequest request, StreamObserver<GetRecentPriceChangeInfoResponse> responseObserver) {
        List<PriceChangeInfo> priceChangeInfos = priceChangeInfoService.getRecentPriceChangeInfo(
                request.getStockSymbol(), request.getLimit());
        GetRecentPriceChangeInfoResponse.Builder builder = GetRecentPriceChangeInfoResponse.newBuilder();
        for (PriceChangeInfo info : priceChangeInfos) {
            builder.addPriceChangeInfos(convertToPriceChangeInfoProto(info));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    // 日程事件相关方法
    @Override
    public void saveScheduleEvent(SaveScheduleEventRequest request, StreamObserver<SaveScheduleEventResponse> responseObserver) {
        ScheduleEventProto proto = request.getScheduleEvent();
        ScheduleEvent scheduleEvent = convertToScheduleEvent(proto);
        ScheduleEvent saved = scheduleEventService.saveScheduleEvent(scheduleEvent);
        SaveScheduleEventResponse response = SaveScheduleEventResponse.newBuilder().setScheduleEvent(convertToScheduleEventProto(saved)).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllActiveScheduleEvents(GetAllActiveScheduleEventsRequest request, StreamObserver<GetAllActiveScheduleEventsResponse> responseObserver) {
        List<ScheduleEvent> scheduleEvents = scheduleEventService.getAllActiveScheduleEvents();
        GetAllActiveScheduleEventsResponse.Builder builder = GetAllActiveScheduleEventsResponse.newBuilder();
        for (ScheduleEvent event : scheduleEvents) {
            builder.addScheduleEvents(convertToScheduleEventProto(event));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateScheduleEvent(UpdateScheduleEventRequest request, StreamObserver<UpdateScheduleEventResponse> responseObserver) {
        ScheduleEventProto proto = request.getScheduleEvent();
        ScheduleEvent scheduleEvent = convertToScheduleEvent(proto);
        scheduleEventService.updateScheduleEvent(scheduleEvent);
        UpdateScheduleEventResponse response = UpdateScheduleEventResponse.newBuilder().setSuccess(true).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteScheduleEvent(DeleteScheduleEventRequest request, StreamObserver<DeleteScheduleEventResponse> responseObserver) {
        boolean success = scheduleEventService.deleteScheduleEvent(request.getId());
        DeleteScheduleEventResponse response = DeleteScheduleEventResponse.newBuilder().setSuccess(success).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // 用户偏好相关方法
    @Override
    public void saveUserPreference(SaveUserPreferenceRequest request, StreamObserver<SaveUserPreferenceResponse> responseObserver) {
        UserPreference userPreference = new UserPreference();
        userPreference.setUserId(request.getUserId());
        userPreference.setPreferenceKey(request.getPreferenceKey());
        userPreference.setValue(request.getValue());
        userPreference.setPreferenceType(request.getPreferenceType());
        userPreference.setPriority(request.getPriority());
        userPreference.setCreatedAt(LocalDateTime.now());
        userPreference.setUpdatedAt(LocalDateTime.now());

        UserPreference saved = userPreferenceService.saveUserPreference(userPreference);
        UserPreferenceProto proto = convertToUserPreferenceProto(saved);
        SaveUserPreferenceResponse response = SaveUserPreferenceResponse.newBuilder().setUserPreference(proto).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getUserPreferences(GetUserPreferencesRequest request, StreamObserver<GetUserPreferencesResponse> responseObserver) {
        List<UserPreference> userPreferences;
        if (request.getPreferenceKey().isEmpty()) {
            userPreferences = userPreferenceService.getUserPreferences(request.getUserId());
        } else {
            userPreferences = userPreferenceService.getUserPreferences(request.getUserId(), request.getPreferenceKey());
        }
        GetUserPreferencesResponse.Builder builder = GetUserPreferencesResponse.newBuilder();
        for (UserPreference userPreference : userPreferences) {
            builder.addUserPreferences(convertToUserPreferenceProto(userPreference));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteUserPreference(DeleteUserPreferenceRequest request, StreamObserver<DeleteUserPreferenceResponse> responseObserver) {
        userPreferenceService.deleteUserPreference(request.getUserId(), request.getPreferenceKey());
        DeleteUserPreferenceResponse response = DeleteUserPreferenceResponse.newBuilder().setSuccess(true).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // 记忆系统专用方法
    @Override
    public void cleanExpiredChatHistory(CleanExpiredChatHistoryRequest request, StreamObserver<CleanExpiredChatHistoryResponse> responseObserver) {
        int deletedCount = chatHistoryService.cleanExpiredChatHistory(request.getDays());
        CleanExpiredChatHistoryResponse response = CleanExpiredChatHistoryResponse.newBuilder()
                .setSuccess(true)
                .setDeletedCount(deletedCount)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void resetUserMemory(ResetUserMemoryRequest request, StreamObserver<ResetUserMemoryResponse> responseObserver) {
        userPreferenceService.deleteUserPreferences(request.getUserId());
        chatHistoryService.deleteChatHistoryByUserId(request.getUserId());
        ResetUserMemoryResponse response = ResetUserMemoryResponse.newBuilder().setSuccess(true).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getUserPreferencesByType(GetUserPreferencesByTypeRequest request, StreamObserver<GetUserPreferencesByTypeResponse> responseObserver) {
        List<UserPreference> userPreferences = userPreferenceService.getUserPreferencesByType(
                request.getUserId(), request.getPreferenceType());
        GetUserPreferencesByTypeResponse.Builder builder = GetUserPreferencesByTypeResponse.newBuilder();
        for (UserPreference userPreference : userPreferences) {
            builder.addUserPreferences(convertToUserPreferenceProto(userPreference));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getRecentInteractions(GetRecentInteractionsRequest request, StreamObserver<GetRecentInteractionsResponse> responseObserver) {
        List<ChatHistory> chatHistories = chatHistoryService.getRecentInteractions(
                request.getUserId(), request.getHours());
        GetRecentInteractionsResponse.Builder builder = GetRecentInteractionsResponse.newBuilder();
        for (ChatHistory chatHistory : chatHistories) {
            builder.addChatHistories(convertToChatHistoryProto(chatHistory));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    // 转换方法：实体类到Proto类
    private ChatHistoryProto convertToChatHistoryProto(ChatHistory chatHistory) {
        ChatHistoryProto.Builder builder = ChatHistoryProto.newBuilder()
                .setId(chatHistory.getId())
                .setUserId(chatHistory.getUserId())
                .setContent(chatHistory.getContent())
                .setRole(chatHistory.getRole())
                .setCreatedAt(chatHistory.getCreatedAt().format(DATE_TIME_FORMATTER))
                .setSessionId(chatHistory.getSessionId());
        
        // 添加多模态字段
        if (chatHistory.getContentType() != null) {
            builder.setContentType(chatHistory.getContentType());
        }
        if (chatHistory.getMediaUrl() != null) {
            builder.setMediaUrl(chatHistory.getMediaUrl());
        }
        if (chatHistory.getMetadata() != null) {
            builder.setMetadata(chatHistory.getMetadata());
        }
        
        return builder.build();
    }

    private StockPriceProto convertToStockPriceProto(StockPrice stockPrice) {
        return StockPriceProto.newBuilder()
                .setId(stockPrice.getId())
                .setStockSymbol(stockPrice.getStockSymbol())
                .setPrice(stockPrice.getPrice())
                .setTimestamp(stockPrice.getTimestamp().format(DATE_TIME_FORMATTER))
                .setPreviousPrice(stockPrice.getPreviousPrice())
                .build();
    }

    private PriceChangeInfoProto convertToPriceChangeInfoProto(PriceChangeInfo priceChangeInfo) {
        return PriceChangeInfoProto.newBuilder()
                .setId(priceChangeInfo.getId())
                .setStockSymbol(priceChangeInfo.getStockSymbol())
                .setCurrentPrice(priceChangeInfo.getCurrentPrice())
                .setPreviousPrice(priceChangeInfo.getPreviousPrice())
                .setChangePercent(priceChangeInfo.getChangePercent())
                .setIsSignificantChange(priceChangeInfo.isSignificantChange())
                .setChangeDirection(priceChangeInfo.getChangeDirection())
                .setRecommendation(priceChangeInfo.getRecommendation())
                .setTimestamp(priceChangeInfo.getTimestamp().format(DATE_TIME_FORMATTER))
                .build();
    }

    private ScheduleEventProto convertToScheduleEventProto(ScheduleEvent scheduleEvent) {
        return ScheduleEventProto.newBuilder()
                .setId(scheduleEvent.getId())
                .setTitle(scheduleEvent.getTitle())
                .setDateTime(scheduleEvent.getDateTime().format(DATE_TIME_FORMATTER))
                .setDescription(scheduleEvent.getDescription())
                .setCreatedAt(scheduleEvent.getCreatedAt().format(DATE_TIME_FORMATTER))
                .setRepeatType(scheduleEvent.getRepeatType())
                .setStockSymbols(scheduleEvent.getStockSymbols())
                .setPriceChangeThreshold(scheduleEvent.getPriceChangeThreshold())
                .setActive(scheduleEvent.isActive())
                .setLastCheckTime(scheduleEvent.getLastCheckTime())
                .build();
    }

    private UserPreferenceProto convertToUserPreferenceProto(UserPreference userPreference) {
        return UserPreferenceProto.newBuilder()
                .setId(userPreference.getId())
                .setUserId(userPreference.getUserId())
                .setPreferenceKey(userPreference.getPreferenceKey())
                .setValue(userPreference.getValue())
                .setCreatedAt(userPreference.getCreatedAt().format(DATE_TIME_FORMATTER))
                .setUpdatedAt(userPreference.getUpdatedAt().format(DATE_TIME_FORMATTER))
                .setPreferenceType(userPreference.getPreferenceType())
                .setPriority(userPreference.getPriority())
                .build();
    }

    // 转换方法：Proto类到实体类
    private StockPrice convertToStockPrice(StockPriceProto proto) {
        StockPrice stockPrice = new StockPrice();
        // 对于long类型，protobuf默认值是0，所以只有当id大于0时才设置
        if (proto.getId() > 0) {
            stockPrice.setId(proto.getId());
        }
        stockPrice.setStockSymbol(proto.getStockSymbol());
        stockPrice.setPrice(proto.getPrice());
        stockPrice.setTimestamp(LocalDateTime.parse(proto.getTimestamp(), DATE_TIME_FORMATTER));
        stockPrice.setPreviousPrice(proto.getPreviousPrice());
        return stockPrice;
    }

    private PriceChangeInfo convertToPriceChangeInfo(PriceChangeInfoProto proto) {
        PriceChangeInfo priceChangeInfo = new PriceChangeInfo();
        // 对于long类型，protobuf默认值是0，所以只有当id大于0时才设置
        if (proto.getId() > 0) {
            priceChangeInfo.setId(proto.getId());
        }
        priceChangeInfo.setStockSymbol(proto.getStockSymbol());
        priceChangeInfo.setCurrentPrice(proto.getCurrentPrice());
        priceChangeInfo.setPreviousPrice(proto.getPreviousPrice());
        priceChangeInfo.setChangePercent(proto.getChangePercent());
        priceChangeInfo.setSignificantChange(proto.getIsSignificantChange());
        priceChangeInfo.setChangeDirection(proto.getChangeDirection());
        priceChangeInfo.setRecommendation(proto.getRecommendation());
        priceChangeInfo.setTimestamp(LocalDateTime.parse(proto.getTimestamp(), DATE_TIME_FORMATTER));
        return priceChangeInfo;
    }

    private ScheduleEvent convertToScheduleEvent(ScheduleEventProto proto) {
        ScheduleEvent scheduleEvent = new ScheduleEvent();
        // 对于long类型，protobuf默认值是0，所以只有当id大于0时才设置
        if (proto.getId() > 0) {
            scheduleEvent.setId(proto.getId());
        }
        scheduleEvent.setTitle(proto.getTitle());
        scheduleEvent.setDateTime(LocalDateTime.parse(proto.getDateTime(), DATE_TIME_FORMATTER));
        scheduleEvent.setDescription(proto.getDescription());
        // 对于字符串类型，protobuf默认值是空字符串，所以检查是否为空
        if (!proto.getCreatedAt().isEmpty()) {
            scheduleEvent.setCreatedAt(LocalDateTime.parse(proto.getCreatedAt(), DATE_TIME_FORMATTER));
        } else {
            scheduleEvent.setCreatedAt(LocalDateTime.now());
        }
        scheduleEvent.setRepeatType(proto.getRepeatType());
        scheduleEvent.setStockSymbols(proto.getStockSymbols());
        scheduleEvent.setPriceChangeThreshold(proto.getPriceChangeThreshold());
        scheduleEvent.setActive(proto.getActive());
        scheduleEvent.setLastCheckTime(proto.getLastCheckTime());
        return scheduleEvent;
    }
}
