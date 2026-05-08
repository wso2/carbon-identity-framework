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

import org.wso2.carbon.identity.flow.execution.engine.config.FlowContextHandoverConfig;
import org.wso2.carbon.identity.flow.execution.engine.config.FlowContextHandoverConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Builds the controlled In-Flow Extension context tree returned by the metadata endpoint.
 * Consumes the engine-level {@link FlowContextHandoverConfig} flat allow-lists and projects
 * them back into the user/flow/properties tree shape the Console UI expects, so the frontend
 * requires no change. Nodes are pruned when the corresponding attribute is absent from the
 * allow-list.
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

    // Flow context attributes that appear under the "flow" branch of the tree.
    private static final List<String> FLOW_BRANCH_ATTRS =
            Arrays.asList("tenantDomain", "applicationId", "flowType", "callbackUrl", "portalUrl");

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

        Set<String> attrs = handoverConfig.getIncludedAttributes();
        Set<String> userAttrs = handoverConfig.isFullUserPassthrough()
                ? null   // null → all user children shown
                : handoverConfig.getIncludedUserAttributes();

        List<InFlowExtensionContextTreeNode> tree = new ArrayList<>();

        InFlowExtensionContextTreeNode userNode = buildUserNode(attrs, userAttrs);
        if (userNode != null) {
            tree.add(userNode);
        }
        InFlowExtensionContextTreeNode flowNode = buildFlowNode(attrs);
        if (flowNode != null) {
            tree.add(flowNode);
        }
        InFlowExtensionContextTreeNode propsNode = buildPropertiesNode(attrs);
        if (propsNode != null) {
            tree.add(propsNode);
        }

        return new InFlowExtensionContextTreeMetadata(
                flowType,
                tree,
                true,   // redirection is unconditionally enabled
                resolveAllowReadOnlyClaimsModification(flowType));
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
    static boolean resolveAllowReadOnlyClaimsModification(String flowType) {

        if (flowType == null) {
            return true;
        }
        switch (flowType) {
            case FLOW_REGISTRATION:
            case FLOW_INVITED_USER_REGISTRATION:
                return true;
            case FLOW_PASSWORD_RECOVERY:
            default:
                return false;
        }
    }

    /**
     * Build the "user" subtree. {@code userAttrs == null} means full passthrough (show all).
     */
    private InFlowExtensionContextTreeNode buildUserNode(Set<String> attrs, Set<String> userAttrs) {

        // User branch is shown when flowUser is in attrs OR any individual user attr is listed.
        boolean fullPassthrough = attrs.contains(FlowContextHandoverConstants.ATTR_FLOW_USER);
        boolean hasAnyUserAttr = fullPassthrough
                || (userAttrs != null && !userAttrs.isEmpty());
        if (!hasAnyUserAttr) {
            return null;
        }

        List<InFlowExtensionContextTreeNode> children = new ArrayList<>();

        if (fullPassthrough || (userAttrs != null && userAttrs.contains("userId"))) {
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
        if (fullPassthrough || (userAttrs != null && userAttrs.contains("username"))) {
            children.add(InFlowExtensionContextTreeNode.builder()
                    .key("username")
                    .title("Username")
                    .path("/user/username")
                    .dataType("String")
                    .nodeType(NODE_LEAF)
                    .allowedOperations(Collections.singletonList(OP_EXPOSE))
                    .replaceable(false)
                    .build());
        }
        if (fullPassthrough || (userAttrs != null && userAttrs.contains("userStoreDomain"))) {
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
        if (fullPassthrough || (userAttrs != null && userAttrs.contains("claims"))) {
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
        if (fullPassthrough || (userAttrs != null && userAttrs.contains("userCredentials"))) {
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

    /**
     * Build the "flow" subtree from top-level context attributes that map to the flow branch.
     */
    private InFlowExtensionContextTreeNode buildFlowNode(Set<String> attrs) {

        List<InFlowExtensionContextTreeNode> children = new ArrayList<>();
        for (String attr : FLOW_BRANCH_ATTRS) {
            if (attrs.contains(attr)) {
                String title = attrTitle(attr);
                children.add(flowLeaf(attr, title, "/flow/" + attr));
            }
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

    /**
     * Build the "properties" node if {@code "properties"} is in the included attributes.
     */
    private InFlowExtensionContextTreeNode buildPropertiesNode(Set<String> attrs) {

        if (!attrs.contains("properties")) {
            return InFlowExtensionContextTreeNode.builder()
                    .key("properties")
                    .title("Properties")
                    .path("/properties/")
                    .dataType("Map<String, Object>")
                    .nodeType(NODE_COMPLEX_MAP)
                    .allowedOperations(Arrays.asList(OP_MODIFY))
                    .dynamicEntryAllowed(true)
                    .dynamicEntryType("Object")
                    .children(Collections.emptyList())
                    .build();
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

    private static String attrTitle(String attr) {

        switch (attr) {
            case "tenantDomain":   return "Tenant Domain";
            case "applicationId":  return "Application ID";
            case "flowType":       return "Flow Type";
            case "callbackUrl":    return "Callback URL";
            case "portalUrl":      return "Portal URL";
            default:               return attr;
        }
    }
}
