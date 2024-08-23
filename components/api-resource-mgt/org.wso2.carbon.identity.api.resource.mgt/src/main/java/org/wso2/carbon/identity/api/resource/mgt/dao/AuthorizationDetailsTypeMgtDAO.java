package org.wso2.carbon.identity.api.resource.mgt.dao;

import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;

import java.util.List;

public interface AuthorizationDetailsTypeMgtDAO {

    List<AuthorizationDetailsType> getAuthorizationDetailsTypes(Integer tenantId)
            throws APIResourceMgtException;

    List<AuthorizationDetailsType> getAuthorizationDetailsTypesByApiId(String apiId, Integer tenantId)
            throws APIResourceMgtException;

    AuthorizationDetailsType getAuthorizationDetailsTypeByApiIdAndType(String apiId, String type, Integer tenantId)
            throws APIResourceMgtException;

    AuthorizationDetailsType getAuthorizationDetailsTypeByType(String type, Integer tenantId)
            throws APIResourceMgtException;

    void addAuthorizationDetailsTypes(String apiId, List<AuthorizationDetailsType> authorizationDetailsTypes,
                                      Integer tenantId) throws APIResourceMgtException;

    void deleteAuthorizationDetailsTypesByApiId(String apiId, Integer tenantId) throws APIResourceMgtException;

    void updateAuthorizationDetailsType(AuthorizationDetailsType authorizationDetailsType, Integer tenantId)
            throws APIResourceMgtException;

    void updateAuthorizationDetailsTypes(List<AuthorizationDetailsType> authorizationDetailsTypes, Integer tenantId)
            throws APIResourceMgtException;
}
