package org.wso2.carbon.identity.role.mgt.core;

import org.wso2.carbon.identity.xds.common.constant.XDSOperationType;

/**
 * Enum for XDS operation types.
 */
public enum RoleXDSOperationType implements XDSOperationType {

    CREATE,
    DELETE,
    UPDATE_ROLE_NAME,
    UPDATE_ROLE_USER_LIST,
    UPDATE_ROLE_GROUP_LIST,
    UPDATE_ROLE_PERMISSION_LIST,
}
