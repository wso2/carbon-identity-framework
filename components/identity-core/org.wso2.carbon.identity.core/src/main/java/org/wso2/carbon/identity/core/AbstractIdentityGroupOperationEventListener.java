/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.core;

import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.common.AbstractGroupOperationEventListener;
import org.wso2.carbon.user.core.listener.GroupOperationEventListener;

/**
 * Abstract implementation of AbstractGroupOperationEventListener.
 */
public class AbstractIdentityGroupOperationEventListener extends AbstractGroupOperationEventListener {

    /**
     * Whether the listener is enabled.
     *
     * @return True if the listener is enabled.
     */
    public boolean isEnable() {

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (GroupOperationEventListener.class.getName(), this.getClass().getName());
        // By default the listener will be enabled.
        if (identityEventListenerConfig == null) {
            return true;
        }
        return Boolean.parseBoolean(identityEventListenerConfig.getEnable());
    }

    /**
     * Get the order id of the GroupOperationEventListener.
     *
     * @return Order id of the GroupOperationEventListener.
     */
    public int getOrderId() {

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (GroupOperationEventListener.class.getName(), this.getClass().getName());
        if (identityEventListenerConfig == null) {
            return IdentityCoreConstants.EVENT_LISTENER_ORDER_ID;
        }
        return identityEventListenerConfig.getOrder();
    }
}
