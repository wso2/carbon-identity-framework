package org.wso2.carbon.identity.api.resource.mgt;

import org.wso2.carbon.identity.api.resource.mgt.dao.AuthorizationDetailsTypeMgtDAO;
import org.wso2.carbon.identity.api.resource.mgt.dao.impl.AuthorizationDetailsTypeMgtDAOImpl;
import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;

import java.util.List;

import static org.wso2.carbon.identity.core.util.IdentityTenantUtil.getTenantId;

public class AuthorizationDetailsTypeManagerImpl implements AuthorizationDetailsTypeManager {

    private final AuthorizationDetailsTypeMgtDAO authorizationDetailsTypeMgtDAO;

    public AuthorizationDetailsTypeManagerImpl() {
        this.authorizationDetailsTypeMgtDAO = new AuthorizationDetailsTypeMgtDAOImpl();
    }

    @Override
    public AuthorizationDetailsType getAuthorizationDetailsTypeByApiIdAndType(String apiId, String type,
                                                                              String tenantDomain)
            throws APIResourceMgtException {

        return this.authorizationDetailsTypeMgtDAO
                .getAuthorizationDetailsTypeByApiIdAndType(apiId, type, getTenantId(tenantDomain));
    }

    @Override
    public List<AuthorizationDetailsType> getAuthorizationDetailsTypesByApiId(String apiId, String tenantDomain)
            throws APIResourceMgtException {

        return this.authorizationDetailsTypeMgtDAO
                .getAuthorizationDetailsTypesByApiId(apiId, getTenantId(tenantDomain));
    }

    @Override
    public List<AuthorizationDetailsType> getAuthorizationDetailsTypes(String tenantDomain)
            throws APIResourceMgtException {

        return this.authorizationDetailsTypeMgtDAO.getAuthorizationDetailsTypes(getTenantId(tenantDomain));
    }

    @Override
    public void deleteAuthorizationDetailsTypesByApiId(String apiId, String tenantDomain)
            throws APIResourceMgtException {

        this.authorizationDetailsTypeMgtDAO.deleteAuthorizationDetailsTypesByApiId(apiId, getTenantId(tenantDomain));
    }

    @Override
    public void updateAuthorizationDetailsTypes(AuthorizationDetailsType authorizationDetailsType, String tenantDomain)
            throws APIResourceMgtException {

        this.authorizationDetailsTypeMgtDAO
                .updateAuthorizationDetailsType(authorizationDetailsType, getTenantId(tenantDomain));
    }

    @Override
    public boolean isAuthorizationDetailsTypeExists(String type, String tenantDomain) throws APIResourceMgtException {

        return this.authorizationDetailsTypeMgtDAO.getAuthorizationDetailsTypeByType(type, getTenantId(tenantDomain)) != null;
    }
}
