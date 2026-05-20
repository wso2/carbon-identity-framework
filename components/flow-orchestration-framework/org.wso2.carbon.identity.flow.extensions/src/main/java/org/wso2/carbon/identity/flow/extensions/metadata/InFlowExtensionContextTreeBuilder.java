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

package org.wso2.carbon.identity.flow.extensions.metadata;

import org.wso2.carbon.identity.flow.extensions.model.FlowContextHandoverConfig;

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
    private static final String DATA_TYPE_STRING = "String";

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

        // User and properties nodes are always non-null (always construct and return a node).
        tree.add(buildUserNode(userAttrs));
        tree.add(buildPropertiesNode(attrs));

        // Flow node is conditionally non-null (returns null if no flow attributes are configured).
        InFlowExtensionContextTreeNode flowNode = buildFlowNode(attrs);
        if (flowNode != null) {
            tree.add(flowNode);
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
            case FLOW_REGISTRATION, FLOW_INVITED_USER_REGISTRATION:
                return true;
            case FLOW_PASSWORD_RECOVERY:
            default:
                return false;
        }
    }

    /**
     * Build the "user" subtree.
     *
     * <p>Strategy:
     * <ul>
     *   <li><b>Read-only fields</b> (userId, username, userStoreDomain): only emitted when the
     *       attribute is in the allow-list (or full-passthrough is active). They support EXPOSE
     *       only, so excluding them when restricted is the correct behaviour.</li>
     *   <li><b>Modifiable fields</b> (claims, credentials): always emitted because modifications
     *       bypass the context handover — they travel through the executor response and are applied
     *       by the task execution node. EXPOSE is added only when the attribute is configured.</li>
     * </ul>
     *
     * <p>{@code userAttrs == null} signals full-passthrough (show and expose everything).
     */
    private InFlowExtensionContextTreeNode buildUserNode(Set<String> userAttrs) {

        // userAttrs == null → full passthrough set by the caller (build()).
        boolean fullPassthrough = (userAttrs == null);

        List<InFlowExtensionContextTreeNode> children = new ArrayList<>();

        // ── Read-only fields: emit only when exposed ────────────────────────────────────────
        if (fullPassthrough || userAttrs.contains("userId")) {
            children.add(InFlowExtensionContextTreeNode.builder()
                    .key("userId")
                    .title("User ID")
                    .path("/user/userId")
                    .dataType(DATA_TYPE_STRING)
                    .nodeType(NODE_LEAF)
                    .allowedOperations(Collections.singletonList(OP_EXPOSE))
                    .replaceable(false)
                    .build());
        }
        if (fullPassthrough || userAttrs.contains("username")) {
            children.add(InFlowExtensionContextTreeNode.builder()
                    .key("username")
                    .title("Username")
                    .path("/user/username")
                    .dataType(DATA_TYPE_STRING)
                    .nodeType(NODE_LEAF)
                    .allowedOperations(Collections.singletonList(OP_EXPOSE))
                    .replaceable(false)
                    .build());
        }
        if (fullPassthrough || userAttrs.contains("userStoreDomain")) {
            children.add(InFlowExtensionContextTreeNode.builder()
                    .key("userStoreDomain")
                    .title("User Store Domain")
                    .path("/user/userStoreDomain")
                    .dataType(DATA_TYPE_STRING)
                    .nodeType(NODE_LEAF)
                    .allowedOperations(Collections.singletonList(OP_EXPOSE))
                    .replaceable(false)
                    .build());
        }

        // ── Modifiable fields: always present, restrict EXPOSE when not configured ──────────
        List<String> claimsOps = (fullPassthrough || userAttrs.contains("claims"))
                ? Arrays.asList(OP_EXPOSE, OP_MODIFY)
                : Collections.singletonList(OP_MODIFY);
        children.add(InFlowExtensionContextTreeNode.builder()
                .key("claims")
                .title("Claims")
                .path("/user/claims/")
                .dataType("Map<String, String>")
                .nodeType(NODE_MAP)
                .allowedOperations(claimsOps)
                .dynamicEntryAllowed(true)
                .dynamicEntryType("String")
                .children(Collections.emptyList())
                .build());

        List<String> credOps = (fullPassthrough || userAttrs.contains("userCredentials"))
                ? Arrays.asList(OP_EXPOSE, OP_MODIFY)
                : Collections.singletonList(OP_MODIFY);
        children.add(InFlowExtensionContextTreeNode.builder()
                .key("credentials")
                .title("Credentials")
                .path("/user/credentials/")
                .dataType("Map<String, char[]>")
                .nodeType(NODE_MAP)
                .allowedOperations(credOps)
                .dynamicEntryAllowed(true)
                .dynamicEntryType("char[]")
                .children(Collections.emptyList())
                .build());

        // User node is always returned (claims + credentials are always present).
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
                .dataType(DATA_TYPE_STRING)
                .nodeType(NODE_LEAF)
                .allowedOperations(Collections.singletonList(OP_EXPOSE))
                .readOnly(true)
                .build();
    }

    /**
     * Build the "properties" node.
     *
     * <p>Properties is always emitted because modifications bypass the context handover (they
     * travel through the executor response and are applied by the task execution node).
     * EXPOSE is included only when {@code "properties"} is in the allow-list.
     */
    private InFlowExtensionContextTreeNode buildPropertiesNode(Set<String> attrs) {

        List<String> ops = attrs.contains("properties")
                ? Arrays.asList(OP_EXPOSE, OP_MODIFY)
                : Collections.singletonList(OP_MODIFY);
        return InFlowExtensionContextTreeNode.builder()
                .key("properties")
                .title("Properties")
                .path("/properties/")
                .dataType("Map<String, Object>")
                .nodeType(NODE_COMPLEX_MAP)
                .allowedOperations(ops)
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
