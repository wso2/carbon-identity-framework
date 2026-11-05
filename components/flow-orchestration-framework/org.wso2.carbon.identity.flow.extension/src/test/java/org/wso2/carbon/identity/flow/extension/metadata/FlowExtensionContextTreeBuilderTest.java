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

        List<FlowExtensionContextTreeNode> tree = buildWithFlowVariables(mock(FlowMgtService.class));
        FlowExtensionContextTreeNode userNode = tree.stream()
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
     * A name two flows give different types is listed once per type, with keys that name the type, and one two flows
     * give the same type is listed once, naming both flows.
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
        assertEquals(integerAttempts.getKey(), "attempts#INTEGER");
        assertEquals(integerAttempts.getPath(), "/runtimeValues[name=attempts]");
        assertEquals(integerAttempts.getDataType(), "INTEGER");
        assertEquals(integerAttempts.getApplicableFlowTypes(), Collections.singletonList(REGISTRATION));
        FlowExtensionContextTreeNode stringAttempts = leaves.get(2);
        assertEquals(stringAttempts.getKey(), "attempts#STRING");
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
    public void testNoFlowVariableNodesWhenTheyCannotBeRead() throws Exception {

        FlowMgtService flowMgtService = mock(FlowMgtService.class);
        when(flowMgtService.getFlowVariables(TENANT_ID, null)).thenThrow(new FlowMgtServerException("error"));

        List<FlowExtensionContextTreeNode> tree = buildWithFlowVariables(flowMgtService);

        assertFalse(hasNode(tree, "runtimeValues"));
        assertFalse(hasNode(tree, "request"));
        assertTrue(hasNode(tree, "user"));
    }

    /**
     * The tree of a flow type lists only the runtime values and headers of that flow type.
     */
    @Test
    public void testFlowVariableLeavesOfOneFlowType() throws Exception {

        FlowTypeVariablesDTO recovery = new FlowTypeVariablesDTO(PASSWORD_RECOVERY);
        recovery.getRuntimeValues().add(new FlowRuntimeValueDTO("plan", FlowVariableDataType.STRING));
        FlowMgtService flowMgtService = mock(FlowMgtService.class);
        when(flowMgtService.getFlowVariables(TENANT_ID, PASSWORD_RECOVERY))
                .thenReturn(Collections.singletonList(recovery));

        List<FlowExtensionContextTreeNode> tree = build(flowMgtService, PASSWORD_RECOVERY);

        List<FlowExtensionContextTreeNode> leaves = node(tree, "runtimeValues").getChildren();
        assertEquals(leaves.size(), 1);
        assertEquals(leaves.get(0).getKey(), "plan");
        assertEquals(leaves.get(0).getApplicableFlowTypes(), Collections.singletonList(PASSWORD_RECOVERY));
    }

    // ------------------------------------------------------------------ subject organization

    /**
     * Only the registration flow onboards an organization, so the tree of another flow type leaves it out and does
     * not read the registration flow.
     */
    @Test
    public void testNoSubjectOrganizationNodeForOtherFlowTypes() throws Exception {

        FlowMgtService flowMgtService = mock(FlowMgtService.class);

        List<FlowExtensionContextTreeNode> tree = build(flowMgtService, PASSWORD_RECOVERY);

        assertFalse(hasNode(tree, "subjectOrganization"));
        verify(flowMgtService, never()).getOrganizationAttributes(anyInt());
        assertTrue(hasNode(build(flowMgtService, REGISTRATION), "subjectOrganization"));
    }

    /**
     * The organization a registration flow onboards applies to registration only, and its fields, including the
     * attributes the registration flow collects, can only be exposed.
     */
    @Test
    public void testSubjectOrganizationNodeIsExposeOnlyForRegistration() throws Exception {

        FlowMgtService flowMgtService = mock(FlowMgtService.class);
        // A name with the selector's closing bracket cannot be addressed by a path, so it is left out.
        when(flowMgtService.getOrganizationAttributes(TENANT_ID))
                .thenReturn(Arrays.asList("industry", "department[code]", "country"));

        FlowExtensionContextTreeNode subjectOrganization = node(buildWithFlowVariables(flowMgtService),
                "subjectOrganization");

        assertEquals(subjectOrganization.getPath(), FlowContextPaths.SUBJECT_ORGANIZATION_PREFIX);
        Map<String, FlowExtensionContextTreeNode> children = subjectOrganization.getChildren().stream()
                .collect(Collectors.toMap(FlowExtensionContextTreeNode::getKey, n -> n));
        assertEquals(children.keySet(), new java.util.HashSet<>(Arrays.asList("id", "name", "handle", "description",
                "attributes")));
        assertEquals(children.get("id").getPath(), FlowContextPaths.SUBJECT_ORGANIZATION_ID_PATH);
        assertEquals(children.get("name").getPath(), FlowContextPaths.SUBJECT_ORGANIZATION_NAME_PATH);
        assertEquals(children.get("handle").getPath(), FlowContextPaths.SUBJECT_ORGANIZATION_HANDLE_PATH);
        assertEquals(children.get("description").getPath(), FlowContextPaths.SUBJECT_ORGANIZATION_DESCRIPTION_PATH);

        FlowExtensionContextTreeNode attributes = children.get("attributes");
        assertEquals(attributes.getNodeType(), ContextTree.NODE_MAP);
        assertFalse(attributes.isDynamicEntryAllowed());
        assertEquals(attributes.getChildren().size(), 2);
        assertEquals(attributes.getChildren().get(0).getKey(), "industry");
        assertEquals(attributes.getChildren().get(0).getPath(), "/subjectOrganization/attributes[name=industry]");
        assertEquals(attributes.getChildren().get(1).getPath(), "/subjectOrganization/attributes[name=country]");

        assertExposeOnlyForRegistration(subjectOrganization);
    }

    @Test
    public void testSubjectOrganizationHasNoAttributesWhenTheyCannotBeRead() throws Exception {

        FlowMgtService flowMgtService = mock(FlowMgtService.class);
        when(flowMgtService.getOrganizationAttributes(TENANT_ID)).thenThrow(new FlowMgtServerException("error"));

        FlowExtensionContextTreeNode subjectOrganization = node(buildWithFlowVariables(flowMgtService),
                "subjectOrganization");

        FlowExtensionContextTreeNode attributes = subjectOrganization.getChildren().stream()
                .filter(n -> "attributes".equals(n.getKey())).findFirst().orElseThrow(AssertionError::new);
        assertTrue(attributes.getChildren().isEmpty());
        assertEquals(subjectOrganization.getChildren().size(), 5);
    }

    private static void assertExposeOnlyForRegistration(FlowExtensionContextTreeNode node) {

        assertEquals(node.getApplicableFlowTypes(), Collections.singletonList(REGISTRATION),
                node.getPath() + " must apply to registration only.");
        assertEquals(node.getAllowedOperations(), Collections.singletonList(ContextTree.OP_EXPOSE),
                node.getPath() + " must allow EXPOSE only.");
        assertTrue(node.isReadOnly(), node.getPath() + " must be read-only.");
        for (FlowExtensionContextTreeNode child : node.getChildren()) {
            assertExposeOnlyForRegistration(child);
        }
    }

    private List<FlowExtensionContextTreeNode> buildWithFlowVariables(List<FlowTypeVariablesDTO> flowVariables)
            throws Exception {

        FlowMgtService flowMgtService = mock(FlowMgtService.class);
        when(flowMgtService.getFlowVariables(TENANT_ID, null)).thenReturn(flowVariables);
        return buildWithFlowVariables(flowMgtService);
    }

    private List<FlowExtensionContextTreeNode> buildWithFlowVariables(FlowMgtService flowMgtService) {

        return build(flowMgtService, null);
    }

    private List<FlowExtensionContextTreeNode> build(FlowMgtService flowMgtService, String flowType) {

        PrivilegedCarbonContext carbonContext = mock(PrivilegedCarbonContext.class);
        when(carbonContext.getTenantId()).thenReturn(TENANT_ID);
        try (MockedStatic<FlowMgtService> flowMgtServiceStatic = mockStatic(FlowMgtService.class);
             MockedStatic<PrivilegedCarbonContext> carbonContextStatic = mockStatic(PrivilegedCarbonContext.class)) {
            flowMgtServiceStatic.when(FlowMgtService::getInstance).thenReturn(flowMgtService);
            carbonContextStatic.when(PrivilegedCarbonContext::getThreadLocalCarbonContext).thenReturn(carbonContext);

            return new FlowExtensionContextTreeBuilder().build(flowType).getContextTree();
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
