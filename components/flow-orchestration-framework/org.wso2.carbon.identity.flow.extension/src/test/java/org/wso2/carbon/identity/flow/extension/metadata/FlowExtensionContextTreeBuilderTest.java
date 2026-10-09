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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ContextTree;
import org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.FlowContextPaths;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link FlowExtensionContextTreeBuilder}, focused on the user identity leaves
 * (id / username / userStoreDomain) exposed under the {@code /user/} branch.
 */
public class FlowExtensionContextTreeBuilderTest {

    private Map<String, FlowExtensionContextTreeNode> userLeaves;
    private Map<String, FlowExtensionContextTreeNode> organizationLeaves;

    @BeforeClass
    public void buildTree() {

        FlowExtensionContextTreeMetadata metadata = new FlowExtensionContextTreeBuilder().build(null);
        FlowExtensionContextTreeNode userNode = metadata.getContextTree().stream()
                .filter(n -> "user".equals(n.getKey()))
                .findFirst()
                .orElse(null);
        assertNotNull(userNode, "The /user/ node must be present in the context tree.");
        assertEquals(userNode.getPath(), FlowContextPaths.USER_PREFIX);

        userLeaves = userNode.getChildren().stream()
                .collect(Collectors.toMap(FlowExtensionContextTreeNode::getKey, n -> n));

        FlowExtensionContextTreeNode organizationNode = metadata.getContextTree().stream()
                .filter(n -> "organization".equals(n.getKey()))
                .findFirst()
                .orElse(null);
        assertNotNull(organizationNode, "The /organization/ node must be present in the context tree.");
        organizationLeaves = organizationNode.getChildren().stream()
                .collect(Collectors.toMap(FlowExtensionContextTreeNode::getKey, n -> n));
    }

    @Test
    public void testIdentityLeavesPresentUnderUser() {

        assertTrue(userLeaves.containsKey("id"), "/user/id leaf must be advertised.");
        assertTrue(userLeaves.containsKey("username"), "/user/username leaf must be advertised.");
        assertTrue(userLeaves.containsKey("userStoreDomain"),
                "/user/userStoreDomain leaf must be advertised.");
    }

    @Test
    public void testIdentityLeafPaths() {

        assertEquals(userLeaves.get("id").getPath(), FlowContextPaths.USER_ID_PATH);
        assertEquals(userLeaves.get("username").getPath(), FlowContextPaths.USER_USERNAME_PATH);
        assertEquals(userLeaves.get("userStoreDomain").getPath(), FlowContextPaths.USER_STORE_DOMAIN_PATH);
    }

    @Test
    public void testIdentityLeavesAreExposeOnlyAndNonReplaceable() {

        for (String key : new String[]{"id", "username", "userStoreDomain"}) {
            FlowExtensionContextTreeNode leaf = userLeaves.get(key);
            assertEquals(leaf.getNodeType(), ContextTree.NODE_LEAF, key + " must be a leaf node.");
            assertEquals(leaf.getDataType(), ContextTree.DATA_TYPE_STRING, key + " must be a string.");
            List<String> ops = leaf.getAllowedOperations();
            assertEquals(ops, java.util.Collections.singletonList(ContextTree.OP_EXPOSE),
                    key + " must allow EXPOSE only (no MODIFY).");
            assertFalse(leaf.isReplaceable(), key + " must not be replaceable.");
        }
    }

    @Test
    public void testOrganizationAttributesMapIsModifiable() {

        FlowExtensionContextTreeNode attributes = organizationLeaves.get("attributes");
        assertNotNull(attributes);
        assertEquals(attributes.getPath(), FlowContextPaths.ORGANIZATION_ATTRIBUTES_PATH);
        assertEquals(attributes.getNodeType(), ContextTree.NODE_MAP);
        assertTrue(attributes.getAllowedOperations().contains(ContextTree.OP_EXPOSE));
        assertTrue(attributes.getAllowedOperations().contains(ContextTree.OP_MODIFY));
        assertTrue(attributes.isDynamicEntryAllowed());
    }
}
