package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 **
 * JsEngineStreamingService - Bidirectional streaming service
 * Replaces both JsEngineService and HostCallbackService with a single stream.
 * IS opens a stream, sends requests, receives callbacks, sends callback responses,
 * and finally receives the evaluation result - all on one connection.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.51.1)",
    comments = "Source: js_engine_grpc.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class JsEngineStreamingServiceGrpc {

  private JsEngineStreamingServiceGrpc() {}

  public static final String SERVICE_NAME = "org.wso2.carbon.identity.graaljs.engine.proto.JsEngineStreamingService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.StreamMessage,
      org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.StreamMessage> getExecuteScriptMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ExecuteScript",
      requestType = org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.StreamMessage.class,
      responseType = org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.StreamMessage.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.StreamMessage,
      org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.StreamMessage> getExecuteScriptMethod() {
    io.grpc.MethodDescriptor<org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.StreamMessage, org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.StreamMessage> getExecuteScriptMethod;
    if ((getExecuteScriptMethod = JsEngineStreamingServiceGrpc.getExecuteScriptMethod) == null) {
      synchronized (JsEngineStreamingServiceGrpc.class) {
        if ((getExecuteScriptMethod = JsEngineStreamingServiceGrpc.getExecuteScriptMethod) == null) {
          JsEngineStreamingServiceGrpc.getExecuteScriptMethod = getExecuteScriptMethod =
              io.grpc.MethodDescriptor.<org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.StreamMessage, org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.StreamMessage>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ExecuteScript"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.StreamMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.StreamMessage.getDefaultInstance()))
              .setSchemaDescriptor(new JsEngineStreamingServiceMethodDescriptorSupplier("ExecuteScript"))
              .build();
        }
      }
    }
    return getExecuteScriptMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static JsEngineStreamingServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<JsEngineStreamingServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<JsEngineStreamingServiceStub>() {
        @java.lang.Override
        public JsEngineStreamingServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new JsEngineStreamingServiceStub(channel, callOptions);
        }
      };
    return JsEngineStreamingServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static JsEngineStreamingServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<JsEngineStreamingServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<JsEngineStreamingServiceBlockingStub>() {
        @java.lang.Override
        public JsEngineStreamingServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new JsEngineStreamingServiceBlockingStub(channel, callOptions);
        }
      };
    return JsEngineStreamingServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static JsEngineStreamingServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<JsEngineStreamingServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<JsEngineStreamingServiceFutureStub>() {
        @java.lang.Override
        public JsEngineStreamingServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new JsEngineStreamingServiceFutureStub(channel, callOptions);
        }
      };
    return JsEngineStreamingServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   **
   * JsEngineStreamingService - Bidirectional streaming service
   * Replaces both JsEngineService and HostCallbackService with a single stream.
   * IS opens a stream, sends requests, receives callbacks, sends callback responses,
   * and finally receives the evaluation result - all on one connection.
   * </pre>
   */
  public static abstract class JsEngineStreamingServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Single bidirectional stream for entire script execution lifecycle
     * </pre>
     */
    public io.grpc.stub.StreamObserver<org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.StreamMessage> executeScript(
        io.grpc.stub.StreamObserver<org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.StreamMessage> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getExecuteScriptMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getExecuteScriptMethod(),
            io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
              new MethodHandlers<
                org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.StreamMessage,
                org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.StreamMessage>(
                  this, METHODID_EXECUTE_SCRIPT)))
          .build();
    }
  }

  /**
   * <pre>
   **
   * JsEngineStreamingService - Bidirectional streaming service
   * Replaces both JsEngineService and HostCallbackService with a single stream.
   * IS opens a stream, sends requests, receives callbacks, sends callback responses,
   * and finally receives the evaluation result - all on one connection.
   * </pre>
   */
  public static final class JsEngineStreamingServiceStub extends io.grpc.stub.AbstractAsyncStub<JsEngineStreamingServiceStub> {
    private JsEngineStreamingServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected JsEngineStreamingServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new JsEngineStreamingServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Single bidirectional stream for entire script execution lifecycle
     * </pre>
     */
    public io.grpc.stub.StreamObserver<org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.StreamMessage> executeScript(
        io.grpc.stub.StreamObserver<org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.StreamMessage> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getExecuteScriptMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * <pre>
   **
   * JsEngineStreamingService - Bidirectional streaming service
   * Replaces both JsEngineService and HostCallbackService with a single stream.
   * IS opens a stream, sends requests, receives callbacks, sends callback responses,
   * and finally receives the evaluation result - all on one connection.
   * </pre>
   */
  public static final class JsEngineStreamingServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<JsEngineStreamingServiceBlockingStub> {
    private JsEngineStreamingServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected JsEngineStreamingServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new JsEngineStreamingServiceBlockingStub(channel, callOptions);
    }
  }

  /**
   * <pre>
   **
   * JsEngineStreamingService - Bidirectional streaming service
   * Replaces both JsEngineService and HostCallbackService with a single stream.
   * IS opens a stream, sends requests, receives callbacks, sends callback responses,
   * and finally receives the evaluation result - all on one connection.
   * </pre>
   */
  public static final class JsEngineStreamingServiceFutureStub extends io.grpc.stub.AbstractFutureStub<JsEngineStreamingServiceFutureStub> {
    private JsEngineStreamingServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected JsEngineStreamingServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new JsEngineStreamingServiceFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_EXECUTE_SCRIPT = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final JsEngineStreamingServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(JsEngineStreamingServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_EXECUTE_SCRIPT:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.executeScript(
              (io.grpc.stub.StreamObserver<org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.StreamMessage>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class JsEngineStreamingServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    JsEngineStreamingServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.grpc.JsEngineGrpcProtos.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("JsEngineStreamingService");
    }
  }

  private static final class JsEngineStreamingServiceFileDescriptorSupplier
      extends JsEngineStreamingServiceBaseDescriptorSupplier {
    JsEngineStreamingServiceFileDescriptorSupplier() {}
  }

  private static final class JsEngineStreamingServiceMethodDescriptorSupplier
      extends JsEngineStreamingServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    JsEngineStreamingServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (JsEngineStreamingServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new JsEngineStreamingServiceFileDescriptorSupplier())
              .addMethod(getExecuteScriptMethod())
              .build();
        }
      }
    }
    return result;
  }
}
