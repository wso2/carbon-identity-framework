package org.wso2.carbon.identity.application.role.mgt.util;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementClientException;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementException;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementServerException;
import org.wso2.carbon.identity.application.role.mgt.internal.ApplicationRoleMgtServiceComponentHolder;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * GroupId Resolver.
 */
public class GroupIDResolver implements IDResolver {

    @Override
    public String getNameByID(String id, String tenantDomain) throws ApplicationRoleManagementException {

        String groupName = resolveGroupNameFromGroupID(id);
        if (groupName == null) {
            String errorMessage = "A group doesn't exist with id: " + id + " in the tenantDomain: " + tenantDomain;
            throw new ApplicationRoleManagementClientException(errorMessage, errorMessage, "");
        }
        return groupName;
    }

    public String resolveGroupNameFromGroupID(String id) throws ApplicationRoleManagementException {

        AbstractUserStoreManager userStoreManager;
        try {
            userStoreManager = getUserStoreManager(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            return userStoreManager.getGroupNameByGroupId(id);
        } catch (UserStoreException e) {
            throw new ApplicationRoleManagementServerException("Error occurred while retrieving the userstore manager "
                    + "to resolve group name for the groupID", "Error occurred while retrieving the userstore manager "
                    + "to resolve group name for the groupID: " + id, e);
        }
    }

    private AbstractUserStoreManager getUserStoreManager(int tenantId) throws UserStoreException {

        RealmService realmService = ApplicationRoleMgtServiceComponentHolder.getInstance().getRealmService();
        UserRealm tenantUserRealm = realmService.getTenantUserRealm(tenantId);

        return (AbstractUserStoreManager) tenantUserRealm.getUserStoreManager();
    }
}
