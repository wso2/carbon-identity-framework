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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.wso2.carbon.identity.application.authentication.framework.AsyncProcess;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDecisionEvaluator;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthGraphNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthenticationGraph;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.BaseSerializableJsFunction;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.DynamicDecisionNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.FailNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JSExecutionMonitorData;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JSExecutionSupervisor;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsGraphBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.LongWaitNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.ShowPromptNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.StepConfigGraphNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs.JsGraalAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.AdaptiveAuthentication.PROP_EXECUTION_SUPERVISOR_RESULT;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.AUTHENTICATION_OPTIONS;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.AUTHENTICATOR_PARAMS;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.JS_AUTH_FAILURE;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.JS_FUNC_EXECUTE_STEP;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.JS_FUNC_LOAD_FUNC_LIB;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.JS_FUNC_ON_LOGIN_REQUEST;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.JS_FUNC_SEND_ERROR;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.JS_FUNC_SHOW_PROMPT;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.POLYGLOT_LANGUAGE;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.POLYGLOT_SOURCE;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.PROP_CURRENT_NODE;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.STEP_OPTIONS;

/**
 * Translate the authentication graph config to runtime model.
 * This is not thread safe. Should be discarded after each build.
 * <p>
 * Since Nashorn is deprecated in JDK 11 and onwards. We are introducing GraalJS engine.
 */
public class JsGraalGraphBuilder extends JsGraphBuilder {

    private static final Log log = LogFactory.getLog(JsGraalGraphBuilder.class);
    protected Context context;

    private static final String REMOVE_FUNCTIONS = "var quit=function(){Log.error('quit function is restricted.')};" +
            "var exit=function(){Log.error('exit function is restricted.')};" +
            "var print=function(){Log.error('print function is restricted.')};" +
            "var echo=function(){Log.error('echo function is restricted.')};" +
            "var readFully=function(){Log.error('readFully function is restricted.')};" +
            "var readLine=function(){Log.error('readLine function is restricted.')};" +
            "var load=function(){Log.error('load function is restricted.')};" +
            "var loadWithNewGlobal=function(){Log.error('loadWithNewGlobal function is restricted.')};" +
            "var $ARG=null;var $ENV=null;var $EXEC=null;" +
            "var $OPTIONS=null;var $OUT=null;var $ERR=null;var $EXIT=null;" +
            "Object.defineProperty(this, 'engine', {});";

    /**
     * Constructs the builder with the given authentication context.
     *
     * @param authenticationContext current authentication context.
     * @param stepConfigMap         The Step map from the service provider configuration.
     * @param context               Polyglot Context.
     */
    public JsGraalGraphBuilder(AuthenticationContext authenticationContext, Map<Integer, StepConfig> stepConfigMap,
                               Context context) {

        this.authenticationContext = authenticationContext;
        this.context = context;
        stepNamedMap = stepConfigMap.entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Constructs the builder with the given authentication context.
     *
     * @param authenticationContext current authentication context.
     * @param stepConfigMap         The Step map from the service provider configuration.
     * @param context               polyglot context.
     * @param currentNode           Current authentication graph node.
     */
    public JsGraalGraphBuilder(AuthenticationContext authenticationContext, Map<Integer, StepConfig> stepConfigMap,
                               Context context, AuthGraphNode currentNode) {

        this.authenticationContext = authenticationContext;
        this.context = context;
        this.currentNode = currentNode;
        stepNamedMap = stepConfigMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Creates the graph with the given Script and step map.
     *
     * @param script the Dynamic authentication script.
     */
    @Override
    public JsGraalGraphBuilder createWith(String script) {

        try {
            currentBuilder.set(this);
            Value bindings = context.getBindings(POLYGLOT_LANGUAGE);

            bindings.putMember(JS_FUNC_EXECUTE_STEP, new JsGraalStepExecuter());
            bindings.putMember(JS_FUNC_SEND_ERROR, (BiConsumer<String, Map>) this::sendError);
            bindings.putMember(JS_FUNC_SHOW_PROMPT, new PromptExecutorImpl());
            bindings.putMember(JS_FUNC_LOAD_FUNC_LIB, new LoadExecutorImpl());
            JsFunctionRegistry jsFunctionRegistrar = FrameworkServiceDataHolder.getInstance().getJsFunctionRegistry();
            if (jsFunctionRegistrar != null) {
                Map<String, Object> functionMap =
                        jsFunctionRegistrar.getSubsystemFunctionsMap(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER);
                functionMap.forEach(bindings::putMember);
            }
            currentBuilder.set(this);
            context.eval(Source
                    .newBuilder(POLYGLOT_LANGUAGE,
                            FrameworkServiceDataHolder.getInstance().getCodeForRequireFunction(),
                            POLYGLOT_SOURCE)
                    .build());

            String identifier = UUID.randomUUID().toString();
            Optional<JSExecutionMonitorData> optionalScriptExecutionData;

            try {
                startScriptExecutionMonitor(identifier, authenticationContext);
                context.eval(Source.newBuilder(POLYGLOT_LANGUAGE, script, POLYGLOT_SOURCE).build());

                Value onLoginRequestFn = bindings.getMember(JS_FUNC_ON_LOGIN_REQUEST);
                if (onLoginRequestFn == null) {
                    log.error("Could not find the entry function " + JS_FUNC_ON_LOGIN_REQUEST + " \n" + script);
                    result.setBuildSuccessful(false);
                    result.setErrorReason("Error in executing the Javascript. " + JS_FUNC_ON_LOGIN_REQUEST +
                            " function is not defined.");
                    return this;
                }
                onLoginRequestFn.executeVoid(new JsGraalAuthenticationContext(authenticationContext));
            } finally {
                optionalScriptExecutionData = Optional.ofNullable(endScriptExecutionMonitor(identifier));
            }
            optionalScriptExecutionData.ifPresent(
                    scriptExecutionData -> storeAuthScriptExecutionMonitorData(authenticationContext,
                            scriptExecutionData));
            JsGraalGraphBuilderFactory.persistCurrentContext(authenticationContext, context);
        } catch (PolyglotException e) {
            result.setBuildSuccessful(false);
            result.setErrorReason(
                    "Error in executing the Javascript. " + JS_FUNC_ON_LOGIN_REQUEST + " reason, " + e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Error in executing the Javascript.", e);
            }
        } catch (IOException e) {
            result.setBuildSuccessful(false);
            result.setErrorReason("Error in building  the Javascript source" + e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Error in building the Javascript source", e);
            }
        } finally {
            clearCurrentBuilder(context);
        }
        return this;
    }

    private static void attachEventListeners(Map<String, Object> eventsMap, AuthGraphNode currentNode) {

        if (eventsMap == null) {
            return;
        }
        DynamicDecisionNode decisionNode = new DynamicDecisionNode();
        addEventListeners(decisionNode, eventsMap);
        if (!decisionNode.getFunctionMap().isEmpty()) {
            attachToLeaf(currentNode, decisionNode);
        }
    }

    private void attachEventListeners(Map<String, Object> eventsMap) {

        if (eventsMap == null) {
            return;
        }
        DynamicDecisionNode decisionNode = new DynamicDecisionNode();
        addEventListeners(decisionNode, eventsMap);
        if (!decisionNode.getFunctionMap().isEmpty()) {
            attachToLeaf(currentNode, decisionNode);
            currentNode = decisionNode;
        }
    }

    /**
     * Adds all the event listeners to the decision node.
     *
     * @param eventsMap Map of events and event handler functions, which is handled by this execution.
     * @return created Dynamic Decision node.
     */
    private static void addEventListeners(DynamicDecisionNode decisionNode, Map<String, Object> eventsMap) {

        if (eventsMap == null) {
            return;
        }
        eventsMap.forEach((key, value) -> {
            if ((!(value instanceof GraalSerializableJsFunction))) {
                GraalSerializableJsFunction jsFunction = GraalSerializableJsFunction.toSerializableForm(value);
                if (jsFunction != null) {
                    decisionNode.addFunction(key, jsFunction);
                } else {
                    log.error("Event handler : " + key + " is not a function : " + value);
                }
            } else {
                decisionNode.addFunction(key, (GraalSerializableJsFunction) value);
            }
        });
    }

    private static void addHandlers(ShowPromptNode showPromptNode, Map<String, Object> handlersMap) {

        if (handlersMap == null) {
            return;
        }
        handlersMap.forEach((key, value) -> {
            if (!(value instanceof GraalSerializableJsFunction)) {
                GraalSerializableJsFunction jsFunction = GraalSerializableJsFunction.toSerializableForm(value);
                if (jsFunction != null) {
                    showPromptNode.addHandler(key, jsFunction);
                } else {
                    log.error("Event handler : " + key + " is not a function : " + value);
                }
            } else {
                showPromptNode.addHandler(key, (GraalSerializableJsFunction) value);
            }
        });
    }

    /**
     * Adds the step given by step ID tp the authentication graph.
     *
     * @param params params
     */
    @SuppressWarnings("unchecked")
    @HostAccess.Export
    public void executeStepInAsyncEvent(int stepId, Object... params) {

        AuthenticationContext context = contextForJs.get();
        AuthGraphNode currentNode = dynamicallyBuiltBaseNode.get();

        if (log.isDebugEnabled()) {
            log.debug("Execute Step on async event. Step ID : " + stepId);
        }
        AuthenticationGraph graph = context.getSequenceConfig().getAuthenticationGraph();
        if (graph == null) {
            log.error("The graph happens to be null on the sequence config. Can not execute step : " + stepId);
            return;
        }

        StepConfig stepConfig = graph.getStepMap().get(stepId);
        if (stepConfig == null) {
            if (log.isDebugEnabled()) {
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
        if (log.isDebugEnabled()) {
            log.debug("Found step for the Step ID : " + stepId + ", Step Config " + clonedStepConfig);
        }
        StepConfigGraphNode newNode = wrap(clonedStepConfig);
        if (currentNode == null) {
            if (log.isDebugEnabled()) {
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


    /**
     * Executes the given script in an async event.
     */
    public class JsGraalStepExecuterInAsyncEvent implements StepExecutor {

        @HostAccess.Export
        public void executeStep(Integer stepId, Object... parameterMap) {

            JsGraalGraphBuilder.this.executeStepInAsyncEvent(stepId, parameterMap);
        }
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

        JsGraalGraphBuilder currentBuilder = getCurrentBuilder();
        if (currentBuilder.currentNode == null) {
            currentBuilder.result.setStartNode(newNode);
        } else {
            attachToLeaf(currentBuilder.currentNode, newNode);
        }

        currentBuilder.currentNode = newNode;
        addEventListeners(newNode, callbacks);
        addHandlers(newNode, handlers);
    }

    public AuthenticationDecisionEvaluator getScriptEvaluator(BaseSerializableJsFunction fn) {

        return new JsBasedEvaluator((GraalSerializableJsFunction) fn);
    }

    public static void clearCurrentBuilder(Context context) {

        context.close();
        clearCurrentBuilder();
    }

    /**
     * Adds the step given by step ID tp the authentication graph.
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
     * Executes the given script.
     */
    public class JsGraalStepExecuter implements StepExecutor {

        @HostAccess.Export
        public void executeStep(Integer stepId, Object... parameterMap) {

            JsGraalGraphBuilder.this.executeStep(stepId, parameterMap);
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
            log.debug("Authenticator options not provided or invalid, hence proceeding without filtering");
        }

        Object authenticatorParams = options.get(AUTHENTICATOR_PARAMS);
        if (authenticatorParams instanceof Map) {
            authenticatorParamsOptions((Map<String, Object>) authenticatorParams, stepConfig);
        } else {
            log.debug("Authenticator params not provided or invalid, hence proceeding without setting params");
        }

        Object stepOptions = options.get(STEP_OPTIONS);
        if (stepOptions instanceof Map) {
            handleStepOptions(stepConfig, (Map<String, String>) stepOptions, stepConfigMap);
        } else {
            log.debug("Step options not provided or invalid, hence proceeding without handling");
        }
    }

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

    private static FailNode createFailNode(String url, Map<String, Object> parameterMap, boolean isShowErrorPage) {

        FailNode failNode = new FailNode();
        if (isShowErrorPage && StringUtils.isNotBlank(url)) {
            failNode.setErrorPageUri(url);
        }
        // setShowErrorPage is set to true as sendError function redirects to a specific error page.
        failNode.setShowErrorPage(isShowErrorPage);

        parameterMap.forEach((key, value) -> failNode.getFailureData().put(key, String.valueOf(value)));
        return failNode;
    }

    /**
     * Javascript based Decision Evaluator implementation.
     * This is used to create the Authentication Graph structure dynamically on the fly while the authentication flow
     * is happening.
     * The graph is re-organized based on last execution of the decision.
     */
    public class JsBasedEvaluator implements AuthenticationDecisionEvaluator {

        private static final long serialVersionUID = 6853505881096840344L;
        private final GraalSerializableJsFunction jsFunction;

        public JsBasedEvaluator(GraalSerializableJsFunction jsFunction) {

            this.jsFunction = jsFunction;
        }

        @Override
        @HostAccess.Export
        public Object evaluate(AuthenticationContext authenticationContext, Object... params) {

            JsGraalGraphBuilder graphBuilder = JsGraalGraphBuilder.this;
            Object result = null;
            if (jsFunction == null) {
                return null;
            }
            if (!jsFunction.isFunction()) {
                return jsFunction.getSource();
            }
            try {
                currentBuilder.set(graphBuilder);
                JsGraalGraphBuilderFactory.restoreCurrentContext(authenticationContext, context);
                Context context = getContext();
                Value bindings = context.getBindings(POLYGLOT_LANGUAGE);

                bindings.putMember(JS_FUNC_EXECUTE_STEP, new JsGraalStepExecuterInAsyncEvent());
                bindings.putMember(JS_FUNC_SEND_ERROR, (BiConsumer<String, Map>) JsGraalGraphBuilder::sendErrorAsync);
                bindings.putMember(JS_AUTH_FAILURE, new FailAuthenticationFunctionImpl());
                bindings.putMember(JS_FUNC_SHOW_PROMPT, new PromptExecutorImpl());
                bindings.putMember(JS_FUNC_LOAD_FUNC_LIB, new LoadExecutorImpl());
                JsFunctionRegistry jsFunctionRegistrar =
                        FrameworkServiceDataHolder.getInstance().getJsFunctionRegistry();
                if (jsFunctionRegistrar != null) {
                    Map<String, Object> functionMap =
                            jsFunctionRegistrar.getSubsystemFunctionsMap(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER);
                    functionMap.forEach(bindings::putMember);
                }
                removeDefaultFunctions(context);
                JsGraalGraphBuilder.contextForJs.set(authenticationContext);

                String identifier = UUID.randomUUID().toString();
                Optional<JSExecutionMonitorData> optionalScriptExecutionData =
                        Optional.ofNullable(retrieveAuthScriptExecutionMonitorData(authenticationContext));
                try {
                    startScriptExecutionMonitor(identifier, authenticationContext,
                            optionalScriptExecutionData.orElse(null));
                    result = jsFunction.apply(context, params);
                } finally {
                    optionalScriptExecutionData = Optional.ofNullable(endScriptExecutionMonitor(identifier));
                }
                optionalScriptExecutionData.ifPresent(
                        scriptExecutionData -> storeAuthScriptExecutionMonitorData(authenticationContext,
                                scriptExecutionData));

                JsGraalGraphBuilderFactory.persistCurrentContext(authenticationContext, context);

                AuthGraphNode executingNode = (AuthGraphNode) authenticationContext.getProperty(PROP_CURRENT_NODE);
                if (canInfuse(executingNode)) {
                    infuse(executingNode, dynamicallyBuiltBaseNode.get());
                }

            } catch (Throwable e) {
                //We need to catch all the javascript errors here, then log and handle.
                log.error("Error in executing the javascript for service provider : " +
                        authenticationContext.getServiceProviderName() + ", Javascript Fragment : \n" +
                        jsFunction.getSource(), e);
                AuthGraphNode executingNode = (AuthGraphNode) authenticationContext.getProperty(PROP_CURRENT_NODE);
                FailNode failNode = new FailNode();
                attachToLeaf(executingNode, failNode);
            } finally {
                contextForJs.remove();
                dynamicallyBuiltBaseNode.remove();
                clearCurrentBuilder(context);
            }
            return result;
        }
    }

    private Context getContext() {

        return this.context;
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
     * GraalJS specific prompt implementation
     */
    public class PromptExecutorImpl implements PromptExecutor {

        @HostAccess.Export
        public void prompt(String templateId, Object... parameters) {

            JsGraalGraphBuilder.this.addShowPrompt(templateId, parameters);
        }
    }

    private boolean canInfuse(AuthGraphNode executingNode) {

        return executingNode instanceof DynamicDecisionNode && dynamicallyBuiltBaseNode.get() != null;
    }

    @HostAccess.Export
    public static void failAsync(Object... parameters) {

        Map<String, Object> parameterMap;

        if (parameters.length == 1) {
            parameterMap = (Map<String, Object>) parameters[0];
        } else {
            parameterMap = Collections.EMPTY_MAP;
        }

        FailNode newNode = createFailNode(StringUtils.EMPTY, parameterMap, false);

        AuthGraphNode currentNode = dynamicallyBuiltBaseNode.get();
        if (currentNode == null) {
            dynamicallyBuiltBaseNode.set(newNode);
        } else {
            attachToLeaf(currentNode, newNode);
        }
    }

    /**
     * Fail function implementation for GraalJS.
     */
    public class FailAuthenticationFunctionImpl implements FailAuthenticationFunction {

        @HostAccess.Export
        public void fail(Object... parameters) {

            failAsync(parameters);
        }
    }

    private void removeDefaultFunctions(Context context) throws IOException {

        context.eval(Source.newBuilder(POLYGLOT_LANGUAGE, REMOVE_FUNCTIONS, POLYGLOT_SOURCE).build());
    }

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

}
