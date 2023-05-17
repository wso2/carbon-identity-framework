package org.wso2.carbon.identity.template.mgt;

import org.wso2.carbon.identity.xds.common.constant.XDSOperationType;

public enum TemplateXDSOperationType implements XDSOperationType {

    ADD_TEMPLATE,
    UPDATE_TEMPLATE,
    DELETE_TEMPLATE,
    DELETE_TEMPLATE_BY_ID,
    UPDATE_TEMPLATE_BY_ID,
    ADD_TEMPLATE_USING_TEMPLATE_MGT_DAO
}
