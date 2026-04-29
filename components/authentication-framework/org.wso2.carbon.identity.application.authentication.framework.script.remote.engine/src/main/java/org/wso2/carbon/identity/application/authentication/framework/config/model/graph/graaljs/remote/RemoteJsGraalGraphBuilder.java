/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.polyglot.HostAccess;
import org.wso2.carbon.identity.application.authentication.framework.AsyncProcess;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDecisionEvaluator;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthGraphNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthenticationGraph;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.BaseSerializableJsFunction;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.DynamicDecisionNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.FailNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.GenericSerializableJsFunction;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JSExecutionMonitorData;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JSExecutionSupervisor;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsGraphBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.LongWaitNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.ShowPromptNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.StepConfigGraphNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.GraalSerializableJsFunction;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.GraalSerializer;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.JsGraalGraphEngineModeRouter;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.server.GrpcTransportProvider;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsLogger;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.GraalSelectAcrFromFunction;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.AdaptiveAuthentication.PROP_EXECUTION_SUPERVISOR_RESULT;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.AUTHENTICATION_OPTIONS;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.AUTHENTICATOR_PARAMS;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.JS_AUTH_FAILURE;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.JS_FUNC_EXECUTE_STEP;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.JS_FUNC_GET_SECRET_BY_NAME;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.JS_FUNC_LOAD_FUNC_LIB;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.JS_FUNC_ON_LOGIN_REQUEST;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.JS_FUNC_SELECT_ACR_FROM;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.JS_FUNC_SEND_ERROR;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.JS_FUNC_SHOW_PROMPT;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.JS_LOG;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.PROP_CURRENT_NODE;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.STEP_OPTIONS;

/**
 * Remote execution graph builder for adaptive authentication.
 * This builder handles script evaluation and callback execution via the external GraalJS sidecar
 * process over gRPC, while providing its own graph-building logic (executeStep, sendError, showPrompt,
 * addEventListeners, infuse, etc.) independently from the local-mode JsGraalGraphBuilder.
 * <p>
 * Both this class and JsGraalGraphBuilder are siblings extending JsGraphBuilder directly.
 * Common infrastructure (ThreadLocals, attachToLeaf, infuse, build, etc.) comes from JsGraphBuilder.
 * <p>
 * Instances are created by JsGraalGraphBuilderFactory when execution mode is REMOTE.
 * Each login session gets its own builder instance (not thread safe, discarded after each build).
 */
public class RemoteJsGraalGraphBuilder extends JsGraphBuilder {

    private static final Log log = LogFactory.getLog(RemoteJsGraalGraphBuilder.class);

    /**
     * Constructs the remote builder for initial script evaluation (createWith path).
     *
     * @param authenticationContext current authentication context.
     * @param stepConfigMap         The Step map from the service provider configuration.
     */
    public RemoteJsGraalGraphBuilder(AuthenticationContext authenticationContext,
                                     Map<Integer, StepConfig> stepConfigMap) {

        this.authenticationContext = authenticationContext;
        this.stepNamedMap = stepConfigMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Constructs the remote builder for callback evaluation (getScriptEvaluator path).
     *
     * @param authenticationContext current authentication context.
     * @param stepConfigMap         The Step map from the service provider configuration.
     * @param currentNode           Current authentication graph node.
     */
    public RemoteJsGraalGraphBuilder(AuthenticationContext authenticationContext,
                                     Map<Integer, StepConfig> stepConfigMap,
                                     AuthGraphNode currentNode) {

        this.authenticationContext = authenticationContext;
        this.currentNode = currentNode;
        this.stepNamedMap = stepConfigMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Creates the graph with the given Script using remote External execution.
     * This method sends the script to the External sidecar for evaluation and processes
     * callback results including host function invocations (executeStep, sendError, etc.).
     *
     * @param script the Dynamic authentication script.
     * @return This builder.
     */
    @Override
    @SuppressWarnings("unchecked")
    public RemoteJsGraalGraphBuilder createWith(String script) {

        JsEngine jsEngine = new RemoteJsEngine(
                GrpcTransportProvider.getTransport(JsGraalGraphEngineModeRouter.getGrpcTarget()),
                authenticationContext);
        try {
            currentBuilder.set(this);

            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[createWithRemote] Starting for SP: " + authenticationContext.getServiceProviderName() +
                        ", contextId: " + authenticationContext.getContextIdentifier());
            }

            // Register host functions that the External can call back.
            Map<String, Object> hostFunctions = new HashMap<>();
            hostFunctions.put(JS_FUNC_EXECUTE_STEP, new JsGraalStepExecuter());
            hostFunctions.put(JS_FUNC_SEND_ERROR, new SendErrorFunctionImpl());
            hostFunctions.put(JS_FUNC_SHOW_PROMPT, new JsGraalPromptExecutorImpl());
            hostFunctions.put(JS_FUNC_LOAD_FUNC_LIB, new JsGraalLoadExecutorImpl());
            hostFunctions.put(JS_FUNC_GET_SECRET_BY_NAME, new JsGraalGetSecretImpl());
            // Mirror the local-execution factory bindings so adaptive scripts that
            // use Log.info(...) / selectAcrFrom(...) work over the remote engine
            // too. JsLogger has multiple @HostAccess.Export methods — the registry
            // expands those into "Log.info" / "Log.debug" / ... entries; the
            // External's HostFunctionStub routes member access (Log.info) back as
            // the matching dotted host-function call.
            hostFunctions.put(JS_LOG, new JsLogger());
            hostFunctions.put(JS_FUNC_SELECT_ACR_FROM, new GraalSelectAcrFromFunction());

            JsFunctionRegistry jsFunctionRegistrar = FrameworkServiceDataHolder.getInstance().getJsFunctionRegistry();
            if (jsFunctionRegistrar != null) {
                hostFunctions.putAll(jsFunctionRegistrar.getSubsystemFunctionsMap(
                        JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER));
            }
            jsEngine.registerHostFunctions(hostFunctions);
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[createWithRemote] Registered " + hostFunctions.size() + " host functions: " +
                        hostFunctions.keySet());
            }

            // Build the complete script including require function, secrets, and main script.
            String completeScript = FrameworkServiceDataHolder.getInstance().getCodeForRequireFunction() +
                    "\n" +
                    FrameworkServiceDataHolder.getInstance().getCodeForSecretsFunction() +
                    "\n" +
                    script +
                    "\n" +
                    JS_FUNC_ON_LOGIN_REQUEST + "(context);";

            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[createWithRemote] Sending script (length: " + completeScript.length() +
                        ") to External for evaluation");
            }

            // Build initial bindings (the External will attach a DynamicContextProxy as "context").
            Map<String, Object> initialBindings = new HashMap<>();

            String identifier = UUID.randomUUID().toString();
            Optional<JSExecutionMonitorData> optionalScriptExecutionData = Optional.empty();

            try {
                startScriptExecutionMonitor(identifier, authenticationContext);

                // Evaluate script remotely.
                EvaluationResult evalResult = jsEngine.evaluate(
                        completeScript, "adaptive-script", initialBindings);

                if (!evalResult.isSuccess()) {
                    log.error("[createWithRemote] Script evaluation failed: " + evalResult.getErrorMessage());
                    result.setBuildSuccessful(false);
                    result.setErrorReason("Error in executing the Javascript. " + evalResult.getErrorMessage());
                    return this;
                }

                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[createWithRemote] Script evaluation successful, elapsed: " +
                            evalResult.getElapsedMs() + "ms");
                }

                // Update bindings from External response.
                if (evalResult.getUpdatedBindings() != null) {
                    if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                        log.debug("[createWithRemote] Updating bindings from External: " +
                                evalResult.getUpdatedBindings().keySet());
                    }
                    for (Map.Entry<String, Object> entry : evalResult.getUpdatedBindings().entrySet()) {
                        jsEngine.putBinding(entry.getKey(), entry.getValue());
                    }
                }

            } finally {
                optionalScriptExecutionData = Optional.ofNullable(endScriptExecutionMonitor(identifier));
            }

            optionalScriptExecutionData.ifPresent(
                    scriptExecutionData ->
                            storeAuthScriptExecutionMonitorData(authenticationContext,
                            scriptExecutionData));

            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[createWithRemote] Script execution completed for SP: " +
                        authenticationContext.getServiceProviderName());
            }

            // Persist bindings for later callback execution.
            // Note: With remote execution, we persist the updated bindings from External.
            Map<String, Object> persistableBindings = jsEngine.getBindings();
            authenticationContext.setProperty("JS_BINDING_CURRENT_CONTEXT", persistableBindings);
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[createWithRemote] Persisted " + persistableBindings.size() + " bindings");
            }

        } catch (Exception e) {
            log.error("[createWithRemote] Error during remote script evaluation", e);
            result.setBuildSuccessful(false);
            result.setErrorReason("Error in remote JavaScript execution: " + e.getMessage());
        } finally {
            currentBuilder.remove();
        }

        return this;
    }

    @Override
    public AuthenticationDecisionEvaluator getScriptEvaluator(BaseSerializableJsFunction fn) {

        return null;
    }

    @Override
    public AuthenticationDecisionEvaluator getScriptEvaluator(GenericSerializableJsFunction fn) {

        return new RemoteJsBasedEvaluator((GraalSerializableJsFunction) fn);
    }

    // =============================================================================================
    // Graph-building methods (executeStep, event listeners, prompt, long wait)
    // =============================================================================================

    /**
     * Adds the step given by step ID to the authentication graph.
     *
     * @param stepId Step Id
     * @param params params
     */
    @SuppressWarnings("unchecked")
    @HostAccess.Export
    public void executeStep(int stepId, Object... params) {

        StepConfig stepConfig;
        stepConfig = stepNamedMap.get(stepId);

        if (stepConfig == null) {
            log.error("Given Authentication Step :" + stepId + " is not in Environment");
            return;
        }
        StepConfigGraphNode newNode = wrap(stepConfig);
        if (currentNode == null) {
            result.setStartNode(newNode);
        } else {
            attachToLeaf(currentNode, newNode);
        }
        currentNode = newNode;
        if (params.length > 0) {
            // if there are any params provided, last one is assumed to be the event listeners
            if (params[params.length - 1] instanceof Map) {
                attachEventListeners((Map<String, Object>) params[params.length - 1]);
            } else {
                log.error("Invalid argument and hence ignored. Last argument should be a Map of event listeners.");
            }
        }
        if (params.length == 2) {
            // There is an argument with options present
            if (params[0] instanceof Map) {
                Map<String, Object> options = (Map<String, Object>) params[0];
                handleOptions(options, stepConfig);
            }
        }
    }

    /**
     * Adds the step given by step ID to the authentication graph during an async event (callback).
     *
     * @param params params
     */
    @SuppressWarnings("unchecked")
    @HostAccess.Export
    public void executeStepInAsyncEvent(int stepId, Object... params) {

        AuthenticationContext context = contextForJs.get();
        AuthGraphNode currentNode = dynamicallyBuiltBaseNode.get();

        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("Execute Step on async event. Step ID : " + stepId);
        }
        AuthenticationGraph graph = context.getSequenceConfig().getAuthenticationGraph();
        if (graph == null) {
            log.error("The graph happens to be null on the sequence config. Can not execute step : " + stepId);
            return;
        }

        StepConfig stepConfig = graph.getStepMap().get(stepId);
        if (stepConfig == null) {
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.error("The stepConfig of the step ID : " + stepId + " is null");
            }
            return;
        }
        // Inorder to keep original stepConfig as a backup in AuthenticationGraph.
        StepConfig clonedStepConfig = new StepConfig(stepConfig);
        StepConfig stepConfigFromContext = null;
        if (MapUtils.isNotEmpty(context.getSequenceConfig().getStepMap())) {
            stepConfigFromContext = context.getSequenceConfig().getStepMap().values()
                    .stream()
                    .filter(contextStepConfig -> (stepConfig.getOrder() == contextStepConfig.getOrder()))
                    .findFirst()
                    .orElse(null);
        }
        clonedStepConfig.applyStateChangesToNewObjectFromContextStepMap(stepConfigFromContext);
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("Found step for the Step ID : " + stepId + ", Step Config " + clonedStepConfig);
        }
        StepConfigGraphNode newNode = wrap(clonedStepConfig);
        if (currentNode == null) {
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("Setting a new node at the first time. Node : " + newNode.getName());
            }
            dynamicallyBuiltBaseNode.set(newNode);
        } else {
            attachToLeaf(currentNode, newNode);
        }

        if (params.length > 0) {
            // if there is only one param, it is assumed to be the event listeners
            if (params[params.length - 1] instanceof Map) {
                attachEventListeners((Map<String, Object>) params[params.length - 1], newNode);
            } else {
                log.error("Invalid argument and hence ignored. Last argument should be a Map of event listeners.");
            }
        }

        if (params.length == 2) {
            // There is an argument with options present
            if (params[0] instanceof Map) {
                Map<String, Object> options = (Map<String, Object>) params[0];
                handleOptionsAsyncEvent(options, clonedStepConfig, context.getSequenceConfig().getStepMap());
            }
        }
    }

    private static void attachEventListeners(Map<String, Object> eventsMap, AuthGraphNode currentNode) {

        if (eventsMap == null) {
            return;
        }
        DynamicDecisionNode decisionNode = new DynamicDecisionNode();
        addEventListeners(decisionNode, eventsMap);
        if (!decisionNode.getGenericFunctionMap().isEmpty()) {
            attachToLeaf(currentNode, decisionNode);
        }
    }

    private void attachEventListeners(Map<String, Object> eventsMap) {

        if (eventsMap == null) {
            return;
        }
        DynamicDecisionNode decisionNode = new DynamicDecisionNode();
        addEventListeners(decisionNode, eventsMap);
        if (!decisionNode.getGenericFunctionMap().isEmpty()) {
            attachToLeaf(currentNode, decisionNode);
            currentNode = decisionNode;
        }
    }

    /**
     * Adds all the event listeners to the decision node.
     *
     * @param eventsMap Map of events and event handler functions, which is handled by this execution.
     */
    private static void addEventListeners(DynamicDecisionNode decisionNode, Map<String, Object> eventsMap) {

        if (eventsMap == null) {
            return;
        }
        eventsMap.forEach((key, value) -> {
            if (value instanceof GraalSerializableJsFunction) {
                decisionNode.addGenericFunction(key, (GraalSerializableJsFunction) value);
            } else if (value instanceof String) {
                log.error("Event handler : " + key + " arrived as a String, expected a function. Skipping.");
            } else {
                // Local mode: value is a GraalJS Value object.
                GraalSerializableJsFunction jsFunction = GraalSerializableJsFunction.toSerializableForm(value);
                if (jsFunction != null) {
                    decisionNode.addGenericFunction(key, jsFunction);
                } else {
                    log.error("Event handler : " + key + " is not a function : " + value);
                }
            }
        });
    }

    private static void addHandlers(ShowPromptNode showPromptNode, Map<String, Object> handlersMap) {

        if (handlersMap == null) {
            return;
        }
        handlersMap.forEach((key, value) -> {
            if (value instanceof GraalSerializableJsFunction) {
                showPromptNode.addGenericHandler(key, (GraalSerializableJsFunction) value);
            } else if (value instanceof String) {
                log.error("Prompt handler : " + key + " arrived as a String, expected a function. Skipping.");
            } else {
                // Local mode: value is a GraalJS Value object.
                GraalSerializableJsFunction jsFunction = GraalSerializableJsFunction.toSerializableForm(value);
                if (jsFunction != null) {
                    showPromptNode.addGenericHandler(key, jsFunction);
                } else {
                    log.error("Prompt handler : " + key + " is not a function : " + value);
                }
            }
        });
    }

    @Override
    public void addLongWaitProcessInternal(AsyncProcess asyncProcess, Map<String, Object> parameterMap) {

        LongWaitNode newNode = new LongWaitNode(asyncProcess);

        if (parameterMap != null) {
            addEventListeners(newNode, parameterMap);
        }
        if (this.currentNode == null) {
            this.result.setStartNode(newNode);
        } else {
            attachToLeaf(this.currentNode, newNode);
        }

        this.currentNode = newNode;
    }

    /**
     * @param templateId Identifier of the template.
     * @param parameters Parameters.
     * @param handlers   Handlers to run before and after the prompt.
     * @param callbacks  Callbacks to run after the prompt.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void addPromptInternal(String templateId, Map<String, Object> parameters, Map<String, Object> handlers,
            Map<String, Object> callbacks) {

        ShowPromptNode newNode = new ShowPromptNode();
        newNode.setTemplateId(templateId);
        newNode.setParameters(parameters);

        RemoteJsGraalGraphBuilder currentBuilder = getCurrentBuilder();
        if (currentBuilder.currentNode == null) {
            currentBuilder.result.setStartNode(newNode);
        } else {
            attachToLeaf(currentBuilder.currentNode, newNode);
        }

        currentBuilder.currentNode = newNode;
        addEventListeners(newNode, callbacks);
        addHandlers(newNode, handlers);
    }

    /**
     * Adds a function to show a prompt in Javascript code.
     *
     * @param templateId Identifier of the template
     * @param parameters parameters
     */
    @SuppressWarnings("unchecked")
    @HostAccess.Export
    public void addShowPrompt(String templateId, Object... parameters) {

        ShowPromptNode newNode = new ShowPromptNode();
        newNode.setTemplateId(templateId);

        if (parameters.length == 2) {
            newNode.setData((Map<String, Serializable>) GraalSerializer.getInstance().toJsSerializable(parameters[0]));
        }
        if (currentNode == null) {
            result.setStartNode(newNode);
        } else {
            attachToLeaf(currentNode, newNode);
        }

        currentNode = newNode;
        if (parameters.length > 0) {
            if (parameters[parameters.length - 1] instanceof Map) {
                addEventListeners(newNode, (Map<String, Object>) parameters[parameters.length - 1]);
            } else {
                log.error("Invalid argument and hence ignored. Last argument should be a Map of event listeners.");
            }

        }
    }

    /**
     * Handle options within executeStepInAsyncEvent function. This method will update step configs through context.
     *
     * @param options       Map of authenticator options.
     * @param stepConfig    Current stepConfig.
     * @param stepConfigMap Map of stepConfigs get from the context object.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void handleOptionsAsyncEvent(Map<String, Object> options, StepConfig stepConfig,
                                           Map<Integer, StepConfig> stepConfigMap) {

        Object authenticationOptionsObj = options.get(AUTHENTICATION_OPTIONS);
        if (authenticationOptionsObj instanceof List) {
            List<Map<String, String>> authenticationOptionsList = (List<Map<String, String>>) authenticationOptionsObj;
            authenticationOptionsObj = IntStream
                    .range(0, authenticationOptionsList.size())
                    .boxed()
                    .collect(Collectors.toMap(String::valueOf, authenticationOptionsList::get));
        }

        if (authenticationOptionsObj instanceof Map) {
            filterOptions((Map<String, Map<String, String>>) authenticationOptionsObj, stepConfig);
        } else {
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("Authenticator options not provided or invalid, hence proceeding without filtering");
            }
        }

        Object authenticatorParams = options.get(AUTHENTICATOR_PARAMS);
        if (authenticatorParams instanceof Map) {
            authenticatorParamsOptions((Map<String, Object>) authenticatorParams, stepConfig);
        } else {
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("Authenticator params not provided or invalid, hence proceeding without setting params");
            }
        }

        Object stepOptions = options.get(STEP_OPTIONS);
        if (stepOptions instanceof Map) {
            handleStepOptions(stepConfig, (Map<String, String>) stepOptions, stepConfigMap);
        } else {
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("Step options not provided or invalid, hence proceeding without handling");
            }
        }
    }

    // =============================================================================================
    // Async error / fail functions (static, for callback paths)
    // =============================================================================================

    @HostAccess.Export
    public static void sendErrorAsync(String url, Map<String, Object> parameterMap) {

        FailNode newNode = createFailNode(url, parameterMap, true);

        AuthGraphNode currentNode = dynamicallyBuiltBaseNode.get();
        if (currentNode == null) {
            dynamicallyBuiltBaseNode.set(newNode);
        } else {
            attachToLeaf(currentNode, newNode);
        }
    }

    @HostAccess.Export
    public static void failAsync(Object... parameters) {

        Map<String, Object> parameterMap;

        if (parameters.length == 1) {
            parameterMap = (Map<String, Object>) parameters[0];
        } else {
            parameterMap = java.util.Collections.EMPTY_MAP;
        }

        FailNode newNode = createFailNode(org.apache.commons.lang.StringUtils.EMPTY, parameterMap, false);

        AuthGraphNode currentNode = dynamicallyBuiltBaseNode.get();
        if (currentNode == null) {
            dynamicallyBuiltBaseNode.set(newNode);
        } else {
            attachToLeaf(currentNode, newNode);
        }
    }

    private static FailNode createFailNode(String url, Map<String, Object> parameterMap, boolean isShowErrorPage) {

        FailNode failNode = new FailNode();
        if (isShowErrorPage && org.apache.commons.lang.StringUtils.isNotBlank(url)) {
            failNode.setErrorPageUri(url);
        }
        // setShowErrorPage is set to true as sendError function redirects to a specific error page.
        failNode.setShowErrorPage(isShowErrorPage);

        parameterMap.forEach((key, value) -> failNode.getFailureData().put(key, String.valueOf(value)));
        return failNode;
    }

    // =============================================================================================
    // Graph infusion helper
    // =============================================================================================

    private boolean canInfuse(AuthGraphNode executingNode) {

        return executingNode instanceof DynamicDecisionNode && dynamicallyBuiltBaseNode.get() != null;
    }

    // =============================================================================================
    // Script execution monitoring
    // =============================================================================================

    private JSExecutionSupervisor getJSExecutionSupervisor() {

        return FrameworkServiceDataHolder.getInstance().getJsExecutionSupervisor();
    }

    private void storeAuthScriptExecutionMonitorData(AuthenticationContext context,
            JSExecutionMonitorData jsExecutionMonitorData) {

        context.setProperty(PROP_EXECUTION_SUPERVISOR_RESULT, jsExecutionMonitorData);
    }

    private JSExecutionMonitorData retrieveAuthScriptExecutionMonitorData(AuthenticationContext context) {

        JSExecutionMonitorData jsExecutionMonitorData;
        Object storedResult = context.getProperty(PROP_EXECUTION_SUPERVISOR_RESULT);
        if (storedResult != null) {
            jsExecutionMonitorData = (JSExecutionMonitorData) storedResult;
        } else {
            jsExecutionMonitorData = new JSExecutionMonitorData(0L, 0L);
        }
        return jsExecutionMonitorData;
    }

    private void startScriptExecutionMonitor(String identifier, AuthenticationContext context,
                                             JSExecutionMonitorData previousExecutionResult) {

        JSExecutionSupervisor jsExecutionSupervisor = getJSExecutionSupervisor();
        if (jsExecutionSupervisor == null) {
            return;
        }
        getJSExecutionSupervisor().monitor(identifier, context.getServiceProviderName(), context.getTenantDomain(),
                previousExecutionResult.getElapsedTime(), previousExecutionResult.getConsumedMemory());
    }

    private void startScriptExecutionMonitor(String identifier, AuthenticationContext context) {

        startScriptExecutionMonitor(identifier, context, new JSExecutionMonitorData(0L, 0L));
    }

    private JSExecutionMonitorData endScriptExecutionMonitor(String identifier) {

        JSExecutionSupervisor executionSupervisor = getJSExecutionSupervisor();
        if (executionSupervisor == null) {
            return null;
        }
        return getJSExecutionSupervisor().completed(identifier);
    }

    // =============================================================================================
    // Inner classes — host function adaptors
    // =============================================================================================

    /**
     * Executes the given script.
     */
    public class JsGraalStepExecuter implements StepExecutor {

        @HostAccess.Export
        public void executeStep(Integer stepId, Object... parameterMap) {

            RemoteJsGraalGraphBuilder.this.executeStep(stepId, parameterMap);
        }
    }

    /**
     * Executes the given script in an async event.
     */
    public class JsGraalStepExecuterInAsyncEvent implements StepExecutor {

        @HostAccess.Export
        public void executeStep(Integer stepId, Object... parameterMap) {

            RemoteJsGraalGraphBuilder.this.executeStepInAsyncEvent(stepId, parameterMap);
        }
    }

    /**
     * Implementation of the SendErrorFunction interface as an adaptor for sendError function.
     */
    public class SendErrorFunctionImpl implements SendErrorFunction {

        @HostAccess.Export
        public void sendError(String url, Map<String, Object> parameterMap) {
            RemoteJsGraalGraphBuilder.this.sendError(url, parameterMap);
        }
    }

    /**
     * Implementation of the SendErrorFunction interface as an adaptor for sendErrorAsync function.
     */
    public static class SendErrorAsyncFunctionImpl implements SendErrorFunction {

        @HostAccess.Export
        public void sendError(String url, Map<String, Object> parameterMap) {
            sendErrorAsync(url, parameterMap);
        }
    }

    /**
     * Fail function implementation for GraalJS.
     */
    public static class FailAuthenticationFunctionImpl implements FailAuthenticationFunction {

        @HostAccess.Export
        public void fail(Object... parameters) {

            failAsync(parameters);
        }
    }

    /**
     * GraalJS specific prompt implementation
     */
    public class JsGraalPromptExecutorImpl implements PromptExecutor {

        @HostAccess.Export
        public void prompt(String templateId, Object... parameters) {

            RemoteJsGraalGraphBuilder.this.addShowPrompt(templateId, parameters);
        }
    }

    // =============================================================================================
    // Remote JavaScript Decision Evaluator (callback execution)
    // =============================================================================================

    /**
     * Remote JavaScript Decision Evaluator implementation.
     * This handles callback execution (e.g., onSuccess/onFail after a step completes)
     * by sending the serialized function to the external sidecar for evaluation.
     * The graph is re-organized based on the execution result, exactly as the local evaluator does.
     */
    public class RemoteJsBasedEvaluator implements AuthenticationDecisionEvaluator {

        private static final long serialVersionUID = 6853505881096840345L;
        private final GraalSerializableJsFunction jsFunction;

        public RemoteJsBasedEvaluator(GraalSerializableJsFunction jsFunction) {

            this.jsFunction = jsFunction;
        }

        @Override
        @HostAccess.Export
        @SuppressWarnings("unchecked")
        public Object evaluate(AuthenticationContext authenticationContext, Object... params) {

            RemoteJsGraalGraphBuilder graphBuilder = RemoteJsGraalGraphBuilder.this;
            Object result = null;
            if (jsFunction == null) {
                return null;
            }
            if (!jsFunction.isFunction()) {
                return jsFunction.getSource();
            }

            JsEngine jsEngine = new RemoteJsEngine(
                    GrpcTransportProvider.getTransport(JsGraalGraphEngineModeRouter.getGrpcTarget()),
                    authenticationContext);
            try {
                currentBuilder.set(graphBuilder);
                contextForJs.set(authenticationContext);

                // Log context info for debugging.
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[evaluateRemote] Starting for SP: " + authenticationContext.getServiceProviderName() +
                            ", contextId: " + authenticationContext.getContextIdentifier() +
                            ", step: " + authenticationContext.getCurrentStep() +
                            ", authContext hashCode: " + System.identityHashCode(authenticationContext));
                }

                // Get persisted bindings from authentication context (variables like
                // rolesToStepUp).
                Map<String, Object> persistedBindings = (Map<String, Object>) authenticationContext
                        .getProperty("JS_BINDING_CURRENT_CONTEXT");
                if (persistedBindings != null) {
                    if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                        log.debug("[evaluateRemote] Found " + persistedBindings.size() +
                                " persisted bindings: " + persistedBindings.keySet());
                    }
                } else {
                    if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                        log.debug("[evaluateRemote] No persisted bindings found in authContext. " +
                                "Property keys: " + authenticationContext.getProperties().keySet());
                    }
                    persistedBindings = new HashMap<>();
                }

                // Register host functions that the External can call back.
                Map<String, Object> hostFunctions = new HashMap<>();
                hostFunctions.put(JS_FUNC_EXECUTE_STEP, new JsGraalStepExecuterInAsyncEvent());
                hostFunctions.put(JS_FUNC_SEND_ERROR, new SendErrorAsyncFunctionImpl());
                hostFunctions.put(JS_AUTH_FAILURE, new FailAuthenticationFunctionImpl());
                hostFunctions.put(JS_FUNC_SHOW_PROMPT, new JsGraalPromptExecutorImpl());
                hostFunctions.put(JS_FUNC_LOAD_FUNC_LIB, new JsGraalLoadExecutorImpl());
                hostFunctions.put(JS_FUNC_GET_SECRET_BY_NAME, new JsGraalGetSecretImpl());
                // Mirror the local-execution factory bindings — see the matching
                // block in createWithRemote for the rationale.
                hostFunctions.put(JS_LOG, new JsLogger());
                hostFunctions.put(JS_FUNC_SELECT_ACR_FROM, new GraalSelectAcrFromFunction());

                JsFunctionRegistry jsFunctionRegistrar = FrameworkServiceDataHolder.getInstance()
                        .getJsFunctionRegistry();
                if (jsFunctionRegistrar != null) {
                    Map<String, Object> functionMap = jsFunctionRegistrar
                            .getSubsystemFunctionsMap(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER);
                    hostFunctions.putAll(functionMap);
                }
                jsEngine.registerHostFunctions(hostFunctions);

                String identifier = UUID.randomUUID().toString();
                Optional<JSExecutionMonitorData> optionalScriptExecutionData = Optional
                        .ofNullable(retrieveAuthScriptExecutionMonitorData(authenticationContext));
                try {
                    startScriptExecutionMonitor(identifier, authenticationContext,
                            optionalScriptExecutionData.orElse(null));

                    dynamicallyBuiltBaseNode.remove();

                    // Execute the callback function in the External with persisted bindings
                    EvaluationResult evalResult = jsEngine.executeCallback(
                            jsFunction.getSource(),
                            params,
                            persistedBindings,
                            authenticationContext);

                    if (evalResult.isSuccess()) {
                        result = evalResult.getResult();

                        // Re-persist updated bindings so next callback sees changes
                        Map<String, Object> updatedBindings = jsEngine.getBindings();
                        authenticationContext.setProperty("JS_BINDING_CURRENT_CONTEXT", updatedBindings);
                        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                            log.debug("[evaluateRemote] Re-persisted " + updatedBindings.size() +
                                    " bindings after callback");
                        }

                        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                            log.debug("Remote JS execution succeeded for SP: " +
                                    authenticationContext.getServiceProviderName() +
                                    ", elapsed: " + evalResult.getElapsedMs() + "ms");
                        }
                    } else {
                        log.error("Remote JS execution failed for SP: " +
                                authenticationContext.getServiceProviderName() +
                                ", error: " + evalResult.getErrorMessage());
                        AuthGraphNode executingNode = (AuthGraphNode) authenticationContext
                                .getProperty(PROP_CURRENT_NODE);
                        FailNode failNode = new FailNode();
                        failNode.setShowErrorPage(true);
                        failNode.getFailureData().put("errorCode", "18013");
                        failNode.getFailureData().put("errorMessage",
                                "Script execution failed: " + evalResult.getErrorMessage());
                        failNode.getFailureData().put("errorType",
                                evalResult.getErrorType() != null ? evalResult.getErrorType() : "ScriptError");
                        attachToLeaf(executingNode, failNode);
                    }
                } finally {
                    optionalScriptExecutionData = Optional.ofNullable(endScriptExecutionMonitor(identifier));
                }
                optionalScriptExecutionData.ifPresent(
                        scriptExecutionData ->
                                storeAuthScriptExecutionMonitorData(authenticationContext,
                                scriptExecutionData));

                // dynamicallyBuiltBaseNode is already on Thread -- callbacks ran inline
                // via the message loop, so no cross-thread propagation is needed.
                // canInfuse/infuse read the ThreadLocal directly.
                AuthGraphNode executingNode = (AuthGraphNode) authenticationContext.getProperty(PROP_CURRENT_NODE);
                if (canInfuse(executingNode)) {
                    infuse(executingNode, dynamicallyBuiltBaseNode.get());
                }

            } catch (Throwable e) {
                log.error("Error in remote JavaScript execution for service provider : " +
                        authenticationContext.getServiceProviderName() + ", Javascript Fragment : \n" +
                        jsFunction.getSource(), e);
                AuthGraphNode executingNode = (AuthGraphNode) authenticationContext.getProperty(PROP_CURRENT_NODE);
                FailNode failNode = new FailNode();
                failNode.setShowErrorPage(true);
                failNode.getFailureData().put("errorCode", "18013");
                String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
                failNode.getFailureData().put("errorMessage", "Script execution error: " + errorMessage);
                failNode.getFailureData().put("errorType", e.getClass().getSimpleName());
                attachToLeaf(executingNode, failNode);
            } finally {
                contextForJs.remove();
                dynamicallyBuiltBaseNode.remove();
                clearCurrentBuilder();
            }
            return result;
        }
    }
}
