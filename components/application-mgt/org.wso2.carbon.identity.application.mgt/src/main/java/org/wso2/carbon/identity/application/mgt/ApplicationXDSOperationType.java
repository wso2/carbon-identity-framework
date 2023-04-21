package org.wso2.carbon.identity.application.mgt;

import org.wso2.carbon.identity.xds.common.constant.XDSOperationType;

/**
 * Enum for Application XDS operation types.
 */
public enum ApplicationXDSOperationType implements XDSOperationType {

    ADD_APPLICATION,
    CREATE_APPLICATION_WITH_TEMPLATE,
    UPDATE_APPLICATION,
    DELETE_APPLICATION,
    DELETE_APPLICATIONS,
    CREATE_APPLICATION_TEMPLATE,
    CREATE_APPLICATION_TEMPLATE_FROM_SP,
    DELETE_APPLICATION_TEMPLATE,
    UPDATE_APPLICATION_TEMPLATE,
    CREATE_APPLICATION,
    UPDATE_APPLICATION_BY_RESOURCE_ID,
    DELETE_APPLICATION_BY_RESOURCE_ID,
}
