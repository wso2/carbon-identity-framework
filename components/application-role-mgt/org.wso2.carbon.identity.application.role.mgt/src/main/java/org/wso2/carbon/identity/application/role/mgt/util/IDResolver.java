package org.wso2.carbon.identity.application.role.mgt.util;

import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementException;

/**
 * Id Resolver.
 */
public interface IDResolver {

    String getNameByID(String id, String tenantDomain) throws ApplicationRoleManagementException;

    boolean isExists(String id, String tenantDomain) throws ApplicationRoleManagementException;

}
