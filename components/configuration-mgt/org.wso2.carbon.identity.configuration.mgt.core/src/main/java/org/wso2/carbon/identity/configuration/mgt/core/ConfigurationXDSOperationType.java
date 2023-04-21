package org.wso2.carbon.identity.configuration.mgt.core;

import org.wso2.carbon.identity.xds.common.constant.XDSOperationType;

public enum ConfigurationXDSOperationType implements XDSOperationType {

    DELETE_RESOURCE,
    ADD_RESOURCE,
    ADD_RESOURCE_WITH_RESOURCE,
    REPLACE_RESOURCE,
    REPLACE_RESOURCE_WITH_RESOURCE,
    DELETE_RESOURCE_TYPE,
    ADD_RESOURCE_TYPE,
    REPLACE_RESOURCE_TYPE,
    DELETE_ATTRIBUTE,
    UPDATE_ATTRIBUTE,
    ADD_ATTRIBUTE,
    REPLACE_ATTRIBUTE,
    ADD_FILE,
    DELETE_FILES,
    DELETE_FILE_BY_ID,
    DELETE_RESOURCE_BY_ID,
    REPLACE_RESOURCE_WITH_RESOURCE_AND_TYPE

}
