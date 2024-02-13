/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.handler.approles.ApplicationRolesResolver;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.IdPGroup;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PrepareForTest({FrameworkServiceDataHolder.class, FrameworkUtils.class})
@PowerMockIgnore("org.mockito.*")
public class DefaultClaimHandlerTest {

    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private SequenceConfig sequenceConfig;
    @Mock
    private ApplicationConfig applicationConfig;
    @Mock
    FrameworkServiceDataHolder frameworkServiceDataHolder;
    @Mock
    ApplicationRolesResolver applicationRolesResolver;
    private static final String testIdP = "testIdP";
    private static final String idpGroupClaimName = "groups";
    private static final String idpGroupClaim = "admin,hr";
    private static final String testIdPGroupId = "testIdPGroupId";
    private static final String testIdPGroupName = "admin";
    private static final String applicationId = "testAppId";
    private static final List<String> mappedApplicationRoles =
            new ArrayList<>(Arrays.asList("adminMapped", "hrMapped"));

    @BeforeMethod
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAppAssociatedRolesOfFederatedUser() throws Exception {

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

        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getMultiAttributeSeparator()).thenReturn(",");
        when(FrameworkUtils.getEffectiveIdpGroupClaimUri(eq(stepConfig), eq(authenticationContext))).thenReturn(
                idpGroupClaimName);
        when(FrameworkUtils.getAppAssociatedRolesOfFederatedUser(
                eq(authenticatedUser), eq(applicationId), eq(idpGroupClaimName))).thenReturn(mappedApplicationRoles);

        List<String> applicationRoles =
                defaultClaimHandler.getAppAssociatedRolesOfFederatedUser(stepConfig, authenticationContext);

        Assert.assertEquals(applicationRoles, mappedApplicationRoles);
    }

    @Test
    public void testGetAppAssociatedRolesOfLocalUser() throws Exception {

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

        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getMultiAttributeSeparator()).thenReturn(",");
        when(FrameworkUtils.getAppAssociatedRolesOfLocalUser(eq(authenticatedUser), eq(applicationId))).thenReturn(
                mappedApplicationRoles);

        List<String> applicationRoles =
                defaultClaimHandler.getAppAssociatedRolesOfLocalUser(stepConfig, authenticationContext);

        Assert.assertEquals(applicationRoles, mappedApplicationRoles);
    }
}
