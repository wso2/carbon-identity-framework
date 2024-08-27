package org.wso2.carbon.identity.api.resource.mgt;

import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;

import java.util.List;

public interface AuthorizationDetailsTypeManager {

    void addAuthorizationDetailsTypes(String apiId, List<AuthorizationDetailsType> authorizationDetailsTypes,
                                      String tenantDomain) throws APIResourceMgtException;

    List<AuthorizationDetailsType> getAuthorizationDetailsTypesByApiId(String apiId, String tenantDomain)
            throws APIResourceMgtException;

    /**
     * Get registered {@link AuthorizationDetailsType} by type.
     *
     * @param type         authorization details type.
     * @param tenantDomain Tenant domain.
     * @return AuthorizationDetailsType.
     * @throws APIResourceMgtException If an error occurs while retrieving authorization details type.
     */
    AuthorizationDetailsType getAuthorizationDetailsTypeByApiIdAndType(String apiId, String type, String tenantDomain)
            throws APIResourceMgtException;

    List<AuthorizationDetailsType> getAuthorizationDetailsTypes(String filter, String tenantDomain) throws APIResourceMgtException;

    void deleteAuthorizationDetailsTypesByApiId(String apiId, String tenantDomain)
            throws APIResourceMgtException;

    void deleteAuthorizationDetailsTypeByApiIdAndType(String apiId, String type, String tenantDomain)
            throws APIResourceMgtException;

    void updateAuthorizationDetailsType(String apiId, AuthorizationDetailsType authorizationDetailsType,
                                        String tenantDomain) throws APIResourceMgtException;

    boolean isAuthorizationDetailsTypeExists(String apiId, String type, String tenantDomain)
            throws APIResourceMgtException;

    void replaceAuthorizationDetailsTypes(String apiId, List<String> removedAuthorizationDetailsTypes,
                                          List<AuthorizationDetailsType> addedAuthorizationDetailsTypes,
                                          String tenantDomain) throws APIResourceMgtException;
}
