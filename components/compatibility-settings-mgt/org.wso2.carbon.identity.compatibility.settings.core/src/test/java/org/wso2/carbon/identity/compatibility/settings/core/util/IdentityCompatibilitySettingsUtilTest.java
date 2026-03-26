/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.identity.compatibility.settings.core.util;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.compatibility.settings.core.cache.CompatibilitySettingCache;
import org.wso2.carbon.identity.compatibility.settings.core.cache.CompatibilitySettingCacheEntry;
import org.wso2.carbon.identity.compatibility.settings.core.constant.IdentityCompatibilitySettingsConstants.ErrorMessages;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingClientException;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingServerException;
import org.wso2.carbon.identity.compatibility.settings.core.internal.IdentityCompatibilitySettingsDataHolder;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySetting;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingGroup;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.organization.management.service.model.ParentOrganizationDO;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link IdentityCompatibilitySettingsUtil}.
 */
@Listeners(MockitoTestNGListener.class)
public class IdentityCompatibilitySettingsUtilTest {

    private static final String TENANT_DOMAIN = "test.com";
    private static final String SETTING_GROUP = "testSettingGroup";
    private static final String SETTING_KEY = "testSetting";
    private static final String SETTING_VALUE = "testValue";
    private static final String RESOURCE_TYPE = "testResourceType";
    private static final String RESOURCE_NAME = "testResourceName";
    private static final String ORGANIZATION_ID = "test-org-id";
    private static final String ROOT_ORG_ID = "root-org-id";
    private static final String ROOT_TENANT_DOMAIN = "carbon.super";

    @Mock
    private OrganizationManager organizationManager;

    @Mock
    private Organization organization;

    @Mock
    private Organization rootOrganization;

    private MockedStatic<IdentityCompatibilitySettingsDataHolder> dataHolderMockedStatic;
    private MockedStatic<CompatibilitySettingCache> cacheMockedStatic;
    private IdentityCompatibilitySettingsDataHolder dataHolder;
    private CompatibilitySettingCache cache;

    @BeforeMethod
    public void setUp() {

        dataHolder = mock(IdentityCompatibilitySettingsDataHolder.class);
        cache = mock(CompatibilitySettingCache.class);

        dataHolderMockedStatic = mockStatic(IdentityCompatibilitySettingsDataHolder.class);
        cacheMockedStatic = mockStatic(CompatibilitySettingCache.class);

        dataHolderMockedStatic.when(IdentityCompatibilitySettingsDataHolder::getInstance).thenReturn(dataHolder);
        cacheMockedStatic.when(CompatibilitySettingCache::getInstance).thenReturn(cache);
    }

    @AfterMethod
    public void tearDown() {

        if (dataHolderMockedStatic != null) {
            dataHolderMockedStatic.close();
        }
        if (cacheMockedStatic != null) {
            cacheMockedStatic.close();
        }
    }

    /**
     * Test handleServerException with throwable and data parameters.
     */
    @Test
    public void testHandleServerExceptionWithThrowable() {

        Exception cause = new Exception("Test cause");
        CompatibilitySettingServerException exception = IdentityCompatibilitySettingsUtil.handleServerException(
                ErrorMessages.ERROR_CODE_FILE_NOT_FOUND, cause, "test-file.json");

        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), ErrorMessages.ERROR_CODE_FILE_NOT_FOUND.getCode());
        assertEquals(exception.getMessage(), ErrorMessages.ERROR_CODE_FILE_NOT_FOUND.getMessage());
        assertTrue(exception.getDescription().contains("test-file.json"));
        assertEquals(exception.getCause(), cause);
    }

    /**
     * Test handleServerException without throwable.
     */
    @Test
    public void testHandleServerExceptionWithoutThrowable() {

        CompatibilitySettingServerException exception = IdentityCompatibilitySettingsUtil.handleServerException(
                ErrorMessages.ERROR_CODE_FILE_NOT_FOUND, "test-file.json");

        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), ErrorMessages.ERROR_CODE_FILE_NOT_FOUND.getCode());
        assertEquals(exception.getMessage(), ErrorMessages.ERROR_CODE_FILE_NOT_FOUND.getMessage());
        assertTrue(exception.getDescription().contains("test-file.json"));
        assertNull(exception.getCause());
    }

    /**
     * Test handleServerException without data parameters.
     */
    @Test
    public void testHandleServerExceptionWithoutDataParams() {

        CompatibilitySettingServerException exception = IdentityCompatibilitySettingsUtil.handleServerException(
                ErrorMessages.ERROR_CODE_COMPATIBILITY_SETTING_MANAGER_NOT_INITIALIZED);

        assertNotNull(exception);
        assertEquals(exception.getErrorCode(),
                ErrorMessages.ERROR_CODE_COMPATIBILITY_SETTING_MANAGER_NOT_INITIALIZED.getCode());
    }

    /**
     * Test handleClientException with throwable and data parameters.
     */
    @Test
    public void testHandleClientExceptionWithThrowable() {

        Exception cause = new Exception("Test cause");
        CompatibilitySettingClientException exception = IdentityCompatibilitySettingsUtil.handleClientException(
                ErrorMessages.ERROR_CODE_UNSUPPORTED_SETTING_GROUP, cause, "invalidGroup");

        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), ErrorMessages.ERROR_CODE_UNSUPPORTED_SETTING_GROUP.getCode());
        assertEquals(exception.getMessage(), ErrorMessages.ERROR_CODE_UNSUPPORTED_SETTING_GROUP.getMessage());
        assertTrue(exception.getDescription().contains("invalidGroup"));
        assertEquals(exception.getCause(), cause);
    }

    /**
     * Test handleClientException without throwable.
     */
    @Test
    public void testHandleClientExceptionWithoutThrowable() {

        CompatibilitySettingClientException exception = IdentityCompatibilitySettingsUtil.handleClientException(
                ErrorMessages.ERROR_CODE_UNSUPPORTED_SETTING_GROUP, "invalidGroup");

        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), ErrorMessages.ERROR_CODE_UNSUPPORTED_SETTING_GROUP.getCode());
        assertTrue(exception.getDescription().contains("invalidGroup"));
        assertNull(exception.getCause());
    }

    /**
     * Test handleClientException without data parameters.
     */
    @Test
    public void testHandleClientExceptionWithoutDataParams() {

        CompatibilitySettingClientException exception = IdentityCompatibilitySettingsUtil.handleClientException(
                ErrorMessages.ERROR_CODE_INVALID_SETTING_GROUP_NAME);

        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), ErrorMessages.ERROR_CODE_INVALID_SETTING_GROUP_NAME.getCode());
    }

    /**
     * Test buildCompatibilitySettingGroupFromResource with valid resource.
     */
    @Test
    public void testBuildCompatibilitySettingGroupFromResourceWithValidResource() {

        Resource resource = new Resource();
        resource.setResourceName(RESOURCE_NAME);
        resource.setResourceType(RESOURCE_TYPE);

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(SETTING_KEY, SETTING_VALUE));
        attributes.add(new Attribute("anotherKey", "anotherValue"));
        resource.setAttributes(attributes);

        CompatibilitySettingGroup result =
                IdentityCompatibilitySettingsUtil.buildCompatibilitySettingGroupFromResource(resource, SETTING_GROUP);

        assertNotNull(result);
        assertEquals(result.getSettingGroup(), SETTING_GROUP);
        assertEquals(result.getSettingValue(SETTING_KEY), SETTING_VALUE);
        assertEquals(result.getSettingValue("anotherKey"), "anotherValue");
    }

    /**
     * Test buildCompatibilitySettingGroupFromResource with null resource.
     */
    @Test
    public void testBuildCompatibilitySettingGroupFromResourceWithNullResource() {

        CompatibilitySettingGroup result =
                IdentityCompatibilitySettingsUtil.buildCompatibilitySettingGroupFromResource(null, SETTING_GROUP);

        assertNull(result);
    }

    /**
     * Test buildCompatibilitySettingGroupFromResource with empty attributes.
     */
    @Test
    public void testBuildCompatibilitySettingGroupFromResourceWithEmptyAttributes() {

        Resource resource = new Resource();
        resource.setResourceName(RESOURCE_NAME);
        resource.setAttributes(new ArrayList<>());

        CompatibilitySettingGroup result =
                IdentityCompatibilitySettingsUtil.buildCompatibilitySettingGroupFromResource(resource, SETTING_GROUP);

        assertNotNull(result);
        assertEquals(result.getSettingGroup(), SETTING_GROUP);
        assertTrue(result.getSettings().isEmpty());
    }

    /**
     * Test buildCompatibilitySettingGroupFromResource with null attributes.
     */
    @Test
    public void testBuildCompatibilitySettingGroupFromResourceWithNullAttributes() {

        Resource resource = new Resource();
        resource.setResourceName(RESOURCE_NAME);
        resource.setAttributes(null);

        CompatibilitySettingGroup result =
                IdentityCompatibilitySettingsUtil.buildCompatibilitySettingGroupFromResource(resource, SETTING_GROUP);

        assertNotNull(result);
        assertEquals(result.getSettingGroup(), SETTING_GROUP);
        assertTrue(result.getSettings().isEmpty());
    }

    /**
     * Test buildCompatibilitySettingGroupFromResource with blank attribute key.
     */
    @Test
    public void testBuildCompatibilitySettingGroupFromResourceWithBlankKey() {

        Resource resource = new Resource();
        resource.setResourceName(RESOURCE_NAME);

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("", SETTING_VALUE));
        attributes.add(new Attribute(null, "anotherValue"));
        attributes.add(new Attribute(SETTING_KEY, SETTING_VALUE));
        resource.setAttributes(attributes);

        CompatibilitySettingGroup result =
                IdentityCompatibilitySettingsUtil.buildCompatibilitySettingGroupFromResource(resource, SETTING_GROUP);

        assertNotNull(result);
        assertEquals(result.getSettings().size(), 1);
        assertEquals(result.getSettingValue(SETTING_KEY), SETTING_VALUE);
    }

    /**
     * Test buildResourceFromCompatibilitySettingGroup with valid setting group.
     */
    @Test
    public void testBuildResourceFromCompatibilitySettingGroup() {

        CompatibilitySettingGroup settingGroup = new CompatibilitySettingGroup();
        settingGroup.setSettingGroup(SETTING_GROUP);
        settingGroup.addSetting(SETTING_KEY, SETTING_VALUE);
        settingGroup.addSetting("anotherKey", "anotherValue");

        Resource result = IdentityCompatibilitySettingsUtil.buildResourceFromCompatibilitySettingGroup(
                RESOURCE_TYPE, RESOURCE_NAME, settingGroup);

        assertNotNull(result);
        assertEquals(result.getResourceName(), RESOURCE_NAME);
        assertEquals(result.getResourceType(), RESOURCE_TYPE);
        assertEquals(result.getAttributes().size(), 2);
    }

    /**
     * Data provider for getOrganizationCreationTime tests.
     *
     * @return Test data.
     */
    @DataProvider(name = "emptyTenantDomainProvider")
    public Object[][] emptyTenantDomainProvider() {

        return new Object[][]{
                {null},
                {""},
                {"  "}
        };
    }

    /**
     * Test getOrganizationCreationTime with empty tenant domain.
     *
     * @param tenantDomain Tenant domain.
     */
    @Test(dataProvider = "emptyTenantDomainProvider")
    public void testGetOrganizationCreationTimeWithEmptyTenantDomain(String tenantDomain)
            throws OrganizationManagementException {

        Instant result = IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(tenantDomain, false);
        assertNull(result);
    }

    /**
     * Test getOrganizationCreationTime when OrganizationManager is not available.
     */
    @Test
    public void testGetOrganizationCreationTimeWithNullOrganizationManager()
            throws OrganizationManagementException {

        when(dataHolder.getOrganizationManager()).thenReturn(null);

        Instant result = IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(TENANT_DOMAIN, false);
        assertNull(result);
    }

    /**
     * Test getOrganizationCreationTime when organization ID is not found.
     */
    @Test
    public void testGetOrganizationCreationTimeWithNoOrganizationId() throws Exception {

        when(dataHolder.getOrganizationManager()).thenReturn(organizationManager);
        when(organizationManager.resolveOrganizationId(TENANT_DOMAIN)).thenReturn(null);

        Instant result = IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(TENANT_DOMAIN, false);
        assertNull(result);
    }

    /**
     * Test getOrganizationCreationTime when organization is not found.
     */
    @Test
    public void testGetOrganizationCreationTimeWithNoOrganization() throws Exception {

        when(dataHolder.getOrganizationManager()).thenReturn(organizationManager);
        when(organizationManager.resolveOrganizationId(TENANT_DOMAIN)).thenReturn(ORGANIZATION_ID);
        when(organizationManager.getOrganization(ORGANIZATION_ID, false, false)).thenReturn(null);

        Instant result = IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(TENANT_DOMAIN, false);
        assertNull(result);
    }

    /**
     * Test getOrganizationCreationTime with valid organization without inheritance.
     */
    @Test
    public void testGetOrganizationCreationTimeWithoutInheritance() throws Exception {

        Instant createdTime = Instant.now();

        when(dataHolder.getOrganizationManager()).thenReturn(organizationManager);
        when(organizationManager.resolveOrganizationId(TENANT_DOMAIN)).thenReturn(ORGANIZATION_ID);
        when(organizationManager.getOrganization(ORGANIZATION_ID, false, false)).thenReturn(organization);
        when(organization.getCreated()).thenReturn(createdTime);

        Instant result = IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(TENANT_DOMAIN, false);
        assertEquals(result, createdTime);
    }

    /**
     * Test getOrganizationCreationTime with inheritance enabled.
     */
    @Test
    public void testGetOrganizationCreationTimeWithInheritance() throws Exception {

        Instant rootCreatedTime = Instant.now().minusSeconds(3600);
        ParentOrganizationDO parentOrganization = mock(ParentOrganizationDO.class);

        when(dataHolder.getOrganizationManager()).thenReturn(organizationManager);
        when(organizationManager.resolveOrganizationId(TENANT_DOMAIN)).thenReturn(ORGANIZATION_ID);
        when(organizationManager.getOrganization(ORGANIZATION_ID, false, false)).thenReturn(organization);
        when(organization.getId()).thenReturn(ORGANIZATION_ID);
        when(organization.getParent()).thenReturn(parentOrganization);

        when(organizationManager.resolveOrganizationId(ROOT_TENANT_DOMAIN)).thenReturn(ROOT_ORG_ID);
        when(organizationManager.getOrganization(ROOT_ORG_ID, false, false)).thenReturn(rootOrganization);
        when(rootOrganization.getCreated()).thenReturn(rootCreatedTime);

        try (MockedStatic<OrganizationManagementUtil> orgUtilMockedStatic =
                     mockStatic(OrganizationManagementUtil.class);
             MockedStatic<FrameworkUtils> frameworkUtilsMockedStatic = mockStatic(FrameworkUtils.class)) {

            orgUtilMockedStatic.when(() -> OrganizationManagementUtil.getRootOrgTenantDomainBySubOrgTenantDomain(
                    TENANT_DOMAIN)).thenReturn(ROOT_TENANT_DOMAIN);
            frameworkUtilsMockedStatic.when(() -> FrameworkUtils.startTenantFlow(anyString())).thenAnswer(i -> null);
            frameworkUtilsMockedStatic.when(FrameworkUtils::endTenantFlow).thenAnswer(i -> null);

            Instant result = IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(TENANT_DOMAIN, true);
            assertEquals(result, rootCreatedTime);
        }
    }

    /**
     * Test getOrganizationCreationTime when exception occurs.
     */
    @Test(expectedExceptions = OrganizationManagementException.class)
    public void testGetOrganizationCreationTimeWithException() throws Exception {

        when(dataHolder.getOrganizationManager()).thenReturn(organizationManager);
        when(organizationManager.resolveOrganizationId(TENANT_DOMAIN))
                .thenThrow(new OrganizationManagementException("Test error"));

        IdentityCompatibilitySettingsUtil.getOrganizationCreationTime(TENANT_DOMAIN, false);
    }

    /**
     * Test addToCache with valid parameters.
     */
    @Test
    public void testAddToCache() {

        CompatibilitySetting compatibilitySetting = new CompatibilitySetting();
        CompatibilitySettingGroup group = new CompatibilitySettingGroup();
        group.setSettingGroup(SETTING_GROUP);
        group.addSetting(SETTING_KEY, SETTING_VALUE);
        compatibilitySetting.addCompatibilitySetting(group);

        IdentityCompatibilitySettingsUtil.addToCache(TENANT_DOMAIN, compatibilitySetting);

        // Verify cache.addToCache was called with correct parameters.
        verify(cache, times(1)).addToCache(eq(TENANT_DOMAIN), any(CompatibilitySettingCacheEntry.class));
    }

    /**
     * Test addToCache with null tenant domain.
     */
    @Test
    public void testAddToCacheWithNullTenantDomain() {

        CompatibilitySetting compatibilitySetting = new CompatibilitySetting();
        IdentityCompatibilitySettingsUtil.addToCache(null, compatibilitySetting);

        // Verify cache was never accessed when tenant domain is null.
        verify(cache, never()).addToCache(anyString(), any(CompatibilitySettingCacheEntry.class));
    }

    /**
     * Test addToCache with null compatibility setting.
     */
    @Test
    public void testAddToCacheWithNullSetting() {

        IdentityCompatibilitySettingsUtil.addToCache(TENANT_DOMAIN, null);

        // Verify cache was never accessed when setting is null.
        verify(cache, never()).addToCache(anyString(), any(CompatibilitySettingCacheEntry.class));
    }

    /**
     * Test getFromCache with valid tenant domain.
     */
    @Test
    public void testGetFromCache() {

        CompatibilitySetting expectedSetting = new CompatibilitySetting();
        CompatibilitySettingGroup group = new CompatibilitySettingGroup();
        group.setSettingGroup(SETTING_GROUP);
        group.addSetting(SETTING_KEY, SETTING_VALUE);
        expectedSetting.addCompatibilitySetting(group);

        CompatibilitySettingCacheEntry cacheEntry = new CompatibilitySettingCacheEntry(expectedSetting);
        when(cache.getFromCache(TENANT_DOMAIN)).thenReturn(cacheEntry);

        CompatibilitySetting result = IdentityCompatibilitySettingsUtil.getFromCache(TENANT_DOMAIN);

        assertNotNull(result);
        assertNotNull(result.getCompatibilitySettings());
        assertEquals(result.getCompatibilitySettings().size(), 1);
        assertEquals(result.getCompatibilitySetting(SETTING_GROUP).getSettingValue(SETTING_KEY), SETTING_VALUE);
        verify(cache, times(1)).getFromCache(TENANT_DOMAIN);
    }

    /**
     * Test getFromCache with null tenant domain.
     */
    @Test
    public void testGetFromCacheWithNullTenantDomain() {

        CompatibilitySetting result = IdentityCompatibilitySettingsUtil.getFromCache(null);

        assertNull(result);
        verify(cache, never()).getFromCache(anyString());
    }

    /**
     * Test getFromCache when cache entry is not found.
     */
    @Test
    public void testGetFromCacheWhenNotFound() {

        when(cache.getFromCache(TENANT_DOMAIN)).thenReturn(null);

        CompatibilitySetting result = IdentityCompatibilitySettingsUtil.getFromCache(TENANT_DOMAIN);

        assertNull(result);
        verify(cache, times(1)).getFromCache(TENANT_DOMAIN);
    }

    /**
     * Test getFromCache with setting group parameter.
     */
    @Test
    public void testGetFromCacheWithSettingGroup() {

        CompatibilitySetting expectedSetting = new CompatibilitySetting();
        CompatibilitySettingGroup group = new CompatibilitySettingGroup();
        group.setSettingGroup(SETTING_GROUP);
        group.addSetting(SETTING_KEY, SETTING_VALUE);
        expectedSetting.addCompatibilitySetting(group);

        CompatibilitySettingCacheEntry cacheEntry = new CompatibilitySettingCacheEntry(expectedSetting);
        when(cache.getFromCache(TENANT_DOMAIN)).thenReturn(cacheEntry);

        CompatibilitySetting result = IdentityCompatibilitySettingsUtil.getFromCache(TENANT_DOMAIN, SETTING_GROUP);

        assertNotNull(result);
        assertNotNull(result.getCompatibilitySettings());
        assertEquals(result.getCompatibilitySettings().size(), 1);
        assertNotNull(result.getCompatibilitySettings().get(SETTING_GROUP));
        assertEquals(result.getCompatibilitySetting(SETTING_GROUP).getSettingValue(SETTING_KEY), SETTING_VALUE);
        verify(cache, times(1)).getFromCache(TENANT_DOMAIN);
    }

    /**
     * Test getFromCache with setting group when tenant domain is null.
     */
    @Test
    public void testGetFromCacheWithSettingGroupNullTenantDomain() {

        CompatibilitySetting result = IdentityCompatibilitySettingsUtil.getFromCache(null, SETTING_GROUP);

        assertNull(result);
        verify(cache, never()).getFromCache(anyString());
    }

    /**
     * Test getFromCache with setting group when setting group is null.
     */
    @Test
    public void testGetFromCacheWithNullSettingGroup() {

        CompatibilitySetting result = IdentityCompatibilitySettingsUtil.getFromCache(TENANT_DOMAIN, null);

        assertNull(result);
        verify(cache, never()).getFromCache(anyString());
    }

    /**
     * Test getFromCache with setting group when group is not found in cache.
     */
    @Test
    public void testGetFromCacheWithSettingGroupNotFound() {

        CompatibilitySetting cachedSetting = new CompatibilitySetting();
        CompatibilitySettingGroup group = new CompatibilitySettingGroup();
        group.setSettingGroup("differentGroup");
        cachedSetting.addCompatibilitySetting(group);

        CompatibilitySettingCacheEntry cacheEntry = new CompatibilitySettingCacheEntry(cachedSetting);
        when(cache.getFromCache(TENANT_DOMAIN)).thenReturn(cacheEntry);

        CompatibilitySetting result = IdentityCompatibilitySettingsUtil.getFromCache(TENANT_DOMAIN, SETTING_GROUP);

        assertNull(result);
        verify(cache, times(1)).getFromCache(TENANT_DOMAIN);
    }

    /**
     * Test updateCache with existing cache entry.
     */
    @Test
    public void testUpdateCacheWithExistingSetting() {

        CompatibilitySetting existingSetting = new CompatibilitySetting();
        CompatibilitySettingGroup existingGroup = new CompatibilitySettingGroup();
        existingGroup.setSettingGroup(SETTING_GROUP);
        existingGroup.addSetting("existingKey", "existingValue");
        existingSetting.addCompatibilitySetting(existingGroup);

        CompatibilitySettingCacheEntry cacheEntry = new CompatibilitySettingCacheEntry(existingSetting);
        when(cache.getFromCache(TENANT_DOMAIN)).thenReturn(cacheEntry);

        CompatibilitySetting newSetting = new CompatibilitySetting();
        CompatibilitySettingGroup newGroup = new CompatibilitySettingGroup();
        newGroup.setSettingGroup(SETTING_GROUP);
        newGroup.addSetting(SETTING_KEY, SETTING_VALUE);
        newSetting.addCompatibilitySetting(newGroup);

        IdentityCompatibilitySettingsUtil.updateCache(TENANT_DOMAIN, newSetting);

        // Verify cache was retrieved and updateCache was called (not addToCache).
        verify(cache, times(1)).getFromCache(TENANT_DOMAIN);
        verify(cache, times(1)).updateCache(eq(TENANT_DOMAIN), any(CompatibilitySettingCacheEntry.class));
        verify(cache, never()).addToCache(anyString(), any(CompatibilitySettingCacheEntry.class));
    }

    /**
     * Test updateCache when no existing cache entry.
     */
    @Test
    public void testUpdateCacheWithNoExistingSetting() {

        when(cache.getFromCache(TENANT_DOMAIN)).thenReturn(null);

        CompatibilitySetting newSetting = new CompatibilitySetting();
        CompatibilitySettingGroup newGroup = new CompatibilitySettingGroup();
        newGroup.setSettingGroup(SETTING_GROUP);
        newGroup.addSetting(SETTING_KEY, SETTING_VALUE);
        newSetting.addCompatibilitySetting(newGroup);

        IdentityCompatibilitySettingsUtil.updateCache(TENANT_DOMAIN, newSetting);

        // Verify cache was retrieved and addToCache was called (not updateCache).
        verify(cache, times(1)).getFromCache(TENANT_DOMAIN);
        verify(cache, times(1)).addToCache(eq(TENANT_DOMAIN), any(CompatibilitySettingCacheEntry.class));
        verify(cache, never()).updateCache(anyString(), any(CompatibilitySettingCacheEntry.class));
    }

    /**
     * Test updateCache with null tenant domain.
     */
    @Test
    public void testUpdateCacheWithNullTenantDomain() {

        CompatibilitySetting setting = new CompatibilitySetting();
        IdentityCompatibilitySettingsUtil.updateCache(null, setting);

        // Verify cache was never accessed when tenant domain is null.
        verify(cache, never()).getFromCache(anyString());
        verify(cache, never()).addToCache(anyString(), any(CompatibilitySettingCacheEntry.class));
    }

    /**
     * Test updateCache with null setting.
     */
    @Test
    public void testUpdateCacheWithNullSetting() {

        IdentityCompatibilitySettingsUtil.updateCache(TENANT_DOMAIN, null);

        // Verify cache was never accessed when setting is null.
        verify(cache, never()).getFromCache(anyString());
        verify(cache, never()).addToCache(anyString(), any(CompatibilitySettingCacheEntry.class));
    }

    /**
     * Test clearCache with valid tenant domain.
     */
    @Test
    public void testClearCache() {

        IdentityCompatibilitySettingsUtil.clearCache(TENANT_DOMAIN);

        // Verify cache clear was called with correct tenant domain.
        verify(cache, times(1)).clearFromCache(TENANT_DOMAIN);
    }

    /**
     * Test clearCache with null tenant domain.
     */
    @Test
    public void testClearCacheWithNullTenantDomain() {

        IdentityCompatibilitySettingsUtil.clearCache(null);

        // Verify cache clear was never called when tenant domain is null.
        verify(cache, never()).clearFromCache(anyString());
    }

    /**
     * Test clearCache with empty tenant domain.
     */
    @Test
    public void testClearCacheWithEmptyTenantDomain() {

        IdentityCompatibilitySettingsUtil.clearCache("");

        // Verify cache clear was never called when tenant domain is empty.
        verify(cache, never()).clearFromCache(anyString());
    }
}
