package org.wso2.carbon.identity.application.mgt.provider;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationPermission;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;

import java.util.List;

/**
 * Interface to provide application permissions.
 */
public interface ApplicationPermissionProvider {

    void renameAppPermissionPathNode(String oldName, String newName) throws IdentityApplicationManagementException;

    void storePermissions(String applicationName, String username,
                          PermissionsAndRoleConfig permissionsConfig) throws IdentityApplicationManagementException;

    void updatePermissions(String applicationName, ApplicationPermission[] permissions)
            throws IdentityApplicationManagementException;

    List<ApplicationPermission> loadPermissions(String applicationName)
            throws IdentityApplicationManagementException;

    void deletePermissions(String applicationName) throws IdentityApplicationManagementException;
}
