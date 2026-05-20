/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposePIICategory;
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;

import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link PolicyConsentUtil}.
 */
public class PolicyConsentUtilTest {

    private static final String SUBJECT_ID = "testUser";
    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String PURPOSE_UUID_1 = "purpose-uuid-1";
    private static final String PURPOSE_UUID_2 = "purpose-uuid-2";
    private static final String VERSION_UUID_1 = "version-uuid-1";
    private static final String VERSION_UUID_2 = "version-uuid-2";
    private static final String RESIDENT_IDP = "Resident IDP";

    @Mock
    private ConsentManager consentManager;

    private MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolderMock;
    private MockedStatic<PrivilegedCarbonContext> privilegedCarbonContextMock;
    private AutoCloseable openMocksCloseable;
    private String originalCarbonHome;
    private String originalCarbonConfigDirPath;

    @BeforeMethod
    public void setUp() {

        openMocksCloseable = openMocks(this);

        FrameworkServiceDataHolder dataHolder = mock(FrameworkServiceDataHolder.class);
        frameworkServiceDataHolderMock = mockStatic(FrameworkServiceDataHolder.class);
        frameworkServiceDataHolderMock.when(FrameworkServiceDataHolder::getInstance).thenReturn(dataHolder);
        when(dataHolder.getConsentManager()).thenReturn(consentManager);

        originalCarbonHome = System.getProperty(CarbonBaseConstants.CARBON_HOME);
        originalCarbonConfigDirPath = System.getProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH);
        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH,
                Paths.get(carbonHome, "conf").toString());

        PrivilegedCarbonContext carbonContextInstance = mock(PrivilegedCarbonContext.class);
        privilegedCarbonContextMock = mockStatic(PrivilegedCarbonContext.class);
        privilegedCarbonContextMock.when(PrivilegedCarbonContext::getThreadLocalCarbonContext)
                .thenReturn(carbonContextInstance);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        frameworkServiceDataHolderMock.close();
        privilegedCarbonContextMock.close();
        openMocksCloseable.close();

        if (originalCarbonHome != null) {
            System.setProperty(CarbonBaseConstants.CARBON_HOME, originalCarbonHome);
        } else {
            System.clearProperty(CarbonBaseConstants.CARBON_HOME);
        }
        if (originalCarbonConfigDirPath != null) {
            System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, originalCarbonConfigDirPath);
        } else {
            System.clearProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH);
        }
    }

    // ─── shouldPromptOnLogin ────────────────────────────────────────────────────

    @Test(description = "Returns false when version is null.")
    public void testShouldPromptOnLoginReturnsFalseForNullVersion() {

        assertFalse(PolicyConsentUtil.shouldPromptOnLogin(null));
    }

    @Test(description = "Returns false when version properties are null.")
    public void testShouldPromptOnLoginReturnsFalseForNullProperties() {

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getProperties()).thenReturn(null);
        assertFalse(PolicyConsentUtil.shouldPromptOnLogin(version));
    }

    @Test(description = "Returns false when promptOnLogin property is absent.")
    public void testShouldPromptOnLoginReturnsFalseWhenPropertyAbsent() {

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getProperties()).thenReturn(Collections.emptyMap());
        assertFalse(PolicyConsentUtil.shouldPromptOnLogin(version));
    }

    @Test(description = "Returns true when promptOnLogin is set to 'true'.")
    public void testShouldPromptOnLoginReturnsTrueWhenTrue() {

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "true"));
        assertTrue(PolicyConsentUtil.shouldPromptOnLogin(version));
    }

    @Test(description = "Returns true for promptOnLogin value 'TRUE' (case-insensitive).")
    public void testShouldPromptOnLoginCaseInsensitive() {

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "TRUE"));
        assertTrue(PolicyConsentUtil.shouldPromptOnLogin(version));
    }

    @Test(description = "Returns false when promptOnLogin is set to 'false'.")
    public void testShouldPromptOnLoginReturnsFalseWhenFalse() {

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "false"));
        assertFalse(PolicyConsentUtil.shouldPromptOnLogin(version));
    }

    // ─── isMandatoryPurpose ─────────────────────────────────────────────────────

    @Test(description = "Returns false when purpose has no latest version.")
    public void testIsMandatoryPurposeReturnsFalseForNullLatestVersion() {

        Purpose purpose = mock(Purpose.class);
        when(purpose.getLatestVersion()).thenReturn(null);
        assertFalse(PolicyConsentUtil.isMandatoryPurpose(purpose));
    }

    @Test(description = "Returns false when latest version has null PII categories.")
    public void testIsMandatoryPurposeReturnsFalseForNullCategories() {

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getPurposePIICategories()).thenReturn(null);
        Purpose purpose = mock(Purpose.class);
        when(purpose.getLatestVersion()).thenReturn(version);
        assertFalse(PolicyConsentUtil.isMandatoryPurpose(purpose));
    }

    @Test(description = "Returns false when no PII category is marked mandatory.")
    public void testIsMandatoryPurposeReturnsFalseWhenNoMandatoryCategory() {

        PurposePIICategory optional = mock(PurposePIICategory.class);
        when(optional.getMandatory()).thenReturn(false);
        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getPurposePIICategories()).thenReturn(Collections.singletonList(optional));
        Purpose purpose = mock(Purpose.class);
        when(purpose.getLatestVersion()).thenReturn(version);
        assertFalse(PolicyConsentUtil.isMandatoryPurpose(purpose));
    }

    @Test(description = "Returns true when at least one PII category is mandatory.")
    public void testIsMandatoryPurposeReturnsTrueWhenOneMandatoryCategory() {

        PurposePIICategory mandatory = mock(PurposePIICategory.class);
        when(mandatory.getMandatory()).thenReturn(true);
        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getPurposePIICategories()).thenReturn(Collections.singletonList(mandatory));
        Purpose purpose = mock(Purpose.class);
        when(purpose.getLatestVersion()).thenReturn(version);
        assertTrue(PolicyConsentUtil.isMandatoryPurpose(purpose));
    }

    @Test(description = "Returns false when latest version has empty PII category list.")
    public void testIsMandatoryPurposeReturnsFalseForEmptyCategories() {

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getPurposePIICategories()).thenReturn(Collections.emptyList());
        Purpose purpose = mock(Purpose.class);
        when(purpose.getLatestVersion()).thenReturn(version);
        assertFalse(PolicyConsentUtil.isMandatoryPurpose(purpose));
    }

    // ─── getPolicyPurposes ──────────────────────────────────────────────────────

    @Test(description = "Returns empty list when ConsentManager returns null.")
    public void testGetPolicyPurposesReturnsEmptyListWhenManagerReturnsNull()
            throws ConsentManagementException {

        when(consentManager.listPurposes(anyList(), anyInt())).thenReturn(null);
        List<Purpose> result = PolicyConsentUtil.getPolicyPurposes(consentManager);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(description = "Returns the list returned by ConsentManager.")
    public void testGetPolicyPurposesReturnsManagerList() throws ConsentManagementException {

        Purpose p = mock(Purpose.class);
        when(consentManager.listPurposes(anyList(), anyInt())).thenReturn(Collections.singletonList(p));
        List<Purpose> result = PolicyConsentUtil.getPolicyPurposes(consentManager);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0), p);
    }

    // ─── getLatestVersionWithPromptOnLogin ──────────────────────────────────────

    @Test(description = "Returns null when no version has promptOnLogin=true.")
    public void testGetLatestVersionWithPromptOnLoginReturnsNullWhenNonePrompt()
            throws ConsentManagementException {

        PurposeVersion v1 = mock(PurposeVersion.class);
        when(v1.getUuid()).thenReturn(VERSION_UUID_1);
        when(v1.getProperties()).thenReturn(Collections.emptyMap());
        when(consentManager.getPurposeVersion(PURPOSE_UUID_1, VERSION_UUID_1)).thenReturn(v1);

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);

        PurposeVersion result = PolicyConsentUtil.getLatestVersionWithPromptOnLogin(
                purpose, Collections.singletonList(v1), consentManager);

        assertNull(result);
    }

    @Test(description = "Returns the latest version (last in list) that has promptOnLogin=true.")
    public void testGetLatestVersionWithPromptOnLoginReturnsLastMatchingVersion()
            throws ConsentManagementException {

        PurposeVersion v1 = mock(PurposeVersion.class);
        when(v1.getUuid()).thenReturn(VERSION_UUID_1);
        when(v1.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "true"));
        when(consentManager.getPurposeVersion(PURPOSE_UUID_1, VERSION_UUID_1)).thenReturn(v1);

        PurposeVersion v2 = mock(PurposeVersion.class);
        when(v2.getUuid()).thenReturn(VERSION_UUID_2);
        when(v2.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "true"));
        when(consentManager.getPurposeVersion(PURPOSE_UUID_1, VERSION_UUID_2)).thenReturn(v2);

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);

        PurposeVersion result = PolicyConsentUtil.getLatestVersionWithPromptOnLogin(
                purpose, Arrays.asList(v1, v2), consentManager);

        assertEquals(result.getUuid(), VERSION_UUID_2);
    }

    @Test(description = "Skips versions without promptOnLogin and returns the last one with it.")
    public void testGetLatestVersionWithPromptOnLoginSkipsVersionsWithoutFlag()
            throws ConsentManagementException {

        PurposeVersion v1 = mock(PurposeVersion.class);
        when(v1.getUuid()).thenReturn(VERSION_UUID_1);
        when(v1.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "true"));
        when(consentManager.getPurposeVersion(PURPOSE_UUID_1, VERSION_UUID_1)).thenReturn(v1);

        PurposeVersion v2 = mock(PurposeVersion.class);
        when(v2.getUuid()).thenReturn(VERSION_UUID_2);
        when(v2.getProperties()).thenReturn(Collections.emptyMap());
        when(consentManager.getPurposeVersion(PURPOSE_UUID_1, VERSION_UUID_2)).thenReturn(v2);

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);

        // v2 (last) has no promptOnLogin, falls back to v1
        PurposeVersion result = PolicyConsentUtil.getLatestVersionWithPromptOnLogin(
                purpose, Arrays.asList(v1, v2), consentManager);

        assertEquals(result.getUuid(), VERSION_UUID_1);
    }

    // ─── missingConsentForVersion ────────────────────────────────────────────────

    @Test(description = "Returns true when no receipts exist for the promptOnLogin version or later.")
    public void testMissingConsentForVersionReturnsTrueWhenNoReceipts()
            throws ConsentManagementException {

        PurposeVersion promptVersion = mock(PurposeVersion.class);
        when(promptVersion.getUuid()).thenReturn(VERSION_UUID_1);
        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);

        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());

        boolean result = PolicyConsentUtil.missingConsentForVersion(
                SUBJECT_ID, purpose, promptVersion,
                Collections.singletonList(promptVersion), consentManager);

        assertTrue(result);
    }

    @Test(description = "Returns false when a receipt exists for the promptOnLogin version.")
    public void testMissingConsentForVersionReturnsFalseWhenReceiptExists()
            throws ConsentManagementException {

        PurposeVersion promptVersion = mock(PurposeVersion.class);
        when(promptVersion.getUuid()).thenReturn(VERSION_UUID_1);
        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);

        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.singletonList(mock(Receipt.class)));

        boolean result = PolicyConsentUtil.missingConsentForVersion(
                SUBJECT_ID, purpose, promptVersion,
                Collections.singletonList(promptVersion), consentManager);

        assertFalse(result);
    }

    @Test(description = "Returns false when receipt exists for a later version (after the promptOnLogin version).")
    public void testMissingConsentForVersionReturnsFalseWhenReceiptExistsForLaterVersion()
            throws ConsentManagementException {

        PurposeVersion promptVersion = mock(PurposeVersion.class);
        when(promptVersion.getUuid()).thenReturn(VERSION_UUID_1);

        PurposeVersion laterVersion = mock(PurposeVersion.class);
        when(laterVersion.getUuid()).thenReturn(VERSION_UUID_2);

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);

        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());
        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_2), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.singletonList(mock(Receipt.class)));

        boolean result = PolicyConsentUtil.missingConsentForVersion(
                SUBJECT_ID, purpose, promptVersion,
                Arrays.asList(promptVersion, laterVersion), consentManager);

        assertFalse(result);
    }

    @Test(description = "Returns true when promptOnLogin version is not found in the allVersions list.")
    public void testMissingConsentForVersionReturnsTrueWhenVersionNotInList()
            throws ConsentManagementException {

        PurposeVersion promptVersion = mock(PurposeVersion.class);
        when(promptVersion.getUuid()).thenReturn("unknown-version-uuid");

        PurposeVersion otherVersion = mock(PurposeVersion.class);
        when(otherVersion.getUuid()).thenReturn(VERSION_UUID_1);

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);

        boolean result = PolicyConsentUtil.missingConsentForVersion(
                SUBJECT_ID, purpose, promptVersion,
                Collections.singletonList(otherVersion), consentManager);

        assertTrue(result);
        verify(consentManager, never()).listReceipts(anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyInt());
    }

    // ─── hasConsentForAnyVersion ─────────────────────────────────────────────────

    @Test(description = "Returns false when no receipts exist for any version.")
    public void testHasConsentForAnyVersionReturnsFalseWhenNoReceipts()
            throws ConsentManagementException {

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);

        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), isNull(), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());

        assertFalse(PolicyConsentUtil.hasConsentForAnyVersion(SUBJECT_ID, purpose, consentManager));
    }

    @Test(description = "Returns true when a receipt exists for any version.")
    public void testHasConsentForAnyVersionReturnsTrueWhenReceiptExists()
            throws ConsentManagementException {

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);

        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), isNull(), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.singletonList(mock(Receipt.class)));

        assertTrue(PolicyConsentUtil.hasConsentForAnyVersion(SUBJECT_ID, purpose, consentManager));
    }

    @Test(description = "Returns false when ConsentManager returns null receipts.")
    public void testHasConsentForAnyVersionReturnsFalseWhenManagerReturnsNull()
            throws ConsentManagementException {

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);

        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), isNull(), isNull(), isNull(), eq(1)))
                .thenReturn(null);

        assertFalse(PolicyConsentUtil.hasConsentForAnyVersion(SUBJECT_ID, purpose, consentManager));
    }

    // ─── isPolicyConsentMissing ──────────────────────────────────────────────────

    @Test(description = "Returns false when purpose has no versions.")
    public void testIsPolicyConsentMissingReturnsFalseForNoVersions()
            throws ConsentManagementException {

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);
        when(consentManager.listPurposeVersions(PURPOSE_UUID_1)).thenReturn(Collections.emptyList());

        assertFalse(PolicyConsentUtil.isPolicyConsentMissing(SUBJECT_ID, purpose, consentManager));
    }

    @Test(description = "Returns false when no version has promptOnLogin=true.")
    public void testIsPolicyConsentMissingReturnsFalseWhenNoVersionHasPromptOnLogin()
            throws ConsentManagementException {

        PurposeVersion v = mock(PurposeVersion.class);
        when(v.getUuid()).thenReturn(VERSION_UUID_1);
        when(v.getProperties()).thenReturn(Collections.emptyMap());

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);
        when(consentManager.listPurposeVersions(PURPOSE_UUID_1)).thenReturn(Collections.singletonList(v));
        when(consentManager.getPurposeVersion(PURPOSE_UUID_1, VERSION_UUID_1)).thenReturn(v);

        assertFalse(PolicyConsentUtil.isPolicyConsentMissing(SUBJECT_ID, purpose, consentManager));
    }

    @Test(description = "Returns true when promptOnLogin version has no receipt.")
    public void testIsPolicyConsentMissingReturnsTrueWhenReceiptAbsent()
            throws ConsentManagementException {

        PurposeVersion v = mock(PurposeVersion.class);
        when(v.getUuid()).thenReturn(VERSION_UUID_1);
        when(v.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "true"));

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);
        when(consentManager.listPurposeVersions(PURPOSE_UUID_1)).thenReturn(Collections.singletonList(v));
        when(consentManager.getPurposeVersion(PURPOSE_UUID_1, VERSION_UUID_1)).thenReturn(v);
        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());

        assertTrue(PolicyConsentUtil.isPolicyConsentMissing(SUBJECT_ID, purpose, consentManager));
    }

    @Test(description = "Returns false when promptOnLogin version already has a receipt.")
    public void testIsPolicyConsentMissingReturnsFalseWhenReceiptPresent()
            throws ConsentManagementException {

        PurposeVersion v = mock(PurposeVersion.class);
        when(v.getUuid()).thenReturn(VERSION_UUID_1);
        when(v.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "true"));

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);
        when(consentManager.listPurposeVersions(PURPOSE_UUID_1)).thenReturn(Collections.singletonList(v));
        when(consentManager.getPurposeVersion(PURPOSE_UUID_1, VERSION_UUID_1)).thenReturn(v);
        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.singletonList(mock(Receipt.class)));

        assertFalse(PolicyConsentUtil.isPolicyConsentMissing(SUBJECT_ID, purpose, consentManager));
    }

    // ─── buildPurposeMetadataJson ────────────────────────────────────────────────

    @Test(description = "Returns '[]' JSON for an empty purpose list.")
    public void testBuildPurposeMetadataJsonEmptyList() {

        String json = PolicyConsentUtil.buildPurposeMetadataJson(
                Collections.emptyList(), Collections.emptySet(), Collections.emptySet());
        assertEquals(json, "[]");
    }

    @Test(description = "Includes purposeId, name, mandatory, newVersion, description, and policyUrl fields.")
    public void testBuildPurposeMetadataJsonIncludesAllFields() {

        Map<String, String> props = new HashMap<>();
        props.put("policyUrl", "https://example.com/policy");

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getDescription()).thenReturn("Test description");
        when(version.getProperties()).thenReturn(props);

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);
        when(purpose.getName()).thenReturn("Privacy Policy");
        when(purpose.getLatestVersion()).thenReturn(version);

        String json = PolicyConsentUtil.buildPurposeMetadataJson(
                Collections.singletonList(purpose),
                new HashSet<>(Collections.singletonList(PURPOSE_UUID_1)),
                new HashSet<>(Collections.singletonList(PURPOSE_UUID_1)));

        Type listType = new TypeToken<List<Map<String, Object>>>() { }.getType();
        List<Map<String, Object>> parsed = new Gson().fromJson(json, listType);
        assertEquals(parsed.size(), 1);
        Map<String, Object> entry = parsed.get(0);
        assertEquals(entry.get("purposeId"), PURPOSE_UUID_1);
        assertEquals(entry.get("name"), "Privacy Policy");
        assertTrue((Boolean) entry.get("mandatory"));
        assertTrue((Boolean) entry.get("newVersion"));
        assertEquals(entry.get("description"), "Test description");
        assertEquals(entry.get("policyUrl"), "https://example.com/policy");
    }

    @Test(description = "Uses purpose UUID as name when purpose name is null.")
    public void testBuildPurposeMetadataJsonFallsBackToUuidForName() {

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);
        when(purpose.getName()).thenReturn(null);
        when(purpose.getLatestVersion()).thenReturn(null);

        String json = PolicyConsentUtil.buildPurposeMetadataJson(
                Collections.singletonList(purpose), Collections.emptySet(), Collections.emptySet());

        Type listType = new TypeToken<List<Map<String, Object>>>() { }.getType();
        List<Map<String, Object>> parsed = new Gson().fromJson(json, listType);
        assertEquals(parsed.get(0).get("name"), PURPOSE_UUID_1);
    }

    @Test(description = "Falls back to purpose-level description when version description is null.")
    public void testBuildPurposeMetadataJsonFallsBackToPurposeDescription() {

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getDescription()).thenReturn(null);
        when(version.getProperties()).thenReturn(Collections.emptyMap());

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);
        when(purpose.getName()).thenReturn("Policy");
        when(purpose.getDescription()).thenReturn("Purpose-level description");
        when(purpose.getLatestVersion()).thenReturn(version);

        String json = PolicyConsentUtil.buildPurposeMetadataJson(
                Collections.singletonList(purpose), Collections.emptySet(), Collections.emptySet());

        Type listType = new TypeToken<List<Map<String, Object>>>() { }.getType();
        List<Map<String, Object>> parsed = new Gson().fromJson(json, listType);
        assertEquals(parsed.get(0).get("description"), "Purpose-level description");
    }

    @Test(description = "Sets mandatory=false and newVersion=false for a purpose not in either set.")
    public void testBuildPurposeMetadataJsonSetsFalseWhenNotInSets() {

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);
        when(purpose.getName()).thenReturn("Policy");
        when(purpose.getLatestVersion()).thenReturn(null);

        String json = PolicyConsentUtil.buildPurposeMetadataJson(
                Collections.singletonList(purpose), Collections.emptySet(), Collections.emptySet());

        Type listType = new TypeToken<List<Map<String, Object>>>() { }.getType();
        List<Map<String, Object>> parsed = new Gson().fromJson(json, listType);
        assertFalse((Boolean) parsed.get(0).get("mandatory"));
        assertFalse((Boolean) parsed.get(0).get("newVersion"));
    }

    @Test(description = "Produces correct metadata for multiple purposes with mixed mandatory/newVersion flags.")
    public void testBuildPurposeMetadataJsonMultiplePurposes() {

        Purpose p1 = mock(Purpose.class);
        when(p1.getUuid()).thenReturn(PURPOSE_UUID_1);
        when(p1.getName()).thenReturn("Policy1");
        when(p1.getLatestVersion()).thenReturn(null);

        Purpose p2 = mock(Purpose.class);
        when(p2.getUuid()).thenReturn(PURPOSE_UUID_2);
        when(p2.getName()).thenReturn("Policy2");
        when(p2.getLatestVersion()).thenReturn(null);

        String json = PolicyConsentUtil.buildPurposeMetadataJson(
                Arrays.asList(p1, p2),
                new HashSet<>(Collections.singletonList(PURPOSE_UUID_1)),
                new HashSet<>(Collections.singletonList(PURPOSE_UUID_2)));

        Type listType = new TypeToken<List<Map<String, Object>>>() { }.getType();
        List<Map<String, Object>> parsed = new Gson().fromJson(json, listType);
        assertEquals(parsed.size(), 2);

        Map<String, Object> entry1 = parsed.get(0);
        assertEquals(entry1.get("purposeId"), PURPOSE_UUID_1);
        assertTrue((Boolean) entry1.get("mandatory"));
        assertFalse((Boolean) entry1.get("newVersion"));

        Map<String, Object> entry2 = parsed.get(1);
        assertEquals(entry2.get("purposeId"), PURPOSE_UUID_2);
        assertFalse((Boolean) entry2.get("mandatory"));
        assertTrue((Boolean) entry2.get("newVersion"));
    }

    // ─── classifyUnconsentedPolicies ─────────────────────────────────────────────

    @Test(description = "Returns empty ClassifiedPolicies when no policy purposes exist.")
    public void testClassifyUnconsentedPoliciesReturnsEmptyWhenNoPurposes()
            throws ConsentManagementException {

        when(consentManager.listPurposes(anyList(), anyInt())).thenReturn(Collections.emptyList());

        PolicyConsentUtil.ClassifiedPolicies result =
                PolicyConsentUtil.classifyUnconsentedPolicies(SUBJECT_ID, TENANT_DOMAIN);

        assertTrue(result.isEmpty());
        assertEquals(result.getPurposeMetadataJson(), "[]");
    }

    @Test(description = "Places unconsented mandatory purpose in mandatoryUnconsentedIds.")
    public void testClassifyUnconsentedPoliciesPlacesMandatoryUnconsentedCorrectly()
            throws ConsentManagementException {

        PurposePIICategory mandatoryCat = mock(PurposePIICategory.class);
        when(mandatoryCat.getMandatory()).thenReturn(true);

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getUuid()).thenReturn(VERSION_UUID_1);
        when(version.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "true"));
        when(version.getPurposePIICategories()).thenReturn(Collections.singletonList(mandatoryCat));

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);
        when(purpose.getLatestVersion()).thenReturn(version);

        when(consentManager.listPurposes(anyList(), anyInt())).thenReturn(Collections.singletonList(purpose));
        when(consentManager.listPurposeVersions(PURPOSE_UUID_1)).thenReturn(Collections.singletonList(version));
        when(consentManager.getPurposeVersion(PURPOSE_UUID_1, VERSION_UUID_1)).thenReturn(version);
        // No consent for this version
        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());
        // No prior consent for any version
        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), isNull(), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());

        PolicyConsentUtil.ClassifiedPolicies result =
                PolicyConsentUtil.classifyUnconsentedPolicies(SUBJECT_ID, TENANT_DOMAIN);

        assertFalse(result.isEmpty());
        assertTrue(result.getMandatoryUnconsentedIds().contains(PURPOSE_UUID_1));
        assertTrue(result.getMandatoryNewVersionIds().isEmpty());
        assertTrue(result.getOptionalUnconsentedIds().isEmpty());
        assertTrue(result.getOptionalNewVersionIds().isEmpty());
    }

    @Test(description = "Places unconsented mandatory purpose with prior consent in mandatoryNewVersionIds.")
    public void testClassifyUnconsentedPoliciesPlacesMandatoryNewVersionCorrectly()
            throws ConsentManagementException {

        PurposePIICategory mandatoryCat = mock(PurposePIICategory.class);
        when(mandatoryCat.getMandatory()).thenReturn(true);

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getUuid()).thenReturn(VERSION_UUID_1);
        when(version.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "true"));
        when(version.getPurposePIICategories()).thenReturn(Collections.singletonList(mandatoryCat));

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);
        when(purpose.getLatestVersion()).thenReturn(version);

        when(consentManager.listPurposes(anyList(), anyInt())).thenReturn(Collections.singletonList(purpose));
        when(consentManager.listPurposeVersions(PURPOSE_UUID_1)).thenReturn(Collections.singletonList(version));
        when(consentManager.getPurposeVersion(PURPOSE_UUID_1, VERSION_UUID_1)).thenReturn(version);
        // No consent for the current version
        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());
        // Has prior consent for some version
        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), isNull(), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.singletonList(mock(Receipt.class)));

        PolicyConsentUtil.ClassifiedPolicies result =
                PolicyConsentUtil.classifyUnconsentedPolicies(SUBJECT_ID, TENANT_DOMAIN);

        assertFalse(result.isEmpty());
        assertTrue(result.getMandatoryNewVersionIds().contains(PURPOSE_UUID_1));
        assertTrue(result.getMandatoryUnconsentedIds().isEmpty());
    }

    @Test(description = "Places unconsented optional purpose in optionalUnconsentedIds.")
    public void testClassifyUnconsentedPoliciesPlacesOptionalUnconsentedCorrectly()
            throws ConsentManagementException {

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getUuid()).thenReturn(VERSION_UUID_1);
        when(version.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "true"));
        when(version.getPurposePIICategories()).thenReturn(Collections.emptyList());

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);
        when(purpose.getLatestVersion()).thenReturn(version);

        when(consentManager.listPurposes(anyList(), anyInt())).thenReturn(Collections.singletonList(purpose));
        when(consentManager.listPurposeVersions(PURPOSE_UUID_1)).thenReturn(Collections.singletonList(version));
        when(consentManager.getPurposeVersion(PURPOSE_UUID_1, VERSION_UUID_1)).thenReturn(version);
        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());
        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), isNull(), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());

        PolicyConsentUtil.ClassifiedPolicies result =
                PolicyConsentUtil.classifyUnconsentedPolicies(SUBJECT_ID, TENANT_DOMAIN);

        assertFalse(result.isEmpty());
        assertTrue(result.getOptionalUnconsentedIds().contains(PURPOSE_UUID_1));
        assertTrue(result.getOptionalNewVersionIds().isEmpty());
        assertTrue(result.getMandatoryUnconsentedIds().isEmpty());
    }

    @Test(description = "Places unconsented optional purpose with prior consent in optionalNewVersionIds.")
    public void testClassifyUnconsentedPoliciesPlacesOptionalNewVersionCorrectly()
            throws ConsentManagementException {

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getUuid()).thenReturn(VERSION_UUID_1);
        when(version.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "true"));
        when(version.getPurposePIICategories()).thenReturn(Collections.emptyList());

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);
        when(purpose.getLatestVersion()).thenReturn(version);

        when(consentManager.listPurposes(anyList(), anyInt())).thenReturn(Collections.singletonList(purpose));
        when(consentManager.listPurposeVersions(PURPOSE_UUID_1)).thenReturn(Collections.singletonList(version));
        when(consentManager.getPurposeVersion(PURPOSE_UUID_1, VERSION_UUID_1)).thenReturn(version);
        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());
        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), isNull(), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.singletonList(mock(Receipt.class)));

        PolicyConsentUtil.ClassifiedPolicies result =
                PolicyConsentUtil.classifyUnconsentedPolicies(SUBJECT_ID, TENANT_DOMAIN);

        assertFalse(result.isEmpty());
        assertTrue(result.getOptionalNewVersionIds().contains(PURPOSE_UUID_1));
        assertTrue(result.getOptionalUnconsentedIds().isEmpty());
    }

    @Test(description = "Skips consented purpose and returns empty ClassifiedPolicies.")
    public void testClassifyUnconsentedPoliciesSkipsConsentedPurpose()
            throws ConsentManagementException {

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getUuid()).thenReturn(VERSION_UUID_1);
        when(version.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "true"));
        when(version.getPurposePIICategories()).thenReturn(Collections.emptyList());

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);
        when(purpose.getLatestVersion()).thenReturn(version);

        when(consentManager.listPurposes(anyList(), anyInt())).thenReturn(Collections.singletonList(purpose));
        when(consentManager.listPurposeVersions(PURPOSE_UUID_1)).thenReturn(Collections.singletonList(version));
        when(consentManager.getPurposeVersion(PURPOSE_UUID_1, VERSION_UUID_1)).thenReturn(version);
        // User has consent
        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.singletonList(mock(Receipt.class)));

        PolicyConsentUtil.ClassifiedPolicies result =
                PolicyConsentUtil.classifyUnconsentedPolicies(SUBJECT_ID, TENANT_DOMAIN);

        assertTrue(result.isEmpty());
        assertEquals(result.getPurposeMetadataJson(), "[]");
    }

    // ─── hasUnconsentedPolicies ───────────────────────────────────────────────────

    @Test(description = "Returns false when no policy purposes exist.")
    public void testHasUnconsentedPoliciesReturnsFalseWhenNoPurposes()
            throws ConsentManagementException {

        when(consentManager.listPurposes(anyList(), anyInt())).thenReturn(Collections.emptyList());

        assertFalse(PolicyConsentUtil.hasUnconsentedPolicies(SUBJECT_ID, TENANT_DOMAIN));
    }

    @Test(description = "Returns true when at least one purpose has no consent.")
    public void testHasUnconsentedPoliciesReturnsTrueWhenOneUnconsentedPurpose()
            throws ConsentManagementException {

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getUuid()).thenReturn(VERSION_UUID_1);
        when(version.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "true"));

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);

        when(consentManager.listPurposes(anyList(), anyInt())).thenReturn(Collections.singletonList(purpose));
        when(consentManager.listPurposeVersions(PURPOSE_UUID_1)).thenReturn(Collections.singletonList(version));
        when(consentManager.getPurposeVersion(PURPOSE_UUID_1, VERSION_UUID_1)).thenReturn(version);
        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());

        assertTrue(PolicyConsentUtil.hasUnconsentedPolicies(SUBJECT_ID, TENANT_DOMAIN));
    }

    @Test(description = "Returns false when all purposes have consent.")
    public void testHasUnconsentedPoliciesReturnsFalseWhenAllConsented()
            throws ConsentManagementException {

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getUuid()).thenReturn(VERSION_UUID_1);
        when(version.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "true"));

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);

        when(consentManager.listPurposes(anyList(), anyInt())).thenReturn(Collections.singletonList(purpose));
        when(consentManager.listPurposeVersions(PURPOSE_UUID_1)).thenReturn(Collections.singletonList(version));
        when(consentManager.getPurposeVersion(PURPOSE_UUID_1, VERSION_UUID_1)).thenReturn(version);
        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.singletonList(mock(Receipt.class)));

        assertFalse(PolicyConsentUtil.hasUnconsentedPolicies(SUBJECT_ID, TENANT_DOMAIN));
    }

    @Test(description = "Short-circuits and returns true on first unconsented purpose without checking others.")
    public void testHasUnconsentedPoliciesShortCircuitsOnFirstUnconsentedPurpose()
            throws ConsentManagementException {

        PurposeVersion v1 = mock(PurposeVersion.class);
        when(v1.getUuid()).thenReturn(VERSION_UUID_1);
        when(v1.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "true"));

        Purpose p1 = mock(Purpose.class);
        when(p1.getUuid()).thenReturn(PURPOSE_UUID_1);

        Purpose p2 = mock(Purpose.class);
        when(p2.getUuid()).thenReturn(PURPOSE_UUID_2);

        when(consentManager.listPurposes(anyList(), anyInt())).thenReturn(Arrays.asList(p1, p2));
        when(consentManager.listPurposeVersions(PURPOSE_UUID_1)).thenReturn(Collections.singletonList(v1));
        when(consentManager.getPurposeVersion(PURPOSE_UUID_1, VERSION_UUID_1)).thenReturn(v1);
        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());

        assertTrue(PolicyConsentUtil.hasUnconsentedPolicies(SUBJECT_ID, TENANT_DOMAIN));
        // p2 should never be evaluated (short-circuit)
        verify(consentManager, never()).listPurposeVersions(PURPOSE_UUID_2);
    }

    // ─── ClassifiedPolicies.isEmpty ─────────────────────────────────────────────

    @Test(description = "isEmpty returns true only when all four lists are empty.")
    public void testClassifiedPoliciesIsEmptyWhenAllListsEmpty() {

        PolicyConsentUtil.ClassifiedPolicies policies = new PolicyConsentUtil.ClassifiedPolicies(
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), "[]");
        assertTrue(policies.isEmpty());
    }

    @Test(description = "isEmpty returns false when mandatoryUnconsentedIds is non-empty.")
    public void testClassifiedPoliciesIsNotEmptyWhenMandatoryUnconsentedPresent() {

        PolicyConsentUtil.ClassifiedPolicies policies = new PolicyConsentUtil.ClassifiedPolicies(
                Collections.singletonList("id"), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), "[]");
        assertFalse(policies.isEmpty());
    }

    @Test(description = "isEmpty returns false when optionalNewVersionIds is non-empty.")
    public void testClassifiedPoliciesIsNotEmptyWhenOptionalNewVersionPresent() {

        PolicyConsentUtil.ClassifiedPolicies policies = new PolicyConsentUtil.ClassifiedPolicies(
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.singletonList("id"), "[]");
        assertFalse(policies.isEmpty());
    }
}
