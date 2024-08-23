package org.wso2.carbon.identity.api.resource.mgt;

import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;
import org.wso2.carbon.identity.application.common.model.Scope;

import java.util.List;

public interface AuthorizationDetailsTypeManager {

    List<AuthorizationDetailsType> getAuthorizationDetailsTypesByApiId(String apiId, String tenantDomain)
            throws APIResourceMgtException;
    /**
     * Get registered {@link AuthorizationDetailsType} by type.
     *
     * @param type    authorization details type.
     * @param tenantDomain Tenant domain.
     * @return AuthorizationDetailsType.
     * @throws APIResourceMgtException If an error occurs while retrieving authorization details type.
     */
    AuthorizationDetailsType getAuthorizationDetailsTypeByApiIdAndType(String apiId, String type, String tenantDomain)
            throws APIResourceMgtException;

    List<AuthorizationDetailsType> getAuthorizationDetailsTypes(String tenantDomain) throws APIResourceMgtException;

    void deleteAuthorizationDetailsTypesByApiId(String apiId, String tenantDomain)
            throws APIResourceMgtException;

    void updateAuthorizationDetailsTypes(AuthorizationDetailsType authorizationDetailsType, String tenantDomain)
            throws APIResourceMgtException;

    boolean isAuthorizationDetailsTypeExists(String type, String tenantDomain) throws APIResourceMgtException;
}
