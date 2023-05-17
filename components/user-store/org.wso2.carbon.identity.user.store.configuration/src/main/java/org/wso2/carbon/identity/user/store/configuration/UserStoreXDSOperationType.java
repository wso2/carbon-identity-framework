package org.wso2.carbon.identity.user.store.configuration;

import org.wso2.carbon.identity.xds.common.constant.XDSOperationType;

/**
 * This enum is used to define the XDS operation types for user store management.
 */
public enum UserStoreXDSOperationType implements XDSOperationType {

    ADD_USER_STORE,
    UPDATE_USER_STORE,
    UPDATE_USER_STORE_BY_DOMAIN_NAME,
    DELETE_USER_STORE,
    DELETE_USER_STORE_SET,
    MODIFY_USER_STORE_STATE
}
