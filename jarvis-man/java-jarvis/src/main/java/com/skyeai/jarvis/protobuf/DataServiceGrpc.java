package com.skyeai.jarvis.protobuf;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * 数据服务接口
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.54.0)",
    comments = "Source: data_service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class DataServiceGrpc {

  private DataServiceGrpc() {}

  public static final String SERVICE_NAME = "com.skyeai.jarvis.DataService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.SaveChatHistoryRequest,
      com.skyeai.jarvis.protobuf.SaveChatHistoryResponse> getSaveChatHistoryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SaveChatHistory",
      requestType = com.skyeai.jarvis.protobuf.SaveChatHistoryRequest.class,
      responseType = com.skyeai.jarvis.protobuf.SaveChatHistoryResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.SaveChatHistoryRequest,
      com.skyeai.jarvis.protobuf.SaveChatHistoryResponse> getSaveChatHistoryMethod() {
    io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.SaveChatHistoryRequest, com.skyeai.jarvis.protobuf.SaveChatHistoryResponse> getSaveChatHistoryMethod;
    if ((getSaveChatHistoryMethod = DataServiceGrpc.getSaveChatHistoryMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getSaveChatHistoryMethod = DataServiceGrpc.getSaveChatHistoryMethod) == null) {
          DataServiceGrpc.getSaveChatHistoryMethod = getSaveChatHistoryMethod =
              io.grpc.MethodDescriptor.<com.skyeai.jarvis.protobuf.SaveChatHistoryRequest, com.skyeai.jarvis.protobuf.SaveChatHistoryResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SaveChatHistory"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.SaveChatHistoryRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.SaveChatHistoryResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("SaveChatHistory"))
              .build();
        }
      }
    }
    return getSaveChatHistoryMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.GetRecentChatHistoryRequest,
      com.skyeai.jarvis.protobuf.GetRecentChatHistoryResponse> getGetRecentChatHistoryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetRecentChatHistory",
      requestType = com.skyeai.jarvis.protobuf.GetRecentChatHistoryRequest.class,
      responseType = com.skyeai.jarvis.protobuf.GetRecentChatHistoryResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.GetRecentChatHistoryRequest,
      com.skyeai.jarvis.protobuf.GetRecentChatHistoryResponse> getGetRecentChatHistoryMethod() {
    io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.GetRecentChatHistoryRequest, com.skyeai.jarvis.protobuf.GetRecentChatHistoryResponse> getGetRecentChatHistoryMethod;
    if ((getGetRecentChatHistoryMethod = DataServiceGrpc.getGetRecentChatHistoryMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetRecentChatHistoryMethod = DataServiceGrpc.getGetRecentChatHistoryMethod) == null) {
          DataServiceGrpc.getGetRecentChatHistoryMethod = getGetRecentChatHistoryMethod =
              io.grpc.MethodDescriptor.<com.skyeai.jarvis.protobuf.GetRecentChatHistoryRequest, com.skyeai.jarvis.protobuf.GetRecentChatHistoryResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetRecentChatHistory"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.GetRecentChatHistoryRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.GetRecentChatHistoryResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetRecentChatHistory"))
              .build();
        }
      }
    }
    return getGetRecentChatHistoryMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdRequest,
      com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdResponse> getGetChatHistoryBySessionIdMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetChatHistoryBySessionId",
      requestType = com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdRequest.class,
      responseType = com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdRequest,
      com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdResponse> getGetChatHistoryBySessionIdMethod() {
    io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdRequest, com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdResponse> getGetChatHistoryBySessionIdMethod;
    if ((getGetChatHistoryBySessionIdMethod = DataServiceGrpc.getGetChatHistoryBySessionIdMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetChatHistoryBySessionIdMethod = DataServiceGrpc.getGetChatHistoryBySessionIdMethod) == null) {
          DataServiceGrpc.getGetChatHistoryBySessionIdMethod = getGetChatHistoryBySessionIdMethod =
              io.grpc.MethodDescriptor.<com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdRequest, com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetChatHistoryBySessionId"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetChatHistoryBySessionId"))
              .build();
        }
      }
    }
    return getGetChatHistoryBySessionIdMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextRequest,
      com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextResponse> getExtractKeyInfoFromContextMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ExtractKeyInfoFromContext",
      requestType = com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextRequest.class,
      responseType = com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextRequest,
      com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextResponse> getExtractKeyInfoFromContextMethod() {
    io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextRequest, com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextResponse> getExtractKeyInfoFromContextMethod;
    if ((getExtractKeyInfoFromContextMethod = DataServiceGrpc.getExtractKeyInfoFromContextMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getExtractKeyInfoFromContextMethod = DataServiceGrpc.getExtractKeyInfoFromContextMethod) == null) {
          DataServiceGrpc.getExtractKeyInfoFromContextMethod = getExtractKeyInfoFromContextMethod =
              io.grpc.MethodDescriptor.<com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextRequest, com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ExtractKeyInfoFromContext"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("ExtractKeyInfoFromContext"))
              .build();
        }
      }
    }
    return getExtractKeyInfoFromContextMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.SaveStockPriceRequest,
      com.skyeai.jarvis.protobuf.SaveStockPriceResponse> getSaveStockPriceMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SaveStockPrice",
      requestType = com.skyeai.jarvis.protobuf.SaveStockPriceRequest.class,
      responseType = com.skyeai.jarvis.protobuf.SaveStockPriceResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.SaveStockPriceRequest,
      com.skyeai.jarvis.protobuf.SaveStockPriceResponse> getSaveStockPriceMethod() {
    io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.SaveStockPriceRequest, com.skyeai.jarvis.protobuf.SaveStockPriceResponse> getSaveStockPriceMethod;
    if ((getSaveStockPriceMethod = DataServiceGrpc.getSaveStockPriceMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getSaveStockPriceMethod = DataServiceGrpc.getSaveStockPriceMethod) == null) {
          DataServiceGrpc.getSaveStockPriceMethod = getSaveStockPriceMethod =
              io.grpc.MethodDescriptor.<com.skyeai.jarvis.protobuf.SaveStockPriceRequest, com.skyeai.jarvis.protobuf.SaveStockPriceResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SaveStockPrice"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.SaveStockPriceRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.SaveStockPriceResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("SaveStockPrice"))
              .build();
        }
      }
    }
    return getSaveStockPriceMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolRequest,
      com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolResponse> getGetRecentStockPriceBySymbolMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetRecentStockPriceBySymbol",
      requestType = com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolRequest.class,
      responseType = com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolRequest,
      com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolResponse> getGetRecentStockPriceBySymbolMethod() {
    io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolRequest, com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolResponse> getGetRecentStockPriceBySymbolMethod;
    if ((getGetRecentStockPriceBySymbolMethod = DataServiceGrpc.getGetRecentStockPriceBySymbolMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetRecentStockPriceBySymbolMethod = DataServiceGrpc.getGetRecentStockPriceBySymbolMethod) == null) {
          DataServiceGrpc.getGetRecentStockPriceBySymbolMethod = getGetRecentStockPriceBySymbolMethod =
              io.grpc.MethodDescriptor.<com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolRequest, com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetRecentStockPriceBySymbol"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetRecentStockPriceBySymbol"))
              .build();
        }
      }
    }
    return getGetRecentStockPriceBySymbolMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.SavePriceChangeInfoRequest,
      com.skyeai.jarvis.protobuf.SavePriceChangeInfoResponse> getSavePriceChangeInfoMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SavePriceChangeInfo",
      requestType = com.skyeai.jarvis.protobuf.SavePriceChangeInfoRequest.class,
      responseType = com.skyeai.jarvis.protobuf.SavePriceChangeInfoResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.SavePriceChangeInfoRequest,
      com.skyeai.jarvis.protobuf.SavePriceChangeInfoResponse> getSavePriceChangeInfoMethod() {
    io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.SavePriceChangeInfoRequest, com.skyeai.jarvis.protobuf.SavePriceChangeInfoResponse> getSavePriceChangeInfoMethod;
    if ((getSavePriceChangeInfoMethod = DataServiceGrpc.getSavePriceChangeInfoMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getSavePriceChangeInfoMethod = DataServiceGrpc.getSavePriceChangeInfoMethod) == null) {
          DataServiceGrpc.getSavePriceChangeInfoMethod = getSavePriceChangeInfoMethod =
              io.grpc.MethodDescriptor.<com.skyeai.jarvis.protobuf.SavePriceChangeInfoRequest, com.skyeai.jarvis.protobuf.SavePriceChangeInfoResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SavePriceChangeInfo"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.SavePriceChangeInfoRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.SavePriceChangeInfoResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("SavePriceChangeInfo"))
              .build();
        }
      }
    }
    return getSavePriceChangeInfoMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoRequest,
      com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoResponse> getGetRecentPriceChangeInfoMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetRecentPriceChangeInfo",
      requestType = com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoRequest.class,
      responseType = com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoRequest,
      com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoResponse> getGetRecentPriceChangeInfoMethod() {
    io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoRequest, com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoResponse> getGetRecentPriceChangeInfoMethod;
    if ((getGetRecentPriceChangeInfoMethod = DataServiceGrpc.getGetRecentPriceChangeInfoMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetRecentPriceChangeInfoMethod = DataServiceGrpc.getGetRecentPriceChangeInfoMethod) == null) {
          DataServiceGrpc.getGetRecentPriceChangeInfoMethod = getGetRecentPriceChangeInfoMethod =
              io.grpc.MethodDescriptor.<com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoRequest, com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetRecentPriceChangeInfo"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetRecentPriceChangeInfo"))
              .build();
        }
      }
    }
    return getGetRecentPriceChangeInfoMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.SaveScheduleEventRequest,
      com.skyeai.jarvis.protobuf.SaveScheduleEventResponse> getSaveScheduleEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SaveScheduleEvent",
      requestType = com.skyeai.jarvis.protobuf.SaveScheduleEventRequest.class,
      responseType = com.skyeai.jarvis.protobuf.SaveScheduleEventResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.SaveScheduleEventRequest,
      com.skyeai.jarvis.protobuf.SaveScheduleEventResponse> getSaveScheduleEventMethod() {
    io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.SaveScheduleEventRequest, com.skyeai.jarvis.protobuf.SaveScheduleEventResponse> getSaveScheduleEventMethod;
    if ((getSaveScheduleEventMethod = DataServiceGrpc.getSaveScheduleEventMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getSaveScheduleEventMethod = DataServiceGrpc.getSaveScheduleEventMethod) == null) {
          DataServiceGrpc.getSaveScheduleEventMethod = getSaveScheduleEventMethod =
              io.grpc.MethodDescriptor.<com.skyeai.jarvis.protobuf.SaveScheduleEventRequest, com.skyeai.jarvis.protobuf.SaveScheduleEventResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SaveScheduleEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.SaveScheduleEventRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.SaveScheduleEventResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("SaveScheduleEvent"))
              .build();
        }
      }
    }
    return getSaveScheduleEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsRequest,
      com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsResponse> getGetAllActiveScheduleEventsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAllActiveScheduleEvents",
      requestType = com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsRequest.class,
      responseType = com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsRequest,
      com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsResponse> getGetAllActiveScheduleEventsMethod() {
    io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsRequest, com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsResponse> getGetAllActiveScheduleEventsMethod;
    if ((getGetAllActiveScheduleEventsMethod = DataServiceGrpc.getGetAllActiveScheduleEventsMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetAllActiveScheduleEventsMethod = DataServiceGrpc.getGetAllActiveScheduleEventsMethod) == null) {
          DataServiceGrpc.getGetAllActiveScheduleEventsMethod = getGetAllActiveScheduleEventsMethod =
              io.grpc.MethodDescriptor.<com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsRequest, com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAllActiveScheduleEvents"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetAllActiveScheduleEvents"))
              .build();
        }
      }
    }
    return getGetAllActiveScheduleEventsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.UpdateScheduleEventRequest,
      com.skyeai.jarvis.protobuf.UpdateScheduleEventResponse> getUpdateScheduleEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateScheduleEvent",
      requestType = com.skyeai.jarvis.protobuf.UpdateScheduleEventRequest.class,
      responseType = com.skyeai.jarvis.protobuf.UpdateScheduleEventResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.UpdateScheduleEventRequest,
      com.skyeai.jarvis.protobuf.UpdateScheduleEventResponse> getUpdateScheduleEventMethod() {
    io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.UpdateScheduleEventRequest, com.skyeai.jarvis.protobuf.UpdateScheduleEventResponse> getUpdateScheduleEventMethod;
    if ((getUpdateScheduleEventMethod = DataServiceGrpc.getUpdateScheduleEventMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getUpdateScheduleEventMethod = DataServiceGrpc.getUpdateScheduleEventMethod) == null) {
          DataServiceGrpc.getUpdateScheduleEventMethod = getUpdateScheduleEventMethod =
              io.grpc.MethodDescriptor.<com.skyeai.jarvis.protobuf.UpdateScheduleEventRequest, com.skyeai.jarvis.protobuf.UpdateScheduleEventResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpdateScheduleEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.UpdateScheduleEventRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.UpdateScheduleEventResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("UpdateScheduleEvent"))
              .build();
        }
      }
    }
    return getUpdateScheduleEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.DeleteScheduleEventRequest,
      com.skyeai.jarvis.protobuf.DeleteScheduleEventResponse> getDeleteScheduleEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteScheduleEvent",
      requestType = com.skyeai.jarvis.protobuf.DeleteScheduleEventRequest.class,
      responseType = com.skyeai.jarvis.protobuf.DeleteScheduleEventResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.DeleteScheduleEventRequest,
      com.skyeai.jarvis.protobuf.DeleteScheduleEventResponse> getDeleteScheduleEventMethod() {
    io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.DeleteScheduleEventRequest, com.skyeai.jarvis.protobuf.DeleteScheduleEventResponse> getDeleteScheduleEventMethod;
    if ((getDeleteScheduleEventMethod = DataServiceGrpc.getDeleteScheduleEventMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getDeleteScheduleEventMethod = DataServiceGrpc.getDeleteScheduleEventMethod) == null) {
          DataServiceGrpc.getDeleteScheduleEventMethod = getDeleteScheduleEventMethod =
              io.grpc.MethodDescriptor.<com.skyeai.jarvis.protobuf.DeleteScheduleEventRequest, com.skyeai.jarvis.protobuf.DeleteScheduleEventResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteScheduleEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.DeleteScheduleEventRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.DeleteScheduleEventResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("DeleteScheduleEvent"))
              .build();
        }
      }
    }
    return getDeleteScheduleEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.SaveUserPreferenceRequest,
      com.skyeai.jarvis.protobuf.SaveUserPreferenceResponse> getSaveUserPreferenceMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SaveUserPreference",
      requestType = com.skyeai.jarvis.protobuf.SaveUserPreferenceRequest.class,
      responseType = com.skyeai.jarvis.protobuf.SaveUserPreferenceResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.SaveUserPreferenceRequest,
      com.skyeai.jarvis.protobuf.SaveUserPreferenceResponse> getSaveUserPreferenceMethod() {
    io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.SaveUserPreferenceRequest, com.skyeai.jarvis.protobuf.SaveUserPreferenceResponse> getSaveUserPreferenceMethod;
    if ((getSaveUserPreferenceMethod = DataServiceGrpc.getSaveUserPreferenceMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getSaveUserPreferenceMethod = DataServiceGrpc.getSaveUserPreferenceMethod) == null) {
          DataServiceGrpc.getSaveUserPreferenceMethod = getSaveUserPreferenceMethod =
              io.grpc.MethodDescriptor.<com.skyeai.jarvis.protobuf.SaveUserPreferenceRequest, com.skyeai.jarvis.protobuf.SaveUserPreferenceResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SaveUserPreference"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.SaveUserPreferenceRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.SaveUserPreferenceResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("SaveUserPreference"))
              .build();
        }
      }
    }
    return getSaveUserPreferenceMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.GetUserPreferencesRequest,
      com.skyeai.jarvis.protobuf.GetUserPreferencesResponse> getGetUserPreferencesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetUserPreferences",
      requestType = com.skyeai.jarvis.protobuf.GetUserPreferencesRequest.class,
      responseType = com.skyeai.jarvis.protobuf.GetUserPreferencesResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.GetUserPreferencesRequest,
      com.skyeai.jarvis.protobuf.GetUserPreferencesResponse> getGetUserPreferencesMethod() {
    io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.GetUserPreferencesRequest, com.skyeai.jarvis.protobuf.GetUserPreferencesResponse> getGetUserPreferencesMethod;
    if ((getGetUserPreferencesMethod = DataServiceGrpc.getGetUserPreferencesMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getGetUserPreferencesMethod = DataServiceGrpc.getGetUserPreferencesMethod) == null) {
          DataServiceGrpc.getGetUserPreferencesMethod = getGetUserPreferencesMethod =
              io.grpc.MethodDescriptor.<com.skyeai.jarvis.protobuf.GetUserPreferencesRequest, com.skyeai.jarvis.protobuf.GetUserPreferencesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetUserPreferences"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.GetUserPreferencesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.GetUserPreferencesResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("GetUserPreferences"))
              .build();
        }
      }
    }
    return getGetUserPreferencesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.DeleteUserPreferenceRequest,
      com.skyeai.jarvis.protobuf.DeleteUserPreferenceResponse> getDeleteUserPreferenceMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteUserPreference",
      requestType = com.skyeai.jarvis.protobuf.DeleteUserPreferenceRequest.class,
      responseType = com.skyeai.jarvis.protobuf.DeleteUserPreferenceResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.DeleteUserPreferenceRequest,
      com.skyeai.jarvis.protobuf.DeleteUserPreferenceResponse> getDeleteUserPreferenceMethod() {
    io.grpc.MethodDescriptor<com.skyeai.jarvis.protobuf.DeleteUserPreferenceRequest, com.skyeai.jarvis.protobuf.DeleteUserPreferenceResponse> getDeleteUserPreferenceMethod;
    if ((getDeleteUserPreferenceMethod = DataServiceGrpc.getDeleteUserPreferenceMethod) == null) {
      synchronized (DataServiceGrpc.class) {
        if ((getDeleteUserPreferenceMethod = DataServiceGrpc.getDeleteUserPreferenceMethod) == null) {
          DataServiceGrpc.getDeleteUserPreferenceMethod = getDeleteUserPreferenceMethod =
              io.grpc.MethodDescriptor.<com.skyeai.jarvis.protobuf.DeleteUserPreferenceRequest, com.skyeai.jarvis.protobuf.DeleteUserPreferenceResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteUserPreference"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.DeleteUserPreferenceRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.skyeai.jarvis.protobuf.DeleteUserPreferenceResponse.getDefaultInstance()))
              .setSchemaDescriptor(new DataServiceMethodDescriptorSupplier("DeleteUserPreference"))
              .build();
        }
      }
    }
    return getDeleteUserPreferenceMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static DataServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DataServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DataServiceStub>() {
        @java.lang.Override
        public DataServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DataServiceStub(channel, callOptions);
        }
      };
    return DataServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static DataServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DataServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DataServiceBlockingStub>() {
        @java.lang.Override
        public DataServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DataServiceBlockingStub(channel, callOptions);
        }
      };
    return DataServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static DataServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DataServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DataServiceFutureStub>() {
        @java.lang.Override
        public DataServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DataServiceFutureStub(channel, callOptions);
        }
      };
    return DataServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * 数据服务接口
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * 聊天历史相关方法
     * </pre>
     */
    default void saveChatHistory(com.skyeai.jarvis.protobuf.SaveChatHistoryRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.SaveChatHistoryResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSaveChatHistoryMethod(), responseObserver);
    }

    /**
     */
    default void getRecentChatHistory(com.skyeai.jarvis.protobuf.GetRecentChatHistoryRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.GetRecentChatHistoryResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetRecentChatHistoryMethod(), responseObserver);
    }

    /**
     */
    default void getChatHistoryBySessionId(com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetChatHistoryBySessionIdMethod(), responseObserver);
    }

    /**
     */
    default void extractKeyInfoFromContext(com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getExtractKeyInfoFromContextMethod(), responseObserver);
    }

    /**
     * <pre>
     * 股票价格相关方法
     * </pre>
     */
    default void saveStockPrice(com.skyeai.jarvis.protobuf.SaveStockPriceRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.SaveStockPriceResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSaveStockPriceMethod(), responseObserver);
    }

    /**
     */
    default void getRecentStockPriceBySymbol(com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetRecentStockPriceBySymbolMethod(), responseObserver);
    }

    /**
     * <pre>
     * 价格变动信息相关方法
     * </pre>
     */
    default void savePriceChangeInfo(com.skyeai.jarvis.protobuf.SavePriceChangeInfoRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.SavePriceChangeInfoResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSavePriceChangeInfoMethod(), responseObserver);
    }

    /**
     */
    default void getRecentPriceChangeInfo(com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetRecentPriceChangeInfoMethod(), responseObserver);
    }

    /**
     * <pre>
     * 日程事件相关方法
     * </pre>
     */
    default void saveScheduleEvent(com.skyeai.jarvis.protobuf.SaveScheduleEventRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.SaveScheduleEventResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSaveScheduleEventMethod(), responseObserver);
    }

    /**
     */
    default void getAllActiveScheduleEvents(com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetAllActiveScheduleEventsMethod(), responseObserver);
    }

    /**
     */
    default void updateScheduleEvent(com.skyeai.jarvis.protobuf.UpdateScheduleEventRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.UpdateScheduleEventResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdateScheduleEventMethod(), responseObserver);
    }

    /**
     */
    default void deleteScheduleEvent(com.skyeai.jarvis.protobuf.DeleteScheduleEventRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.DeleteScheduleEventResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteScheduleEventMethod(), responseObserver);
    }

    /**
     * <pre>
     * 用户偏好相关方法
     * </pre>
     */
    default void saveUserPreference(com.skyeai.jarvis.protobuf.SaveUserPreferenceRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.SaveUserPreferenceResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSaveUserPreferenceMethod(), responseObserver);
    }

    /**
     */
    default void getUserPreferences(com.skyeai.jarvis.protobuf.GetUserPreferencesRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.GetUserPreferencesResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetUserPreferencesMethod(), responseObserver);
    }

    /**
     */
    default void deleteUserPreference(com.skyeai.jarvis.protobuf.DeleteUserPreferenceRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.DeleteUserPreferenceResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteUserPreferenceMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service DataService.
   * <pre>
   * 数据服务接口
   * </pre>
   */
  public static abstract class DataServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return DataServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service DataService.
   * <pre>
   * 数据服务接口
   * </pre>
   */
  public static final class DataServiceStub
      extends io.grpc.stub.AbstractAsyncStub<DataServiceStub> {
    private DataServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DataServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DataServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * 聊天历史相关方法
     * </pre>
     */
    public void saveChatHistory(com.skyeai.jarvis.protobuf.SaveChatHistoryRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.SaveChatHistoryResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSaveChatHistoryMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getRecentChatHistory(com.skyeai.jarvis.protobuf.GetRecentChatHistoryRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.GetRecentChatHistoryResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetRecentChatHistoryMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getChatHistoryBySessionId(com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetChatHistoryBySessionIdMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void extractKeyInfoFromContext(com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getExtractKeyInfoFromContextMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * 股票价格相关方法
     * </pre>
     */
    public void saveStockPrice(com.skyeai.jarvis.protobuf.SaveStockPriceRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.SaveStockPriceResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSaveStockPriceMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getRecentStockPriceBySymbol(com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetRecentStockPriceBySymbolMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * 价格变动信息相关方法
     * </pre>
     */
    public void savePriceChangeInfo(com.skyeai.jarvis.protobuf.SavePriceChangeInfoRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.SavePriceChangeInfoResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSavePriceChangeInfoMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getRecentPriceChangeInfo(com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetRecentPriceChangeInfoMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * 日程事件相关方法
     * </pre>
     */
    public void saveScheduleEvent(com.skyeai.jarvis.protobuf.SaveScheduleEventRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.SaveScheduleEventResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSaveScheduleEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getAllActiveScheduleEvents(com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetAllActiveScheduleEventsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void updateScheduleEvent(com.skyeai.jarvis.protobuf.UpdateScheduleEventRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.UpdateScheduleEventResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdateScheduleEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteScheduleEvent(com.skyeai.jarvis.protobuf.DeleteScheduleEventRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.DeleteScheduleEventResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteScheduleEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * 用户偏好相关方法
     * </pre>
     */
    public void saveUserPreference(com.skyeai.jarvis.protobuf.SaveUserPreferenceRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.SaveUserPreferenceResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSaveUserPreferenceMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getUserPreferences(com.skyeai.jarvis.protobuf.GetUserPreferencesRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.GetUserPreferencesResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetUserPreferencesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteUserPreference(com.skyeai.jarvis.protobuf.DeleteUserPreferenceRequest request,
        io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.DeleteUserPreferenceResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteUserPreferenceMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service DataService.
   * <pre>
   * 数据服务接口
   * </pre>
   */
  public static final class DataServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<DataServiceBlockingStub> {
    private DataServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DataServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DataServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * 聊天历史相关方法
     * </pre>
     */
    public com.skyeai.jarvis.protobuf.SaveChatHistoryResponse saveChatHistory(com.skyeai.jarvis.protobuf.SaveChatHistoryRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSaveChatHistoryMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.skyeai.jarvis.protobuf.GetRecentChatHistoryResponse getRecentChatHistory(com.skyeai.jarvis.protobuf.GetRecentChatHistoryRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetRecentChatHistoryMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdResponse getChatHistoryBySessionId(com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetChatHistoryBySessionIdMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextResponse extractKeyInfoFromContext(com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getExtractKeyInfoFromContextMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * 股票价格相关方法
     * </pre>
     */
    public com.skyeai.jarvis.protobuf.SaveStockPriceResponse saveStockPrice(com.skyeai.jarvis.protobuf.SaveStockPriceRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSaveStockPriceMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolResponse getRecentStockPriceBySymbol(com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetRecentStockPriceBySymbolMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * 价格变动信息相关方法
     * </pre>
     */
    public com.skyeai.jarvis.protobuf.SavePriceChangeInfoResponse savePriceChangeInfo(com.skyeai.jarvis.protobuf.SavePriceChangeInfoRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSavePriceChangeInfoMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoResponse getRecentPriceChangeInfo(com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetRecentPriceChangeInfoMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * 日程事件相关方法
     * </pre>
     */
    public com.skyeai.jarvis.protobuf.SaveScheduleEventResponse saveScheduleEvent(com.skyeai.jarvis.protobuf.SaveScheduleEventRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSaveScheduleEventMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsResponse getAllActiveScheduleEvents(com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetAllActiveScheduleEventsMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.skyeai.jarvis.protobuf.UpdateScheduleEventResponse updateScheduleEvent(com.skyeai.jarvis.protobuf.UpdateScheduleEventRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdateScheduleEventMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.skyeai.jarvis.protobuf.DeleteScheduleEventResponse deleteScheduleEvent(com.skyeai.jarvis.protobuf.DeleteScheduleEventRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteScheduleEventMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * 用户偏好相关方法
     * </pre>
     */
    public com.skyeai.jarvis.protobuf.SaveUserPreferenceResponse saveUserPreference(com.skyeai.jarvis.protobuf.SaveUserPreferenceRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSaveUserPreferenceMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.skyeai.jarvis.protobuf.GetUserPreferencesResponse getUserPreferences(com.skyeai.jarvis.protobuf.GetUserPreferencesRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetUserPreferencesMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.skyeai.jarvis.protobuf.DeleteUserPreferenceResponse deleteUserPreference(com.skyeai.jarvis.protobuf.DeleteUserPreferenceRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteUserPreferenceMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service DataService.
   * <pre>
   * 数据服务接口
   * </pre>
   */
  public static final class DataServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<DataServiceFutureStub> {
    private DataServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DataServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DataServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * 聊天历史相关方法
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.skyeai.jarvis.protobuf.SaveChatHistoryResponse> saveChatHistory(
        com.skyeai.jarvis.protobuf.SaveChatHistoryRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSaveChatHistoryMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.skyeai.jarvis.protobuf.GetRecentChatHistoryResponse> getRecentChatHistory(
        com.skyeai.jarvis.protobuf.GetRecentChatHistoryRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetRecentChatHistoryMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdResponse> getChatHistoryBySessionId(
        com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetChatHistoryBySessionIdMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextResponse> extractKeyInfoFromContext(
        com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getExtractKeyInfoFromContextMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * 股票价格相关方法
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.skyeai.jarvis.protobuf.SaveStockPriceResponse> saveStockPrice(
        com.skyeai.jarvis.protobuf.SaveStockPriceRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSaveStockPriceMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolResponse> getRecentStockPriceBySymbol(
        com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetRecentStockPriceBySymbolMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * 价格变动信息相关方法
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.skyeai.jarvis.protobuf.SavePriceChangeInfoResponse> savePriceChangeInfo(
        com.skyeai.jarvis.protobuf.SavePriceChangeInfoRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSavePriceChangeInfoMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoResponse> getRecentPriceChangeInfo(
        com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetRecentPriceChangeInfoMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * 日程事件相关方法
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.skyeai.jarvis.protobuf.SaveScheduleEventResponse> saveScheduleEvent(
        com.skyeai.jarvis.protobuf.SaveScheduleEventRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSaveScheduleEventMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsResponse> getAllActiveScheduleEvents(
        com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetAllActiveScheduleEventsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.skyeai.jarvis.protobuf.UpdateScheduleEventResponse> updateScheduleEvent(
        com.skyeai.jarvis.protobuf.UpdateScheduleEventRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdateScheduleEventMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.skyeai.jarvis.protobuf.DeleteScheduleEventResponse> deleteScheduleEvent(
        com.skyeai.jarvis.protobuf.DeleteScheduleEventRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteScheduleEventMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * 用户偏好相关方法
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.skyeai.jarvis.protobuf.SaveUserPreferenceResponse> saveUserPreference(
        com.skyeai.jarvis.protobuf.SaveUserPreferenceRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSaveUserPreferenceMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.skyeai.jarvis.protobuf.GetUserPreferencesResponse> getUserPreferences(
        com.skyeai.jarvis.protobuf.GetUserPreferencesRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetUserPreferencesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.skyeai.jarvis.protobuf.DeleteUserPreferenceResponse> deleteUserPreference(
        com.skyeai.jarvis.protobuf.DeleteUserPreferenceRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteUserPreferenceMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SAVE_CHAT_HISTORY = 0;
  private static final int METHODID_GET_RECENT_CHAT_HISTORY = 1;
  private static final int METHODID_GET_CHAT_HISTORY_BY_SESSION_ID = 2;
  private static final int METHODID_EXTRACT_KEY_INFO_FROM_CONTEXT = 3;
  private static final int METHODID_SAVE_STOCK_PRICE = 4;
  private static final int METHODID_GET_RECENT_STOCK_PRICE_BY_SYMBOL = 5;
  private static final int METHODID_SAVE_PRICE_CHANGE_INFO = 6;
  private static final int METHODID_GET_RECENT_PRICE_CHANGE_INFO = 7;
  private static final int METHODID_SAVE_SCHEDULE_EVENT = 8;
  private static final int METHODID_GET_ALL_ACTIVE_SCHEDULE_EVENTS = 9;
  private static final int METHODID_UPDATE_SCHEDULE_EVENT = 10;
  private static final int METHODID_DELETE_SCHEDULE_EVENT = 11;
  private static final int METHODID_SAVE_USER_PREFERENCE = 12;
  private static final int METHODID_GET_USER_PREFERENCES = 13;
  private static final int METHODID_DELETE_USER_PREFERENCE = 14;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SAVE_CHAT_HISTORY:
          serviceImpl.saveChatHistory((com.skyeai.jarvis.protobuf.SaveChatHistoryRequest) request,
              (io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.SaveChatHistoryResponse>) responseObserver);
          break;
        case METHODID_GET_RECENT_CHAT_HISTORY:
          serviceImpl.getRecentChatHistory((com.skyeai.jarvis.protobuf.GetRecentChatHistoryRequest) request,
              (io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.GetRecentChatHistoryResponse>) responseObserver);
          break;
        case METHODID_GET_CHAT_HISTORY_BY_SESSION_ID:
          serviceImpl.getChatHistoryBySessionId((com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdRequest) request,
              (io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdResponse>) responseObserver);
          break;
        case METHODID_EXTRACT_KEY_INFO_FROM_CONTEXT:
          serviceImpl.extractKeyInfoFromContext((com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextRequest) request,
              (io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextResponse>) responseObserver);
          break;
        case METHODID_SAVE_STOCK_PRICE:
          serviceImpl.saveStockPrice((com.skyeai.jarvis.protobuf.SaveStockPriceRequest) request,
              (io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.SaveStockPriceResponse>) responseObserver);
          break;
        case METHODID_GET_RECENT_STOCK_PRICE_BY_SYMBOL:
          serviceImpl.getRecentStockPriceBySymbol((com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolRequest) request,
              (io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolResponse>) responseObserver);
          break;
        case METHODID_SAVE_PRICE_CHANGE_INFO:
          serviceImpl.savePriceChangeInfo((com.skyeai.jarvis.protobuf.SavePriceChangeInfoRequest) request,
              (io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.SavePriceChangeInfoResponse>) responseObserver);
          break;
        case METHODID_GET_RECENT_PRICE_CHANGE_INFO:
          serviceImpl.getRecentPriceChangeInfo((com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoRequest) request,
              (io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoResponse>) responseObserver);
          break;
        case METHODID_SAVE_SCHEDULE_EVENT:
          serviceImpl.saveScheduleEvent((com.skyeai.jarvis.protobuf.SaveScheduleEventRequest) request,
              (io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.SaveScheduleEventResponse>) responseObserver);
          break;
        case METHODID_GET_ALL_ACTIVE_SCHEDULE_EVENTS:
          serviceImpl.getAllActiveScheduleEvents((com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsRequest) request,
              (io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsResponse>) responseObserver);
          break;
        case METHODID_UPDATE_SCHEDULE_EVENT:
          serviceImpl.updateScheduleEvent((com.skyeai.jarvis.protobuf.UpdateScheduleEventRequest) request,
              (io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.UpdateScheduleEventResponse>) responseObserver);
          break;
        case METHODID_DELETE_SCHEDULE_EVENT:
          serviceImpl.deleteScheduleEvent((com.skyeai.jarvis.protobuf.DeleteScheduleEventRequest) request,
              (io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.DeleteScheduleEventResponse>) responseObserver);
          break;
        case METHODID_SAVE_USER_PREFERENCE:
          serviceImpl.saveUserPreference((com.skyeai.jarvis.protobuf.SaveUserPreferenceRequest) request,
              (io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.SaveUserPreferenceResponse>) responseObserver);
          break;
        case METHODID_GET_USER_PREFERENCES:
          serviceImpl.getUserPreferences((com.skyeai.jarvis.protobuf.GetUserPreferencesRequest) request,
              (io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.GetUserPreferencesResponse>) responseObserver);
          break;
        case METHODID_DELETE_USER_PREFERENCE:
          serviceImpl.deleteUserPreference((com.skyeai.jarvis.protobuf.DeleteUserPreferenceRequest) request,
              (io.grpc.stub.StreamObserver<com.skyeai.jarvis.protobuf.DeleteUserPreferenceResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getSaveChatHistoryMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.skyeai.jarvis.protobuf.SaveChatHistoryRequest,
              com.skyeai.jarvis.protobuf.SaveChatHistoryResponse>(
                service, METHODID_SAVE_CHAT_HISTORY)))
        .addMethod(
          getGetRecentChatHistoryMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.skyeai.jarvis.protobuf.GetRecentChatHistoryRequest,
              com.skyeai.jarvis.protobuf.GetRecentChatHistoryResponse>(
                service, METHODID_GET_RECENT_CHAT_HISTORY)))
        .addMethod(
          getGetChatHistoryBySessionIdMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdRequest,
              com.skyeai.jarvis.protobuf.GetChatHistoryBySessionIdResponse>(
                service, METHODID_GET_CHAT_HISTORY_BY_SESSION_ID)))
        .addMethod(
          getExtractKeyInfoFromContextMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextRequest,
              com.skyeai.jarvis.protobuf.ExtractKeyInfoFromContextResponse>(
                service, METHODID_EXTRACT_KEY_INFO_FROM_CONTEXT)))
        .addMethod(
          getSaveStockPriceMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.skyeai.jarvis.protobuf.SaveStockPriceRequest,
              com.skyeai.jarvis.protobuf.SaveStockPriceResponse>(
                service, METHODID_SAVE_STOCK_PRICE)))
        .addMethod(
          getGetRecentStockPriceBySymbolMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolRequest,
              com.skyeai.jarvis.protobuf.GetRecentStockPriceBySymbolResponse>(
                service, METHODID_GET_RECENT_STOCK_PRICE_BY_SYMBOL)))
        .addMethod(
          getSavePriceChangeInfoMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.skyeai.jarvis.protobuf.SavePriceChangeInfoRequest,
              com.skyeai.jarvis.protobuf.SavePriceChangeInfoResponse>(
                service, METHODID_SAVE_PRICE_CHANGE_INFO)))
        .addMethod(
          getGetRecentPriceChangeInfoMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoRequest,
              com.skyeai.jarvis.protobuf.GetRecentPriceChangeInfoResponse>(
                service, METHODID_GET_RECENT_PRICE_CHANGE_INFO)))
        .addMethod(
          getSaveScheduleEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.skyeai.jarvis.protobuf.SaveScheduleEventRequest,
              com.skyeai.jarvis.protobuf.SaveScheduleEventResponse>(
                service, METHODID_SAVE_SCHEDULE_EVENT)))
        .addMethod(
          getGetAllActiveScheduleEventsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsRequest,
              com.skyeai.jarvis.protobuf.GetAllActiveScheduleEventsResponse>(
                service, METHODID_GET_ALL_ACTIVE_SCHEDULE_EVENTS)))
        .addMethod(
          getUpdateScheduleEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.skyeai.jarvis.protobuf.UpdateScheduleEventRequest,
              com.skyeai.jarvis.protobuf.UpdateScheduleEventResponse>(
                service, METHODID_UPDATE_SCHEDULE_EVENT)))
        .addMethod(
          getDeleteScheduleEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.skyeai.jarvis.protobuf.DeleteScheduleEventRequest,
              com.skyeai.jarvis.protobuf.DeleteScheduleEventResponse>(
                service, METHODID_DELETE_SCHEDULE_EVENT)))
        .addMethod(
          getSaveUserPreferenceMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.skyeai.jarvis.protobuf.SaveUserPreferenceRequest,
              com.skyeai.jarvis.protobuf.SaveUserPreferenceResponse>(
                service, METHODID_SAVE_USER_PREFERENCE)))
        .addMethod(
          getGetUserPreferencesMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.skyeai.jarvis.protobuf.GetUserPreferencesRequest,
              com.skyeai.jarvis.protobuf.GetUserPreferencesResponse>(
                service, METHODID_GET_USER_PREFERENCES)))
        .addMethod(
          getDeleteUserPreferenceMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.skyeai.jarvis.protobuf.DeleteUserPreferenceRequest,
              com.skyeai.jarvis.protobuf.DeleteUserPreferenceResponse>(
                service, METHODID_DELETE_USER_PREFERENCE)))
        .build();
  }

  private static abstract class DataServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    DataServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.skyeai.jarvis.protobuf.DataServiceProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("DataService");
    }
  }

  private static final class DataServiceFileDescriptorSupplier
      extends DataServiceBaseDescriptorSupplier {
    DataServiceFileDescriptorSupplier() {}
  }

  private static final class DataServiceMethodDescriptorSupplier
      extends DataServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    DataServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (DataServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new DataServiceFileDescriptorSupplier())
              .addMethod(getSaveChatHistoryMethod())
              .addMethod(getGetRecentChatHistoryMethod())
              .addMethod(getGetChatHistoryBySessionIdMethod())
              .addMethod(getExtractKeyInfoFromContextMethod())
              .addMethod(getSaveStockPriceMethod())
              .addMethod(getGetRecentStockPriceBySymbolMethod())
              .addMethod(getSavePriceChangeInfoMethod())
              .addMethod(getGetRecentPriceChangeInfoMethod())
              .addMethod(getSaveScheduleEventMethod())
              .addMethod(getGetAllActiveScheduleEventsMethod())
              .addMethod(getUpdateScheduleEventMethod())
              .addMethod(getDeleteScheduleEventMethod())
              .addMethod(getSaveUserPreferenceMethod())
              .addMethod(getGetUserPreferencesMethod())
              .addMethod(getDeleteUserPreferenceMethod())
              .build();
        }
      }
    }
    return result;
  }
}
