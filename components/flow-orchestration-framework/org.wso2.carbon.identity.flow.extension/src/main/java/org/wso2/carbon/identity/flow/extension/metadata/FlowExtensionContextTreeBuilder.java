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

package org.wso2.carbon.identity.flow.extension.metadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ContextTree;
import org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.FlowContextPaths;
import org.wso2.carbon.identity.flow.mgt.FlowMgtService;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtFrameworkException;
import org.wso2.carbon.identity.flow.mgt.model.FlowTypeVariablesDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builds the controlled Flow Extension context tree returned by the metadata endpoint.
 * The set of exposed attributes is governed at code level by this builder so the frontend
 * receives the user/flow tree shape the Console UI expects.
 */
public class FlowExtensionContextTreeBuilder {

    private static final Log LOG = LogFactory.getLog(FlowExtensionContextTreeBuilder.class);

    /**
     * Build the metadata response for the given flow type.
     *
     * @param flowType the flow type (null → default tree).
     * @return a fully populated metadata DTO.
     */
    public FlowExtensionContextTreeMetadata build(String flowType) {

        List<FlowExtensionContextTreeNode> tree = new ArrayList<>();
        tree.add(buildUserNode());
        tree.add(buildTenantNode());
        tree.add(buildApplicationNode());
        tree.add(buildOrganizationNode());
        // Only the registration flow onboards an organization, so the tree of another flow type leaves it out.
        if (flowType == null || ContextTree.FLOW_REGISTRATION.equals(flowType)) {
            tree.add(buildSubjectOrganizationNode());
        }
        tree.add(buildFlowNode());
        addFlowVariableNodes(tree, flowType);

        return new FlowExtensionContextTreeMetadata(
                flowType,
                tree,
                true,
                allowReadOnlyClaimsModification(flowType));
    }

    /**
     * Whether the Console UI may permit MODIFY on read-only claims for this flow type.
     * Hardcoded enumerative mapping so that any future flow type defaults to false until
     * explicitly added here.
     *
     * <p>The default tree (null flowType) returns true — matches current behaviour for the
     * connection-level access-config editor which doesn't yet know which flow the action
     * will be wired into.</p>
     */
    static boolean allowReadOnlyClaimsModification(String flowType) {
        return flowType == null
                || ContextTree.FLOW_REGISTRATION.equals(flowType)
                || ContextTree.FLOW_INVITED_USER_REGISTRATION.equals(flowType);
    }

    private FlowExtensionContextTreeNode buildUserNode() {

        List<FlowExtensionContextTreeNode> children = new ArrayList<>();

        children.add(FlowExtensionContextTreeNode.builder()
                .key("id")
                .title("User ID")
                .path(FlowContextPaths.USER_ID_PATH)
                .dataType(ContextTree.DATA_TYPE_STRING)
                .nodeType(ContextTree.NODE_LEAF)
                .allowedOperations(Collections.singletonList(ContextTree.OP_EXPOSE))
                .replaceable(false)
                .build());
        children.add(FlowExtensionContextTreeNode.builder()
                .key("username")
                .title("Username")
                .path(FlowContextPaths.USER_USERNAME_PATH)
                .dataType(ContextTree.DATA_TYPE_STRING)
                .nodeType(ContextTree.NODE_LEAF)
                .allowedOperations(Collections.singletonList(ContextTree.OP_EXPOSE))
                .replaceable(false)
                .build());
        children.add(FlowExtensionContextTreeNode.builder()
                .key("userStoreDomain")
                .title("User Store Domain")
                .path(FlowContextPaths.USER_STORE_DOMAIN_PATH)
                .dataType(ContextTree.DATA_TYPE_STRING)
                .nodeType(ContextTree.NODE_LEAF)
                .allowedOperations(Collections.singletonList(ContextTree.OP_EXPOSE))
                .replaceable(false)
                .build());

        children.add(FlowExtensionContextTreeNode.builder()
                .key("claims")
                .title("Claims")
                .path("/user/claims")
                .dataType("Map<String, String>")
                .nodeType(ContextTree.NODE_MAP)
                .allowedOperations(Arrays.asList(ContextTree.OP_EXPOSE, ContextTree.OP_MODIFY))
                .dynamicEntryAllowed(true)
                .dynamicEntryType("String")
                .children(Collections.emptyList())
                .build());

        List<FlowExtensionContextTreeNode> credentialsChildren = new ArrayList<>();
        credentialsChildren.add(FlowExtensionContextTreeNode.builder()
                .key("password")
                .title("Password")
                .path(FlowContextPaths.USER_CREDENTIALS_PATH_PREFIX + "password")
                .dataType("char[]")
                .nodeType(ContextTree.NODE_LEAF)
                .allowedOperations(Arrays.asList(ContextTree.OP_EXPOSE, ContextTree.OP_MODIFY))
                .build());
        children.add(FlowExtensionContextTreeNode.builder()
                .key("credentials")
                .title("Credentials")
                .path(FlowContextPaths.USER_CREDENTIALS_PATH_PREFIX)
                .dataType("Map<String, char[]>")
                .nodeType(ContextTree.NODE_MAP)
                .allowedOperations(Arrays.asList(ContextTree.OP_EXPOSE, ContextTree.OP_MODIFY))
                .dynamicEntryAllowed(false)
                .children(credentialsChildren)
                .build());

        return FlowExtensionContextTreeNode.builder()
                .key("user")
                .title("User")
                .path(FlowContextPaths.USER_PREFIX)
                .dataType("")
                .nodeType(ContextTree.NODE_OBJECT)
                .allowedOperations(Collections.singletonList(ContextTree.OP_EXPOSE))
                .children(children)
                .build();
    }

    private FlowExtensionContextTreeNode buildFlowNode() {

        List<FlowExtensionContextTreeNode> children = new ArrayList<>();
        children.add(readOnlyLeaf("flowType", "Flow Type", FlowContextPaths.FLOW_TYPE_PATH));
        children.add(readOnlyLeaf("portalUrl", "Portal URL", FlowContextPaths.FLOW_PORTAL_URL_PATH));
        return FlowExtensionContextTreeNode.builder()
                .key("flow")
                .title("Flow")
                .path(FlowContextPaths.FLOW_PREFIX)
                .dataType("")
                .nodeType(ContextTree.NODE_OBJECT)
                .allowedOperations(Collections.singletonList(ContextTree.OP_EXPOSE))
                .readOnly(true)
                .children(children)
                .build();
    }

    private FlowExtensionContextTreeNode buildTenantNode() {

        List<FlowExtensionContextTreeNode> children = new ArrayList<>();
        children.add(readOnlyLeaf("domain", "Tenant Domain", FlowContextPaths.TENANT_DOMAIN_PATH));
        return FlowExtensionContextTreeNode.builder()
                .key("tenant")
                .title("Tenant")
                .path(FlowContextPaths.TENANT_PREFIX)
                .dataType("")
                .nodeType(ContextTree.NODE_OBJECT)
                .allowedOperations(Collections.singletonList(ContextTree.OP_EXPOSE))
                .readOnly(true)
                .children(children)
                .build();
    }

    private FlowExtensionContextTreeNode buildApplicationNode() {

        List<FlowExtensionContextTreeNode> children = new ArrayList<>();
        children.add(readOnlyLeaf("id", "Application ID", FlowContextPaths.APPLICATION_ID_PATH));
        return FlowExtensionContextTreeNode.builder()
                .key("application")
                .title("Application")
                .path(FlowContextPaths.APPLICATION_PREFIX)
                .dataType("")
                .nodeType(ContextTree.NODE_OBJECT)
                .allowedOperations(Collections.singletonList(ContextTree.OP_EXPOSE))
                .readOnly(true)
                .children(children)
                .build();
    }

    private FlowExtensionContextTreeNode buildOrganizationNode() {

        List<FlowExtensionContextTreeNode> children = new ArrayList<>();
        children.add(readOnlyLeaf("id", "Organization ID", FlowContextPaths.ORGANIZATION_ID_PATH));
        children.add(readOnlyLeaf("name", "Organization Name", FlowContextPaths.ORGANIZATION_NAME_PATH));
        children.add(readOnlyLeaf("orgHandle", "Organization Handle", FlowContextPaths.ORGANIZATION_HANDLE_PATH));
        children.add(readOnlyLeaf("depth", "Organization Depth", FlowContextPaths.ORGANIZATION_DEPTH_PATH));
        return FlowExtensionContextTreeNode.builder()
                .key("organization")
                .title("Organization")
                .path(FlowContextPaths.ORGANIZATION_PREFIX)
                .dataType("")
                .nodeType(ContextTree.NODE_OBJECT)
                .allowedOperations(Collections.singletonList(ContextTree.OP_EXPOSE))
                .readOnly(true)
                .children(children)
                .build();
    }

    private FlowExtensionContextTreeNode buildSubjectOrganizationNode() {

        List<String> registrationOnly = Collections.singletonList(ContextTree.FLOW_REGISTRATION);
        List<FlowExtensionContextTreeNode> attributeLeaves = new ArrayList<>();
        for (String name : getOrganizationAttributes()) {
            // A name with the selector's closing bracket cannot be addressed by a path.
            if (name.contains(FlowContextPaths.NAME_SELECTOR_SUFFIX)) {
                continue;
            }
            attributeLeaves.add(subjectOrganizationLeaf(name, name,
                    FlowContextPaths.SUBJECT_ORGANIZATION_ATTRIBUTES_SELECTOR_PREFIX + name
                            + FlowContextPaths.NAME_SELECTOR_SUFFIX));
        }

        List<FlowExtensionContextTreeNode> children = new ArrayList<>();
        children.add(subjectOrganizationLeaf("id", "Organization ID", FlowContextPaths.SUBJECT_ORGANIZATION_ID_PATH));
        children.add(subjectOrganizationLeaf("name", "Organization Name",
                FlowContextPaths.SUBJECT_ORGANIZATION_NAME_PATH));
        children.add(subjectOrganizationLeaf("handle", "Organization Handle",
                FlowContextPaths.SUBJECT_ORGANIZATION_HANDLE_PATH));
        children.add(subjectOrganizationLeaf("description", "Organization Description",
                FlowContextPaths.SUBJECT_ORGANIZATION_DESCRIPTION_PATH));
        children.add(FlowExtensionContextTreeNode.builder()
                .key("attributes")
                .title("Attributes")
                .path(FlowContextPaths.SUBJECT_ORGANIZATION_ATTRIBUTES_PATH)
                .dataType("Map<String, String>")
                .nodeType(ContextTree.NODE_MAP)
                .allowedOperations(Collections.singletonList(ContextTree.OP_EXPOSE))
                .readOnly(true)
                .dynamicEntryAllowed(false)
                .applicableFlowTypes(registrationOnly)
                .children(attributeLeaves)
                .build());
        return FlowExtensionContextTreeNode.builder()
                .key("subjectOrganization")
                .title("Subject Organization")
                .path(FlowContextPaths.SUBJECT_ORGANIZATION_PREFIX)
                .dataType("")
                .nodeType(ContextTree.NODE_OBJECT)
                .allowedOperations(Collections.singletonList(ContextTree.OP_EXPOSE))
                .readOnly(true)
                .applicableFlowTypes(registrationOnly)
                .children(children)
                .build();
    }

    private FlowExtensionContextTreeNode subjectOrganizationLeaf(String key, String title, String path) {

        return FlowExtensionContextTreeNode.builder()
                .key(key)
                .title(title)
                .path(path)
                .dataType(ContextTree.DATA_TYPE_STRING)
                .nodeType(ContextTree.NODE_LEAF)
                .allowedOperations(Collections.singletonList(ContextTree.OP_EXPOSE))
                .readOnly(true)
                .applicableFlowTypes(Collections.singletonList(ContextTree.FLOW_REGISTRATION))
                .build();
    }

    private List<String> getOrganizationAttributes() {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return FlowMgtService.getInstance().getOrganizationAttributes(tenantId);
        } catch (FlowMgtFrameworkException e) {
            LOG.error("Error while retrieving the organization attributes of the registration flow of tenant: "
                    + tenantId + ". The attributes are left out of the context tree.", e);
            return Collections.emptyList();
        }
    }

    private FlowExtensionContextTreeNode readOnlyLeaf(String key, String title, String path) {

        return FlowExtensionContextTreeNode.builder()
                .key(key)
                .title(title)
                .path(path)
                .dataType(ContextTree.DATA_TYPE_STRING)
                .nodeType(ContextTree.NODE_LEAF)
                .allowedOperations(Collections.singletonList(ContextTree.OP_EXPOSE))
                .readOnly(true)
                .build();
    }

    private void addFlowVariableNodes(List<FlowExtensionContextTreeNode> tree, String flowType) {

        List<FlowTypeVariablesDTO> flowVariables;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            flowVariables = FlowMgtService.getInstance().getFlowVariables(tenantId, flowType);
        } catch (FlowMgtFrameworkException e) {
            LOG.error("Error while retrieving the flow variables of tenant: " + tenantId
                    + ". Runtime values and request headers are left out of the context tree.", e);
            return;
        }

        tree.add(FlowExtensionContextTreeNode.builder()
                .key("runtimeValues")
                .title("Runtime Values")
                .path(FlowContextPaths.RUNTIME_VALUES_PATH)
                .dataType("Map<String, Object>")
                .nodeType(ContextTree.NODE_MAP)
                .allowedOperations(Arrays.asList(ContextTree.OP_EXPOSE, ContextTree.OP_MODIFY))
                .dynamicEntryAllowed(true)
                .children(buildVariableLeaves(flowVariables, FlowTypeVariablesDTO::getRuntimeValues, false))
                .build());

        FlowExtensionContextTreeNode headersNode = FlowExtensionContextTreeNode.builder()
                .key("additionalHeaders")
                .title("Additional Headers")
                .path(FlowContextPaths.REQUEST_HEADERS_PATH)
                .dataType("Map<String, String[]>")
                .nodeType(ContextTree.NODE_MAP)
                .allowedOperations(Collections.singletonList(ContextTree.OP_EXPOSE))
                .readOnly(true)
                .dynamicEntryAllowed(true)
                .children(buildVariableLeaves(flowVariables, FlowTypeVariablesDTO::getHeaders, true))
                .build();
        tree.add(FlowExtensionContextTreeNode.builder()
                .key("request")
                .title("Request")
                .path(FlowContextPaths.REQUEST_PREFIX)
                .dataType("")
                .nodeType(ContextTree.NODE_OBJECT)
                .allowedOperations(Collections.singletonList(ContextTree.OP_EXPOSE))
                .readOnly(true)
                .children(Collections.singletonList(headersNode))
                .build());
    }

    private List<FlowExtensionContextTreeNode> buildVariableLeaves(List<FlowTypeVariablesDTO> flowVariables,
                                                                   Function<FlowTypeVariablesDTO,
                                                                           List<? extends FlowVariableDTO>> variablesOf,
                                                                   boolean isHeaders) {

        // Name and data type → flow types that define it, in flow type order.
        Map<String, List<String>> flowTypesByEntry = new LinkedHashMap<>();
        Map<String, FlowVariableDTO> variableByEntry = new LinkedHashMap<>();
        for (FlowTypeVariablesDTO flow : flowVariables) {
            for (FlowVariableDTO variable : variablesOf.apply(flow)) {
                if (variable == null || variable.getName() == null || variable.getDataType() == null) {
                    continue;
                }
                String entry = variable.getName() + "#" + variable.getDataType().name();
                variableByEntry.putIfAbsent(entry, variable);
                flowTypesByEntry.computeIfAbsent(entry, k -> new ArrayList<>()).add(flow.getFlowType());
            }
        }

        // A name the flows give more than one data type has a leaf per data type, all with the same path, as the
        // access config entry is the path and the flow type. Their keys name the data type, so that they differ.
        Map<String, Long> entriesByName = variableByEntry.values().stream()
                .collect(Collectors.groupingBy(FlowVariableDTO::getName, Collectors.counting()));

        List<FlowExtensionContextTreeNode> leaves = new ArrayList<>();
        variableByEntry.forEach((entry, variable) -> {
            String name = variable.getName();
            boolean readOnly = isHeaders;
            leaves.add(FlowExtensionContextTreeNode.builder()
                    .key(entriesByName.get(name) > 1 ? entry : name)
                    .title(name)
                    .path((isHeaders ? FlowContextPaths.REQUEST_HEADERS_SELECTOR_PREFIX
                            : FlowContextPaths.RUNTIME_VALUES_SELECTOR_PREFIX) + name
                            + FlowContextPaths.NAME_SELECTOR_SUFFIX)
                    .dataType(variable.getDataType().name())
                    .nodeType(ContextTree.NODE_LEAF)
                    .allowedOperations(readOnly ? Collections.singletonList(ContextTree.OP_EXPOSE)
                            : Arrays.asList(ContextTree.OP_EXPOSE, ContextTree.OP_MODIFY))
                    .readOnly(readOnly)
                    .applicableFlowTypes(flowTypesByEntry.get(entry))
                    .build());
        });
        leaves.sort(Comparator.comparing(FlowExtensionContextTreeNode::getTitle)
                .thenComparing(FlowExtensionContextTreeNode::getDataType));
        return leaves;
    }
}
