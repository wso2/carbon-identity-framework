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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverClientException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionProperty;
import org.wso2.carbon.identity.action.management.api.model.BinaryObject;
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
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ActionManagement.ACCESS_CONFIG_EXPOSE;
import static org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ActionManagement.ACCESS_CONFIG_MODIFY;
import static org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ActionManagement.NON_MODIFIABLE_PATHS;

/**
 * Unit tests for {@link FlowExtensionActionDTOModelResolver}: enforcement of the server-configured
 * non-modifiable context paths on the add and update paths of the modify access config.
 */
public class FlowExtensionActionDTOModelResolverTest {

    private static final String TENANT = "carbon.super";
    private static final String USER_ID_PATH = "/user/claims[uri=http://wso2.org/claims/userid]";
    private static final String GIVEN_NAME_PATH = "/user/claims[uri=http://wso2.org/claims/givenname]";
    private static final String PLAN_PATH = "/runtimeValues[name=plan]";
    private static final String DEVICE_HEADER_PATH = "/request/additionalHeaders[name=x-device]";
    private static final String AB_TESTING_KEY_PATH = "/runtimeValues[name=abTestingKey]";
    private static final String REGISTRATION = "REGISTRATION";
    private static final String PASSWORD_RECOVERY = "PASSWORD_RECOVERY";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

        identityUtil.when(() -> IdentityUtil.getPropertyAsList(NON_MODIFIABLE_PATHS))
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

    private ActionDTO accessConfigDTO(List<?> expose, List<?> modify) {

        Map<String, ActionProperty> properties = new HashMap<>();
        if (expose != null) {
            properties.put(ACCESS_CONFIG_EXPOSE, new ActionProperty.BuilderForService(expose).build());
        }
        if (modify != null) {
            properties.put(ACCESS_CONFIG_MODIFY, new ActionProperty.BuilderForService(modify).build());
        }
        return new ActionDTO.Builder(new Action.ActionResponseBuilder().name("ext").build())
                .properties(properties)
                .build();
    }

    private ActionDTO exposeDTO(ContextPath... expose) {

        return accessConfigDTO(Arrays.asList(expose), null);
    }

    private static List<ContextPath> stored(ActionDTO resolved, String accessConfig) throws Exception {

        String json = ((BinaryObject) resolved.getPropertyValue(accessConfig)).getJSONString();
        return OBJECT_MAPPER.readValue(json, new TypeReference<List<ContextPath>>() { });
    }

    private ActionDTO emptyActionDTO() {

        return actionDTO(null);
    }

    // ------------------------------------------------------------------ stored before flow types

    /**
     * Access config stored before entries had a flow type is read with no flow type, and is written back without
     * one.
     */
    @Test
    public void testGetReadsEntriesStoredWithoutFlowType() throws Exception {

        Map<String, ActionProperty> properties = new HashMap<>();
        properties.put(ACCESS_CONFIG_EXPOSE, new ActionProperty.BuilderForDAO(BinaryObject.fromJsonString(
                "[{\"path\":\"" + USER_ID_PATH + "\",\"encrypted\":false}]")).build());
        properties.put(ACCESS_CONFIG_MODIFY, new ActionProperty.BuilderForDAO(BinaryObject.fromJsonString(
                "[{\"path\":\"/user/claims/\",\"encrypted\":true}]")).build());
        ActionDTO storedDTO = new ActionDTO.Builder(new Action.ActionResponseBuilder().name("ext").build())
                .properties(properties)
                .build();

        ActionDTO resolved = resolver.resolveForGetOperation(storedDTO, TENANT);

        @SuppressWarnings("unchecked")
        List<ContextPath> expose = (List<ContextPath>) resolved.getPropertyValue(ACCESS_CONFIG_EXPOSE);
        assertEquals(expose.size(), 1);
        assertEquals(expose.get(0).getPath(), USER_ID_PATH);
        assertFalse(expose.get(0).isEncrypted());
        assertNull(expose.get(0).getFlowType());
        @SuppressWarnings("unchecked")
        List<ContextPath> modify = (List<ContextPath>) resolved.getPropertyValue(ACCESS_CONFIG_MODIFY);
        assertEquals(modify.get(0).getPath(), "/user/claims/");
        assertTrue(modify.get(0).isEncrypted());
        assertNull(modify.get(0).getFlowType());
        assertFalse(OBJECT_MAPPER.writeValueAsString(expose).contains("flowType"));

        // An update that keeps the stored entries writes them back as they were.
        withNonModifiablePaths();
        ActionDTO updated = resolver.resolveForUpdateOperation(emptyActionDTO(), resolved, TENANT);

        assertEquals(stored(updated, ACCESS_CONFIG_EXPOSE).get(0).getPath(), USER_ID_PATH);
        assertNull(stored(updated, ACCESS_CONFIG_EXPOSE).get(0).getFlowType());
        assertFalse(((BinaryObject) updated.getPropertyValue(ACCESS_CONFIG_MODIFY)).getJSONString()
                .contains("flowType"));
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

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testAddRejectsNonModifiablePathCarryingTypeAnnotation() throws Exception {

        // The path type annotation is stripped before the path is advertised as modifiable, so it
        // must not be usable to slip a restricted path past validation.
        withNonModifiablePaths(USER_ID_PATH);

        resolver.resolveForAddOperation(
                actionDTO(Collections.singletonList(new ContextPath(USER_ID_PATH + "{[string]}", false))), TENANT);
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

    // ------------------------------------------------------------------ flow-scoped paths

    @Test
    public void testAddAcceptsRuntimeValueAndHeaderEntriesForAFlowType() throws Exception {

        withNonModifiablePaths();

        ActionDTO resolved = resolver.resolveForAddOperation(accessConfigDTO(
                Arrays.asList(new ContextPath("/user/id", false),
                        new ContextPath(PLAN_PATH, false, REGISTRATION),
                        new ContextPath(PLAN_PATH, true, PASSWORD_RECOVERY),
                        new ContextPath(AB_TESTING_KEY_PATH, false, "INVITED_USER_REGISTRATION"),
                        new ContextPath(DEVICE_HEADER_PATH, true, REGISTRATION)),
                Collections.singletonList(new ContextPath(PLAN_PATH, false, REGISTRATION))), TENANT);

        List<ContextPath> expose = stored(resolved, ACCESS_CONFIG_EXPOSE);
        assertEquals(expose.size(), 5);
        assertNull(expose.get(0).getFlowType());
        assertEquals(expose.get(1).getFlowType(), REGISTRATION);
        assertEquals(expose.get(2).getFlowType(), PASSWORD_RECOVERY);
        assertEquals(expose.get(4).getPath(), DEVICE_HEADER_PATH);
        assertEquals(expose.get(4).getFlowType(), REGISTRATION);
        assertEquals(expose.get(4).isEncrypted(), true);

        List<ContextPath> modify = stored(resolved, ACCESS_CONFIG_MODIFY);
        assertEquals(modify.size(), 1);
        assertEquals(modify.get(0).getPath(), PLAN_PATH);
        assertEquals(modify.get(0).getFlowType(), REGISTRATION);
    }

    /**
     * Entries may come as the JSON objects the API reads, with the flow type as a field.
     */
    @Test
    public void testAddAcceptsFlowTypeFromAMapEntry() throws Exception {

        withNonModifiablePaths();
        Map<String, Object> entry = new HashMap<>();
        entry.put("path", PLAN_PATH);
        entry.put("encrypted", "true");
        entry.put("flowType", REGISTRATION);

        ActionDTO resolved = resolver.resolveForAddOperation(
                accessConfigDTO(Collections.singletonList(entry), null), TENANT);

        List<ContextPath> expose = stored(resolved, ACCESS_CONFIG_EXPOSE);
        assertEquals(expose.get(0).getPath(), PLAN_PATH);
        assertEquals(expose.get(0).isEncrypted(), true);
        assertEquals(expose.get(0).getFlowType(), REGISTRATION);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testAddRejectsANonTextFlowTypeInAMapEntry() throws Exception {

        withNonModifiablePaths();
        Map<String, Object> entry = new HashMap<>();
        entry.put("path", PLAN_PATH);
        entry.put("flowType", 1);

        resolver.resolveForAddOperation(accessConfigDTO(Collections.singletonList(entry), null), TENANT);
    }

    @Test
    public void testHeaderNameIsKeptInLowerCase() throws Exception {

        withNonModifiablePaths();

        ActionDTO resolved = resolver.resolveForAddOperation(
                exposeDTO(new ContextPath("/request/additionalHeaders[name=X-Device]", false, REGISTRATION)),
                TENANT);

        assertEquals(stored(resolved, ACCESS_CONFIG_EXPOSE).get(0).getPath(), DEVICE_HEADER_PATH);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testHeaderNamesDifferingOnlyInCaseAreDuplicates() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForAddOperation(
                exposeDTO(new ContextPath("/request/additionalHeaders[name=X-Device]", false, REGISTRATION),
                        new ContextPath(DEVICE_HEADER_PATH, true, REGISTRATION)), TENANT);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testRejectsDuplicateEntryForTheSameFlowType() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForAddOperation(exposeDTO(new ContextPath(PLAN_PATH, false, REGISTRATION),
                new ContextPath(PLAN_PATH, true, REGISTRATION)), TENANT);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testRejectsRuntimeValueWithoutFlowType() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForAddOperation(exposeDTO(new ContextPath(PLAN_PATH, false)), TENANT);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testRejectsHeaderWithoutFlowType() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForAddOperation(exposeDTO(new ContextPath(DEVICE_HEADER_PATH, false)), TENANT);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testRejectsUnknownFlowType() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForAddOperation(exposeDTO(new ContextPath(PLAN_PATH, false, "LOGIN")), TENANT);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testRejectsFlowTypeInADifferentCase() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForAddOperation(exposeDTO(new ContextPath(PLAN_PATH, false, "registration")), TENANT);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testRejectsFlowTypeOnAPathForAllFlows() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForAddOperation(exposeDTO(new ContextPath("/user/id", false, REGISTRATION)), TENANT);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testRejectsFlowTypeOnAModifyPathForAllFlows() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForAddOperation(accessConfigDTO(null,
                Collections.singletonList(new ContextPath(GIVEN_NAME_PATH, false, REGISTRATION))), TENANT);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testRejectsWholeRuntimeValuesPath() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForAddOperation(exposeDTO(new ContextPath("/runtimeValues", false, REGISTRATION)), TENANT);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testRejectsWholeRequestHeadersPath() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForAddOperation(
                exposeDTO(new ContextPath("/request/additionalHeaders", false, REGISTRATION)), TENANT);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testRejectsOtherRequestPath() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForAddOperation(exposeDTO(new ContextPath("/request/body", false)), TENANT);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testRejectsMalformedRuntimeValueSelector() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForAddOperation(
                exposeDTO(new ContextPath("/runtimeValues[name=plan", false, REGISTRATION)), TENANT);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testRejectsRuntimeValueSelectorWithAPathAfterIt() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForAddOperation(
                exposeDTO(new ContextPath("/runtimeValues[name=plan]/x", false, REGISTRATION)), TENANT);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testRejectsInvalidRuntimeValueName() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForAddOperation(
                exposeDTO(new ContextPath("/runtimeValues[name=my-plan]", false, REGISTRATION)), TENANT);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testRejectsInvalidHeaderName() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForAddOperation(
                exposeDTO(new ContextPath("/request/additionalHeaders[name=-device]", false, REGISTRATION)),
                TENANT);
    }

    @Test
    public void testAcceptsSubjectOrganizationInExpose() throws Exception {

        withNonModifiablePaths();

        ActionDTO resolved = resolver.resolveForAddOperation(exposeDTO(
                new ContextPath("/subjectOrganization/id", false, REGISTRATION),
                new ContextPath("/subjectOrganization/attributes[name=industry]", true, REGISTRATION)), TENANT);

        List<ContextPath> expose = stored(resolved, ACCESS_CONFIG_EXPOSE);
        assertEquals(expose.size(), 2);
        assertEquals(expose.get(1).getPath(), "/subjectOrganization/attributes[name=industry]");
        assertTrue(expose.get(1).isEncrypted());
        assertEquals(expose.get(1).getFlowType(), REGISTRATION);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testRejectsSubjectOrganizationInModify() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForAddOperation(accessConfigDTO(null,
                Collections.singletonList(new ContextPath("/subjectOrganization/name", false, REGISTRATION))), TENANT);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testRejectsSubjectOrganizationAttributePathWithoutName() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForAddOperation(exposeDTO(
                new ContextPath("/subjectOrganization/attributes[name=]", false, REGISTRATION)), TENANT);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testRejectsSubjectOrganizationWithoutFlowType() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForAddOperation(exposeDTO(new ContextPath("/subjectOrganization/name", false)), TENANT);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testRejectsSubjectOrganizationForAnotherFlowType() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForAddOperation(exposeDTO(
                new ContextPath("/subjectOrganization/name", false, PASSWORD_RECOVERY)), TENANT);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testRejectsDuplicateSubjectOrganizationEntry() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForAddOperation(exposeDTO(
                new ContextPath("/subjectOrganization/name", false, REGISTRATION),
                new ContextPath("/subjectOrganization/name", true, REGISTRATION)), TENANT);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testRejectsHeaderInModify() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForAddOperation(accessConfigDTO(null,
                Collections.singletonList(new ContextPath(DEVICE_HEADER_PATH, false, REGISTRATION))), TENANT);
    }

    @Test
    public void testAcceptsAbTestingKeyInModify() throws Exception {

        withNonModifiablePaths();

        ActionDTO resolved = resolver.resolveForAddOperation(accessConfigDTO(null,
                Collections.singletonList(new ContextPath(AB_TESTING_KEY_PATH, false, REGISTRATION))), TENANT);

        assertNotNull(resolved);
    }

    @Test(expectedExceptions = ActionDTOModelResolverClientException.class)
    public void testUpdateRejectsRuntimeValueWithoutFlowType() throws Exception {

        withNonModifiablePaths();

        resolver.resolveForUpdateOperation(exposeDTO(new ContextPath(PLAN_PATH, false)), emptyActionDTO(), TENANT);
    }

    @Test
    public void testUpdateLowerCasesHeaderName() throws Exception {

        withNonModifiablePaths();

        ActionDTO resolved = resolver.resolveForUpdateOperation(
                exposeDTO(new ContextPath("/request/additionalHeaders[name=X-DEVICE]", false, REGISTRATION)),
                emptyActionDTO(), TENANT);

        assertEquals(stored(resolved, ACCESS_CONFIG_EXPOSE).get(0).getPath(), DEVICE_HEADER_PATH);
    }
}
