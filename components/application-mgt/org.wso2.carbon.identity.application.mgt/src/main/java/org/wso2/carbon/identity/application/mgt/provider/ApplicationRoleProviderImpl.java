package org.wso2.carbon.identity.application.mgt.provider;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementServerException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;

/**
 * Default implementation of {@link ApplicationRoleProvider}.
 */
public class ApplicationRoleProviderImpl implements ApplicationRoleProvider {

    @Override
    public String[] getRoles(ServiceProvider application, String creator, int tenantId)
            throws IdentityApplicationManagementServerException {

        try {
            // Set Owner Permissions
            AbstractUserStoreManager userStoreManager =
                    (AbstractUserStoreManager) ApplicationManagementServiceComponentHolder.getInstance()
                            .getRealmService().getTenantUserRealm(tenantId).getUserStoreManager();
            String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
            String[] roles = userStoreManager.getHybridRoleListOfUser(creator, tenantDomain).toArray(new String[0]);
            return roles;
        } catch (UserStoreException e) {
            throw new IdentityApplicationManagementServerException("Error while retrieving user roles.", e);
        }
    }
}
