/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.api.idpmgt.endpoint.impl;

import org.powermock.core.classloader.annotations.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.junit.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.FederatedAuthenticatorConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.IdPDetailDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.JustInTimeProvisioningConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.PermissionsAndRoleConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ProvisioningConnectorConfigDTO;
import org.wso2.carbon.identity.application.common.model.ApplicationPermission;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.LocalRole;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.utils.CarbonUtils;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

@PrepareForTest({PrivilegedCarbonContext.class, CarbonUtils.class, IdentityProviderManager.class, IdentityUtil.class})
public class IdpsApiServiceImplTest extends PowerMockTestCase {

    @Mock
    private IdPDetailDTO idPDetailDTO;

    @Mock
    private IdentityProvider identityProvider;

    private final String name = "name";
    private final String idp = "idp";
    private final String provision = "provision";
    private final String federated = "federated";
    private final String prop = "property";
    private final String serverUrl = "serverUrl";
    private final String id = "idVal";
    private final String alias = "alias";
    private final String claim = "claim";
    private final String display = "display";
    private final String homeRealm = "homeRealm";
    private final String claimUri = "claimUri";
    private final String role = "role";
    private final String description = "description";
    private final String type = "type";
    private final String value = "value";
    private final String deflt = "default";
    private final String provisioningRole = "provisioningRole";
    private final String userStore = "userStore";

    @BeforeMethod
    public void setUp() throws IdentityProviderManagementException {

        initializeCarbonContext();
        initializeIdentityManager();
        initializeIdentityUtils();
    }

    private void initializeIdentityUtils() {

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getServerURL(anyString(), anyBoolean(), anyBoolean())).thenReturn(serverUrl);
        byte[] stringBuffer = (id).getBytes();
        when(IdentityUtil.base58Decode(anyString())).thenReturn(stringBuffer);
    }

    private void initializeIdentityManager() throws IdentityProviderManagementException {

        mockStatic(IdentityProviderManager.class);
        IdentityProviderManager identityProviderManager = mock(IdentityProviderManager.class);
        when(IdentityProviderManager.getInstance()).thenReturn(identityProviderManager);
        when(identityProviderManager.getIdPByName(anyString(), anyString())).thenReturn(identityProvider);
        when(identityProvider.getIdentityProviderName()).thenReturn(idp+name);
        when(identityProvider.getAlias()).thenReturn(alias);
        when(identityProvider.getDisplayName()).thenReturn(display);
        when(identityProvider.getHomeRealmId()).thenReturn(homeRealm);
        when(identityProvider.getProvisioningRole()).thenReturn(role);

        ClaimConfig claimConfig = new ClaimConfig();
        claimConfig.setUserClaimURI(claim);
        claimConfig.setLocalClaimDialect(true);
        claimConfig.setAlwaysSendMappedLocalSubjectId(true);
        claimConfig.setRoleClaimURI(claim);
        ClaimMapping claimMapping = new ClaimMapping();
        Claim claim = new Claim();
        claim.setClaimId(1);
        claim.setClaimUri(claimUri);
        claimMapping.setRemoteClaim(claim);
        claimMapping.setLocalClaim(claim);
        List<ClaimMapping> claimMappings = new ArrayList<>();
        claimMappings.add(claimMapping);
        claimConfig.setClaimMappings(claimMappings.toArray(new ClaimMapping[0]));
        when(identityProvider.getClaimConfig()).thenReturn(claimConfig);

        when(identityProviderManager.getIdPById(anyString(), anyString())).thenReturn(identityProvider);

        List<FederatedAuthenticatorConfig> federatedAuthenticatorConfigList = new ArrayList<>();
        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setDisplayName(display);
        federatedAuthenticatorConfig.setName(federated+ name);
        federatedAuthenticatorConfig.setEnabled(true);
        List<Property> properties = new ArrayList<>();
        Property property = new Property();
        property.setValue(value);
        property.setType(type);
        property.setName(prop+name);
        property.setConfidential(true);
        property.setRequired(true);
        property.setDefaultValue(deflt);
        property.setAdvanced(true);
        properties.add(property);
        federatedAuthenticatorConfig.setProperties(properties.toArray(new Property[0]));
        federatedAuthenticatorConfigList.add(federatedAuthenticatorConfig);
        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs = federatedAuthenticatorConfigList.toArray
                (new FederatedAuthenticatorConfig[0]);
        when(identityProvider.getFederatedAuthenticatorConfigs()).thenReturn(federatedAuthenticatorConfigs);

        List<ProvisioningConnectorConfig> provisioningConnectorConfigs = new ArrayList<>();
        ProvisioningConnectorConfig provisioningConnectorConfig = new ProvisioningConnectorConfig();
        provisioningConnectorConfig.setName(provision+name);
        provisioningConnectorConfig.setEnabled(true);
        provisioningConnectorConfig.setProvisioningProperties(properties.toArray(new Property[0]));
        provisioningConnectorConfigs.add(provisioningConnectorConfig);
        when(identityProvider.getProvisioningConnectorConfigs()).thenReturn(provisioningConnectorConfigs.toArray
                (new ProvisioningConnectorConfig[0]));

        PermissionsAndRoleConfig permissionsAndRoleConfig = new PermissionsAndRoleConfig();
        List<String> roleList = new ArrayList<>();
        roleList.add("role1");
        roleList.add("role2");
        List<ApplicationPermission> applicationPermissions = new ArrayList<>();
        ApplicationPermission applicationPermission = new ApplicationPermission();
        applicationPermission.setValue("value1");
        applicationPermissions.add(applicationPermission);
        applicationPermission = new ApplicationPermission();
        applicationPermission.setValue("value2");
        applicationPermissions.add(applicationPermission);
        List<RoleMapping> roleMappings = new ArrayList<>();
        RoleMapping roleMapping = new RoleMapping(new LocalRole("userStore", "role1"), "role2");
        roleMappings.add(roleMapping);
        permissionsAndRoleConfig.setRoleMappings(roleMappings.toArray(new RoleMapping[0]));
        permissionsAndRoleConfig.setIdpRoles(roleList.toArray(new String[0]));
        permissionsAndRoleConfig.setPermissions(applicationPermissions.toArray(new ApplicationPermission[0]));
        when(identityProvider.getPermissionAndRoleConfig()).thenReturn(permissionsAndRoleConfig);

        JustInTimeProvisioningConfig justInTimeProvisioningConfig = new JustInTimeProvisioningConfig();
        justInTimeProvisioningConfig.setUserStoreClaimUri(userStore);
        justInTimeProvisioningConfig.setPromptConsent(true);
        justInTimeProvisioningConfig.setPasswordProvisioningEnabled(true);
        justInTimeProvisioningConfig.setModifyUserNameAllowed(true);
        when(identityProvider.getJustInTimeProvisioningConfig()).thenReturn(justInTimeProvisioningConfig);

    }

    private void initializeCarbonContext() {

        mockStatic(CarbonUtils.class);
        mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = mock(PrivilegedCarbonContext.class);

        when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        when(privilegedCarbonContext.getTenantDomain()).thenReturn(SUPER_TENANT_DOMAIN_NAME);
        when(privilegedCarbonContext.getTenantId()).thenReturn(SUPER_TENANT_ID);
        when(privilegedCarbonContext.getUsername()).thenReturn("admin");

        when(CarbonUtils.getCarbonConfigDirPath()).thenReturn(Paths.get(System.getProperty("user.dir"),
                "src", "test", "resources", "conf").toString());

    }
    private IdPDetailDTO getIdPDetailDTO() {

        when(idPDetailDTO.getAlias()).thenReturn(alias);
        when(idPDetailDTO.getId()).thenReturn(id);
        when(idPDetailDTO.getDisplayName()).thenReturn(display);
        when(idPDetailDTO.getIdentityProviderName()).thenReturn(name);
        when(idPDetailDTO.getEnable()).thenReturn(true);
        when(idPDetailDTO.getFederationHub()).thenReturn(true);
        when(idPDetailDTO.getHomeRealmId()).thenReturn(homeRealm);
        when(idPDetailDTO.getIdentityProviderDescription()).thenReturn(description);
        when(idPDetailDTO.getProvisioningRole()).thenReturn(provisioningRole);

        when(idPDetailDTO.getEnable()).thenReturn(false);
        return idPDetailDTO;
    }

    @Test
    public void testIdPCreation() {

        IdpsApiServiceImpl idpsApiService = new IdpsApiServiceImpl();
        Response response = idpsApiService.idpsPost(getIdPDetailDTO());

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testIdPGet() {

        IdpsApiServiceImpl idpsApiService = new IdpsApiServiceImpl();
        Response response = idpsApiService.idpsNameGet(id);

        Assert.assertEquals(200, response.getStatus());
        Assert.assertTrue("Object should be IDP Detail", response.getEntity() instanceof IdPDetailDTO);
        IdPDetailDTO idPDetailDTOReceived = (IdPDetailDTO) response.getEntity();
        Assert.assertEquals(idPDetailDTOReceived.getIdentityProviderName(), idp+name);
        Assert.assertEquals(idPDetailDTOReceived.getClaimConfig().getRoleClaimURI(), claim);
        Assert.assertEquals(idPDetailDTOReceived.getClaimConfig().getUserClaimURI(), claim);
        Assert.assertEquals(idPDetailDTOReceived.getClaimConfig().getClaimMappings().get(0).getRemoteClaim()
                .getClaimUri(), claimUri);
        Assert.assertEquals((Integer) 1, idPDetailDTOReceived.getClaimConfig().getClaimMappings().get(0).getLocalClaim()
                .getClaimId());
    }

    @Test
    public void testIdPDelete() {

        IdpsApiServiceImpl idpsApiService = new IdpsApiServiceImpl();
        Response response = idpsApiService.idpsNameDelete(id);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testIdPUpdate() {

        IdpsApiServiceImpl idpsApiService = new IdpsApiServiceImpl();
        Response response = idpsApiService.idpsNamePut(id, getIdPDetailDTO());
        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void testIdPGetAuthenticators() {

        IdpsApiServiceImpl idpsApiService = new IdpsApiServiceImpl();
        Response response = idpsApiService.idpsNameAuthenticatorsGet(id, 1, 0);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertTrue(response.getEntity() instanceof List);
        List<?> entityList = (List<?>) response.getEntity();
        Assert.assertFalse(entityList.isEmpty());
        Assert.assertTrue(entityList.get(0) instanceof FederatedAuthenticatorConfigDTO);
        Assert.assertEquals((federated+name), ((FederatedAuthenticatorConfigDTO)entityList.get(0)).getName());
        Assert.assertEquals( (display), ((FederatedAuthenticatorConfigDTO)entityList.get(0)).getDisplayName());
        Assert.assertEquals("default", ((FederatedAuthenticatorConfigDTO)entityList.get(0)).getPropertyList().get(0)
                .getDefaultValue());
        Assert.assertEquals("type", ((FederatedAuthenticatorConfigDTO)entityList.get(0)).getPropertyList().get(0)
                .getType());
        Assert.assertEquals("value", ((FederatedAuthenticatorConfigDTO)entityList.get(0)).getPropertyList().get(0)
                .getValue());
        Assert.assertEquals(((FederatedAuthenticatorConfigDTO)entityList.get(0)).getPropertyList().get(0)
                .getName(), prop+name);
        Assert.assertEquals(true, ((FederatedAuthenticatorConfigDTO)entityList.get(0)).getPropertyList().get(0)
                .getRequired());
        Assert.assertEquals(true, ((FederatedAuthenticatorConfigDTO)entityList.get(0)).getPropertyList().get(0)
                .getAdvanced());
    }

    @Test
    public void testIdPGetOutboundConnectors() {

        IdpsApiServiceImpl idpsApiService = new IdpsApiServiceImpl();
        Response response = idpsApiService.idpsNameOutboundProvisioningConnectorConfigsGet(id, 1, 0);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertTrue(response.getEntity() instanceof List);

        List<?> entityList = (List<?>) response.getEntity();
        Assert.assertFalse(entityList.isEmpty());

        Assert.assertTrue(entityList.get(0) instanceof ProvisioningConnectorConfigDTO);
        Assert.assertEquals((provision+name), ((ProvisioningConnectorConfigDTO)entityList.get(0)).getName());
        Assert.assertEquals(true, ((ProvisioningConnectorConfigDTO)entityList.get(0)).getEnabled());

        Assert.assertEquals(deflt, ((ProvisioningConnectorConfigDTO)entityList.get(0)).getProvisioningProperties()
                .get(0)
                .getDefaultValue());
        Assert.assertEquals(type, ((ProvisioningConnectorConfigDTO)entityList.get(0)).getProvisioningProperties().get(0)
                .getType());
        Assert.assertEquals(value, ((ProvisioningConnectorConfigDTO)entityList.get(0)).getProvisioningProperties()
                .get(0)
                .getValue());
        Assert.assertEquals(prop+name, ((ProvisioningConnectorConfigDTO)entityList.get(0)).getProvisioningProperties()
                .get(0)
                .getName());
        Assert.assertEquals(true, ((ProvisioningConnectorConfigDTO)entityList.get(0)).getProvisioningProperties().get(0)
                .getRequired());
        Assert.assertEquals(true, ((ProvisioningConnectorConfigDTO)entityList.get(0)).getProvisioningProperties().get(0)
                .getAdvanced());
    }

    @Test
    public void testIdPGetRoles() {

        IdpsApiServiceImpl idpsApiService = new IdpsApiServiceImpl();
        Response response = idpsApiService.idpsNamePermissionAndRoleConfigGet(id);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertTrue(response.getEntity() instanceof PermissionsAndRoleConfigDTO);
        Assert.assertEquals(((PermissionsAndRoleConfigDTO)response.getEntity()).getIdpRoles().get(0), ("role1"));
        Assert.assertEquals(((PermissionsAndRoleConfigDTO)response.getEntity()).getIdpRoles().get(1), ("role2"));

        Assert.assertEquals(("value1"), ((PermissionsAndRoleConfigDTO)response.getEntity()).getPermissions().get(0)
                        .getValue());
        Assert.assertEquals(("value2"), ((PermissionsAndRoleConfigDTO)response.getEntity()).getPermissions().get(1)
                .getValue());

        Assert.assertEquals(((PermissionsAndRoleConfigDTO)response.getEntity()).getRoleMappings().get(0)
                .getRemoteRole(), ("role2"));
        Assert.assertEquals(((PermissionsAndRoleConfigDTO)response.getEntity()).getRoleMappings().get(0).getLocalRole
                ().getLocalRoleName(), ("role1"));
        Assert.assertEquals(((PermissionsAndRoleConfigDTO)response.getEntity()).getRoleMappings().get(0).getLocalRole
                ().getUserStoreId(), (userStore));
    }

    @Test
    public void testIdPGetJITProvisioning() {

        IdpsApiServiceImpl idpsApiService = new IdpsApiServiceImpl();
        Response response = idpsApiService.idpsNameJitProvisioningGet(id);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertTrue(response.getEntity() instanceof JustInTimeProvisioningConfigDTO);
        Assert.assertEquals(true, ((JustInTimeProvisioningConfigDTO)response.getEntity()).getModifyUserNameAllowed
                ());
        Assert.assertEquals(true, ((JustInTimeProvisioningConfigDTO)response.getEntity())
                        .getPasswordProvisioningEnabled());
        Assert.assertEquals(true, ((JustInTimeProvisioningConfigDTO)response.getEntity()).getModifyUserNameAllowed());
        Assert.assertEquals(((JustInTimeProvisioningConfigDTO)response.getEntity()).getUserStoreClaimUri(),
                userStore);
    }
}
