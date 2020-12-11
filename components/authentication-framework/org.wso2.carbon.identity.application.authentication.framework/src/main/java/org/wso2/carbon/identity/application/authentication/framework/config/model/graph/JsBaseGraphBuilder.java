/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.AsyncProcess;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceComponent;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.functions.library.mgt.FunctionLibraryManagementService;
import org.wso2.carbon.identity.functions.library.mgt.exception.FunctionLibraryManagementException;
import org.wso2.carbon.identity.functions.library.mgt.model.FunctionLibrary;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.script.ScriptEngine;

/**
 * Common methods for Authentication graph (sequence) builder with different script languages.
 *
 */
public abstract class JsBaseGraphBuilder implements JsGraphBuilder {

    private static final Log log = LogFactory.getLog(JsBaseGraphBuilder.class);

    protected Map<Integer, StepConfig> stepNamedMap;
    protected AuthenticationGraph result = new AuthenticationGraph();
    protected AuthGraphNode currentNode = null;
    protected AuthenticationContext authenticationContext;
    protected ScriptEngine engine;
    protected static ThreadLocal<AuthenticationContext> contextForJs = new ThreadLocal<>();
    protected static ThreadLocal<AuthGraphNode> dynamicallyBuiltBaseNode = new ThreadLocal<>();
    protected static ThreadLocal<JsGraphBuilder> currentBuilder = new ThreadLocal<>();

    /**
     * Returns the built graph.
     *
     * @return AuthenticationGraph built from JsNashornGraphBuilder
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
                addEventListeners(newNode, (Map<String, Object>) parameters[parameters.length - 1],
                     effectiveFunctionSerializer() );
            } else {
                log.error("Invalid argument and hence ignored. Last argument should be a Map of event listeners.");
            }

        }
    }

    /**
     * Adds a function to show a prompt in Javascript code.
     *
     * @param parameterMap parameterMap
     */
    public static void addLongWaitProcess(AsyncProcess asyncProcess,
                                          Map<String, Object> parameterMap) {

        JsBaseGraphBuilder builder = getCurrentBuilder();
        builder.addLongWaitProcessInternal(asyncProcess, parameterMap);
    }

    public void exitFunction(Object... arg) {

        log.error("Exit function is restricted.");
    }

    public void quitFunction(Object... arg) {

        log.error("Quit function is restricted.");
    }

    /**
     * Adding the long wait process.
     *
     * @param asyncProcess
     * @param parameterMap
     */
    private void addLongWaitProcessInternal(AsyncProcess asyncProcess,
                                           Map<String, Object> parameterMap) {

        LongWaitNode newNode = new LongWaitNode(asyncProcess);

        if (parameterMap != null) {
            addEventListeners(newNode, parameterMap, effectiveFunctionSerializer());
        }
        if (this.currentNode == null) {
            this.result.setStartNode(newNode);
        } else {
            attachToLeaf(this.currentNode, newNode);
        }

        this.currentNode = newNode;
    }

    protected abstract Function<Object, SerializableJsFunction> effectiveFunctionSerializer();

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
        Map<String, AuthenticatorConfig> idpsToRemove = new HashMap<>();
        stepConfig.getAuthenticatorList().forEach(authenticatorConfig -> authenticatorConfig.getIdps()
                .forEach((idpName, idp) -> {
                    Set<String> authenticators = filteredOptions.get(idpName);
                    boolean removeOption = false;
                    if (authenticators == null) {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format(
                                    "Authentication options didn't include idp: %s. Hence excluding from " +
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
                                                    "authenticator: %s. Hence removing the option",
                                            authenticatorConfig.getName()));
                                } else {
                                    log.debug(String.format("Authenticator options contained a match for local " +
                                                    "authenticator: %s. Hence keeping the option",
                                            authenticatorConfig.getName()));
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
                                    log.debug(
                                            String.format("Authenticator options don't match any entry for idp: %s, " +
                                                            "authenticator: %s. Hence removing the option", idpName,
                                                    authenticatorConfig
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
                            log.debug(
                                    String.format("No authenticator filters for idp %s, hence keeping it as an option",
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
     * Adds all the event listeners to the decision node.
     *
     * @param eventsMap Map of events and event handler functions, which is handled by this execution.
     * @return created Dynamic Decision node.
     */
    protected static void addEventListeners(DynamicDecisionNode decisionNode,
                                            Map<String, Object> eventsMap, Function<Object,
            SerializableJsFunction> serializerFunction) {

        if (eventsMap == null) {
            return;
        }
        eventsMap.forEach((key, value) -> {
            System.out.println("Fn " + key + " " + value);
            SerializableJsFunction jsFunction = serializerFunction.apply(value);
            jsFunction.setName(key);
            if (jsFunction != null) {
                decisionNode.addFunction(key, jsFunction);
            } else {
                log.error("Event handler : " + key + " is not a function : " + value);
            }
        });
    }

    private static void addHandlers(ShowPromptNode showPromptNode, Map<String, Object> handlersMap,
                                    Function<Object, SerializableJsFunction> serializerFunction) {

        if (handlersMap == null) {
            return;
        }
        handlersMap.forEach((key, value) -> {
            if (value instanceof ScriptObjectMirror) {
                SerializableJsFunction jsFunction = serializerFunction.apply(value);
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
}
