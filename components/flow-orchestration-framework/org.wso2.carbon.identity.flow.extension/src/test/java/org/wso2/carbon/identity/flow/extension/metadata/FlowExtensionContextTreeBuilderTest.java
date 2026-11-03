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

import org.mockito.MockedStatic;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ContextTree;
import org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.FlowContextPaths;
import org.wso2.carbon.identity.flow.mgt.FlowMgtService;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtServerException;
import org.wso2.carbon.identity.flow.mgt.model.FlowRuntimeValueDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowTypeVariablesDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDataType;
import org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link FlowExtensionContextTreeBuilder}, focused on the user identity leaves
 * (id / username / userStoreDomain) exposed under the {@code /user/} branch.
 */
@WithCarbonHome
public class FlowExtensionContextTreeBuilderTest {

    private static final int TENANT_ID = -1234;
    private static final String REGISTRATION = "REGISTRATION";
    private static final String PASSWORD_RECOVERY = "PASSWORD_RECOVERY";
    private static final String INVITED_USER_REGISTRATION = "INVITED_USER_REGISTRATION";
    private static final List<String> ALL = Collections.singletonList(ContextTree.ALL_FLOW_TYPES);

    private Map<String, FlowExtensionContextTreeNode> userLeaves;

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

    // ------------------------------------------------------------------ runtime values and request headers

    /**
     * A name two flows give different types is listed once per type, and one two flows give the same type is listed
     * once, naming both flows.
     */
    @Test
    public void testRuntimeValueLeavesAreListedPerNameAndType() throws Exception {

        FlowTypeVariablesDTO registration = new FlowTypeVariablesDTO(REGISTRATION);
        registration.getRuntimeValues().add(new FlowRuntimeValueDTO("plan", FlowVariableDataType.STRING));
        registration.getRuntimeValues().add(new FlowRuntimeValueDTO("attempts", FlowVariableDataType.INTEGER));
        registration.getRuntimeValues().add(new FlowRuntimeValueDTO("abTestingKey", FlowVariableDataType.INTEGER));
        FlowTypeVariablesDTO recovery = new FlowTypeVariablesDTO(PASSWORD_RECOVERY);
        recovery.getRuntimeValues().add(new FlowRuntimeValueDTO("attempts", FlowVariableDataType.STRING));
        recovery.getRuntimeValues().add(new FlowRuntimeValueDTO("plan", FlowVariableDataType.STRING));
        recovery.getRuntimeValues().add(new FlowRuntimeValueDTO("abTestingKey", FlowVariableDataType.INTEGER));

        List<FlowExtensionContextTreeNode> tree = buildWithFlowVariables(Arrays.asList(registration, recovery,
                new FlowTypeVariablesDTO(INVITED_USER_REGISTRATION)));

        FlowExtensionContextTreeNode runtimeValues = node(tree, "runtimeValues");
        assertEquals(runtimeValues.getPath(), FlowContextPaths.RUNTIME_VALUES_PATH);
        assertEquals(runtimeValues.getNodeType(), ContextTree.NODE_MAP);
        assertEquals(runtimeValues.getAllowedOperations(), Arrays.asList(ContextTree.OP_EXPOSE, ContextTree.OP_MODIFY));
        assertTrue(runtimeValues.isDynamicEntryAllowed());
        assertEquals(runtimeValues.getApplicableFlowTypes(), ALL);

        List<FlowExtensionContextTreeNode> leaves = runtimeValues.getChildren();
        assertEquals(leaves.size(), 4);

        // Sorted by name, then by data type.
        FlowExtensionContextTreeNode abTestingKey = leaves.get(0);
        assertEquals(abTestingKey.getKey(), "abTestingKey");
        assertEquals(abTestingKey.getApplicableFlowTypes(), Arrays.asList(REGISTRATION, PASSWORD_RECOVERY));
        assertEquals(abTestingKey.getAllowedOperations(), Arrays.asList(ContextTree.OP_EXPOSE, ContextTree.OP_MODIFY));
        assertFalse(abTestingKey.isReadOnly());

        FlowExtensionContextTreeNode integerAttempts = leaves.get(1);
        assertEquals(integerAttempts.getKey(), "attempts");
        assertEquals(integerAttempts.getDataType(), "INTEGER");
        assertEquals(integerAttempts.getApplicableFlowTypes(), Collections.singletonList(REGISTRATION));
        FlowExtensionContextTreeNode stringAttempts = leaves.get(2);
        assertEquals(stringAttempts.getKey(), "attempts");
        assertEquals(stringAttempts.getDataType(), "STRING");
        assertEquals(stringAttempts.getApplicableFlowTypes(), Collections.singletonList(PASSWORD_RECOVERY));
        assertEquals(stringAttempts.getPath(), "/runtimeValues[name=attempts]");

        FlowExtensionContextTreeNode plan = leaves.get(3);
        assertEquals(plan.getKey(), "plan");
        assertEquals(plan.getPath(), "/runtimeValues[name=plan]");
        assertEquals(plan.getNodeType(), ContextTree.NODE_LEAF);
        assertEquals(plan.getApplicableFlowTypes(), Arrays.asList(REGISTRATION, PASSWORD_RECOVERY));
        assertEquals(plan.getAllowedOperations(), Arrays.asList(ContextTree.OP_EXPOSE, ContextTree.OP_MODIFY));
        assertFalse(plan.isReadOnly());
    }

    @Test
    public void testRequestHeaderLeavesAreExposeOnly() throws Exception {

        FlowTypeVariablesDTO registration = new FlowTypeVariablesDTO(REGISTRATION);
        registration.getHeaders().add(new FlowVariableDTO("x-device", FlowVariableDataType.STRING_ARRAY));
        FlowTypeVariablesDTO invited = new FlowTypeVariablesDTO(INVITED_USER_REGISTRATION);
        invited.getHeaders().add(new FlowVariableDTO("x-device", FlowVariableDataType.STRING_ARRAY));

        List<FlowExtensionContextTreeNode> tree = buildWithFlowVariables(Arrays.asList(registration,
                new FlowTypeVariablesDTO(PASSWORD_RECOVERY), invited));

        FlowExtensionContextTreeNode request = node(tree, "request");
        assertEquals(request.getPath(), FlowContextPaths.REQUEST_PREFIX);
        assertEquals(request.getNodeType(), ContextTree.NODE_OBJECT);
        assertEquals(request.getAllowedOperations(), Collections.singletonList(ContextTree.OP_EXPOSE));
        assertTrue(request.isReadOnly());
        assertEquals(request.getApplicableFlowTypes(), ALL);
        assertEquals(request.getChildren().size(), 1);

        FlowExtensionContextTreeNode headers = request.getChildren().get(0);
        assertEquals(headers.getKey(), "additionalHeaders");
        assertEquals(headers.getPath(), FlowContextPaths.REQUEST_HEADERS_PATH);
        assertEquals(headers.getNodeType(), ContextTree.NODE_MAP);
        assertEquals(headers.getAllowedOperations(), Collections.singletonList(ContextTree.OP_EXPOSE));
        assertTrue(headers.isReadOnly());
        assertEquals(headers.getApplicableFlowTypes(), ALL);

        assertEquals(headers.getChildren().size(), 1);
        FlowExtensionContextTreeNode device = headers.getChildren().get(0);
        assertEquals(device.getPath(), "/request/additionalHeaders[name=x-device]");
        assertEquals(device.getDataType(), "STRING_ARRAY");
        assertEquals(device.getAllowedOperations(), Collections.singletonList(ContextTree.OP_EXPOSE));
        assertTrue(device.isReadOnly());
        assertEquals(device.getApplicableFlowTypes(), Arrays.asList(REGISTRATION, INVITED_USER_REGISTRATION));

        assertTrue(node(tree, "runtimeValues").getChildren().isEmpty());
    }

    @Test
    public void testExistingNodesApplyToAllFlows() throws Exception {

        List<FlowExtensionContextTreeNode> tree =
                buildWithFlowVariables(Collections.singletonList(new FlowTypeVariablesDTO(REGISTRATION)));

        for (String key : new String[]{"user", "tenant", "application", "organization", "flow"}) {
            assertAllFlows(node(tree, key));
        }
    }

    @Test
    public void testNoFlowVariableNodesWhenDisabled() throws Exception {

        FlowMgtService flowMgtService = mock(FlowMgtService.class);
        try (MockedStatic<FlowMgtUtils> flowMgtUtils = mockStatic(FlowMgtUtils.class);
             MockedStatic<FlowMgtService> flowMgtServiceStatic = mockStatic(FlowMgtService.class)) {
            flowMgtUtils.when(FlowMgtUtils::isFlowVariablesEnabled).thenReturn(false);
            flowMgtServiceStatic.when(FlowMgtService::getInstance).thenReturn(flowMgtService);

            List<FlowExtensionContextTreeNode> tree = new FlowExtensionContextTreeBuilder().build(null)
                    .getContextTree();

            assertFalse(hasNode(tree, "runtimeValues"));
            assertFalse(hasNode(tree, "request"));
            verify(flowMgtService, never()).getFlowVariables(anyInt());
        }
    }

    @Test
    public void testNoFlowVariableNodesWhenTheyCannotBeRead() throws Exception {

        FlowMgtService flowMgtService = mock(FlowMgtService.class);
        when(flowMgtService.getFlowVariables(TENANT_ID)).thenThrow(new FlowMgtServerException("error"));

        List<FlowExtensionContextTreeNode> tree = buildWithFlowVariables(flowMgtService);

        assertFalse(hasNode(tree, "runtimeValues"));
        assertFalse(hasNode(tree, "request"));
        assertTrue(hasNode(tree, "user"));
    }

    private List<FlowExtensionContextTreeNode> buildWithFlowVariables(List<FlowTypeVariablesDTO> flowVariables)
            throws Exception {

        FlowMgtService flowMgtService = mock(FlowMgtService.class);
        when(flowMgtService.getFlowVariables(TENANT_ID)).thenReturn(flowVariables);
        return buildWithFlowVariables(flowMgtService);
    }

    private List<FlowExtensionContextTreeNode> buildWithFlowVariables(FlowMgtService flowMgtService) {

        PrivilegedCarbonContext carbonContext = mock(PrivilegedCarbonContext.class);
        when(carbonContext.getTenantId()).thenReturn(TENANT_ID);
        try (MockedStatic<FlowMgtUtils> flowMgtUtils = mockStatic(FlowMgtUtils.class);
             MockedStatic<FlowMgtService> flowMgtServiceStatic = mockStatic(FlowMgtService.class);
             MockedStatic<PrivilegedCarbonContext> carbonContextStatic = mockStatic(PrivilegedCarbonContext.class)) {
            flowMgtUtils.when(FlowMgtUtils::isFlowVariablesEnabled).thenReturn(true);
            flowMgtServiceStatic.when(FlowMgtService::getInstance).thenReturn(flowMgtService);
            carbonContextStatic.when(PrivilegedCarbonContext::getThreadLocalCarbonContext).thenReturn(carbonContext);

            return new FlowExtensionContextTreeBuilder().build(REGISTRATION).getContextTree();
        }
    }

    private static FlowExtensionContextTreeNode node(List<FlowExtensionContextTreeNode> tree, String key) {

        return tree.stream().filter(n -> key.equals(n.getKey())).findFirst()
                .orElseThrow(() -> new AssertionError("No '" + key + "' node in the context tree."));
    }

    private static boolean hasNode(List<FlowExtensionContextTreeNode> tree, String key) {

        return tree.stream().anyMatch(n -> key.equals(n.getKey()));
    }

    private static void assertAllFlows(FlowExtensionContextTreeNode node) {

        assertEquals(node.getApplicableFlowTypes(), ALL, node.getPath() + " must apply to all flows.");
        for (FlowExtensionContextTreeNode child : node.getChildren()) {
            assertAllFlows(child);
        }
    }
}
