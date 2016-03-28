/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.store.configuration.listener;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;

public abstract class AbstractUserStoreConfigListener implements UserStoreConfigListener {

    public boolean isEnable() {
        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (UserOperationEventListener.class.getName(), this.getClass().getName());

        if (identityEventListenerConfig == null) {
            return true;
        }

        if (StringUtils.isNotBlank(identityEventListenerConfig.getEnable())) {
            return Boolean.parseBoolean(identityEventListenerConfig.getEnable());
        } else {
            return true;
        }
    }

    public int getOrderId() {
        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (UserOperationEventListener.class.getName(), this.getClass().getName());
        if (identityEventListenerConfig == null) {
            return IdentityCoreConstants.EVENT_LISTENER_ORDER_ID;
        }
        return identityEventListenerConfig.getOrder();
    }

    @Override
    public void onUserStoreNamePreUpdate(int tenantId, String currentUserStoreName, String newUserStoreName) throws UserStoreException {
        /* Method not implemented */
    }

    @Override
    public void onUserStoreNamePostUpdate(int tenantId, String currentUserStoreName, String newUserStoreName) throws UserStoreException {
        /* Method not implemented */
    }

    @Override
    public void onUserStorePreDelete(int tenantId, String userStoreName) throws UserStoreException {
        /* Method not implemented */
    }

    @Override
    public void onUserStorePostDelete(int tenantId, String userStoreName) throws UserStoreException {
        /* Method not implemented */
    }
}
