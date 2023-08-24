package org.wso2.carbon.identity.application.mgt.provider;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;

/**
 * Interface to provide application roles.
 */
public interface ApplicationRoleProvider {

    /**
     * Get user roles.
     *
     * @param username Username.
     * @param tenantId Tenant ID.
     * @return Array of user roles.
     */
    String[] getUserRoles(String username, int tenantId) throws IdentityApplicationManagementException;
}
