/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.server;

import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.JsGraalGraphEngineModeRouter;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.RemoteEngineTransport;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.RemoteJsEngine;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.EvaluateRequest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.EvaluateResponse;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.ExecuteCallbackRequest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.ExecuteCallbackResponse;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.StreamMessage;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.grpc.JsEngineStreamingServiceGrpc;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Bidirectional streaming gRPC transport implementation.
 * Manages per-session HTTP/2 stream lifecycle for script evaluation and callback execution.
 * <p>
 * Each {@link #sendEvaluate(EvaluateRequest, RemoteJsEngine)} /
 * {@link #sendExecuteCallback(ExecuteCallbackRequest, RemoteJsEngine)} call opens its own
 * HTTP/2 stream. This gives each session its own lock, its own stream lifecycle, and avoids
 * contention between concurrent sessions. The stream closes after the response is received.
 * <p>
 * External callback handling (host functions, context property get/set) is delegated to
 * {@link RemoteJsEngine} callback methods, keeping this class focused on stream mechanics.
 * <p>
 * Thread model:
 * <ul>
 *   <li>IS HTTP thread (Thread A) calls sendEvaluate()/sendExecuteCallback(), sends the initial
 *       request, then enters a message loop polling a BlockingQueue</li>
 *   <li>gRPC event thread receives StreamMessage via onNext() and enqueues to the BlockingQueue</li>
 *   <li>Thread A polls the queue, dispatches callbacks to {@link RemoteJsEngine} inline
 *       (same thread), and sends the returned response on the stream</li>
 *   <li>All stream writes happen on Thread A — no concurrent writer contention</li>
 * </ul>
 */
public class GrpcStreamingTransportImpl implements RemoteEngineTransport {

    private static final Log log = LogFactory.getLog(GrpcStreamingTransportImpl.class);
    private static final int DEFAULT_REQUEST_TIMEOUT_SECONDS = 600;
    private static final String GRAALJS_GRPC_REQUEST_TIMEOUT = "AdaptiveAuth.GraalJS.GrpcRequestTimeout";

    /**
     * Sentinel message used to unblock the message loop when the stream terminates
     * via onError() or onCompleted(). Identity-compared (==) in the message loop.
     */
    private static final StreamMessage STREAM_TERMINATED_SENTINEL = StreamMessage.getDefaultInstance();

    private final String grpcTarget;
    private final int requestTimeout;
    private final GrpcConnectionManager connectionManager;
    private final String correlationId;

    public GrpcStreamingTransportImpl(String grpcTarget) {
        this(grpcTarget, resolveRequestTimeout());
    }

    public GrpcStreamingTransportImpl(String grpcTarget, int requestTimeout) {
        this.grpcTarget = grpcTarget;
        this.requestTimeout = requestTimeout;
        this.connectionManager = GrpcConnectionManager.getInstance();
        this.correlationId = UUID.randomUUID().toString();
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("Created streaming transport for target: " + grpcTarget +
                    ", timeout: " + requestTimeout + "s, correlationId: " + correlationId);
        }
    }

    private static int resolveRequestTimeout() {

        String timeoutStr = IdentityUtil.getProperty(GRAALJS_GRPC_REQUEST_TIMEOUT);
        if (timeoutStr != null) {
            try {
                int timeout = Integer.parseInt(timeoutStr);
                if (timeout > 0) {
                    return timeout;
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid gRPC request timeout: " + timeoutStr);
            }
        }
        return DEFAULT_REQUEST_TIMEOUT_SECONDS;
    }

    // ============ RemoteEngineTransport Methods ============

    @Override
    public EvaluateResponse sendEvaluate(EvaluateRequest request, RemoteJsEngine handler) throws IOException {
        String sessionId = request.getSessionId();
        long t0 = System.currentTimeMillis();
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("[PERF] [" + t0 + "] IS_EVALUATE_START sessionId=" + sessionId + " startTs=" + t0 +
                    " scriptLen=" + request.getScript().length());
        }
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("sendEvaluate() sessionId=" + sessionId + ", script length: " + request.getScript().length());
        }

        JsEngineStreamingServiceGrpc.JsEngineStreamingServiceStub stub = getStub();

        BlockingQueue<StreamMessage> messageQueue = new LinkedBlockingQueue<>();
        AtomicReference<Throwable> streamError = new AtomicReference<>();
        final Object streamLock = new Object();

        long t1 = System.currentTimeMillis();
        StreamObserver<StreamMessage> outboundStream = stub.executeScript(
                createResponseObserver(sessionId, messageQueue, streamError, t0));
        long t2 = System.currentTimeMillis();
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("[PERF] [" + t2 + "] IS_STREAM_OPENED sessionId=" + sessionId +
                    " startTs=" + t0 + " stubReadyTs=" + t1 + " streamOpenedTs=" + t2 +
                    " openMs=" + (t2 - t1) + " sinceStartMs=" + (t2 - t0));
        }

        // Send the evaluate request (sessionId kept in protobuf for debugging/tracing)
        StreamMessage streamMsg = StreamMessage.newBuilder()
                .setSessionId(sessionId)
                .setEvaluateRequest(request)
                .build();

        synchronized (streamLock) {
            outboundStream.onNext(streamMsg);
        }
        long t3 = System.currentTimeMillis();
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("[PERF] [" + t3 + "] IS_EVALUATE_SENT sessionId=" + sessionId +
                    " startTs=" + t0 + " streamOpenedTs=" + t2 + " sentTs=" + t3 +
                    " sendMs=" + (t3 - t2) + " sinceStartMs=" + (t3 - t0));
        }
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("Sent EvaluateRequest on stream, sessionId=" + sessionId);
        }

        try {
            long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(requestTimeout);
            StreamMessage terminalMsg = processMessageLoop(
                    messageQueue, streamError, sessionId, handler, outboundStream, streamLock, deadlineNanos);

            EvaluateResponse response = terminalMsg.getEvaluateResponse();
            long t4 = System.currentTimeMillis();
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[PERF] [" + t4 + "] IS_EVALUATE_RESPONSE sessionId=" + sessionId +
                        " success=" + response.getSuccess() +
                        " startTs=" + t0 + " sentTs=" + t3 + " responseTs=" + t4 +
                        " waitMs=" + (t4 - t3) + " totalMs=" + (t4 - t0));
            }
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("Received EvaluateResponse, sessionId=" + sessionId + ", success: " +
                        response.getSuccess());
            }
            return response;
        } catch (IOException e) {
            long tErr = System.currentTimeMillis();
            if (e.getMessage() != null && e.getMessage().startsWith("Request timed out")) {
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[PERF] [" + tErr + "] IS_EVALUATE_TIMEOUT sessionId=" + sessionId +
                            " startTs=" + t0 + " sentTs=" + t3 + " timeoutTs=" + tErr +
                            " timeoutMs=" + (tErr - t0));
                }
            } else {
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[PERF] [" + tErr + "] IS_EVALUATE_ERROR sessionId=" + sessionId +
                            " errorType=" + e.getClass().getSimpleName() + " startTs=" + t0 +
                            " errorTs=" + tErr + " totalMs=" + (tErr - t0));
                }
            }
            throw e;
        } finally {
            synchronized (streamLock) {
                try {
                    outboundStream.onCompleted();
                } catch (Exception e) {
                    if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                        log.debug("Error completing stream: " + e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public ExecuteCallbackResponse sendExecuteCallback(ExecuteCallbackRequest request,
                                                       RemoteJsEngine handler) throws IOException {
        String sessionId = request.getSessionId();
        long t0 = System.currentTimeMillis();
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("[PERF] [" + t0 + "] IS_EXEC_CALLBACK_START sessionId=" + sessionId + " startTs=" + t0 +
                    " fnLen=" + request.getFunctionSource().length());
        }
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("sendExecuteCallback() sessionId=" + sessionId + ", function length: " +
                    request.getFunctionSource().length());
        }

        JsEngineStreamingServiceGrpc.JsEngineStreamingServiceStub stub = getStub();

        BlockingQueue<StreamMessage> messageQueue = new LinkedBlockingQueue<>();
        AtomicReference<Throwable> streamError = new AtomicReference<>();
        final Object streamLock = new Object();

        long t1 = System.currentTimeMillis();
        StreamObserver<StreamMessage> outboundStream = stub.executeScript(
                createResponseObserver(sessionId, messageQueue, streamError, t0));
        long t2 = System.currentTimeMillis();
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("[PERF] [" + t2 + "] IS_EXEC_CALLBACK_STREAM_OPENED sessionId=" + sessionId +
                    " startTs=" + t0 + " stubReadyTs=" + t1 + " streamOpenedTs=" + t2 +
                    " openMs=" + (t2 - t1) + " sinceStartMs=" + (t2 - t0));
        }

        // Send the execute callback request (sessionId kept in protobuf for debugging/tracing)
        StreamMessage streamMsg = StreamMessage.newBuilder()
                .setSessionId(sessionId)
                .setExecuteCallbackRequest(request)
                .build();

        synchronized (streamLock) {
            outboundStream.onNext(streamMsg);
        }
        long t3 = System.currentTimeMillis();
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("[PERF] [" + t3 + "] IS_EXEC_CALLBACK_SENT sessionId=" + sessionId +
                    " startTs=" + t0 + " streamOpenedTs=" + t2 + " sentTs=" + t3 +
                    " sendMs=" + (t3 - t2) + " sinceStartMs=" + (t3 - t0));
        }
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("Sent ExecuteCallbackRequest on stream, sessionId=" + sessionId);
        }

        try {
            long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(requestTimeout);
            StreamMessage terminalMsg = processMessageLoop(
                    messageQueue, streamError, sessionId, handler, outboundStream, streamLock, deadlineNanos);

            ExecuteCallbackResponse response = terminalMsg.getExecuteCallbackResponse();
            long t4 = System.currentTimeMillis();
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[PERF] [" + t4 + "] IS_EXEC_CALLBACK_RESPONSE sessionId=" + sessionId +
                        " success=" + response.getSuccess() +
                        " startTs=" + t0 + " sentTs=" + t3 + " responseTs=" + t4 +
                        " waitMs=" + (t4 - t3) + " totalMs=" + (t4 - t0));
            }
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("Received ExecuteCallbackResponse, sessionId=" + sessionId + ", success: " +
                        response.getSuccess());
            }
            return response;
        } catch (IOException e) {
            long tErr = System.currentTimeMillis();
            if (e.getMessage() != null && e.getMessage().startsWith("Request timed out")) {
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[PERF] [" + tErr + "] IS_EXEC_CALLBACK_TIMEOUT sessionId=" + sessionId +
                            " startTs=" + t0 + " sentTs=" + t3 + " timeoutTs=" + tErr +
                            " timeoutMs=" + (tErr - t0));
                }
            } else {
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[PERF] [" + tErr + "] IS_EXEC_CALLBACK_ERROR sessionId=" + sessionId +
                            " errorType=" + e.getClass().getSimpleName() + " startTs=" + t0 +
                            " errorTs=" + tErr + " totalMs=" + (tErr - t0));
                }
            }
            throw e;
        } finally {
            synchronized (streamLock) {
                try {
                    outboundStream.onCompleted();
                } catch (Exception e) {
                    if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                        log.debug("Error completing stream: " + e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void connect() throws IOException {
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("connect() to " + grpcTarget);
        }
        // Verify channel pool is initialized by requesting a channel
        connectionManager.getClientChannel(grpcTarget);
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("Connected successfully to: " + grpcTarget);
        }
    }

    @Override
    public boolean isConnected() {
        return connectionManager.isClientChannelConnected();
    }

    // ============ Message Loop ============

    /**
     * Polls the message queue, dispatches callback messages to {@link RemoteJsEngine} inline
     * on the calling thread (Thread A), and returns when a terminal response
     * (EvaluateResponse or ExecuteCallbackResponse) is received.
     * <p>
     * Timeout is deadline-based: the total time across ALL callbacks + sidecar execution
     * must not exceed {@code deadlineNanos}. Each poll uses the remaining time until the deadline.
     * Uses monotonic {@code System.nanoTime()} to be immune to wall-clock adjustments (NTP).
     *
     * @param messageQueue The queue populated by the gRPC response observer.
     * @param streamError  Holds any error set by onError()/onCompleted().
     * @param sessionId    Session identifier for logging.
     * @param handler      The host function handler for this session.
     * @param outbound     The outbound stream observer for sending callback responses.
     * @param streamLock   Lock for synchronized stream writes.
     * @param deadlineNanos Monotonic nanoTime deadline by which a terminal response must arrive.
     * @return The terminal StreamMessage containing EvaluateResponse or ExecuteCallbackResponse.
     * @throws IOException On timeout, interruption, or stream error.
     */
    private StreamMessage processMessageLoop(
            BlockingQueue<StreamMessage> messageQueue,
            AtomicReference<Throwable> streamError,
            String sessionId,
            RemoteJsEngine handler,
            StreamObserver<StreamMessage> outbound,
            Object streamLock,
            long deadlineNanos) throws IOException {

        while (true) {
            long remainingNanos = deadlineNanos - System.nanoTime();
            if (remainingNanos <= 0) {
                throw new IOException("Request timed out after " + requestTimeout + "s");
            }

            StreamMessage msg;
            try {
                msg = messageQueue.poll(remainingNanos, NANOSECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Request interrupted", e);
            }

            if (msg == null) {
                throw new IOException("Request timed out after " + requestTimeout + "s");
            }

            // Check for stream termination sentinel from onError()/onCompleted()
            if (msg == STREAM_TERMINATED_SENTINEL) {

                Throwable err = streamError.get();

                if (err != null) {
                    // real stream error
                    throw new IOException("Stream error: " + err.getMessage(), err);
                }

                // stream ended but no terminal response seen
                throw new IOException("Stream completed without response");
            }

            switch (msg.getPayloadCase()) {
                case EVALUATE_RESPONSE:
                case EXECUTE_CALLBACK_RESPONSE:
                    // Terminal response — return to caller
                    return msg;

                case HOST_FUNCTION_REQUEST:
                    sendOnStream(outbound, streamLock,
                            handler.handleHostFunctionCallback(sessionId, msg.getHostFunctionRequest()),
                            streamError, messageQueue);
                    break;

                case CONTEXT_PROPERTY_REQUEST:
                    sendOnStream(outbound, streamLock,
                            handler.handleContextPropertyCallback(sessionId, msg.getContextPropertyRequest()),
                            streamError, messageQueue);
                    break;

                case CONTEXT_PROPERTY_SET_REQUEST:
                    sendOnStream(outbound, streamLock,
                            handler.handleContextPropertySetCallback(sessionId,
                                    msg.getContextPropertySetRequest()), streamError, messageQueue);
                    break;

                default:
                    log.warn("Unexpected message type in loop: " + msg.getPayloadCase() +
                            ", sessionId=" + sessionId);
            }

        }
    }

    // ============ Stream Observer Factory ============

    /**
     * Creates a StreamObserver that enqueues all incoming messages into the BlockingQueue.
     * Thread A (the IS HTTP thread) polls this queue in {@link #processMessageLoop}.
     * <p>
     * onError()/onCompleted() signal stream termination by setting the error reference
     * and offering a sentinel message to unblock the queue poll.
     */
    private StreamObserver<StreamMessage> createResponseObserver(
            String sessionId,
            BlockingQueue<StreamMessage> messageQueue,
            AtomicReference<Throwable> streamError,
            long streamStartTime) {

        return new StreamObserver<StreamMessage>() {
            @Override
            public void onNext(StreamMessage message) {
                long now = System.currentTimeMillis();
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("Received message type: " + message.getPayloadCase() +
                            ", sessionId=" + sessionId);
                }

                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    switch (message.getPayloadCase()) {
                        case EVALUATE_RESPONSE:
                            log.debug("[PERF] [" + now + "] IS_EVALUATE_RESPONSE_ARRIVED sessionId=" + sessionId +
                                    " streamStartTs=" + streamStartTime + " arrivedTs=" + now +
                                    " sinceStreamStartMs=" + (now - streamStartTime));
                            break;

                        case EXECUTE_CALLBACK_RESPONSE:
                            log.debug("[PERF] [" + now + "] IS_EXEC_CALLBACK_RESPONSE_ARRIVED sessionId=" +
                                    sessionId + " streamStartTs=" + streamStartTime + " arrivedTs=" + now +
                                    " sinceStreamStartMs=" + (now - streamStartTime));
                            break;

                        case HOST_FUNCTION_REQUEST:
                            log.debug("[PERF] [" + now + "] IS_HOST_FN_REQUEST_RECEIVED sessionId=" + sessionId +
                                    " streamStartTs=" + streamStartTime + " receivedTs=" + now +
                                    " sinceStreamStartMs=" + (now - streamStartTime));
                            break;

                        case CONTEXT_PROPERTY_REQUEST:
                            log.debug("[PERF] [" + now + "] IS_CTX_PROP_REQUEST_RECEIVED sessionId=" + sessionId +
                                    " streamStartTs=" + streamStartTime + " receivedTs=" + now +
                                    " sinceStreamStartMs=" + (now - streamStartTime));
                            break;

                        case CONTEXT_PROPERTY_SET_REQUEST:
                            log.debug("[PERF] [" + now + "] IS_CTX_PROP_SET_REQUEST_RECEIVED sessionId=" + sessionId +
                                    " streamStartTs=" + streamStartTime + " receivedTs=" + now +
                                    " sinceStreamStartMs=" + (now - streamStartTime));
                            break;

                        default:
                            log.warn("Unexpected message type: " + message.getPayloadCase() +
                                    ", sessionId=" + sessionId);
                    }
                }
                // All messages go into the queue
                messageQueue.offer(message);
            }

            @Override
            public void onError(Throwable t) {
                long errTs = System.currentTimeMillis();
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[PERF] [" + errTs + "] IS_STREAM_ERROR sessionId=" + sessionId +
                            " streamStartTs=" + streamStartTime + " errorTs=" + errTs +
                            " errorType=" + t.getClass().getSimpleName() +
                            " sinceStreamStartMs=" + (errTs - streamStartTime));
                }
                log.error("Stream error, sessionId=" + sessionId, t);
                streamError.set(t);
                messageQueue.offer(STREAM_TERMINATED_SENTINEL);
            }

            @Override
            public void onCompleted() {
                long completedTs = System.currentTimeMillis();
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[PERF] [" + completedTs + "] IS_STREAM_COMPLETED sessionId=" + sessionId +
                            " streamStartTs=" + streamStartTime + " completedTs=" + completedTs +
                            " sinceStreamStartMs=" + (completedTs - streamStartTime));
                }
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("Stream completed, sessionId=" + sessionId);
                }

                messageQueue.offer(STREAM_TERMINATED_SENTINEL);
            }
        };
    }

    // ============ Stream Write Helper ============

    /**
     * Thread-safe send on the bidirectional stream.
     *
     * @param outbound The outbound stream observer.
     * @param lock     The lock object for synchronized access.
     * @param message  The message to send.
     * @param streamError Holds stream error state for sentinel-based termination.
     * @param messageQueue Queue used by the message loop.
     */
    private void sendOnStream(StreamObserver<StreamMessage> outbound, Object lock, StreamMessage message,
                              AtomicReference<Throwable> streamError,
                              BlockingQueue<StreamMessage> messageQueue) throws IOException {

        synchronized (lock) {
            try {
                outbound.onNext(message);
            } catch (Exception e) {
                log.error("Error sending on stream: " + e.getMessage(), e);
                streamError.compareAndSet(null, e);
                messageQueue.offer(STREAM_TERMINATED_SENTINEL);
                throw new IOException("Error sending on stream: " + e.getMessage(), e);
            }
        }
    }

    // ============ Channel Management ============

    /**
     * Get a stub for a round-robin selected channel from the pool.
     * This distributes streams across multiple TCP connections, avoiding
     * HTTP/2 flow control contention and head-of-line blocking.
     */
    private JsEngineStreamingServiceGrpc.JsEngineStreamingServiceStub getStub() {
        ManagedChannel channel = connectionManager.getClientChannel(grpcTarget);
        return JsEngineStreamingServiceGrpc.newStub(channel)
                .withDeadline(Deadline.after(requestTimeout, TimeUnit.SECONDS));
    }
}
