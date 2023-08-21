package org.wso2.carbon.identity.application.role.mgt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementClientException;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementException;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementServerException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;

import java.util.ArrayList;
import java.util.List;

/**
 * UserId Resolver.
 */
public class UserIDResolver implements IDResolver {

    private Log log = LogFactory.getLog(UserIDResolver.class);

    @Override
    public String getNameByID(String id, String tenantDomain) throws ApplicationRoleManagementException {

        String userName = resolveUserNameFromUserID(id);
        if (userName == null) {
            String errorMessage = "A user doesn't exist with id: " + id + " in the tenantDomain: " + tenantDomain;
            throw new ApplicationRoleManagementClientException(errorMessage, errorMessage, "");
        }
        return userName;
    }

    public List<String> getNamesByIDs(List<String> idList, String tenantDomain)
            throws ApplicationRoleManagementException {

        List<String> usersList = new ArrayList<>();
        for (String id : idList) {
            usersList.add(getNameByID(id, tenantDomain));
        }
        return usersList;
    }

    /**
     * Retrieves the username of the given userID.
     *
     * @param id userID.
     * @return username of the user.
     * @throws ApplicationRoleManagementException ApplicationRoleManagementException.
     */
    public String resolveUserNameFromUserID(String id) throws ApplicationRoleManagementException {

        try {
            UserStoreManager userStoreManager = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                    .getUserStoreManager();
            try {
                if (userStoreManager instanceof AbstractUserStoreManager) {
                    return ((AbstractUserStoreManager) userStoreManager).getUserNameFromUserID(id);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Provided user store manager for the userID: " + id + ", is not an instance " +
                            "of the AbstractUserStore manager");
                }
                throw new ApplicationRoleManagementClientException("Unable to get the username of the userID",
                        "Unable to get the username of the userID: " + id + ".", "");
            } catch (UserStoreException e) {
                throw new ApplicationRoleManagementServerException("Error occurred while resolving username for " +
                        "the userID Error occurred while resolving username for the userID: " + id, "");
            }
        } catch (UserStoreException e) {
            throw new ApplicationRoleManagementServerException("Error occurred while retrieving the userstore manager "
                    + "to resolve username for the userID", "Error occurred while retrieving the userstore manager to "
                    + "resolve username for the userID: " + id, e);
        }
    }

}
