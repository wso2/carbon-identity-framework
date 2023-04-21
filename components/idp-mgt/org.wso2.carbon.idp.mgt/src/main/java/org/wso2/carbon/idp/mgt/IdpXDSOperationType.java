package org.wso2.carbon.idp.mgt;

import org.wso2.carbon.identity.xds.common.constant.XDSOperationType;

public enum IdpXDSOperationType implements XDSOperationType {

    ADD_IDP_WITH_RESOURCE_ID,
    ADD_RESIDENT_IDP,
    UPDATE_RESIDENT_IDP,
    DELETE_IDP,
    DELETE_IDPS,
    DELETE_IDP_BY_RESOURCE_ID,
    FORCE_DELETE_IDP,
    FORCE_DELETE_IDP_BY_RESOURCE_ID,
    UPDATE_IDP,
    UPDATE_IDP_BY_RESOURCE_ID
}
