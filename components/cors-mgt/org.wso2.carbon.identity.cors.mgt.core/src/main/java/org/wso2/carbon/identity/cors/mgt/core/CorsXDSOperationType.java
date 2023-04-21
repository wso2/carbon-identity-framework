package org.wso2.carbon.identity.cors.mgt.core;

import org.wso2.carbon.identity.xds.common.constant.XDSOperationType;

/**
 * Enum for Cors XDS operation types.
 */
public enum CorsXDSOperationType implements XDSOperationType {
    SET_CORS_ORIGINS,
    ADD_CORS_ORIGINS,
    DELETE_CORS_ORIGINS,
    SET_CORS_CONFIGURATIONS
}
