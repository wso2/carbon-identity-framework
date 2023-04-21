package org.wso2.carbon.identity.claim.metadata.mgt;

import org.wso2.carbon.identity.xds.common.constant.XDSOperationType;

public enum ClaimXDSOperationType implements XDSOperationType {

    ADD_CLAIM_DIALECT,
    RENAME_CLAIM_DIALECT,
    REMOVE_CLAIM_DIALECT,
    ADD_LOCAL_CLAIM,
    UPDATE_LOCAL_CLAIM,
    UPDATE_LOCAL_CLAIM_MAPPINGS,
    REMOVE_LOCAL_CLAIM,
    ADD_EXTERNAL_CLAIM,
    UPDATE_EXTERNAL_CLAIM,
    REMOVE_EXTERNAL_CLAIM,
    REMOVE_CLAIM_MAPPING_ATTRIBUTES,
    REMOVE_ALL_CLAIMS,
}
