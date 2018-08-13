/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AsyncProcess;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDecisionEvaluator;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Translate the authentication graph config to runtime model.
 * This is not thread safe. Should be discarded after each build.
 */
public class JsGraphBuilder {

    private static final Log log = LogFactory.getLog(JsGraphBuilder.class);
    private Map<Integer, StepConfig> stepNamedMap;
    private AuthenticationGraph result = new AuthenticationGraph();
    private AuthGraphNode currentNode = null;
    private AuthenticationContext authenticationContext;
    private ScriptEngine engine;
    private static ThreadLocal<AuthenticationContext> contextForJs = new ThreadLocal<>();
    private static ThreadLocal<AuthGraphNode> dynamicallyBuiltBaseNode = new ThreadLocal<>();
    private static ThreadLocal<JsGraphBuilder> currentBuilder = new ThreadLocal<>();

    /**
     * Constructs the builder with the given authentication context.
     *
     * @param authenticationContext current authentication context.
     * @param stepConfigMap         The Step map from the service provider configuration.
     * @param scriptEngine          Script engine.
     */
    public JsGraphBuilder(AuthenticationContext authenticationContext, Map<Integer, StepConfig> stepConfigMap,
                          ScriptEngine scriptEngine) {

        this.engine = scriptEngine;
        this.authenticationContext = authenticationContext;
        stepNamedMap = stepConfigMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Constructs the builder with the given authentication context.
     *
     * @param authenticationContext current authentication context.
     * @param stepConfigMap         The Step map from the service provider configuration.
     * @param scriptEngine          Script engine.
     * @param currentNode           Current authentication graph node.
     */
    public JsGraphBuilder(AuthenticationContext authenticationContext, Map<Integer, StepConfig> stepConfigMap,
                          ScriptEngine scriptEngine, AuthGraphNode currentNode) {

        this.engine = scriptEngine;
        this.authenticationContext = authenticationContext;
        this.currentNode = currentNode;
        stepNamedMap = stepConfigMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Returns the built graph.
     *
     * @return AuthenticationGraph built from JsGraphBuilder
     */
    public AuthenticationGraph build() {

        if (result.isBuildSuccessful()) {
            if (currentNode != null && !(currentNode instanceof EndStep)) {
                attachToLeaf(currentNode, new EndStep());
            }
        } else {
            //no need to do anything
            if (log.isDebugEnabled()) {
                log.debug("Not building the graph as the initialization was unsuccessful.");
            }
        }
        return result;
    }

    /**
     * Creates the graph with the given Script and step map.
     *
     * @param script the Dynamic authentication script.
     */
    public JsGraphBuilder createWith(String script) {

        try {
            currentBuilder.set(this);
            Bindings globalBindings = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
            globalBindings.put(FrameworkConstants.JSAttributes.JS_FUNC_EXECUTE_STEP, (StepExecutor) this::executeStep);
            globalBindings.put(FrameworkConstants.JSAttributes.JS_FUNC_SEND_ERROR, (BiConsumer<String, Map>)
                this::sendError);
            globalBindings.put(FrameworkConstants.JSAttributes.JS_FUNC_SHOW_PROMPT,
                    (PromptExecutor) this::addShowPrompt);
            JsFunctionRegistry jsFunctionRegistrar = FrameworkServiceDataHolder.getInstance().getJsFunctionRegistry();
            if (jsFunctionRegistrar != null) {
                Map<String, Object> functionMap = jsFunctionRegistrar
                        .getSubsystemFunctionsMap(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER);
                functionMap.forEach(globalBindings::put);
            }
            Invocable invocable = (Invocable) engine;
            engine.eval(script);
            invocable.invokeFunction(FrameworkConstants.JSAttributes.JS_FUNC_ON_LOGIN_REQUEST,
                    new JsAuthenticationContext(authenticationContext));
            JsGraphBuilderFactory.persistCurrentContext(authenticationContext, engine);
        } catch (ScriptException e) {
            result.setBuildSuccessful(false);
            result.setErrorReason("Error in executing the Javascript. Nested exception is: " + e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Error in executing the Javascript.", e);
            }
        } catch (NoSuchMethodException e) {
            result.setBuildSuccessful(false);
            result.setErrorReason("Error in executing the Javascript. " + FrameworkConstants.JSAttributes
                    .JS_FUNC_ON_LOGIN_REQUEST + " function is not defined.");
            if (log.isDebugEnabled()) {
                log.debug("Error in executing the Javascript.", e);
            }
        } finally {
            clearCurrentBuilder();
        }
        return this;
    }

    public static void clearCurrentBuilder() {

        currentBuilder.remove();
    }

    public static JsGraphBuilder getCurrentBuilder() {

        return currentBuilder.get();
    }

    /**
     * Add authentication fail node to the authentication graph during subsequent requests.
     *
     * @param parameterMap Parameters needed to send the error.
     */
    public static void sendErrorAsync(String url, Map<String, Object> parameterMap) {

        FailNode newNode = createFailNode(url, parameterMap);

        AuthGraphNode currentNode = dynamicallyBuiltBaseNode.get();
        if (currentNode == null) {
            dynamicallyBuiltBaseNode.set(newNode);
        } else {
            attachToLeaf(currentNode, newNode);
        }
    }

    private static FailNode createFailNode(String url, Map<String, Object> parameterMap) {

        FailNode failNode = new FailNode();
        failNode.setErrorPageUri(url);

        parameterMap.forEach((key, value) -> failNode.getFailureData().put(key, String.valueOf(value)));
        return failNode;
    }

    /**
     * Add authentication fail node to the authentication graph in the initial request.
     *
     * @param parameterMap Parameters needed to send the error.
     */
    public void sendError(String url, Map<String, Object> parameterMap) {

        FailNode newNode = createFailNode(url, parameterMap);
        if (currentNode == null) {
            result.setStartNode(newNode);
        } else {
            attachToLeaf(currentNode, newNode);
        }
    }

    /**
     * Adds the step given by step ID tp the authentication graph.
     *
     * @param stepId Step Id
     * @param params params
     */
    @SuppressWarnings("unchecked")
    public final void executeStep(int stepId, Object... params) {

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

    @SuppressWarnings("unchecked")
    protected void handleOptions(Map<String, Object> options, StepConfig stepConfig) {

        Object authenticationOptionsObj = options.get(FrameworkConstants.JSAttributes.AUTHENTICATION_OPTIONS);
        if (authenticationOptionsObj instanceof Map) {
            filterOptions((Map<String, Map<String, String>>) authenticationOptionsObj, stepConfig);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Authenticator options not provided or invalid, hence proceeding without filtering");
            }
        }

        Object authenticatorParams = options.get(FrameworkConstants.JSAttributes.AUTHENTICATOR_PARAMS);
        if (authenticatorParams instanceof Map) {
            authenticatorParamsOptions((Map<String, Object>) authenticatorParams, stepConfig);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Authenticator params not provided or invalid, hence proceeding without setting params");
            }
        }
    }

    /**
     * Filter out options in the step config to retain only the options provided in authentication options
     * @param authenticationOptions Authentication options to keep
     * @param stepConfig The step config to be modified
     */
    protected void filterOptions(Map<String, Map<String, String>> authenticationOptions, StepConfig stepConfig) {

        Map<String, Set<String>> filteredOptions = new HashMap<>();
        authenticationOptions.forEach((id, option) -> {
            String idp = option.get(FrameworkConstants.JSAttributes.IDP);
            String authenticator = option.get(FrameworkConstants.JSAttributes.AUTHENTICATOR);
            if (StringUtils.isNotBlank(authenticator) && StringUtils.isBlank(idp)) {
                // If Idp is not set, but authenticator is set, idp is assumed as local
                idp = FrameworkConstants.LOCAL_IDP_NAME;
            }
            if (StringUtils.isNotBlank(idp)) {
                filteredOptions.putIfAbsent(idp, new HashSet<>());
                if (StringUtils.isNotBlank(authenticator)) {
                    filteredOptions.get(idp).add(authenticator);
                }
            }
        });
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Set<String>> entry : filteredOptions.entrySet()) {
                sb.append('\n').append(entry.getKey()).append(" : ");
                sb.append(StringUtils.join(entry.getValue(), ","));
            }
            log.debug("Authenticator options: " + sb.toString());
        }
        Set<AuthenticatorConfig> authenticatorsToRemove = new HashSet<>();
        stepConfig.getAuthenticatorList().forEach(authenticatorConfig -> authenticatorConfig.getIdps()
            .forEach((idpName, idp) -> {
                Set<String> authenticators = filteredOptions.get(idpName);
                boolean removeOption = false;
                if (authenticators == null) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Authentication options didn't include idp: %s. Hence excluding from " +
                            "options list", idpName));
                    }
                    removeOption = true;
                } else if (!authenticators.isEmpty()) {
                    // Both idp and authenticator present, but authenticator is given by display name due to the fact
                    // that it is the one available at UI. Should translate the display name to actual name, and
                    // keep/remove option
                    removeOption = true;

                    if (FrameworkConstants.LOCAL_IDP_NAME.equals(idpName)) {
                        List<LocalAuthenticatorConfig> localAuthenticators = ApplicationAuthenticatorService
                            .getInstance().getLocalAuthenticators();
                        for (LocalAuthenticatorConfig localAuthenticatorConfig : localAuthenticators) {
                            if (authenticatorConfig.getName().equals(localAuthenticatorConfig.getName()) &&
                                authenticators.contains(localAuthenticatorConfig.getDisplayName())) {
                                removeOption = false;
                                break;
                            }
                        }
                        if (log.isDebugEnabled()) {
                            if (removeOption) {
                                log.debug(String.format("Authenticator options don't match any entry for local" +
                                    "authenticator: %s. Hence removing the option", authenticatorConfig.getName()));
                            } else {
                                log.debug(String.format("Authenticator options contained a match for local " +
                                    "authenticator: %s. Hence keeping the option", authenticatorConfig.getName()));
                            }
                        }
                    } else {
                        for (FederatedAuthenticatorConfig federatedAuthConfig : idp.getFederatedAuthenticatorConfigs()) {
                            if (authenticatorConfig.getName().equals(federatedAuthConfig.getName()) &&
                                authenticators.contains(federatedAuthConfig.getDisplayName())) {
                                removeOption = false;
                                break;
                            }
                        }
                        if (log.isDebugEnabled()) {
                            if (removeOption) {
                                log.debug(String.format("Authenticator options don't match any entry for idp: %s, " +
                                    "authenticator: %s. Hence removing the option", idpName, authenticatorConfig
                                    .getName()));
                            } else {
                                log.debug(String.format("Authenticator options contained a match for idp: %s, " +
                                    "authenticator: %s. Hence keeping the option", idpName, authenticatorConfig
                                    .getName()));
                            }
                        }
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("No authenticator filters for idp %s, hence keeping it as an option",
                            idpName));
                    }
                }
                if (removeOption) {
                    authenticatorsToRemove.add(authenticatorConfig);
                }
            }));
        if (stepConfig.getAuthenticatorList().size() > authenticatorsToRemove.size()) {
            stepConfig.getAuthenticatorList().removeAll(authenticatorsToRemove);
            if (log.isDebugEnabled()) {
                log.debug("Removed " + authenticatorsToRemove.size() + " options which doesn't match the " +
                    "provided authenticator options");
            }
        } else {
            log.warn("The filtered authenticator list is empty, hence proceeding without filtering");
        }
    }

    /**
     * Add authenticator params in the message context.
     *
     * @param options Authentication options
     */
    protected void authenticatorParamsOptions(Map<String, Object> options, StepConfig stepConfig) {

        Map<String, Map<String, String>> authenticatorParams = new HashMap<>();

        Object localOptions = options.get(FrameworkConstants.JSAttributes.JS_LOCAL_IDP);
        if (localOptions instanceof Map) {
            ((Map<String, Object>) localOptions).forEach((authenticatorName, params) -> {
                if (params instanceof Map) {
                    authenticatorParams.put(authenticatorName, new HashMap<>((Map<String, String>) params));
                }
            });
        }

        Object federatedOptionsObj = options.get(FrameworkConstants.JSAttributes.JS_FEDERATED_IDP);
        if (federatedOptionsObj instanceof Map) {
            Map<String, Map<String, String>> federatedOptions = (Map<String, Map<String, String>>) federatedOptionsObj;
            stepConfig.getAuthenticatorList().forEach(authenticatorConfig -> authenticatorConfig.getIdps()
                    .forEach((idpName, idp) -> {
                        if (!FrameworkConstants.LOCAL_IDP_NAME.equals(idpName)
                                && federatedOptions.containsKey(idpName)) {
                            for (FederatedAuthenticatorConfig federatedAuthConfig
                                    : idp.getFederatedAuthenticatorConfigs()) {
                                String authenticatorName = authenticatorConfig.getApplicationAuthenticator().getName();
                                if (authenticatorConfig.getName().equals(federatedAuthConfig.getName())) {
                                    authenticatorParams.put(authenticatorName,
                                            new HashMap<>(federatedOptions.get(idpName)));
                                }
                            }
                        }
                    }));
        }

        Object commonOptions = options.get(FrameworkConstants.JSAttributes.JS_COMMON_OPTIONS);
        if (commonOptions instanceof Map) {
            authenticatorParams.put(FrameworkConstants.JSAttributes.JS_COMMON_OPTIONS,
                    new HashMap<>((Map<String, String>) commonOptions));

        }

        if (!authenticatorParams.isEmpty()) {
            authenticationContext.addAuthenticatorParams(authenticatorParams);
        }
    }

    /**
     * Adds the step given by step ID tp the authentication graph.
     *
     * @param params params
     */
    @SuppressWarnings("unchecked")
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
        if (log.isDebugEnabled()) {
            log.debug("Found step for the Step ID : " + stepId + ", Step Config " + stepConfig);
        }
        StepConfigGraphNode newNode = wrap(stepConfig);
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
                handleOptions(options, stepConfig);
            }
        }
    }

    /**
     * Adds a function to show a prompt in Javascript code.
     *
     * @param templateId Identifier of the template
     * @param parameters parameters
     */
    @SuppressWarnings("unchecked")
    public void addShowPrompt(String templateId, Object... parameters) {

        ShowPromptNode newNode = new ShowPromptNode();
        newNode.setTemplateId(templateId);

        if (parameters.length == 2) {
            newNode.setData((Map<String, Serializable>) FrameworkUtils.toJsSerializable(parameters[0]));
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
     *
     * @param templateId Identifier of the template.
     * @param parameters Parameters.
     * @param handlers Handlers to run before and after the prompt.
     * @param callbacks Callbacks to run after the prompt.
     */
    @SuppressWarnings("unchecked")
    public static void addPrompt(String templateId, Map<String, Object> parameters, Map<String, Object> handlers,
                                 Map<String, Object> callbacks) {

        ShowPromptNode newNode = new ShowPromptNode();
        newNode.setTemplateId(templateId);
        newNode.setParameters(parameters);

        JsGraphBuilder currentBuilder = getCurrentBuilder();
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
     * @param parameterMap parameterMap
     */
    public static void addLongWaitProcess(AsyncProcess asyncProcess,
                                          Map<String, Object> parameterMap) {

        addLongWaitProcess(getCurrentBuilder(), asyncProcess, parameterMap);
    }

    private static void addLongWaitProcess(JsGraphBuilder jsGraphBuilder, AsyncProcess asyncProcess,
                                           Map<String, Object> parameterMap) {

        LongWaitNode newNode = new LongWaitNode(asyncProcess);

        if (parameterMap != null) {
            addEventListeners(newNode, parameterMap);
        }
        if (jsGraphBuilder.currentNode == null) {
            jsGraphBuilder.result.setStartNode(newNode);
        } else {
            attachToLeaf(jsGraphBuilder.currentNode, newNode);
        }

        jsGraphBuilder.currentNode = newNode;
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
    private static void addEventListeners(DynamicDecisionNode decisionNode,
                                          Map<String, Object> eventsMap) {

        if (eventsMap == null) {
            return;
        }
        eventsMap.forEach((key, value) -> {
            if (value instanceof ScriptObjectMirror) {
                SerializableJsFunction jsFunction = SerializableJsFunction
                        .toSerializableForm((ScriptObjectMirror) value);
                if (jsFunction != null) {
                    decisionNode.addFunction(key, jsFunction);
                } else {
                    log.error("Event handler : " + key + " is not a function : " + value);
                }
            } else if (value instanceof SerializableJsFunction) {
                decisionNode.addFunction(key, (SerializableJsFunction) value);
            }
        });
    }

    private static void addHandlers(ShowPromptNode showPromptNode, Map<String, Object> handlersMap) {

        if (handlersMap == null) {
            return;
        }
        handlersMap.forEach((key, value) -> {
            if (value instanceof ScriptObjectMirror) {
                SerializableJsFunction jsFunction = SerializableJsFunction
                        .toSerializableForm((ScriptObjectMirror) value);
                if (jsFunction != null) {
                    showPromptNode.addHandler(key, jsFunction);
                } else {
                    log.error("Event handler : " + key + " is not a function : " + value);
                }
            } else if (value instanceof SerializableJsFunction) {
                showPromptNode.addHandler(key, (SerializableJsFunction) value);
            }
        });
    }

    /**
     * Attach the new node to the destination node.
     * Any immediate branches available in the destination will be re-attached to the new node.
     * New node may be cloned if needed to attach on multiple branches.
     *
     * @param destination Current node.
     * @param newNode New node to attach.
     */
    private static void infuse(AuthGraphNode destination, AuthGraphNode newNode) {

        if (destination instanceof StepConfigGraphNode) {
            StepConfigGraphNode stepConfigGraphNode = ((StepConfigGraphNode) destination);
            attachToLeaf(newNode, stepConfigGraphNode.getNext());
            stepConfigGraphNode.setNext(newNode);
        } else if (destination instanceof DynamicDecisionNode) {
            DynamicDecisionNode dynamicDecisionNode = (DynamicDecisionNode) destination;
            attachToLeaf(newNode, dynamicDecisionNode.getDefaultEdge());
            dynamicDecisionNode.setDefaultEdge(newNode);
        } else {
            log.error("Can not infuse nodes in node type : " + destination);
        }

    }

    /**
     * Attach the new node to end of the base node.
     * The new node is added to each leaf node of the Tree structure given in the destination node.
     * Effectively this will join all the leaf nodes to new node, converting the tree into a graph.
     *
     * @param baseNode Base node.
     * @param nodeToAttach Node to attach.
     */
    private static void attachToLeaf(AuthGraphNode baseNode, AuthGraphNode nodeToAttach) {

        if (baseNode instanceof StepConfigGraphNode) {
            StepConfigGraphNode stepConfigGraphNode = ((StepConfigGraphNode) baseNode);
            if (stepConfigGraphNode.getNext() == null) {
                stepConfigGraphNode.setNext(nodeToAttach);
            } else {
                attachToLeaf(stepConfigGraphNode.getNext(), nodeToAttach);
            }
        } else if (baseNode instanceof LongWaitNode) {
            LongWaitNode longWaitNode = (LongWaitNode) baseNode;
            longWaitNode.setDefaultEdge(nodeToAttach);
        } else if (baseNode instanceof ShowPromptNode) {
            ShowPromptNode showPromptNode = (ShowPromptNode) baseNode;
            showPromptNode.setDefaultEdge(nodeToAttach);
        } else if (baseNode instanceof DynamicDecisionNode) {
            DynamicDecisionNode dynamicDecisionNode = (DynamicDecisionNode) baseNode;
            dynamicDecisionNode.setDefaultEdge(nodeToAttach);
        } else if (baseNode instanceof EndStep) {
            if (log.isDebugEnabled()) {
                log.debug("The destination is an End Step. Unable to attach the node : " + nodeToAttach);
            }
        } else if (baseNode instanceof FailNode) {
            if (log.isDebugEnabled()) {
                log.debug("The destination is an Fail Step. Unable to attach the node : " + nodeToAttach);
            }
        } else {
            log.error("Unknown graph node found : " + baseNode);
        }
    }

    /**
     * Creates the StepConfigGraphNode with given StepConfig.
     *
     * @param stepConfig Step Config Object.
     * @return built and wrapped new StepConfigGraphNode.
     */
    private static StepConfigGraphNode wrap(StepConfig stepConfig) {

        return new StepConfigGraphNode(stepConfig);
    }

    @FunctionalInterface
    public interface StepExecutor {

        void executeStep(Integer stepId, Object... parameterMap);
    }

    @FunctionalInterface
    public interface PromptExecutor {

        void prompt(String template, Object... parameterMap);
    }

    /**
     * Javascript based Decision Evaluator implementation.
     * This is used to create the Authentication Graph structure dynamically on the fly while the authentication flow
     * is happening.
     * The graph is re-organized based on last execution of the decision.
     */
    public class JsBasedEvaluator implements AuthenticationDecisionEvaluator {

        private static final long serialVersionUID = 6853505881096840344L;
        private SerializableJsFunction jsFunction;

        public JsBasedEvaluator(SerializableJsFunction jsFunction) {

            this.jsFunction = jsFunction;
        }

        @Override
        public Object evaluate(AuthenticationContext authenticationContext, Function<JSObject, Object> jsConsumer) {

            JsGraphBuilder graphBuilder = JsGraphBuilder.this;
            Object result = null;
            if (jsFunction == null) {
                return null;
            }
            if (jsFunction.isFunction()) {
                ScriptEngine scriptEngine = getEngine(authenticationContext);
                try {
                    currentBuilder.set(graphBuilder);
                    JsGraphBuilderFactory.restoreCurrentContext(authenticationContext, scriptEngine);
                    Bindings globalBindings = scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE);
                    //Now re-assign the executeStep function to dynamic evaluation
                    globalBindings.put(FrameworkConstants.JSAttributes.JS_FUNC_EXECUTE_STEP,
                        (StepExecutor) graphBuilder::executeStepInAsyncEvent);
                    globalBindings.put(FrameworkConstants.JSAttributes.JS_FUNC_SEND_ERROR,
                        (BiConsumer<String, Map>) JsGraphBuilder::sendErrorAsync);
                    globalBindings.put(FrameworkConstants.JSAttributes.JS_FUNC_SHOW_PROMPT, (PromptExecutor)
                            graphBuilder::addShowPrompt);
                    JsFunctionRegistry jsFunctionRegistry = FrameworkServiceDataHolder.getInstance()
                            .getJsFunctionRegistry();
                    if (jsFunctionRegistry != null) {
                        Map<String, Object> functionMap = jsFunctionRegistry
                                .getSubsystemFunctionsMap(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER);
                        functionMap.forEach(globalBindings::put);
                    }
                    Compilable compilable = (Compilable) scriptEngine;
                    JsGraphBuilder.contextForJs.set(authenticationContext);

                    CompiledScript compiledScript = compilable.compile(jsFunction.getSource());
                    JSObject builderFunction = (JSObject) compiledScript.eval();
                    result = jsConsumer.apply(builderFunction);

                    JsGraphBuilderFactory.persistCurrentContext(authenticationContext, scriptEngine);

                    AuthGraphNode executingNode = (AuthGraphNode) authenticationContext
                            .getProperty(FrameworkConstants.JSAttributes.PROP_CURRENT_NODE);
                    if (canInfuse(executingNode)) {
                        infuse(executingNode, dynamicallyBuiltBaseNode.get());
                    }

                } catch (Throwable e) {
                    //We need to catch all the javascript errors here, then log and handle.
                    log.error("Error in executing the javascript for service provider : " + authenticationContext
                            .getServiceProviderName() + ", Javascript Fragment : \n" + jsFunction.getSource(), e);
                    AuthGraphNode executingNode = (AuthGraphNode) authenticationContext
                            .getProperty(FrameworkConstants.JSAttributes.PROP_CURRENT_NODE);
                    FailNode failNode = new FailNode();
                    attachToLeaf(executingNode, failNode);
                } finally {
                    contextForJs.remove();
                    dynamicallyBuiltBaseNode.remove();
                    clearCurrentBuilder();
                }

            } else {
                result = jsFunction.getSource();
            }
            return result;
        }

        @Deprecated
        public Object evaluate(AuthenticationContext authenticationContext) {

            return this.evaluate(authenticationContext, (fn) -> fn.call(null, new JsAuthenticationContext
                    (authenticationContext)));
        }

        private boolean canInfuse(AuthGraphNode executingNode) {

            return executingNode instanceof DynamicDecisionNode && dynamicallyBuiltBaseNode.get() != null;
        }

        private ScriptEngine getEngine(AuthenticationContext authenticationContext) {

            return FrameworkServiceDataHolder.getInstance().getJsGraphBuilderFactory()
                    .createEngine(authenticationContext);
        }
    }
}
