/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.claim.metadata.mgt.listener;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;

/**
 * Abstract implementation for the {@link ClaimMetadataMgtListener}.
 */
public abstract class AbstractClaimMetadataMgtListener  implements ClaimMetadataMgtListener {

    public boolean isEnable() {

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (ClaimMetadataMgtListener.class.getName(), this.getClass().getName());
        if (identityEventListenerConfig == null) {
            return true;
        }
        if (StringUtils.isNotBlank(identityEventListenerConfig.getEnable())) {
            return Boolean.parseBoolean(identityEventListenerConfig.getEnable());
        } else {
            return true;
        }
    }

    public int getExecutionOrderId() {

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (ClaimMetadataMgtListener.class.getName(), this.getClass().getName());
        int orderId = IdentityCoreConstants.EVENT_LISTENER_ORDER_ID;
        if (identityEventListenerConfig != null) {
            orderId = identityEventListenerConfig.getOrder();
        }
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }
        return getDefaultOrderId();
    }

    @Override
    public boolean doPreDeleteClaim(String claimUri, String tenantDomain) throws ClaimMetadataException {

        return true;
    }

    @Override
    public boolean doPostDeleteClaim(String claimUri, String tenantDomain) throws ClaimMetadataException {

        return true;
    }

    @Override
    public boolean doPreUpdateLocalClaim(LocalClaim localClaim, String tenantDomain) throws ClaimMetadataException {

        return true;
    }
}
