package org.wso2.carbon.identity.api.idpmgt.endpoint;

import org.wso2.carbon.identity.api.idpmgt.endpoint.*;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.*;

import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.IdPListDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ErrorDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.FederatedAuthenticatorConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.IdPDetailDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ClaimConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.JustInTimeProvisioningConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ProvisioningConnectorConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.PermissionsAndRoleConfigDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class IdpsApiService {
    public abstract Response idpsGet(Integer limit,Integer offset,String spTenantDomain);
    public abstract Response idpsNameAuthenticatorsAuthNameGet(String name,String authName);
    public abstract Response idpsNameAuthenticatorsAuthNamePut(String name,String authName,FederatedAuthenticatorConfigDTO body);
    public abstract Response idpsNameAuthenticatorsGet(String name,Integer limit,Integer offset);
    public abstract Response idpsNameClaimConfigGet(String name);
    public abstract Response idpsNameClaimConfigPut(String name,ClaimConfigDTO body);
    public abstract Response idpsNameDelete(String name);
    public abstract Response idpsNameGet(String name);
    public abstract Response idpsNameJitProvisioningGet(String name);
    public abstract Response idpsNameJitProvisioningPut(String name,JustInTimeProvisioningConfigDTO body);
    public abstract Response idpsNameOutboundProvisioningConnectorConfigsConnectorNameDelete(String name,String connectorName);
    public abstract Response idpsNameOutboundProvisioningConnectorConfigsConnectorNameGet(String name,String connectorName);
    public abstract Response idpsNameOutboundProvisioningConnectorConfigsConnectorNamePut(String name,String connectorName,ProvisioningConnectorConfigDTO body);
    public abstract Response idpsNameOutboundProvisioningConnectorConfigsGet(String name,Integer limit,Integer offset);
    public abstract Response idpsNameOutboundProvisioningConnectorConfigsPost(String name,ProvisioningConnectorConfigDTO body);
    public abstract Response idpsNamePermissionAndRoleConfigGet(String name);
    public abstract Response idpsNamePermissionAndRoleConfigPut(String name,PermissionsAndRoleConfigDTO body);
    public abstract Response idpsNamePut(String name,IdPDetailDTO body);
    public abstract Response idpsPost(IdPDetailDTO body);
}

