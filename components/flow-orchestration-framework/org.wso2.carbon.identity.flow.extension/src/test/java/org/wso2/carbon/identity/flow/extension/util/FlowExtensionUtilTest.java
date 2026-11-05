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

package org.wso2.carbon.identity.flow.extension.util;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverClientException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.flow.extension.model.ContextPath;
import org.wso2.carbon.identity.flow.mgt.model.FlowRuntimeValueDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDataType;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariablesDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ActionManagement.NON_MODIFIABLE_PATHS;

/**
 * Unit tests for the non-modifiable context path helpers in {@link FlowExtensionUtil}, shared by
 * the management layer ({@link FlowExtensionUtil#validateModifyPaths}) and the runtime response
 * processor ({@link FlowExtensionUtil#isNonModifiablePath}).
 */
public class FlowExtensionUtilTest {

    private static final String USER_ID_PATH = "/user/claims[uri=http://wso2.org/claims/userid]";
    private static final String GIVEN_NAME_PATH = "/user/claims[uri=http://wso2.org/claims/givenname]";

    private MockedStatic<IdentityUtil> identityUtil;

    @BeforeMethod
    public void setUp() {

        // The non-modifiable path list is read from identity.xml, which is unavailable in a plain
        // unit test; stub the lookup so each test can declare its own server configuration.
        identityUtil = mockStatic(IdentityUtil.class);
    }

    @AfterMethod
    public void tearDown() {

        identityUtil.close();
    }

    private void withNonModifiablePaths(String... paths) {

        identityUtil.when(() -> IdentityUtil.getPropertyAsList(NON_MODIFIABLE_PATHS))
                .thenReturn(Arrays.asList(paths));
    }

    // ------------------------------------------------------------------ validateModifyPaths

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testValidateRejectsConfiguredPath() throws Exception {

        withNonModifiablePaths(USER_ID_PATH);

        FlowExtensionUtil.validateModifyPaths(
                Collections.singletonList(new ContextPath(USER_ID_PATH, false)));
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testValidateRejectsConfiguredPathCarryingTypeAnnotation() throws Exception {

        // Modify paths may carry a path type annotation (stripped later by the request builder),
        // but restrictions are configured against clean paths — the annotation must not defeat them.
        withNonModifiablePaths(USER_ID_PATH);

        FlowExtensionUtil.validateModifyPaths(
                Collections.singletonList(new ContextPath(USER_ID_PATH + "{[string]}", false)));
    }

    @Test
    public void testValidateAcceptsUnlistedPath() throws Exception {

        withNonModifiablePaths(USER_ID_PATH);

        FlowExtensionUtil.validateModifyPaths(
                Collections.singletonList(new ContextPath(GIVEN_NAME_PATH, false)));
    }

    @Test
    public void testValidateSkippedWhenNoPathsConfigured() throws Exception {

        withNonModifiablePaths();

        FlowExtensionUtil.validateModifyPaths(
                Collections.singletonList(new ContextPath(USER_ID_PATH, false)));
    }

    @Test
    public void testValidateAcceptsAnnotatedUnlistedPath() throws Exception {

        withNonModifiablePaths(USER_ID_PATH);

        FlowExtensionUtil.validateModifyPaths(
                Collections.singletonList(new ContextPath(GIVEN_NAME_PATH + "{[string]}", false)));
    }

    @Test
    public void testValidateToleratesNullEntries() throws Exception {

        withNonModifiablePaths(USER_ID_PATH);

        FlowExtensionUtil.validateModifyPaths(
                Arrays.asList(null, new ContextPath(null, false), new ContextPath(GIVEN_NAME_PATH, false)));
    }

    @Test
    public void testValidateAcceptsEmptyModifyList() throws Exception {

        withNonModifiablePaths(USER_ID_PATH);

        FlowExtensionUtil.validateModifyPaths(Collections.emptyList());
    }

    // ------------------------------------------------------------------ isNonModifiablePath

    @Test
    public void testIsNonModifiablePathMatchesConfiguredPath() {

        withNonModifiablePaths(USER_ID_PATH, GIVEN_NAME_PATH);

        assertTrue(FlowExtensionUtil.isNonModifiablePath(USER_ID_PATH));
        assertTrue(FlowExtensionUtil.isNonModifiablePath(GIVEN_NAME_PATH));
    }

    @Test
    public void testIsNonModifiablePathRejectsUnlistedPath() {

        withNonModifiablePaths(USER_ID_PATH);

        assertFalse(FlowExtensionUtil.isNonModifiablePath(GIVEN_NAME_PATH));
    }

    @Test
    public void testIsNonModifiablePathWhenNothingConfigured() {

        withNonModifiablePaths();

        assertFalse(FlowExtensionUtil.isNonModifiablePath(USER_ID_PATH));
    }

    @Test
    public void testIsNonModifiablePathIsExactMatch() {

        // Matching is exact: a prefix of a configured path must not be treated as non-modifiable.
        withNonModifiablePaths(USER_ID_PATH);

        assertFalse(FlowExtensionUtil.isNonModifiablePath("/user/claims"));
        assertFalse(FlowExtensionUtil.isNonModifiablePath(USER_ID_PATH + "{[string]}"));
    }

    // ------------------------------------------------------------------ runtime value and header selectors

    @Test
    public void testRuntimeValueSelector() {

        assertTrue(FlowExtensionUtil.isRuntimeValuePath("/runtimeValues[name=plan]"));
        assertFalse(FlowExtensionUtil.isRuntimeValuePath("/runtimeValues"));
        assertFalse(FlowExtensionUtil.isRuntimeValuePath("/request/additionalHeaders[name=x-device]"));
        assertFalse(FlowExtensionUtil.isRuntimeValuePath(null));

        assertEquals(FlowExtensionUtil.getRuntimeValueName("/runtimeValues[name=plan]"), "plan");
        assertNull(FlowExtensionUtil.getRuntimeValueName("/runtimeValues[name=]"));
        assertNull(FlowExtensionUtil.getRuntimeValueName("/runtimeValues[name=plan"));
        assertNull(FlowExtensionUtil.getRuntimeValueName("/runtimeValues[name=plan]]"));
        assertNull(FlowExtensionUtil.getRuntimeValueName("/runtimeValues[name=a]/b]"));
        assertNull(FlowExtensionUtil.getRuntimeValueName("/request/additionalHeaders[name=plan]"));
        assertNull(FlowExtensionUtil.getRuntimeValueName(null));
    }

    @Test
    public void testRequestHeaderSelector() {

        assertTrue(FlowExtensionUtil.isRequestHeaderPath("/request/additionalHeaders[name=x-device]"));
        assertFalse(FlowExtensionUtil.isRequestHeaderPath("/request/additionalHeaders"));
        assertFalse(FlowExtensionUtil.isRequestHeaderPath("/runtimeValues[name=plan]"));
        assertFalse(FlowExtensionUtil.isRequestHeaderPath(null));

        assertEquals(FlowExtensionUtil.getRequestHeaderName("/request/additionalHeaders[name=X-Device]"),
                "X-Device");
        assertNull(FlowExtensionUtil.getRequestHeaderName("/request/additionalHeaders[name=]"));
        assertNull(FlowExtensionUtil.getRequestHeaderName("/request/additionalHeaders[name=x-device"));
        assertNull(FlowExtensionUtil.getRequestHeaderName("/runtimeValues[name=plan]"));
    }

    // ------------------------------------------------------------------ filterContext

    /**
     * The copy handed to an extension carries the runtime values, the request headers and the definitions of the
     * flow's variables, but not its steps. The runtime values and request headers are copied, so changing them in the
     * copy leaves the original as it was. The definitions are only read, so they are shared rather than copied.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testFilterContextCopiesFlowVariables() {

        FlowVariablesDTO variables = new FlowVariablesDTO();
        variables.getRuntimeValues().add(new FlowRuntimeValueDTO("plan", FlowVariableDataType.STRING));
        variables.getHeaders().add(new FlowVariableDTO("x-device", FlowVariableDataType.STRING_ARRAY));
        GraphConfig graphConfig = new GraphConfig();
        graphConfig.setFlowVariables(variables);
        graphConfig.setFirstNodeId("first");
        graphConfig.getNodeConfigs().put("first", new NodeConfig.Builder().id("first").type("VIEW").build());

        FlowExecutionContext original = contextWithUser();
        original.setFlowType("REGISTRATION");
        original.setGraphConfig(graphConfig);
        original.getRuntimeValues().put("plan", "gold");
        List<String> device = new ArrayList<>(Collections.singletonList("mobile"));
        original.getRequest().getHeaders().put("x-device", device);

        FlowExecutionContext copy = FlowExtensionUtil.filterContext(original);

        assertEquals(copy.getRuntimeValues(), Collections.singletonMap("plan", "gold"));
        assertEquals(copy.getRequest().getHeaders().get("x-device"), Collections.singletonList("mobile"));
        assertNotSame(copy.getGraphConfig(), graphConfig);
        assertSame(copy.getGraphConfig().getFlowVariables(), variables);
        assertNull(copy.getGraphConfig().getFirstNodeId());
        assertTrue(copy.getGraphConfig().getNodeConfigs().isEmpty());

        copy.getRuntimeValues().put("plan", "basic");
        ((List<Object>) copy.getRequest().getHeaders().get("x-device")).add("tablet");
        assertEquals(original.getRuntimeValues().get("plan"), "gold");
        assertEquals(device, Collections.singletonList("mobile"));
    }

    /**
     * The copy carries the organization the flow onboards, with the same ID, and changing the copy leaves the
     * original as it was.
     */
    @Test
    public void testFilterContextCopiesTheFlowOrganization() {

        FlowExecutionContext original = contextWithUser();
        original.getFlowOrganization().setOrganizationName("Acme");
        original.getFlowOrganization().setAttribute("industry", "Retail");

        FlowExecutionContext copy = FlowExtensionUtil.filterContext(original);

        assertNotSame(copy.getFlowOrganization(), original.getFlowOrganization());
        assertEquals(copy.getFlowOrganization().getOrganizationId(),
                original.getFlowOrganization().getOrganizationId());
        assertEquals(copy.getFlowOrganization().getOrganizationName(), "Acme");
        assertEquals(copy.getFlowOrganization().getAttribute("industry"), "Retail");

        copy.getFlowOrganization().setAttribute("industry", "Banking");
        assertEquals(original.getFlowOrganization().getAttribute("industry"), "Retail");
    }

    @Test
    public void testSubjectOrganizationAttributeSelector() {

        assertTrue(FlowExtensionUtil.isSubjectOrganizationAttributePath(
                "/subjectOrganization/attributes[name=industry]"));
        assertFalse(FlowExtensionUtil.isSubjectOrganizationAttributePath("/subjectOrganization/name"));
        assertFalse(FlowExtensionUtil.isSubjectOrganizationAttributePath(null));

        assertEquals(FlowExtensionUtil.getSubjectOrganizationAttributeName(
                "/subjectOrganization/attributes[name=industry]"), "industry");
        assertNull(FlowExtensionUtil.getSubjectOrganizationAttributeName("/subjectOrganization/attributes[name=]"));
        assertNull(FlowExtensionUtil.getSubjectOrganizationAttributeName("/subjectOrganization/attributes"));
        assertNull(FlowExtensionUtil.getSubjectOrganizationAttributeName("/runtimeValues[name=industry]"));
    }

    @Test
    public void testFilterContextWithoutGraphConfig() {

        FlowExecutionContext original = contextWithUser();

        FlowExecutionContext copy = FlowExtensionUtil.filterContext(original);

        assertNull(copy.getGraphConfig());
        assertTrue(copy.getRuntimeValues().isEmpty());
        assertTrue(copy.getRequest().getHeaders().isEmpty());
    }

    private static FlowExecutionContext contextWithUser() {

        // A user with a username, so that copying the user does not resolve one from the tenant's settings.
        FlowUser user = new FlowUser();
        user.setUsername("alice");
        FlowExecutionContext context = new FlowExecutionContext();
        context.setFlowUser(user);
        return context;
    }
}
