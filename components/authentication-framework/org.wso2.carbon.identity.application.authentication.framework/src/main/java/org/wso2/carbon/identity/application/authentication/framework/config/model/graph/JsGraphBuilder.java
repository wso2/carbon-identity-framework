/*
 * Copyright (c) 2017, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.AsyncProcess;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceComponent;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.functions.library.mgt.FunctionLibraryManagementService;
import org.wso2.carbon.identity.functions.library.mgt.exception.FunctionLibraryManagementException;
import org.wso2.carbon.identity.functions.library.mgt.model.FunctionLibrary;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Abstract class for Graph Builders which translate the authentication graph config to runtime model.
 * This is not thread safe. Should be discarded after each build.
 */
public abstract class JsGraphBuilder implements JsBaseGraphBuilder {

    private static final Log log = LogFactory.getLog(JsGraphBuilder.class);
    protected Map<Integer, StepConfig> stepNamedMap;
    protected AuthenticationGraph result = new AuthenticationGraph();
    protected AuthGraphNode currentNode = null;
    protected AuthenticationContext authenticationContext;
    private ScriptEngine engine;
    protected static ThreadLocal<AuthenticationContext> contextForJs = new ThreadLocal<>();
    protected static ThreadLocal<AuthGraphNode> dynamicallyBuiltBaseNode = new ThreadLocal<>();
    protected static ThreadLocal<JsGraphBuilder> currentBuilder = new ThreadLocal<>();
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

    public static void clearCurrentBuilder() {

        currentBuilder.remove();
    }

    public static <T extends JsGraphBuilder> T getCurrentBuilder() {

        return (T) currentBuilder.get();
    }

    /**
     * Add authentication fail node to the authentication graph during subsequent requests.
     *
     * @param parameterMap Parameters needed to send the error.
     */
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
     * Add authentication fail node to the authentication graph in the initial request.
     *
     * @param parameterMap Parameters needed to send the error.
     */
    public void sendError(String url, Map<String, Object> parameterMap) {

        FailNode newNode = createFailNode(url, parameterMap, true);
        if (currentNode == null) {
            result.setStartNode(newNode);
        } else {
            attachToLeaf(currentNode, newNode);
        }
    }

    @SuppressWarnings("unchecked")
    public void fail(Object... parameters) {

        Map<String, Object> parameterMap;

        if (parameters.length == 1) {
            parameterMap = (Map<String, Object>) parameters[0];
        } else {
            parameterMap = Collections.EMPTY_MAP;
        }

        FailNode newNode = createFailNode(StringUtils.EMPTY, parameterMap, false);

        if (currentNode == null) {
            result.setStartNode(newNode);
        } else {
            attachToLeaf(currentNode, newNode);
        }
    }

    @SuppressWarnings("unchecked")
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
     * Handle options within executeStep function. This method will update step configs through stepNamedMap.
     *
     * @param options    Map of authenticator options.
     * @param stepConfig Current stepConfig.
     */
    @SuppressWarnings("unchecked")
    protected void handleOptions(Map<String, Object> options, StepConfig stepConfig) {

        handleOptionsAsyncEvent(options, stepConfig, stepNamedMap);
    }

    /**
     * Handle options within executeStepInAsyncEvent function. This method will update step configs through context.
     *
     * @param options       Map of authenticator options.
     * @param stepConfig    Current stepConfig.
     * @param stepConfigMap Map of stepConfigs get from the context object.
     */
    @SuppressWarnings("unchecked")
    protected void handleOptionsAsyncEvent(Map<String, Object> options, StepConfig stepConfig,
                                           Map<Integer, StepConfig> stepConfigMap) {

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

        Object stepOptions = options.get(FrameworkConstants.JSAttributes.STEP_OPTIONS);
        if (stepOptions instanceof Map) {
            handleStepOptions(stepConfig, (Map<String, String>) stepOptions, stepConfigMap);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Step options not provided or invalid, hence proceeding without handling");
            }
        }
    }

    /**
     * Handle step options provided for the step from the authentication script.
     *
     * @param stepConfig    Config of the step.
     * @param stepOptions   Options provided from the script for the step.
     * @param stepConfigMap StepConfigs of each step as a map.
     */
    protected void handleStepOptions(StepConfig stepConfig, Map<String, String> stepOptions,
                                   Map<Integer, StepConfig> stepConfigMap) {

        stepConfig.setForced(Boolean.parseBoolean(stepOptions.get(FrameworkConstants.JSAttributes.FORCE_AUTH_PARAM)));
        if (Boolean.parseBoolean(stepOptions.get(FrameworkConstants.JSAttributes.SUBJECT_IDENTIFIER_PARAM))) {
            setCurrentStepAsSubjectIdentifier(stepConfig, stepConfigMap);
        }
        if (Boolean.parseBoolean(stepOptions.get(FrameworkConstants.JSAttributes.SUBJECT_ATTRIBUTE_PARAM))) {
            setCurrentStepAsSubjectAttribute(stepConfig, stepConfigMap);
        }
        stepConfig.setSkipPrompt(Boolean.parseBoolean(stepOptions.get(
                FrameworkConstants.JSAttributes.SKIP_PROMPT)));
    }

    /**
     * Filter out options in the step config to retain only the options provided in authentication options
     *
     * @param authenticationOptions Authentication options to keep
     * @param stepConfig            The step config to be modified
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
                    if (FrameworkUtils.isAuthenticatorNameInAuthConfigEnabled()) {
                        filteredOptions.get(idp).add(authenticator);
                    } else {
                        filteredOptions.get(idp).add(authenticator.toLowerCase());
                    }
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
        Map<String, AuthenticatorConfig> idpsToRemove = new HashMap<>();
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
                            if (FrameworkUtils.isAuthenticatorNameInAuthConfigEnabled()) {
                                if (authenticatorConfig.getName().equals(localAuthenticatorConfig.getName()) &&
                                        authenticators.contains(localAuthenticatorConfig.getName())) {
                                    removeOption = false;
                                    break;
                                }
                            } else {
                                if (authenticatorConfig.getName().equals(localAuthenticatorConfig.getName()) &&
                                        authenticators.contains(localAuthenticatorConfig.getDisplayName()
                                                .toLowerCase())) {
                                    removeOption = false;
                                    break;
                                }
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
                        for (FederatedAuthenticatorConfig federatedAuthConfig
                                : idp.getFederatedAuthenticatorConfigs()) {
                            if (FrameworkUtils.isAuthenticatorNameInAuthConfigEnabled()) {
                                if (authenticatorConfig.getName().equals(federatedAuthConfig.getName()) &&
                                        authenticators.contains(federatedAuthConfig.getName())) {
                                    removeOption = false;
                                    break;
                                }
                            } else {
                                if (authenticatorConfig.getName().equals(federatedAuthConfig.getName()) &&
                                        authenticators.contains(federatedAuthConfig.getDisplayName().toLowerCase())) {
                                    removeOption = false;
                                    break;
                                }
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
                    if (authenticatorConfig.getIdps().size() > 1) {
                        idpsToRemove.put(idpName, authenticatorConfig);
                    } else {
                        authenticatorsToRemove.add(authenticatorConfig);
                    }
                }
            }));
        if (stepConfig.getAuthenticatorList().size() > authenticatorsToRemove.size()) {
            idpsToRemove.forEach((idp, authenticatorConfig) -> {
                int index = stepConfig.getAuthenticatorList().indexOf(authenticatorConfig);
                stepConfig.getAuthenticatorList().get(index).getIdps().remove(idp);
                stepConfig.getAuthenticatorList().get(index).getIdpNames().remove(idp);
                if (log.isDebugEnabled()) {
                    log.debug("Removed " + idp + " option from " + authenticatorConfig.getName() + " as it " +
                            "doesn't match the provided authenticator options");
                }
            });
            // If all idps are removed from the authenticator the authenticator should be removed.
            stepConfig.getAuthenticatorList().forEach(authenticatorConfig -> {
                if (authenticatorConfig.getIdps().isEmpty()) {
                    authenticatorsToRemove.add(authenticatorConfig);
                }
            });
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
     * @param templateId Identifier of the template.
     * @param parameters Parameters.
     * @param handlers   Handlers to run before and after the prompt.
     * @param callbacks  Callbacks to run after the prompt.
     */
    @SuppressWarnings("unchecked")
    public static void addPrompt(String templateId, Map<String, Object> parameters, Map<String, Object> handlers,
                                 Map<String, Object> callbacks) {

        FrameworkServiceDataHolder.getInstance().getJsGraphBuilderFactory().getCurrentBuilder()
                .addPromptInternal(templateId, parameters, handlers, callbacks);
    }

    /**
     * Loads the required function library from the database.
     *
     * @param functionLibraryName functionLibraryName
     * @return functionLibraryScript
     * @throws FunctionLibraryManagementException
     */
    public String loadLocalLibrary(String functionLibraryName) throws FunctionLibraryManagementException {

        FunctionLibraryManagementService functionLibMgtService = FrameworkServiceComponent.
                getFunctionLibraryManagementService();
        FunctionLibrary functionLibrary;
        String libraryScript = null;

        functionLibrary = functionLibMgtService.getFunctionLibrary(functionLibraryName,
                CarbonContext.getThreadLocalCarbonContext().getTenantDomain());

        if (functionLibrary != null) {
            libraryScript = functionLibrary.getFunctionLibraryScript();
        } else {
            log.error("No function library available with " + functionLibraryName + "name.");
        }
        return libraryScript;
    }

    /**
     * Adds a function to show a prompt in Javascript code.
     *
     * @param parameterMap parameterMap
     */
    public static void addLongWaitProcess(AsyncProcess asyncProcess,
                                          Map<String, Object> parameterMap) {

        FrameworkServiceDataHolder.getInstance().getJsGraphBuilderFactory().getCurrentBuilder()
                .addLongWaitProcessInternal(asyncProcess, parameterMap);
    }

    /**
     * Attach the new node to the destination node.
     * Any immediate branches available in the destination will be re-attached to the new node.
     * New node may be cloned if needed to attach on multiple branches.
     *
     * @param destination Current node.
     * @param newNode     New node to attach.
     */
    protected static void infuse(AuthGraphNode destination, AuthGraphNode newNode) {

        if (destination instanceof StepConfigGraphNode) {
            StepConfigGraphNode stepConfigGraphNode = ((StepConfigGraphNode) destination);
            attachToLeaf(newNode, stepConfigGraphNode.getNext());
            newNode.setParent(destination);
            stepConfigGraphNode.setNext(newNode);
        } else if (destination instanceof DynamicDecisionNode) {
            DynamicDecisionNode dynamicDecisionNode = (DynamicDecisionNode) destination;
            newNode.setParent(destination);
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
     * @param baseNode     Base node.
     * @param nodeToAttach Node to attach.
     */
    protected static void attachToLeaf(AuthGraphNode baseNode, AuthGraphNode nodeToAttach) {

        if (baseNode instanceof StepConfigGraphNode) {
            StepConfigGraphNode stepConfigGraphNode = ((StepConfigGraphNode) baseNode);
            if (stepConfigGraphNode.getNext() == null) {
                stepConfigGraphNode.setNext(nodeToAttach);
                if (nodeToAttach != null) {
                    nodeToAttach.setParent(stepConfigGraphNode);
                }
            } else {
                attachToLeaf(stepConfigGraphNode.getNext(), nodeToAttach);
            }
        } else if (baseNode instanceof LongWaitNode) {
            LongWaitNode longWaitNode = (LongWaitNode) baseNode;
            longWaitNode.setDefaultEdge(nodeToAttach);
            if (nodeToAttach != null) {
                nodeToAttach.setParent(longWaitNode);
            }
        } else if (baseNode instanceof ShowPromptNode) {
            ShowPromptNode showPromptNode = (ShowPromptNode) baseNode;
            showPromptNode.setDefaultEdge(nodeToAttach);
            if (nodeToAttach != null) {
                nodeToAttach.setParent(showPromptNode);
            }
        } else if (baseNode instanceof DynamicDecisionNode) {
            DynamicDecisionNode dynamicDecisionNode = (DynamicDecisionNode) baseNode;
            dynamicDecisionNode.setDefaultEdge(nodeToAttach);
            if (nodeToAttach != null) {
                nodeToAttach.setParent(dynamicDecisionNode);
            }
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
    protected static StepConfigGraphNode wrap(StepConfig stepConfig) {

        return new StepConfigGraphNode(stepConfig);
    }

    /**
     * Functional interface for authentication failed callback.
     */
    @FunctionalInterface
    public interface FailAuthenticationFunction {

        void fail(Object... parameterMap);
    }

    /**
     * Functional interface for executeStep function.
     */
    @FunctionalInterface
    public interface StepExecutor {

        void executeStep(Integer stepId, Object... parameterMap);
    }

    /**
     * Functional interface for prompt in the authentication.
     */
    @FunctionalInterface
    public interface PromptExecutor {

        void prompt(String template, Object... parameterMap);
    }

    /**
     * Functional interface for restricted functions in authentication script.
     */
    @Deprecated
    @FunctionalInterface
    public interface RestrictedFunction {

        void exit(Object... arg);
    }

    /**
     * Functional interface to load authentication library.
     */
    @FunctionalInterface
    public interface LoadExecutor {

        String loadLocalLibrary(String libraryName) throws FunctionLibraryManagementException;
    }

    @Deprecated
    public void exitFunction(Object... arg) {

        log.error("Exit function is restricted.");
    }

    @Deprecated
    public void quitFunction(Object... arg) {

        log.error("Quit function is restricted.");
    }

    private void removeDefaultFunctions(ScriptEngine engine) throws ScriptException {

        engine.eval(REMOVE_FUNCTIONS);
    }

    private JSExecutionSupervisor getJSExecutionSupervisor() {

        return FrameworkServiceDataHolder.getInstance().getJsExecutionSupervisor();
    }

    private void storeAuthScriptExecutionMonitorData(AuthenticationContext context,
                                                     JSExecutionMonitorData jsExecutionMonitorData) {

        context.setProperty(FrameworkConstants.AdaptiveAuthentication.PROP_EXECUTION_SUPERVISOR_RESULT,
                jsExecutionMonitorData);
    }

    private JSExecutionMonitorData retrieveAuthScriptExecutionMonitorData(AuthenticationContext context) {

        JSExecutionMonitorData jsExecutionMonitorData;
        Object storedResult = context.getProperty(
                FrameworkConstants.AdaptiveAuthentication.PROP_EXECUTION_SUPERVISOR_RESULT);
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
        getJSExecutionSupervisor().monitor(identifier, context.getServiceProviderName()
                , context.getTenantDomain(), previousExecutionResult.getElapsedTime(),
                previousExecutionResult.getConsumedMemory());
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

    private void setCurrentStepAsSubjectIdentifier(StepConfig stepConfig, Map<Integer, StepConfig> stepConfigMap) {

        stepConfigMap.forEach((integer, config) -> { // Remove existing subject identifier step.
            if (config.isSubjectIdentifierStep()) {
                config.setSubjectIdentifierStep(false);
            }
        });
        stepConfig.setSubjectIdentifierStep(true);
    }

    private void setCurrentStepAsSubjectAttribute(StepConfig stepConfig, Map<Integer, StepConfig> stepConfigMap) {

        stepConfigMap.forEach((integer, config) -> { // Remove existing subject attribute step.
            if (config.isSubjectAttributeStep()) {
                config.setSubjectAttributeStep(false);
            }
        });
        stepConfig.setSubjectAttributeStep(true);
    }
}
