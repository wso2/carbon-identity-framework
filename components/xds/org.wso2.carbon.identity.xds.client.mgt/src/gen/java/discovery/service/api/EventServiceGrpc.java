package discovery.service.api;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.4.0)",
    comments = "Source: event.proto")
public final class EventServiceGrpc {

  private EventServiceGrpc() {}

  public static final String SERVICE_NAME = "discovery.service.api.EventService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<discovery.service.api.EventOuterClass.Event,
      discovery.service.api.EventOuterClass.Event> METHOD_ADD_EVENT =
      io.grpc.MethodDescriptor.<discovery.service.api.EventOuterClass.Event, discovery.service.api.EventOuterClass.Event>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "discovery.service.api.EventService", "AddEvent"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              discovery.service.api.EventOuterClass.Event.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              discovery.service.api.EventOuterClass.Event.getDefaultInstance()))
          .build();

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static EventServiceStub newStub(io.grpc.Channel channel) {
    return new EventServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static EventServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new EventServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static EventServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new EventServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class EventServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void addEvent(discovery.service.api.EventOuterClass.Event request,
        io.grpc.stub.StreamObserver<discovery.service.api.EventOuterClass.Event> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_ADD_EVENT, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_ADD_EVENT,
            asyncUnaryCall(
              new MethodHandlers<
                discovery.service.api.EventOuterClass.Event,
                discovery.service.api.EventOuterClass.Event>(
                  this, METHODID_ADD_EVENT)))
          .build();
    }
  }

  /**
   */
  public static final class EventServiceStub extends io.grpc.stub.AbstractStub<EventServiceStub> {
    private EventServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private EventServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EventServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new EventServiceStub(channel, callOptions);
    }

    /**
     */
    public void addEvent(discovery.service.api.EventOuterClass.Event request,
        io.grpc.stub.StreamObserver<discovery.service.api.EventOuterClass.Event> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ADD_EVENT, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class EventServiceBlockingStub extends io.grpc.stub.AbstractStub<EventServiceBlockingStub> {
    private EventServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private EventServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EventServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new EventServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public discovery.service.api.EventOuterClass.Event addEvent(discovery.service.api.EventOuterClass.Event request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ADD_EVENT, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class EventServiceFutureStub extends io.grpc.stub.AbstractStub<EventServiceFutureStub> {
    private EventServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private EventServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EventServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new EventServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<discovery.service.api.EventOuterClass.Event> addEvent(
        discovery.service.api.EventOuterClass.Event request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ADD_EVENT, getCallOptions()), request);
    }
  }

  private static final int METHODID_ADD_EVENT = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final EventServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(EventServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_ADD_EVENT:
          serviceImpl.addEvent((discovery.service.api.EventOuterClass.Event) request,
              (io.grpc.stub.StreamObserver<discovery.service.api.EventOuterClass.Event>) responseObserver);
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

  private static final class EventServiceDescriptorSupplier implements io.grpc.protobuf.ProtoFileDescriptorSupplier {
    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return discovery.service.api.EventOuterClass.getDescriptor();
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (EventServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new EventServiceDescriptorSupplier())
              .addMethod(METHOD_ADD_EVENT)
              .build();
        }
      }
    }
    return result;
  }
}
