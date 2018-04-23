/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.core;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.common.AbstractUserManagementErrorListener;
import org.wso2.carbon.user.core.listener.UserManagementErrorEventListener;

/**
 * This class maintains the utility methods to check whether a particular listener is enabled or not and to get the
 * executor id.
 */
public class AbstractIdentityUserMgtFailureEventListener extends AbstractUserManagementErrorListener {

    /**
     * To get the execution order id from the configuration file.
     *
     * @return relevant order id of the event listener.
     */
    public int getOrderId() {

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (UserManagementErrorEventListener.class.getName(), this.getClass().getName());
        if (identityEventListenerConfig == null) {
            return IdentityCoreConstants.EVENT_LISTENER_ORDER_ID;
        }
        return identityEventListenerConfig.getOrder();
    }

    @Override
    public boolean isEnable() {

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil
                .readEventListenerProperty(UserManagementErrorEventListener.class.getName(), this.getClass().getName());

        return identityEventListenerConfig == null || StringUtils.isEmpty(identityEventListenerConfig.getEnable())
                || Boolean.parseBoolean(identityEventListenerConfig.getEnable());
    }
}
