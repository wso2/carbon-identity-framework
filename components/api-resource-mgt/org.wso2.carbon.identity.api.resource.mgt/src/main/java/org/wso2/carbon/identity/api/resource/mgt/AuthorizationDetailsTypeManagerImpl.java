package org.wso2.carbon.identity.api.resource.mgt;

import org.apache.commons.collections.CollectionUtils;
import org.wso2.carbon.identity.api.resource.mgt.dao.AuthorizationDetailsTypeMgtDAO;
import org.wso2.carbon.identity.api.resource.mgt.dao.impl.AuthorizationDetailsTypeMgtDAOImpl;
import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;
import org.wso2.carbon.identity.core.model.ExpressionNode;

import java.util.Collections;
import java.util.List;

import static org.wso2.carbon.identity.api.resource.mgt.util.FilterQueriesUtil.getExpressionNodes;
import static org.wso2.carbon.identity.core.util.IdentityTenantUtil.getTenantId;

public class AuthorizationDetailsTypeManagerImpl implements AuthorizationDetailsTypeManager {

    private final AuthorizationDetailsTypeMgtDAO authorizationDetailsTypeMgtDAO;

    public AuthorizationDetailsTypeManagerImpl() {

        this(new AuthorizationDetailsTypeMgtDAOImpl());
    }

    public AuthorizationDetailsTypeManagerImpl(AuthorizationDetailsTypeMgtDAO authorizationDetailsTypeMgtDAO) {

        this.authorizationDetailsTypeMgtDAO = authorizationDetailsTypeMgtDAO;
    }

    @Override
    public AuthorizationDetailsType getAuthorizationDetailsTypeByApiIdAndType(
            String apiId, String type, String tenantDomain) throws APIResourceMgtException {

        return this.authorizationDetailsTypeMgtDAO
                .getAuthorizationDetailsTypeByApiIdAndType(apiId, type, getTenantId(tenantDomain));
    }

    @Override
    public void addAuthorizationDetailsTypes(String apiId, List<AuthorizationDetailsType> authorizationDetailsTypes,
                                             String tenantDomain) throws APIResourceMgtException {

        this.authorizationDetailsTypeMgtDAO.addAuthorizationDetailsTypes(apiId, authorizationDetailsTypes,
                getTenantId(tenantDomain));
    }

    @Override
    public List<AuthorizationDetailsType> getAuthorizationDetailsTypesByApiId(String apiId, String tenantDomain)
            throws APIResourceMgtException {

        return this.authorizationDetailsTypeMgtDAO
                .getAuthorizationDetailsTypesByApiId(apiId, getTenantId(tenantDomain));
    }

    @Override
    public List<AuthorizationDetailsType> getAuthorizationDetailsTypes(String filter, String tenantDomain)
            throws APIResourceMgtException {

        return this.authorizationDetailsTypeMgtDAO
                .getAuthorizationDetailsTypes(getExpressionNodes(filter, null, null), getTenantId(tenantDomain));
    }

    @Override
    public void deleteAuthorizationDetailsTypesByApiId(String apiId, String tenantDomain)
            throws APIResourceMgtException {

        this.authorizationDetailsTypeMgtDAO.deleteAuthorizationDetailsTypesByApiId(apiId, getTenantId(tenantDomain));
    }

    @Override
    public void deleteAuthorizationDetailsTypeByApiIdAndType(String apiId, String type, String tenantDomain)
            throws APIResourceMgtException {

        this.authorizationDetailsTypeMgtDAO
                .deleteAuthorizationDetailsTypeByApiIdAndType(apiId, type, getTenantId(tenantDomain));
    }

    @Override
    public void updateAuthorizationDetailsType(String apiId, AuthorizationDetailsType authorizationDetailsType,
                                               String tenantDomain) throws APIResourceMgtException {

        this.authorizationDetailsTypeMgtDAO.updateAuthorizationDetailsTypes(apiId,
                Collections.singletonList(authorizationDetailsType), getTenantId(tenantDomain));
    }

    @Override
    public boolean isAuthorizationDetailsTypeExists(String apiId, String type, String tenantDomain)
            throws APIResourceMgtException {

        return this.authorizationDetailsTypeMgtDAO
                .isAuthorizationDetailsTypeExists(apiId, type, getTenantId(tenantDomain));
    }

    @Override
    public void replaceAuthorizationDetailsTypes(String apiId,
                                                 List<String> removedAuthorizationDetailsTypes,
                                                 List<AuthorizationDetailsType> addedAuthorizationDetailsTypes,
                                                 String tenantDomain) throws APIResourceMgtException {

        if (CollectionUtils.isNotEmpty(removedAuthorizationDetailsTypes)) {
            for (String removedType : removedAuthorizationDetailsTypes) {
                this.authorizationDetailsTypeMgtDAO
                        .deleteAuthorizationDetailsTypeByApiIdAndType(apiId, removedType, getTenantId(tenantDomain));
            }
        }

        if (CollectionUtils.isNotEmpty(addedAuthorizationDetailsTypes)) {
            this.authorizationDetailsTypeMgtDAO
                    .addAuthorizationDetailsTypes(apiId, addedAuthorizationDetailsTypes, getTenantId(tenantDomain));
        }
    }
}
