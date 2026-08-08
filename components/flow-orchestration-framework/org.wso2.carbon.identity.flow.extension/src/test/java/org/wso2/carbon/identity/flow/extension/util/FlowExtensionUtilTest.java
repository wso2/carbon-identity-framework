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
import org.wso2.carbon.identity.flow.extension.model.ContextPath;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ActionManagement.NON_MODIFIABLE_PATHS_PROPERTY;

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

        identityUtil.when(() -> IdentityUtil.getPropertyAsList(NON_MODIFIABLE_PATHS_PROPERTY))
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
}
