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
        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());
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
        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());
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
        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.singletonList(mock(Receipt.class)));

        PolicyConsentUtil.ClassifiedPolicies result =
                PolicyConsentUtil.classifyUnconsentedPolicies(SUBJECT_ID, TENANT_DOMAIN);

        assertTrue(result.isEmpty());
        assertEquals(result.getPurposeMetadataJson(), "[]");
    }

    @Test(description = "Metadata JSON includes all expected fields for an unconsented mandatory purpose.")
    public void testClassifyUnconsentedPoliciesMetadataJsonContainsExpectedFields()
            throws ConsentManagementException {

        Map<String, String> props = new HashMap<>();
        props.put("promptOnLogin", "true");
        props.put("policyUrl", "https://example.com/policy");

        PurposePIICategory mandatoryCat = mock(PurposePIICategory.class);
        when(mandatoryCat.getMandatory()).thenReturn(true);

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getUuid()).thenReturn(VERSION_UUID_1);
        when(version.getProperties()).thenReturn(props);
        when(version.getDescription()).thenReturn("Test description");
        when(version.getPurposePIICategories()).thenReturn(Collections.singletonList(mandatoryCat));

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(PURPOSE_UUID_1);
        when(purpose.getName()).thenReturn("Privacy Policy");
        when(purpose.getLatestVersion()).thenReturn(version);

        when(consentManager.listPurposes(anyList(), anyInt())).thenReturn(Collections.singletonList(purpose));
        when(consentManager.listPurposeVersions(PURPOSE_UUID_1)).thenReturn(Collections.singletonList(version));
        when(consentManager.getPurposeVersion(PURPOSE_UUID_1, VERSION_UUID_1)).thenReturn(version);
        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), eq("ACTIVE"),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());
        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_1), isNull(), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());

        PolicyConsentUtil.ClassifiedPolicies result =
                PolicyConsentUtil.classifyUnconsentedPolicies(SUBJECT_ID, TENANT_DOMAIN);

        Type listType = new TypeToken<List<Map<String, Object>>>() { }.getType();
        List<Map<String, Object>> parsed = new Gson().fromJson(result.getPurposeMetadataJson(), listType);
        assertEquals(parsed.size(), 1);
        Map<String, Object> entry = parsed.get(0);
        assertEquals(entry.get("purposeId"), PURPOSE_UUID_1);
        assertEquals(entry.get("name"), "Privacy Policy");
        assertTrue((Boolean) entry.get("mandatory"));
        assertFalse((Boolean) entry.get("newVersion"));
        assertEquals(entry.get("description"), "Test description");
        assertEquals(entry.get("policyUrl"), "https://example.com/policy");
    }

    @Test(description = "Returns false when no policy purposes exist.")
    public void testHasUnconsentedPoliciesReturnsFalseWhenNoPurposes()
            throws ConsentManagementException {

        when(consentManager.listPurposes(anyList(), anyInt())).thenReturn(Collections.emptyList());

        assertFalse(PolicyConsentUtil.hasUnconsentedPolicies(SUBJECT_ID, TENANT_DOMAIN, Collections.emptySet()));
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

        assertTrue(PolicyConsentUtil.hasUnconsentedPolicies(SUBJECT_ID, TENANT_DOMAIN, Collections.emptySet()));
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

        assertFalse(PolicyConsentUtil.hasUnconsentedPolicies(SUBJECT_ID, TENANT_DOMAIN, Collections.emptySet()));
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

        assertTrue(PolicyConsentUtil.hasUnconsentedPolicies(SUBJECT_ID, TENANT_DOMAIN, Collections.emptySet()));
        verify(consentManager, never()).listPurposeVersions(PURPOSE_UUID_2);
    }

    @Test(description = "Filters purposes by policyIds set when non-empty.")
    public void testHasUnconsentedPoliciesFiltersToSpecifiedPolicyIds()
            throws ConsentManagementException {

        PurposeVersion v1 = mock(PurposeVersion.class);
        when(v1.getUuid()).thenReturn(VERSION_UUID_1);
        when(v1.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "true"));

        Purpose p1 = mock(Purpose.class);
        when(p1.getUuid()).thenReturn(PURPOSE_UUID_1);

        Purpose p2 = mock(Purpose.class);
        when(p2.getUuid()).thenReturn(PURPOSE_UUID_2);

        when(consentManager.listPurposes(anyList(), anyInt())).thenReturn(Arrays.asList(p1, p2));
        when(consentManager.listPurposeVersions(PURPOSE_UUID_2)).thenReturn(Collections.singletonList(v1));
        when(consentManager.getPurposeVersion(PURPOSE_UUID_2, VERSION_UUID_1)).thenReturn(v1);
        when(consentManager.listReceipts(eq(SUBJECT_ID), eq(RESIDENT_IDP), isNull(),
                eq(PURPOSE_UUID_2), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.singletonList(mock(Receipt.class)));

        // Filter to only PURPOSE_UUID_2 (which is consented) — PURPOSE_UUID_1 is ignored.
        assertFalse(PolicyConsentUtil.hasUnconsentedPolicies(SUBJECT_ID, TENANT_DOMAIN,
                new HashSet<>(Collections.singletonList(PURPOSE_UUID_2))));
        verify(consentManager, never()).listPurposeVersions(PURPOSE_UUID_1);
    }

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
