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

package org.wso2.carbon.identity.flow.inflow.extensions.metadata;

import org.wso2.carbon.identity.flow.inflow.extensions.config.FlowContextHandoverConfig;
import org.wso2.carbon.identity.flow.inflow.extensions.config.FlowContextHandoverPolicy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Builds the controlled In-Flow Extension context tree returned by the metadata endpoint.
 * The canonical tree shape lives in code here (rather than a static resource) so it can
 * evolve alongside the {@link FlowContextHandoverPolicy} without an extra file to keep in
 * sync. Nodes are pruned when the policy disables them, so the Console UI never offers the
 * admin a checkbox for a field the deployment has switched off at the server level.
 */
public class InFlowExtensionContextTreeBuilder {

    // Flow type identifiers — must match the values produced by FlowTypes.getType().
    private static final String FLOW_REGISTRATION = "REGISTRATION";
    private static final String FLOW_INVITED_USER_REGISTRATION = "INVITED_USER_REGISTRATION";
    private static final String FLOW_PASSWORD_RECOVERY = "PASSWORD_RECOVERY";

    // Node-type sentinels matching the tree component's NodeType enum on the Console side.
    private static final String NODE_OBJECT = "OBJECT";
    private static final String NODE_LEAF = "LEAF";
    private static final String NODE_MAP = "MAP";
    private static final String NODE_COMPLEX_MAP = "COMPLEX_MAP";

    private static final String OP_EXPOSE = "EXPOSE";
    private static final String OP_MODIFY = "MODIFY";

    private final FlowContextHandoverConfig handoverConfig;

    public InFlowExtensionContextTreeBuilder(FlowContextHandoverConfig handoverConfig) {

        this.handoverConfig = handoverConfig;
    }

    /**
     * Build the metadata response for the given flow type.
     *
     * @param flowType the flow type (null → default tree).
     * @return a fully populated metadata DTO.
     */
    public InFlowExtensionContextTreeMetadata build(String flowType) {

        FlowContextHandoverPolicy policy = handoverConfig.resolve(flowType);

        List<InFlowExtensionContextTreeNode> tree = new ArrayList<>();
        InFlowExtensionContextTreeNode userNode = buildUserNode(policy);
        if (userNode != null) {
            tree.add(userNode);
        }
        InFlowExtensionContextTreeNode flowNode = buildFlowNode(policy);
        if (flowNode != null) {
            tree.add(flowNode);
        }
        InFlowExtensionContextTreeNode propsNode = buildPropertiesNode(policy);
        if (propsNode != null) {
            tree.add(propsNode);
        }

        return new InFlowExtensionContextTreeMetadata(
                flowType,
                tree,
                policy.isRedirectionEnabled(),
                resolveAllowReadOnlyClaimsModification(flowType));
    }

    /**
     * Whether the Console UI may permit MODIFY on read-only claims for this flow type.
     * Hardcoded enumerative mapping (rather than {@code != PASSWORD_RECOVERY}) so that any
     * future flow type defaults to the safe value (false) until explicitly added here.
     *
     * <p>The default tree (null flowType) inherits the registration-style permissive default
     * — that matches today's behaviour for the connection-level access-config editor, which
     * doesn't yet know which flow the action will be wired into.</p>
     */
    static boolean resolveAllowReadOnlyClaimsModification(String flowType) {

        if (flowType == null) {
            return true;
        }
        switch (flowType) {
            case FLOW_REGISTRATION:
            case FLOW_INVITED_USER_REGISTRATION:
                return true;
            case FLOW_PASSWORD_RECOVERY:
                return false;
            default:
                return false;
        }
    }

    private InFlowExtensionContextTreeNode buildUserNode(FlowContextHandoverPolicy policy) {

        if (!policy.isUserEnabled()) {
            return null;
        }
        List<InFlowExtensionContextTreeNode> children = new ArrayList<>();
        if (policy.isUserUserId()) {
            children.add(InFlowExtensionContextTreeNode.builder()
                    .key("userId")
                    .title("User ID")
                    .path("/user/userId")
                    .dataType("String")
                    .nodeType(NODE_LEAF)
                    .allowedOperations(Collections.singletonList(OP_EXPOSE))
                    .replaceable(false)
                    .build());
        }
        if (policy.isUserUserStoreDomain()) {
            children.add(InFlowExtensionContextTreeNode.builder()
                    .key("userStoreDomain")
                    .title("User Store Domain")
                    .path("/user/userStoreDomain")
                    .dataType("String")
                    .nodeType(NODE_LEAF)
                    .allowedOperations(Collections.singletonList(OP_EXPOSE))
                    .replaceable(false)
                    .build());
        }
        if (policy.isUserClaims()) {
            children.add(InFlowExtensionContextTreeNode.builder()
                    .key("claims")
                    .title("Claims")
                    .path("/user/claims/")
                    .dataType("Map<String, String>")
                    .nodeType(NODE_MAP)
                    .allowedOperations(Arrays.asList(OP_EXPOSE, OP_MODIFY))
                    .dynamicEntryAllowed(true)
                    .dynamicEntryType("String")
                    .children(Collections.emptyList())
                    .build());
        }
        if (policy.isUserCredentials()) {
            children.add(InFlowExtensionContextTreeNode.builder()
                    .key("credentials")
                    .title("Credentials")
                    .path("/user/credentials/")
                    .dataType("Map<String, char[]>")
                    .nodeType(NODE_MAP)
                    .allowedOperations(Arrays.asList(OP_EXPOSE, OP_MODIFY))
                    .dynamicEntryAllowed(true)
                    .dynamicEntryType("char[]")
                    .children(Collections.emptyList())
                    .build());
        }
        if (children.isEmpty()) {
            return null;
        }
        return InFlowExtensionContextTreeNode.builder()
                .key("user")
                .title("User")
                .path("/user/")
                .dataType("")
                .nodeType(NODE_OBJECT)
                .allowedOperations(Collections.singletonList(OP_EXPOSE))
                .children(children)
                .build();
    }

    private InFlowExtensionContextTreeNode buildFlowNode(FlowContextHandoverPolicy policy) {

        if (!policy.isFlowEnabled()) {
            return null;
        }
        List<InFlowExtensionContextTreeNode> children = new ArrayList<>();
        if (policy.isFlowTenantDomain()) {
            children.add(flowLeaf("tenantDomain", "Tenant Domain", "/flow/tenantDomain"));
        }
        if (policy.isFlowApplicationId()) {
            children.add(flowLeaf("applicationId", "Application ID", "/flow/applicationId"));
        }
        if (policy.isFlowFlowType()) {
            children.add(flowLeaf("flowType", "Flow Type", "/flow/flowType"));
        }
        if (policy.isFlowCallbackUrl()) {
            children.add(flowLeaf("callbackUrl", "Callback URL", "/flow/callbackUrl"));
        }
        if (policy.isFlowPortalUrl()) {
            children.add(flowLeaf("portalUrl", "Portal URL", "/flow/portalUrl"));
        }
        if (children.isEmpty()) {
            return null;
        }
        return InFlowExtensionContextTreeNode.builder()
                .key("flow")
                .title("Flow")
                .path("/flow/")
                .dataType("")
                .nodeType(NODE_OBJECT)
                .allowedOperations(Collections.singletonList(OP_EXPOSE))
                .readOnly(true)
                .children(children)
                .build();
    }

    private InFlowExtensionContextTreeNode flowLeaf(String key, String title, String path) {

        return InFlowExtensionContextTreeNode.builder()
                .key(key)
                .title(title)
                .path(path)
                .dataType("String")
                .nodeType(NODE_LEAF)
                .allowedOperations(Collections.singletonList(OP_EXPOSE))
                .readOnly(true)
                .build();
    }

    private InFlowExtensionContextTreeNode buildPropertiesNode(FlowContextHandoverPolicy policy) {

        if (!policy.isPropertiesEnabled()) {
            return null;
        }
        return InFlowExtensionContextTreeNode.builder()
                .key("properties")
                .title("Properties")
                .path("/properties/")
                .dataType("Map<String, Object>")
                .nodeType(NODE_COMPLEX_MAP)
                .allowedOperations(Arrays.asList(OP_EXPOSE, OP_MODIFY))
                .dynamicEntryAllowed(true)
                .dynamicEntryType("Object")
                .children(Collections.emptyList())
                .build();
    }
}
