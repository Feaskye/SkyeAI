package api;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * 边缘服务定义
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.54.0)",
    comments = "Source: edge.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class EdgeServiceGrpc {

  private EdgeServiceGrpc() {}

  public static final String SERVICE_NAME = "api.EdgeService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<api.Edge.HealthCheckRequest,
      api.Edge.HealthCheckResponse> getHealthCheckMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "HealthCheck",
      requestType = api.Edge.HealthCheckRequest.class,
      responseType = api.Edge.HealthCheckResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<api.Edge.HealthCheckRequest,
      api.Edge.HealthCheckResponse> getHealthCheckMethod() {
    io.grpc.MethodDescriptor<api.Edge.HealthCheckRequest, api.Edge.HealthCheckResponse> getHealthCheckMethod;
    if ((getHealthCheckMethod = EdgeServiceGrpc.getHealthCheckMethod) == null) {
      synchronized (EdgeServiceGrpc.class) {
        if ((getHealthCheckMethod = EdgeServiceGrpc.getHealthCheckMethod) == null) {
          EdgeServiceGrpc.getHealthCheckMethod = getHealthCheckMethod =
              io.grpc.MethodDescriptor.<api.Edge.HealthCheckRequest, api.Edge.HealthCheckResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "HealthCheck"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  api.Edge.HealthCheckRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  api.Edge.HealthCheckResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EdgeServiceMethodDescriptorSupplier("HealthCheck"))
              .build();
        }
      }
    }
    return getHealthCheckMethod;
  }

  private static volatile io.grpc.MethodDescriptor<api.Edge.StatusRequest,
      api.Edge.StatusResponse> getGetStatusMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetStatus",
      requestType = api.Edge.StatusRequest.class,
      responseType = api.Edge.StatusResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<api.Edge.StatusRequest,
      api.Edge.StatusResponse> getGetStatusMethod() {
    io.grpc.MethodDescriptor<api.Edge.StatusRequest, api.Edge.StatusResponse> getGetStatusMethod;
    if ((getGetStatusMethod = EdgeServiceGrpc.getGetStatusMethod) == null) {
      synchronized (EdgeServiceGrpc.class) {
        if ((getGetStatusMethod = EdgeServiceGrpc.getGetStatusMethod) == null) {
          EdgeServiceGrpc.getGetStatusMethod = getGetStatusMethod =
              io.grpc.MethodDescriptor.<api.Edge.StatusRequest, api.Edge.StatusResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetStatus"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  api.Edge.StatusRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  api.Edge.StatusResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EdgeServiceMethodDescriptorSupplier("GetStatus"))
              .build();
        }
      }
    }
    return getGetStatusMethod;
  }

  private static volatile io.grpc.MethodDescriptor<api.Edge.AudioRequest,
      api.Edge.AudioResponse> getStreamAudioMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "StreamAudio",
      requestType = api.Edge.AudioRequest.class,
      responseType = api.Edge.AudioResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<api.Edge.AudioRequest,
      api.Edge.AudioResponse> getStreamAudioMethod() {
    io.grpc.MethodDescriptor<api.Edge.AudioRequest, api.Edge.AudioResponse> getStreamAudioMethod;
    if ((getStreamAudioMethod = EdgeServiceGrpc.getStreamAudioMethod) == null) {
      synchronized (EdgeServiceGrpc.class) {
        if ((getStreamAudioMethod = EdgeServiceGrpc.getStreamAudioMethod) == null) {
          EdgeServiceGrpc.getStreamAudioMethod = getStreamAudioMethod =
              io.grpc.MethodDescriptor.<api.Edge.AudioRequest, api.Edge.AudioResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "StreamAudio"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  api.Edge.AudioRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  api.Edge.AudioResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EdgeServiceMethodDescriptorSupplier("StreamAudio"))
              .build();
        }
      }
    }
    return getStreamAudioMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static EdgeServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EdgeServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EdgeServiceStub>() {
        @java.lang.Override
        public EdgeServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EdgeServiceStub(channel, callOptions);
        }
      };
    return EdgeServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static EdgeServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EdgeServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EdgeServiceBlockingStub>() {
        @java.lang.Override
        public EdgeServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EdgeServiceBlockingStub(channel, callOptions);
        }
      };
    return EdgeServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static EdgeServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EdgeServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EdgeServiceFutureStub>() {
        @java.lang.Override
        public EdgeServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EdgeServiceFutureStub(channel, callOptions);
        }
      };
    return EdgeServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * 边缘服务定义
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * 健康检查
     * </pre>
     */
    default void healthCheck(api.Edge.HealthCheckRequest request,
        io.grpc.stub.StreamObserver<api.Edge.HealthCheckResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getHealthCheckMethod(), responseObserver);
    }

    /**
     * <pre>
     * 获取服务状态
     * </pre>
     */
    default void getStatus(api.Edge.StatusRequest request,
        io.grpc.stub.StreamObserver<api.Edge.StatusResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetStatusMethod(), responseObserver);
    }

    /**
     * <pre>
     * 音频流处理（双向流）
     * </pre>
     */
    default io.grpc.stub.StreamObserver<api.Edge.AudioRequest> streamAudio(
        io.grpc.stub.StreamObserver<api.Edge.AudioResponse> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getStreamAudioMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service EdgeService.
   * <pre>
   * 边缘服务定义
   * </pre>
   */
  public static abstract class EdgeServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return EdgeServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service EdgeService.
   * <pre>
   * 边缘服务定义
   * </pre>
   */
  public static final class EdgeServiceStub
      extends io.grpc.stub.AbstractAsyncStub<EdgeServiceStub> {
    private EdgeServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EdgeServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EdgeServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * 健康检查
     * </pre>
     */
    public void healthCheck(api.Edge.HealthCheckRequest request,
        io.grpc.stub.StreamObserver<api.Edge.HealthCheckResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getHealthCheckMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * 获取服务状态
     * </pre>
     */
    public void getStatus(api.Edge.StatusRequest request,
        io.grpc.stub.StreamObserver<api.Edge.StatusResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetStatusMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * 音频流处理（双向流）
     * </pre>
     */
    public io.grpc.stub.StreamObserver<api.Edge.AudioRequest> streamAudio(
        io.grpc.stub.StreamObserver<api.Edge.AudioResponse> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getStreamAudioMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service EdgeService.
   * <pre>
   * 边缘服务定义
   * </pre>
   */
  public static final class EdgeServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<EdgeServiceBlockingStub> {
    private EdgeServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EdgeServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EdgeServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * 健康检查
     * </pre>
     */
    public api.Edge.HealthCheckResponse healthCheck(api.Edge.HealthCheckRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getHealthCheckMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * 获取服务状态
     * </pre>
     */
    public api.Edge.StatusResponse getStatus(api.Edge.StatusRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetStatusMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service EdgeService.
   * <pre>
   * 边缘服务定义
   * </pre>
   */
  public static final class EdgeServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<EdgeServiceFutureStub> {
    private EdgeServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EdgeServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EdgeServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * 健康检查
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<api.Edge.HealthCheckResponse> healthCheck(
        api.Edge.HealthCheckRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getHealthCheckMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * 获取服务状态
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<api.Edge.StatusResponse> getStatus(
        api.Edge.StatusRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetStatusMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_HEALTH_CHECK = 0;
  private static final int METHODID_GET_STATUS = 1;
  private static final int METHODID_STREAM_AUDIO = 2;

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
        case METHODID_HEALTH_CHECK:
          serviceImpl.healthCheck((api.Edge.HealthCheckRequest) request,
              (io.grpc.stub.StreamObserver<api.Edge.HealthCheckResponse>) responseObserver);
          break;
        case METHODID_GET_STATUS:
          serviceImpl.getStatus((api.Edge.StatusRequest) request,
              (io.grpc.stub.StreamObserver<api.Edge.StatusResponse>) responseObserver);
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
        case METHODID_STREAM_AUDIO:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.streamAudio(
              (io.grpc.stub.StreamObserver<api.Edge.AudioResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getHealthCheckMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              api.Edge.HealthCheckRequest,
              api.Edge.HealthCheckResponse>(
                service, METHODID_HEALTH_CHECK)))
        .addMethod(
          getGetStatusMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              api.Edge.StatusRequest,
              api.Edge.StatusResponse>(
                service, METHODID_GET_STATUS)))
        .addMethod(
          getStreamAudioMethod(),
          io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
            new MethodHandlers<
              api.Edge.AudioRequest,
              api.Edge.AudioResponse>(
                service, METHODID_STREAM_AUDIO)))
        .build();
  }

  private static abstract class EdgeServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    EdgeServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return api.Edge.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("EdgeService");
    }
  }

  private static final class EdgeServiceFileDescriptorSupplier
      extends EdgeServiceBaseDescriptorSupplier {
    EdgeServiceFileDescriptorSupplier() {}
  }

  private static final class EdgeServiceMethodDescriptorSupplier
      extends EdgeServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    EdgeServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (EdgeServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new EdgeServiceFileDescriptorSupplier())
              .addMethod(getHealthCheckMethod())
              .addMethod(getGetStatusMethod())
              .addMethod(getStreamAudioMethod())
              .build();
        }
      }
    }
    return result;
  }
}
