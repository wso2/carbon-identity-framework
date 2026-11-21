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

package org.wso2.carbon.identity.application.authentication.framework.util;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingServerException;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySetting;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingGroup;
import org.wso2.carbon.identity.compatibility.settings.core.service.CompatibilitySettingsService;
import org.wso2.carbon.identity.organization.management.organization.user.sharing.OrganizationUserSharingService;
import org.wso2.carbon.identity.organization.management.organization.user.sharing.models.UserAssociation;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;

/**
 * Unit tests for the shared user subject identifier utilities in FrameworkUtils.
 */
public class SharedUserSubjectUtilsTest {

    private static final String TENANT_DOMAIN = "suborga";
    private static final String RESIDENT_USER_ID = "2a160d20-9bb2-41ed-8ecb-44e99d581963";
    private static final String SHARED_USER_ID = "f411c2c0-5437-4088-bec2-adf562ebe26a";
    private static final String ACCESSING_ORG_ID = "2d4e28f5-9bbc-4e2d-b880-426b272708c7";

    @Mock
    private CompatibilitySettingsService compatibilitySettingsService;
    @Mock
    private OrganizationUserSharingService organizationUserSharingService;
    @Mock
    private UserAssociation userAssociation;

    private AutoCloseable closeable;

    @BeforeMethod
    public void setUp() {

        closeable = MockitoAnnotations.openMocks(this);
        FrameworkServiceDataHolder.getInstance().setCompatibilitySettingsService(compatibilitySettingsService);
        FrameworkServiceDataHolder.getInstance().setOrganizationUserSharingService(organizationUserSharingService);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        FrameworkServiceDataHolder.getInstance().setCompatibilitySettingsService(null);
        FrameworkServiceDataHolder.getInstance().setOrganizationUserSharingService(null);
        closeable.close();
    }

    @DataProvider(name = "settingValueProvider")
    public Object[][] settingValueProvider() {

        return new Object[][]{
                {"true", true},
                {"false", false},
                {null, false},
        };
    }

    @Test(dataProvider = "settingValueProvider")
    public void testIsSharedUserIdAsSubjectEnabled(String settingValue, boolean expected) throws Exception {

        CompatibilitySetting setting = new CompatibilitySetting();
        if (settingValue != null) {
            CompatibilitySettingGroup group = new CompatibilitySettingGroup();
            group.setSettingGroup(FrameworkConstants.Config.SHARED_USER_COMPATIBILITY_SETTING_GROUP);
            group.addSetting(FrameworkConstants.Config.USE_SHARED_USER_ID_AS_SUBJECT_COMPATIBILITY_SETTING,
                    settingValue);
            setting.addCompatibilitySetting(group);
        }
        when(compatibilitySettingsService.getCompatibilitySettingsByGroupAndSetting(TENANT_DOMAIN,
                FrameworkConstants.Config.SHARED_USER_COMPATIBILITY_SETTING_GROUP,
                FrameworkConstants.Config.USE_SHARED_USER_ID_AS_SUBJECT_COMPATIBILITY_SETTING)).thenReturn(setting);

        assertEquals(FrameworkUtils.isSharedUserIdAsSubjectEnabled(TENANT_DOMAIN), expected);
    }

    @Test
    public void testIsSharedUserIdAsSubjectEnabledWhenEvaluationFails() throws Exception {

        when(compatibilitySettingsService.getCompatibilitySettingsByGroupAndSetting(anyString(), anyString(),
                anyString())).thenThrow(new CompatibilitySettingServerException("error"));

        assertFalse(FrameworkUtils.isSharedUserIdAsSubjectEnabled(TENANT_DOMAIN));
    }

    @Test
    public void testIsSharedUserIdAsSubjectEnabledWithoutService() {

        FrameworkServiceDataHolder.getInstance().setCompatibilitySettingsService(null);

        assertFalse(FrameworkUtils.isSharedUserIdAsSubjectEnabled(TENANT_DOMAIN));
        assertFalse(FrameworkUtils.isSharedUserIdAsSubjectEnabled(null));
    }

    @Test
    public void testResolveSharedUserIdFromAuthenticatedUser() throws Exception {

        AuthenticatedUser user = getSharedUser();
        user.setSharedUserId(SHARED_USER_ID);

        assertEquals(FrameworkUtils.resolveSharedUserIdOfAccessingOrganization(user), SHARED_USER_ID);
        verify(organizationUserSharingService, never()).getUserAssociationOfAssociatedUserByOrgId(anyString(),
                anyString());
    }

    @Test
    public void testResolveSharedUserIdFromUserAssociation() throws Exception {

        when(userAssociation.getUserId()).thenReturn(SHARED_USER_ID);
        when(organizationUserSharingService.getUserAssociationOfAssociatedUserByOrgId(RESIDENT_USER_ID,
                ACCESSING_ORG_ID)).thenReturn(userAssociation);

        assertEquals(FrameworkUtils.resolveSharedUserIdOfAccessingOrganization(getSharedUser()), SHARED_USER_ID);
    }

    @Test
    public void testResolveSharedUserIdWithoutUserAssociation() throws Exception {

        when(organizationUserSharingService.getUserAssociationOfAssociatedUserByOrgId(RESIDENT_USER_ID,
                ACCESSING_ORG_ID)).thenReturn(null);

        assertNull(FrameworkUtils.resolveSharedUserIdOfAccessingOrganization(getSharedUser()));
    }

    private AuthenticatedUser getSharedUser() {

        AuthenticatedUser user = new AuthenticatedUser();
        user.setUserId(RESIDENT_USER_ID);
        user.setSharedUser(true);
        user.setAccessingOrganization(ACCESSING_ORG_ID);
        return user;
    }
}
