package org.wso2.carbon.identity.application.mgt.provider;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

/**
 * Interface to provide application roles.
 */
public interface ApplicationRoleProvider {

    /**
     * Get roles of the application creator.
     *
     * @param application Application.
     * @param creator     Application Creator username.
     * @param tenantId    Tenant id.
     * @return Roles of the application.
     * @throws IdentityApplicationManagementException If an error occurs while getting the roles.
     */
    String[] getRoles(ServiceProvider application, String creator, int tenantId)
            throws IdentityApplicationManagementException;
}
