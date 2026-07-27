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

package org.wso2.carbon.identity.flow.extension.management;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverClientException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionProperty;
import org.wso2.carbon.identity.certificate.management.service.CertificateManagementService;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.flow.extension.model.ContextPath;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ActionManagement.ACCESS_CONFIG_MODIFY;
import static org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ActionManagement.NON_MODIFIABLE_PATHS_PROPERTY;

/**
 * Unit tests for {@link FlowExtensionActionDTOModelResolver}: enforcement of the server-configured
 * non-modifiable context paths on the add and update paths of the modify access config.
 */
public class FlowExtensionActionDTOModelResolverTest {

    private static final String TENANT = "carbon.super";
    private static final String USER_ID_PATH = "/user/claims[uri=http://wso2.org/claims/userid]";
    private static final String GIVEN_NAME_PATH = "/user/claims[uri=http://wso2.org/claims/givenname]";

    private FlowExtensionActionDTOModelResolver resolver;
    private MockedStatic<IdentityUtil> identityUtil;

    @BeforeMethod
    public void setUp() {

        resolver = new FlowExtensionActionDTOModelResolver(mock(CertificateManagementService.class));
        // The non-modifiable path list is read from identity.xml, which is unavailable in a plain
        // unit test; stub the lookup so each test can declare its own server configuration.
        identityUtil = mockStatic(IdentityUtil.class);
    }

    @AfterMethod
    public void tearDown() {

        identityUtil.close();
    }

    // ------------------------------------------------------------------ helpers

    private void withNonModifiablePaths(String... paths) {

        identityUtil.when(() -> IdentityUtil.getPropertyAsList(NON_MODIFIABLE_PATHS_PROPERTY))
                .thenReturn(Arrays.asList(paths));
    }

    private ActionDTO actionDTO(List<ContextPath> modify) {

        Map<String, ActionProperty> properties = new HashMap<>();
        if (modify != null) {
            properties.put(ACCESS_CONFIG_MODIFY, new ActionProperty.BuilderForService(modify).build());
        }
        return new ActionDTO.Builder(new Action.ActionResponseBuilder().name("ext").build())
                .properties(properties)
                .build();
    }

    private ActionDTO emptyActionDTO() {

        return actionDTO(null);
    }

    // ------------------------------------------------------------------ add

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testAddRejectsNonModifiablePath() throws Exception {

        withNonModifiablePaths(USER_ID_PATH);

        resolver.resolveForAddOperation(
                actionDTO(Collections.singletonList(new ContextPath(USER_ID_PATH, false))), TENANT);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testAddRejectsNonModifiablePathAmongAllowedOnes() throws Exception {

        withNonModifiablePaths(USER_ID_PATH);

        resolver.resolveForAddOperation(
                actionDTO(Arrays.asList(new ContextPath(GIVEN_NAME_PATH, false),
                        new ContextPath(USER_ID_PATH, true))), TENANT);
    }

    @Test
    public void testAddAcceptsPathOutsideTheNonModifiableList() throws Exception {

        withNonModifiablePaths(USER_ID_PATH);

        ActionDTO resolved = resolver.resolveForAddOperation(
                actionDTO(Collections.singletonList(new ContextPath(GIVEN_NAME_PATH, false))), TENANT);

        assertNotNull(resolved.getPropertyValue(ACCESS_CONFIG_MODIFY));
    }

    @Test
    public void testAddAcceptsAnyPathWhenNoRestrictionConfigured() throws Exception {

        withNonModifiablePaths();

        ActionDTO resolved = resolver.resolveForAddOperation(
                actionDTO(Collections.singletonList(new ContextPath(USER_ID_PATH, false))), TENANT);

        assertNotNull(resolved.getPropertyValue(ACCESS_CONFIG_MODIFY));
    }

    @Test
    public void testAddWithoutModifyConfigSkipsValidation() throws Exception {

        withNonModifiablePaths(USER_ID_PATH);

        ActionDTO resolved = resolver.resolveForAddOperation(emptyActionDTO(), TENANT);

        assertNull(resolved.getPropertyValue(ACCESS_CONFIG_MODIFY));
    }

    // ------------------------------------------------------------------ update

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testUpdateRejectsNonModifiablePath() throws Exception {

        withNonModifiablePaths(USER_ID_PATH);

        resolver.resolveForUpdateOperation(
                actionDTO(Collections.singletonList(new ContextPath(USER_ID_PATH, false))),
                emptyActionDTO(), TENANT);
    }

    @Test
    public void testUpdateAcceptsPathOutsideTheNonModifiableList() throws Exception {

        withNonModifiablePaths(USER_ID_PATH);

        ActionDTO resolved = resolver.resolveForUpdateOperation(
                actionDTO(Collections.singletonList(new ContextPath(GIVEN_NAME_PATH, false))),
                emptyActionDTO(), TENANT);

        assertNotNull(resolved.getPropertyValue(ACCESS_CONFIG_MODIFY));
    }

    @Test
    public void testUpdateCarriesForwardExistingModifyWithoutRevalidation() throws Exception {

        // Untouched modify config is re-sent as-is (the DAO treats updates as PUT), so a value
        // already persisted before the restriction was configured must not fail the update.
        withNonModifiablePaths(USER_ID_PATH);

        ActionDTO resolved = resolver.resolveForUpdateOperation(
                emptyActionDTO(),
                actionDTO(Collections.singletonList(new ContextPath(USER_ID_PATH, false))),
                TENANT);

        assertNotNull(resolved.getPropertyValue(ACCESS_CONFIG_MODIFY));
    }

    @Test
    public void testUpdateWithNoModifyConfigOnEitherSideSkipsValidation() throws Exception {

        withNonModifiablePaths(USER_ID_PATH);

        ActionDTO resolved = resolver.resolveForUpdateOperation(emptyActionDTO(), emptyActionDTO(), TENANT);

        assertNull(resolved.getPropertyValue(ACCESS_CONFIG_MODIFY));
    }
}
