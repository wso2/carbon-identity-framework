/*
 * Copyright (c) 2023-2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.handler.claims.impl;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.IdPGroup;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class DefaultClaimHandlerTest {

    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private SequenceConfig sequenceConfig;
    @Mock
    private ApplicationConfig applicationConfig;
    @Mock
    private AuthenticatedUser mockAuthenticatedUser;
    @Mock
    private RealmService realmService;
    @Mock
    private TenantManager tenantManager;
    @Mock
    private org.wso2.carbon.user.core.UserRealm userRealm;
    @Mock
    private ClaimManager claimManager;
    @Mock
    private AbstractUserStoreManager userStoreManager;
    @Mock
    private RealmConfiguration realmConfiguration;

    private static final String testIdP = "testIdP";
    private static final String idpGroupClaimName = "groups";
    private static final String testIdPGroupId = "testIdPGroupId";
    private static final String testIdPGroupName = "admin";
    private static final String applicationId = "testAppId";
    private static final List<String> mappedApplicationRoles =
            new ArrayList<>(Arrays.asList("adminMapped", "hrMapped"));

    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_TENANT_DOMAIN = "carbon.super";
    private static final int TEST_TENANT_ID = -1234;
    private static final String TEST_SHARED_USER_ID = "shared-user-id";
    private static final String TEST_ORG_ID = "test-org-id";
    private static final String TEST_ORG_TENANT_DOMAIN = "org.carbon.super";
    private static final String LOCAL_GROUPS_CLAIM_URI = "http://wso2.org/claims/groups";
    private static final String TEST_SP_CLAIM_URI = "http://sp.claims/email";
    private static final String TEST_LOCAL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    private static final String TEST_CLAIM_VALUE = "test@example.com";

    private AutoCloseable mockitoCloseable;

    @BeforeMethod
    public void setUp() throws Exception {

        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAppAssociatedRolesOfFederatedUser() throws Exception {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {
            DefaultClaimHandler defaultClaimHandler = new DefaultClaimHandler();

            StepConfig stepConfig = new StepConfig();
            stepConfig.setSubjectAttributeStep(true);
            stepConfig.setAuthenticatedIdP(testIdP);
            AuthenticatedUser authenticatedUser = new AuthenticatedUser();
            stepConfig.setAuthenticatedUser(authenticatedUser);

            ClaimMapping claimMapping = ClaimMapping.build(FrameworkConstants.GROUPS_CLAIM,
                    idpGroupClaimName, null, true, true);
            ClaimMapping[] idPClaimMappings = new ClaimMapping[1];
            idPClaimMappings[0] = claimMapping;

            IdPGroup possibleGroup = new IdPGroup();
            possibleGroup.setIdpGroupId(testIdPGroupId);
            possibleGroup.setIdpGroupName(testIdPGroupName);
            IdPGroup[] idPGroups = new IdPGroup[1];
            idPGroups[0] = possibleGroup;
            IdentityProvider identityProvider = new IdentityProvider();
            identityProvider.setIdPGroupConfig(idPGroups);
            ExternalIdPConfig externalIdPConfig = new ExternalIdPConfig(identityProvider);

            ServiceProvider serviceProvider = new ServiceProvider();
            serviceProvider.setApplicationResourceId(applicationId);

            when(authenticationContext.getSequenceConfig()).thenReturn(sequenceConfig);
            when(sequenceConfig.getApplicationConfig()).thenReturn(applicationConfig);
            when(applicationConfig.getServiceProvider()).thenReturn(serviceProvider);
            when(authenticationContext.getExternalIdP()).thenReturn(externalIdPConfig);

            frameworkUtils.when(FrameworkUtils::getMultiAttributeSeparator).thenReturn(",");
            frameworkUtils.when(() -> FrameworkUtils
                            .getEffectiveIdpGroupClaimUri(eq(stepConfig), eq(authenticationContext)))
                    .thenReturn(idpGroupClaimName);
            when(FrameworkUtils.getAppAssociatedRolesOfFederatedUser(
                    eq(authenticatedUser), eq(applicationId), eq(idpGroupClaimName))).thenReturn(
                    mappedApplicationRoles);

            List<String> applicationRoles =
                    defaultClaimHandler.getAppAssociatedRolesOfFederatedUser(stepConfig, authenticationContext);

            Assert.assertEquals(applicationRoles, mappedApplicationRoles);
        }
    }

    @AfterMethod
    public void tearDown() throws Exception {

        FrameworkServiceDataHolder.getInstance().setRealmService(null);
        FrameworkServiceDataHolder.getInstance().setOrganizationManager(null);
        mockitoCloseable.close();
    }

    @Test
    public void testGetAppAssociatedRolesOfLocalUser() throws Exception {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {

            DefaultClaimHandler defaultClaimHandler = new DefaultClaimHandler();

            StepConfig stepConfig = new StepConfig();
            stepConfig.setSubjectAttributeStep(true);
            AuthenticatedUser authenticatedUser = new AuthenticatedUser();
            stepConfig.setAuthenticatedUser(authenticatedUser);

            ServiceProvider serviceProvider = new ServiceProvider();
            serviceProvider.setApplicationResourceId(applicationId);

            when(authenticationContext.getSequenceConfig()).thenReturn(sequenceConfig);
            when(sequenceConfig.getApplicationConfig()).thenReturn(applicationConfig);
            when(applicationConfig.getServiceProvider()).thenReturn(serviceProvider);

            frameworkUtils.when(FrameworkUtils::getMultiAttributeSeparator).thenReturn(",");
            frameworkUtils.when(() -> FrameworkUtils
                            .getAppAssociatedRolesOfLocalUser(eq(authenticatedUser), eq(applicationId)))
                    .thenReturn(mappedApplicationRoles);

            List<String> applicationRoles =
                    defaultClaimHandler.getAppAssociatedRolesOfLocalUser(stepConfig, authenticationContext);

            Assert.assertEquals(applicationRoles, mappedApplicationRoles);
        }
    }

    @Test
    public void testHandleLocalClaimsWithUnsharedUser() throws Exception {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class);
             MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {

            CarbonConstants.ENABLE_LEGACY_AUTHZ_RUNTIME = false;

            StepConfig stepConfig = new StepConfig();
            stepConfig.setAuthenticatedUser(mockAuthenticatedUser);
            stepConfig.setSubjectAttributeStep(false);
            stepConfig.setSubjectIdentifierStep(false);

            Map<String, String> spToLocalClaims = new HashMap<>();
            spToLocalClaims.put(TEST_SP_CLAIM_URI, TEST_LOCAL_CLAIM_URI);
            Map<String, String> requestedClaims = new HashMap<>();
            requestedClaims.put(TEST_SP_CLAIM_URI, TEST_LOCAL_CLAIM_URI);

            when(authenticationContext.getSequenceConfig()).thenReturn(sequenceConfig);
            when(sequenceConfig.getApplicationConfig()).thenReturn(applicationConfig);
            when(applicationConfig.getClaimMappings()).thenReturn(spToLocalClaims);
            when(applicationConfig.getRequestedClaimMappings()).thenReturn(requestedClaims);
            when(authenticationContext.getRuntimeClaims()).thenReturn(new HashMap<>());
            when(authenticationContext.getRequestType()).thenReturn("oauth2");
            when(mockAuthenticatedUser.getTenantDomain()).thenReturn(TEST_TENANT_DOMAIN);
            when(mockAuthenticatedUser.getUserId()).thenReturn(TEST_USER_ID);
            when(mockAuthenticatedUser.getUserStoreDomain()).thenReturn("");

            frameworkUtils.when(() -> FrameworkUtils.getSharedUserIdentifiedInSequence(authenticationContext))
                    .thenReturn(Optional.empty());
            frameworkUtils.when(FrameworkUtils::getMultiAttributeSeparator).thenReturn(",");
            identityUtil.when(IdentityUtil::getLocalGroupsClaimURI).thenReturn(LOCAL_GROUPS_CLAIM_URI);

            FrameworkServiceDataHolder.getInstance().setRealmService(realmService);
            when(realmService.getTenantManager()).thenReturn(tenantManager);
            when(tenantManager.getTenantId(TEST_TENANT_DOMAIN)).thenReturn(TEST_TENANT_ID);
            when(realmService.getTenantUserRealm(TEST_TENANT_ID)).thenReturn(userRealm);
            when(userRealm.getClaimManager()).thenReturn(claimManager);
            when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
            when(claimManager.getAllClaimMappings(ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT))
                    .thenReturn(new org.wso2.carbon.user.api.ClaimMapping[0]);

            Map<String, String> userClaims = new HashMap<>();
            userClaims.put(TEST_LOCAL_CLAIM_URI, TEST_CLAIM_VALUE);
            when(userStoreManager.getUserClaimValuesWithID(eq(TEST_USER_ID), any(String[].class), isNull()))
                    .thenReturn(userClaims);
            when(userStoreManager.getRealmConfiguration()).thenReturn(realmConfiguration);
            when(realmConfiguration.getUserStoreProperty(anyString())).thenReturn(null);

            DefaultClaimHandler handler = new DefaultClaimHandler();
            Map<String, String> result = handler.handleLocalClaims(null, stepConfig, authenticationContext);

            Assert.assertEquals(result.get(TEST_SP_CLAIM_URI), TEST_CLAIM_VALUE);
        }
    }

    @Test
    public void testHandleLocalClaimsWithSharedUser() throws Exception {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class);
             MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {

            CarbonConstants.ENABLE_LEGACY_AUTHZ_RUNTIME = false;

            StepConfig stepConfig = new StepConfig();
            stepConfig.setAuthenticatedUser(mockAuthenticatedUser);
            stepConfig.setSubjectAttributeStep(false);
            stepConfig.setSubjectIdentifierStep(false);

            AuthenticatedUser sharedUser = Mockito.mock(AuthenticatedUser.class);
            when(sharedUser.getAccessingOrganization()).thenReturn(TEST_ORG_ID);
            when(sharedUser.getSharedUserId()).thenReturn(TEST_SHARED_USER_ID);

            Map<String, String> spToLocalClaims = new HashMap<>();
            spToLocalClaims.put(TEST_SP_CLAIM_URI, TEST_LOCAL_CLAIM_URI);
            Map<String, String> requestedClaims = new HashMap<>();
            requestedClaims.put(TEST_SP_CLAIM_URI, TEST_LOCAL_CLAIM_URI);

            when(authenticationContext.getSequenceConfig()).thenReturn(sequenceConfig);
            when(sequenceConfig.getApplicationConfig()).thenReturn(applicationConfig);
            when(applicationConfig.getClaimMappings()).thenReturn(spToLocalClaims);
            when(applicationConfig.getRequestedClaimMappings()).thenReturn(requestedClaims);
            when(authenticationContext.getRuntimeClaims()).thenReturn(new HashMap<>());
            when(authenticationContext.getRequestType()).thenReturn("oauth2");
            when(mockAuthenticatedUser.getTenantDomain()).thenReturn(TEST_TENANT_DOMAIN);
            when(mockAuthenticatedUser.getUserId()).thenReturn(TEST_USER_ID);
            when(mockAuthenticatedUser.getUserStoreDomain()).thenReturn("");

            frameworkUtils.when(() -> FrameworkUtils.getSharedUserIdentifiedInSequence(authenticationContext))
                    .thenReturn(Optional.of(sharedUser));
            frameworkUtils.when(FrameworkUtils::getMultiAttributeSeparator).thenReturn(",");
            identityUtil.when(IdentityUtil::getLocalGroupsClaimURI).thenReturn(LOCAL_GROUPS_CLAIM_URI);

            org.wso2.carbon.identity.organization.management.service.OrganizationManager orgManager =
                    Mockito.mock(
                            org.wso2.carbon.identity.organization.management.service.OrganizationManager.class);
            FrameworkServiceDataHolder.getInstance().setOrganizationManager(orgManager);
            when(orgManager.resolveTenantDomain(TEST_ORG_ID)).thenReturn(TEST_ORG_TENANT_DOMAIN);

            FrameworkServiceDataHolder.getInstance().setRealmService(realmService);
            when(realmService.getTenantManager()).thenReturn(tenantManager);
            when(tenantManager.getTenantId(TEST_ORG_TENANT_DOMAIN)).thenReturn(TEST_TENANT_ID);
            when(realmService.getTenantUserRealm(TEST_TENANT_ID)).thenReturn(userRealm);
            when(userRealm.getClaimManager()).thenReturn(claimManager);
            when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
            when(claimManager.getAllClaimMappings(ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT))
                    .thenReturn(new org.wso2.carbon.user.api.ClaimMapping[0]);

            Map<String, String> sharedUserClaims = new HashMap<>();
            sharedUserClaims.put(TEST_LOCAL_CLAIM_URI, TEST_CLAIM_VALUE);
            when(userStoreManager.getUserClaimValuesWithID(eq(TEST_SHARED_USER_ID), any(String[].class), isNull()))
                    .thenReturn(sharedUserClaims);
            when(userStoreManager.getRealmConfiguration()).thenReturn(realmConfiguration);
            when(realmConfiguration.getUserStoreProperty(anyString())).thenReturn(null);

            DefaultClaimHandler handler = new DefaultClaimHandler();
            Map<String, String> result = handler.handleLocalClaims(null, stepConfig, authenticationContext);

            // Claims are fetched using the shared user's ID in the org's tenant domain.
            Assert.assertEquals(result.get(TEST_SP_CLAIM_URI), TEST_CLAIM_VALUE);
        }
    }
}
