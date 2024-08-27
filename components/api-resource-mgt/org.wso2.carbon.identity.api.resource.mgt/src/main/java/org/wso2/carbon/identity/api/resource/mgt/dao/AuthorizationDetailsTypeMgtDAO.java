package org.wso2.carbon.identity.api.resource.mgt.dao;

import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;
import org.wso2.carbon.identity.core.model.ExpressionNode;

import java.sql.Connection;
import java.util.List;

public interface AuthorizationDetailsTypeMgtDAO {

    List<AuthorizationDetailsType> getAuthorizationDetailsTypes(List<ExpressionNode> expressionNodes, Integer tenantId)
            throws APIResourceMgtException;

    List<AuthorizationDetailsType> getAuthorizationDetailsTypesByApiId(String apiId, Integer tenantId)
            throws APIResourceMgtException;

    AuthorizationDetailsType getAuthorizationDetailsTypeByApiIdAndType(String apiId, String type, Integer tenantId)
            throws APIResourceMgtException;

    void addAuthorizationDetailsTypes(String apiId, List<AuthorizationDetailsType> authorizationDetailsTypes,
                                      Integer tenantId) throws APIResourceMgtException;

    void addAuthorizationDetailsTypes(Connection connection, String apiId,
                                      List<AuthorizationDetailsType> authorizationDetailsTypes, Integer tenantId)
            throws APIResourceMgtException;

    void deleteAuthorizationDetailsTypesByApiId(String apiId, Integer tenantId) throws APIResourceMgtException;

    void deleteAuthorizationDetailsTypeByApiIdAndType(String apiId, String type, Integer tenantId)
            throws APIResourceMgtException;

    void updateAuthorizationDetailsTypes(Connection connection, String apiId,
                                         List<AuthorizationDetailsType> authorizationDetailsTypes, Integer tenantId)
            throws APIResourceMgtException;

    void updateAuthorizationDetailsTypes(String apiId, List<AuthorizationDetailsType> authorizationDetailsTypes,
                                         Integer tenantId) throws APIResourceMgtException;

    boolean isAuthorizationDetailsTypeExists(String apiId, String type, Integer tenantId)
            throws APIResourceMgtException;
}
