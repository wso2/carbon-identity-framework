/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.handler.approles.impl;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.IdPGroup;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.RoleV2;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Unit tests for {@link AppAssociatedRolesResolverImpl}.
 */
public class AppAssociatedRolesResolverImplTest {

    private final String applicationId = "testApp";
    private final String tenantDomain = "testTenant";
    private final String idpGroupClaimURI = "testIdPGroupClaimURI1";
    private final AppAssociatedRolesResolverImpl resolver = new AppAssociatedRolesResolverImpl();

    private AutoCloseable closeable;

    @Mock
    private IdentityProvider identityProvider;
    @Mock
    private RoleManagementService roleManagementService;
    @Mock
    private ApplicationManagementService applicationManagementService;
    @Mock
    private FrameworkServiceDataHolder frameworkServiceDataHolderInstance;

    private MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolderMock;
    private MockedStatic<FrameworkUtils> frameworkUtilsMock;

    @BeforeClass
    public void setup() {

        closeable = MockitoAnnotations.openMocks(this);

        frameworkServiceDataHolderMock = mockStatic(FrameworkServiceDataHolder.class);
        frameworkUtilsMock = mockStatic(FrameworkUtils.class);

        frameworkUtilsMock.when(FrameworkUtils::getIdpGroupClaimValueSeparator).thenReturn(",");
        frameworkServiceDataHolderMock.when(FrameworkServiceDataHolder::getInstance)
                .thenReturn(frameworkServiceDataHolderInstance);

        when(frameworkServiceDataHolderInstance.getRoleManagementServiceV2()).thenReturn(roleManagementService);
        when(frameworkServiceDataHolderInstance.getApplicationManagementService()).thenReturn(
                applicationManagementService);
    }

    @AfterClass
    public void tearDown() throws Exception {

        if (frameworkServiceDataHolderMock != null) {
            frameworkServiceDataHolderMock.close();
        }
        if (frameworkUtilsMock != null) {
            frameworkUtilsMock.close();
        }
        closeable.close();
    }

    @Test
    public void testGetAppAssociatedRolesOfFederatedUserForIdPGroupFiltering() throws Exception {

        Map<ClaimMapping, String> userAttributes = generateTestFederatedUserAttributes();
        IdPGroup[] idpGroups = generateTestIdPGroupListForIdPConfiguration(2);
        List<RoleV2> appRoles = generateTestRoleListForAppAssociation(2);

        when(identityProvider.getIdPGroupConfig()).thenReturn(idpGroups);
        when(roleManagementService.getRoleIdListOfIdpGroups(Arrays.asList("idpGroupId1", "idpGroupId2"),
                tenantDomain))
                .thenReturn(Arrays.asList("roleId1", "roleId2"));
        when(applicationManagementService.getAssociatedRolesOfApplication(applicationId, tenantDomain))
                .thenReturn(appRoles);

        String[] resolvedRoles = resolver.getAppAssociatedRolesOfFederatedUser(
                userAttributes, identityProvider, applicationId, idpGroupClaimURI, tenantDomain);

        assertEquals(resolvedRoles, new String[]{"Internal/role1", "Internal/role2"});
    }

    @Test
    public void testGetAppAssociatedRolesOfFederatedUserForAppRoleFiltering() throws Exception {

        Map<ClaimMapping, String> userAttributes = generateTestFederatedUserAttributes();
        IdPGroup[] idpGroups = generateTestIdPGroupListForIdPConfiguration(3);
        List<RoleV2> appRoles = generateTestRoleListForAppAssociation(1);

        when(identityProvider.getIdPGroupConfig()).thenReturn(idpGroups);
        when(roleManagementService.getRoleIdListOfIdpGroups(
                Arrays.asList("idpGroupId1", "idpGroupId2", "idpGroupId3"), tenantDomain))
                .thenReturn(Arrays.asList("roleId1", "roleId2"));
        when(applicationManagementService.getAssociatedRolesOfApplication(applicationId, tenantDomain))
                .thenReturn(appRoles);

        String[] resolvedRoles = resolver.getAppAssociatedRolesOfFederatedUser(
                userAttributes, identityProvider, applicationId, idpGroupClaimURI, tenantDomain);

        assertEquals(resolvedRoles, new String[]{"Internal/role1"});
    }

    private Map<ClaimMapping, String> generateTestFederatedUserAttributes() {

        Map<ClaimMapping, String> attributes = new HashMap<>();
        attributes.put(ClaimMapping.build("claim1", "claim1", null, false), "value1");
        attributes.put(ClaimMapping.build(idpGroupClaimURI, idpGroupClaimURI, null, false),
                "idpGroup1,idpGroup2,idpGroup3");
        return attributes;
    }

    private IdPGroup[] generateTestIdPGroupListForIdPConfiguration(int count) {

        IdPGroup[] idPGroups = new IdPGroup[count];

        for (int i = 0; i < count; i++) {
            IdPGroup idPGroup = new IdPGroup();
            idPGroup.setIdpGroupId("idpGroupId" + (i + 1));
            idPGroup.setIdpGroupName("idpGroup" + (i + 1));
            idPGroups[i] = idPGroup;
        }
        return idPGroups;
    }

    private List<RoleV2> generateTestRoleListForAppAssociation(int count) {

        List<RoleV2> roles = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            RoleV2 role = new RoleV2();
            role.setId("roleId" + (i + 1));
            role.setName("role" + (i + 1));
            roles.add(role);
        }
        return roles;
    }
}
