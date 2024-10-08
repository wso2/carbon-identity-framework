/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.action.management.listener;

import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;

/**
 * Abstract implementation of the ActionManagementListener interface.
 */
public abstract class AbstractActionManagementListener implements ActionManagementListener {

    @Override
    public void preAddAction(String actionType, Action action, String tenantDomain) throws ActionMgtException {

    }

    @Override
    public void postAddAction(String actionType, Action action, String tenantDomain) throws ActionMgtException {

    }

    @Override
    public void preUpdateAction(String actionType, String actionId, Action action, String tenantDomain)
            throws ActionMgtException {

    }

    @Override
    public void postUpdateAction(String actionType, String actionId, Action action, String tenantDomain)
            throws ActionMgtException {

    }

    @Override
    public void preDeleteAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

    }

    @Override
    public void postDeleteAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

    }

    @Override
    public void preActivateAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

    }

    @Override
    public void postActivateAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

    }

    @Override
    public void preDeactivateAction(String actionType, String actionId, String tenantDomain)
            throws ActionMgtException {

    }

    @Override
    public void postDeactivateAction(String actionType, String actionId, String tenantDomain)
            throws ActionMgtException {

    }

    @Override
    public int getExecutionOrderId() {

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (ActionManagementListener.class.getName(), this.getClass().getName());
        if (identityEventListenerConfig == null) {
            return IdentityCoreConstants.EVENT_LISTENER_ORDER_ID;
        }
        return identityEventListenerConfig.getOrder();
    }
}
