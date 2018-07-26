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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.api.idpmgt.IDPMgtBridgeService;
import org.wso2.carbon.identity.api.idpmgt.IDPMgtBridgeServiceClientException;
import org.wso2.carbon.identity.api.idpmgt.IDPMgtBridgeServiceException;
import org.wso2.carbon.identity.api.idpmgt.IdPUtils;
import org.wso2.carbon.identity.api.idpmgt.endpoint.EndpointUtils;
import org.wso2.carbon.identity.api.idpmgt.endpoint.IdpsApiService;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ClaimConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.FederatedAuthenticatorConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.IdPDetailDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.JustInTimeProvisioningConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.PermissionsAndRoleConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ProvisioningConnectorConfigDTO;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;

import java.util.List;
import javax.ws.rs.core.Response;

public class IdpsApiServiceImpl extends IdpsApiService {

    private static final Log log = LogFactory.getLog(IdpsApiServiceImpl.class);

    private IDPMgtBridgeService idpMgtBridgeService = IDPMgtBridgeService.getInstance();

    @Override
    public Response idpsGet(Integer limit, Integer offset, String spTenantDomain) {

        try {
            List<IdentityProvider> identityProviders = idpMgtBridgeService.getIDPs(limit, offset);
            return Response.ok().entity(EndpointUtils.translateIDPDetailList(identityProviders)).build();
        } catch (IDPMgtBridgeServiceClientException e) {
            return handleBadRequestResponse(e);
        } catch (IDPMgtBridgeServiceException e) {
            return handleServerErrorResponse(e);
        }
    }

    @Override
    public Response idpsNameAuthenticatorsAuthNameGet(String name, String authName) {

        try {
            List<FederatedAuthenticatorConfig> federatedAuthenticatorConfigs = idpMgtBridgeService
                    .getAuthenticatorByName(name, authName);
            return Response.ok().entity(EndpointUtils.createFederatorAuthenticatorDTOList(federatedAuthenticatorConfigs)).build();
        } catch (IDPMgtBridgeServiceClientException e) {
            return handleBadRequestResponse(e);
        } catch (IDPMgtBridgeServiceException e) {
            return handleServerErrorResponse(e);
        }
    }

    @Override
    public Response idpsNameAuthenticatorsAuthNamePut(String name, String authName, FederatedAuthenticatorConfigDTO body) {

        try {
            FederatedAuthenticatorConfig receivedAuthenticatorConfig = EndpointUtils.createDefaultAuthenticator(body);
            IdentityProvider idp = idpMgtBridgeService.updateAuthenticator(receivedAuthenticatorConfig, authName, name);
            return Response.ok().entity(EndpointUtils.translateIDPToIDPDetail(idp)).build();
        } catch (IDPMgtBridgeServiceClientException e) {
            return handleBadRequestResponse(e);
        } catch (IDPMgtBridgeServiceException e) {
            return handleServerErrorResponse(e);
        }
    }

    @Override
    public Response idpsNameAuthenticatorsGet(String name, Integer limit, Integer offset) {

        try {
            List<FederatedAuthenticatorConfig> federatedAuthenticatorConfigs = idpMgtBridgeService
                    .getAuthenticatorList(name, limit, offset);
            return Response.ok().entity(EndpointUtils.createFederatorAuthenticatorDTOList
                    (federatedAuthenticatorConfigs)).build();
        } catch (IDPMgtBridgeServiceClientException e) {
            return handleBadRequestResponse(e);
        } catch (IDPMgtBridgeServiceException e) {
            return handleServerErrorResponse(e);
        }
    }

    @Override
    public Response idpsNameClaimConfigGet(String name) {

        try {
            IdentityProvider idp = idpMgtBridgeService.getIDPByName(name);
            return Response.ok().entity(EndpointUtils.createClaimConfigDTO(idp.getClaimConfig())).build();
        } catch (IDPMgtBridgeServiceClientException e) {
            return handleBadRequestResponse(e);
        } catch (IDPMgtBridgeServiceException e) {
            return handleServerErrorResponse(e);
        }
    }

    @Override
    public Response idpsNameClaimConfigPut(String name, ClaimConfigDTO body) {

        try {
            ClaimConfig claimConfig = EndpointUtils.createClaimConfig(body);
            IdentityProvider idp = idpMgtBridgeService.updateClaimConfiguration(claimConfig, name);
            return Response.ok().entity(EndpointUtils.translateIDPToIDPDetail(idp)).build();
        } catch (IDPMgtBridgeServiceClientException e) {
            return handleBadRequestResponse(e);
        } catch (IDPMgtBridgeServiceException e) {
            return handleServerErrorResponse(e);
        }
    }

    @Override
    public Response idpsNameDelete(String name) {

        try {
            idpMgtBridgeService.deleteIDP(name);
            return Response.ok().build();
        } catch (IDPMgtBridgeServiceClientException e) {
            return handleBadRequestResponse(e);
        } catch (IDPMgtBridgeServiceException e) {
            return handleServerErrorResponse(e);
        }
    }

    @Override
    public Response idpsNameGet(String name) {

        try {
            IdentityProvider idp = idpMgtBridgeService.getIDPByName(name);
            return Response.ok().entity(EndpointUtils.translateIDPToIDPDetail(idp)).build();
        } catch (IDPMgtBridgeServiceClientException e) {
            return handleBadRequestResponse(e);
        } catch (IDPMgtBridgeServiceException e) {
            return handleServerErrorResponse(e);
        }
    }

    @Override
    public Response idpsNameJitProvisioningGet(String name) {

        try {
            IdentityProvider idp = idpMgtBridgeService.getIDPByName(name);
            return Response.ok().entity(EndpointUtils.createJustinTimeProvisioningConfigDTO(idp
                    .getJustInTimeProvisioningConfig())).build();
        } catch (IDPMgtBridgeServiceClientException e) {
            return handleBadRequestResponse(e);
        } catch (IDPMgtBridgeServiceException e) {
            return handleServerErrorResponse(e);
        }
    }

    @Override
    public Response idpsNameJitProvisioningPut(String name, JustInTimeProvisioningConfigDTO body) {

        try {
            JustInTimeProvisioningConfig justInTimeProvisioningConfig = EndpointUtils
                    .createJustinTimeProvisioningConfig(body);
            IdentityProvider idp = idpMgtBridgeService.updateJITProvisioningConfig(justInTimeProvisioningConfig, name);
            return Response.ok().entity(EndpointUtils.translateIDPToIDPDetail(idp)).build();
        } catch (IDPMgtBridgeServiceClientException e) {
            return handleBadRequestResponse(e);
        } catch (IDPMgtBridgeServiceException e) {
            return handleServerErrorResponse(e);
        }
    }

    @Override
    public Response idpsNameOutboundProvisioningConnectorConfigsConnectorNameDelete(String name, String connectorName) {

        try {
            idpMgtBridgeService.deleteOutboundConnector(name, connectorName);
            return Response.ok().build();
        } catch (IDPMgtBridgeServiceClientException e) {
            return handleBadRequestResponse(e);
        } catch (IDPMgtBridgeServiceException e) {
            return handleServerErrorResponse(e);
        }
    }

    @Override
    public Response idpsNameOutboundProvisioningConnectorConfigsConnectorNameGet(String name, String connectorName) {

        try {
            List<ProvisioningConnectorConfig> provisioningConnectorConfigs = idpMgtBridgeService
                    .getOutboundConnectorByName(name, connectorName);
            return Response.ok().entity(EndpointUtils.createProvisioningConnectorConfigDTOs(
                    provisioningConnectorConfigs)).build();
        } catch (IDPMgtBridgeServiceClientException e) {
            return handleBadRequestResponse(e);
        } catch (IDPMgtBridgeServiceException e) {
            return handleServerErrorResponse(e);
        }
    }

    @Override
    public Response idpsNameOutboundProvisioningConnectorConfigsConnectorNamePut(String name, String connectorName,
                                                                                 ProvisioningConnectorConfigDTO body) {

        try {
            ProvisioningConnectorConfig provisioningConnectorConfig = EndpointUtils
                    .createProvisioningConnectorConfig(body);
            IdentityProvider idp = idpMgtBridgeService.updateProvisioningConnectorConfig(provisioningConnectorConfig,
                    connectorName, name);
            return Response.ok().entity(EndpointUtils.translateIDPToIDPDetail(idp)).build();
        } catch (IDPMgtBridgeServiceClientException e) {
            return handleBadRequestResponse(e);
        } catch (IDPMgtBridgeServiceException e) {
            return handleServerErrorResponse(e);
        }
    }

    @Override
    public Response idpsNameOutboundProvisioningConnectorConfigsGet(String name, Integer limit, Integer offset) {

        try {
            List<ProvisioningConnectorConfig> provisioningConnectorConfigs = idpMgtBridgeService
                    .getOutboundConnectorList(name, limit, offset);
            return Response.ok().entity(EndpointUtils.createProvisioningConnectorConfigDTOs(
                    provisioningConnectorConfigs)).build();
        } catch (IDPMgtBridgeServiceClientException e) {
            return handleBadRequestResponse(e);
        } catch (IDPMgtBridgeServiceException e) {
            return handleServerErrorResponse(e);
        }
    }

    @Override
    public Response idpsNameOutboundProvisioningConnectorConfigsPost(String name, ProvisioningConnectorConfigDTO body) {

        try {
            ProvisioningConnectorConfig provisioningConnectorConfig = EndpointUtils
                    .createProvisioningConnectorConfig(body);
            IdentityProvider idp = idpMgtBridgeService.addProvisioningConnectorConfig(provisioningConnectorConfig,
                    name);
            return Response.ok().entity(EndpointUtils.translateIDPToIDPDetail(idp)).build();
        } catch (IDPMgtBridgeServiceClientException e) {
            return handleBadRequestResponse(e);
        } catch (IDPMgtBridgeServiceException e) {
            return handleServerErrorResponse(e);
        }
    }

    @Override
    public Response idpsNamePermissionAndRoleConfigGet(String name) {

        try {
            IdentityProvider idp = idpMgtBridgeService.getIDPByName(name);
            return Response.ok().entity(EndpointUtils.createPermissionAndRoleConfigDTO(idp.getPermissionAndRoleConfig
                    ())).build();
        } catch (IDPMgtBridgeServiceClientException e) {
            return handleBadRequestResponse(e);
        } catch (IDPMgtBridgeServiceException e) {
            return handleServerErrorResponse(e);
        }
    }

    @Override
    public Response idpsNamePermissionAndRoleConfigPut(String name, PermissionsAndRoleConfigDTO body) {

        try {
            PermissionsAndRoleConfig permissionsAndRoleConfig = EndpointUtils.createPermissionAndRoleConfig(body);
            IdentityProvider idp = idpMgtBridgeService.updateRoles(permissionsAndRoleConfig, name);
            return Response.ok().entity(EndpointUtils.translateIDPToIDPDetail(idp)).build();
        } catch (IDPMgtBridgeServiceClientException e) {
            return handleBadRequestResponse(e);
        } catch (IDPMgtBridgeServiceException e) {
            return handleServerErrorResponse(e);
        }
    }

    @Override
    public Response idpsNamePut(String name, IdPDetailDTO body) {

        try {
            IdentityProvider translatedIDP = EndpointUtils.translateIDPDetailToIDP(body);
            IdentityProvider updatedIDP = idpMgtBridgeService.updateIDP(translatedIDP, name);
            return Response.ok().entity(EndpointUtils.translateIDPToIDPDetail(updatedIDP)).header("Location",
                    IdPUtils.getIDPLocation(updatedIDP.getId())).build();
        } catch (IDPMgtBridgeServiceClientException e) {
            return handleBadRequestResponse(e);
        } catch (IDPMgtBridgeServiceException e) {
            return handleServerErrorResponse(e);
        }
    }

    @Override
    public Response idpsPost(IdPDetailDTO body) {

        try {
            IdentityProvider translatedIDP = EndpointUtils.translateIDPDetailToIDP(body);
            IdentityProvider newIDP = idpMgtBridgeService.addIDP(translatedIDP);
            return Response.ok().entity(EndpointUtils.translateIDPToIDPDetail(newIDP)).header("Location",
                    IdPUtils.getIDPLocation(newIDP.getId())).build();
        } catch (IDPMgtBridgeServiceClientException e) {
            return handleBadRequestResponse(e);
        } catch (IDPMgtBridgeServiceException e) {
            return handleServerErrorResponse(e);
        }
    }

    private Response handleServerErrorResponse(IDPMgtBridgeServiceException e) {

        throw EndpointUtils.buildInternalServerErrorException(e.getErrorCode(), log, e);
    }

    private Response handleBadRequestResponse(IDPMgtBridgeServiceClientException e) {

        throw EndpointUtils.buildBadRequestException(e.getErrorCode(), e.getMessage(), log, e);
    }
}
