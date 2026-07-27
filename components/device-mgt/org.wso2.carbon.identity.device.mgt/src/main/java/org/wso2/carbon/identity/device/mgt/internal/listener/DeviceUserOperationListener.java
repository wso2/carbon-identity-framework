/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.device.mgt.internal.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.internal.service.impl.DeviceManagementServiceImpl;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

/**
 * User operation event listener that removes a user's registered devices when the user is deleted,
 * so that no orphaned device records or public keys are left behind.
 */
public class DeviceUserOperationListener extends AbstractIdentityUserOperationEventListener {

    private static final Log LOG = LogFactory.getLog(DeviceUserOperationListener.class);
    private static final int DEFAULT_EXECUTION_ORDER_ID = 100;

    /**
     * Returns the execution order of this listener.
     *
     * @return Execution order id.
     */
    @Override
    public int getExecutionOrderId() {

        int orderId = getOrderId();
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }
        return DEFAULT_EXECUTION_ORDER_ID;
    }

    /**
     * Deletes the devices registered by the user after the user has been deleted.
     * A cleanup failure is logged but does not abort the user deletion, matching the behaviour of
     * other post-delete cleanup listeners in the framework.
     *
     * @param userID           Identifier of the deleted user.
     * @param userStoreManager User store manager.
     * @return {@code true} to let the user deletion flow continue.
     * @throws UserStoreException If the tenant id cannot be resolved.
     */
    @Override
    public boolean doPostDeleteUserWithID(String userID, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String tenantDomain = IdentityTenantUtil.getTenantDomain(userStoreManager.getTenantId());
        try {
            DeviceManagementServiceImpl.getInstance().deleteDevicesByUserId(userID, tenantDomain);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Deleted devices for user id: " + userID + " in tenant: " + tenantDomain
                        + " on user deletion.");
            }
        } catch (DeviceMgtException e) {
            LOG.error("Error while deleting devices for user id: " + userID + " in tenant: "
                    + tenantDomain + " on user deletion.", e);
        }
        return true;
    }
}
