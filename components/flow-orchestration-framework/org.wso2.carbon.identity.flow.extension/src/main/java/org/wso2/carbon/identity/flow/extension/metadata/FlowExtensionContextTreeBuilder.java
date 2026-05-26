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

import org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ContextTree;
import org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.FlowContextPaths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Builds the controlled In-Flow Extension context tree returned by the metadata endpoint.
 * The set of exposed attributes is governed at code level by this builder so the frontend
 * receives the user/flow/properties tree shape the Console UI expects.
 */
public class FlowExtensionContextTreeBuilder {

    // Flow context attributes that appear under the "flow" branch of the tree.
    private static final List<String> FLOW_BRANCH_ATTRS =
            Arrays.asList("flowType", "portalUrl");

    /**
     * Build the metadata response for the given flow type.
     *
     * @param flowType the flow type (null → default tree).
     * @return a fully populated metadata DTO.
     */
    public FlowExtensionContextTreeMetadata build(String flowType) {

        List<FlowExtensionContextTreeNode> tree = new ArrayList<>();
        tree.add(buildUserNode());
        tree.add(buildPropertiesNode());
        tree.add(buildTenantNode());
        tree.add(buildApplicationNode());
        tree.add(buildOrganizationNode());
        tree.add(buildFlowNode());

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

        if (flowType == null) {
            return true;
        }
        switch (flowType) {
            case ContextTree.FLOW_REGISTRATION, ContextTree.FLOW_INVITED_USER_REGISTRATION:
                return true;
            case ContextTree.FLOW_PASSWORD_RECOVERY:
            default:
                return false;
        }
    }

    private FlowExtensionContextTreeNode buildUserNode() {

        List<FlowExtensionContextTreeNode> children = new ArrayList<>();

        children.add(FlowExtensionContextTreeNode.builder()
                .key("id")
                .title("User ID")
                .path("/user/id")
                .dataType(ContextTree.DATA_TYPE_STRING)
                .nodeType(ContextTree.NODE_LEAF)
                .allowedOperations(Collections.singletonList(ContextTree.OP_EXPOSE))
                .replaceable(false)
                .build());
        children.add(FlowExtensionContextTreeNode.builder()
                .key("username")
                .title("Username")
                .path("/user/username")
                .dataType(ContextTree.DATA_TYPE_STRING)
                .nodeType(ContextTree.NODE_LEAF)
                .allowedOperations(Collections.singletonList(ContextTree.OP_EXPOSE))
                .replaceable(false)
                .build());
        children.add(FlowExtensionContextTreeNode.builder()
                .key("userStoreDomain")
                .title("User Store Domain")
                .path("/user/userStoreDomain")
                .dataType(ContextTree.DATA_TYPE_STRING)
                .nodeType(ContextTree.NODE_LEAF)
                .allowedOperations(Collections.singletonList(ContextTree.OP_EXPOSE))
                .replaceable(false)
                .build());

        children.add(FlowExtensionContextTreeNode.builder()
                .key("claims")
                .title("Claims")
                .path("/user/claims/")
                .dataType("Map<String, String>")
                .nodeType(ContextTree.NODE_MAP)
                .allowedOperations(Arrays.asList(ContextTree.OP_EXPOSE, ContextTree.OP_MODIFY))
                .dynamicEntryAllowed(true)
                .dynamicEntryType("String")
                .children(Collections.emptyList())
                .build());

        children.add(FlowExtensionContextTreeNode.builder()
                .key("credentials")
                .title("Credentials")
                .path("/user/credentials/")
                .dataType("Map<String, char[]>")
                .nodeType(ContextTree.NODE_MAP)
                .allowedOperations(Arrays.asList(ContextTree.OP_EXPOSE, ContextTree.OP_MODIFY))
                .dynamicEntryAllowed(true)
                .dynamicEntryType("char[]")
                .children(Collections.emptyList())
                .build());

        return FlowExtensionContextTreeNode.builder()
                .key("user")
                .title("User")
                .path("/user/")
                .dataType("")
                .nodeType(ContextTree.NODE_OBJECT)
                .allowedOperations(Collections.singletonList(ContextTree.OP_EXPOSE))
                .children(children)
                .build();
    }

    private FlowExtensionContextTreeNode buildFlowNode() {

        List<FlowExtensionContextTreeNode> children = new ArrayList<>();
        for (String attr : FLOW_BRANCH_ATTRS) {
            children.add(readOnlyLeaf(attr, attrTitle(attr), FlowContextPaths.FLOW_PREFIX + attr));
        }
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

    private FlowExtensionContextTreeNode buildPropertiesNode() {

        return FlowExtensionContextTreeNode.builder()
                .key("properties")
                .title("Properties")
                .path("/properties/")
                .dataType("Map<String, Object>")
                .nodeType(ContextTree.NODE_COMPLEX_MAP)
                .allowedOperations(Collections.singletonList(ContextTree.OP_MODIFY))
                .dynamicEntryAllowed(true)
                .dynamicEntryType("Object")
                .children(Collections.emptyList())
                .build();
    }

    private static String attrTitle(String attr) {

        switch (attr) {
            case "flowType":       return "Flow Type";
            case "portalUrl":      return "Portal URL";
            default:               return attr;
        }
    }
}
