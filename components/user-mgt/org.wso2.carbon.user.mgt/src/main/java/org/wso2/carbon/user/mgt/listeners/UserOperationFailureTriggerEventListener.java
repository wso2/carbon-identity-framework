/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.user.mgt.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.AbstractIdentityUserMgtFailureEventListener;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.internal.IdentityEventServiceDataHolder;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.REGISTRATION_FAILURE;

public class UserOperationFailureTriggerEventListener extends AbstractIdentityUserMgtFailureEventListener {

    private static final Log log = LogFactory.getLog(UserOperationFailureTriggerEventListener.class);
    IdentityEventService identityEventService = IdentityEventServiceDataHolder.getInstance().getEventMgtService();

    @Override
    public int getExecutionOrderId() {

        int orderId = getOrderId();
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }
        return 220;
    }

    @Override
    public boolean onAddUserFailure(String errorCode, String errorMessage, String userName, Object credential,
                                    String[] roleList, Map<String, String> claims, String profile,
                                    UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }
        if (log.isDebugEnabled()) {
            log.debug("On add user failure is called in UserOperationFailureListener");
        }

        HashMap<String, Object> properties = new HashMap<>();
        properties.put(IdentityEventConstants.EventProperty.ERROR_CODE, errorCode);
        properties.put(IdentityEventConstants.EventProperty.ERROR_MESSAGE, errorMessage);

        properties.put(IdentityEventConstants.EventProperty.USER_CLAIMS, claims);
        properties.put(IdentityEventConstants.EventProperty.ROLE_LIST, roleList);
        properties.put(IdentityEventConstants.EventProperty.PROFILE_NAME, profile);

        handleEvent(REGISTRATION_FAILURE, properties, userStoreManager);
        return true;
    }

    @Override
    public boolean onAddUserFailureWithID(String errorCode, String errorMessage, String userID, Object credential,
                                          String[] roleList, Map<String, String> claims, String profile,
                                          UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }
        if (log.isDebugEnabled()) {
            log.debug("On add user failure is called in UserOperationFailureListener");
        }

        HashMap<String, Object> properties = new HashMap<>();
        properties.put(IdentityEventConstants.EventProperty.ERROR_CODE, errorCode);
        properties.put(IdentityEventConstants.EventProperty.ERROR_MESSAGE, errorMessage);

        properties.put(IdentityEventConstants.EventProperty.USER_CLAIMS, claims);
        properties.put(IdentityEventConstants.EventProperty.ROLE_LIST, roleList);
        properties.put(IdentityEventConstants.EventProperty.PROFILE_NAME, profile);

        handleEvent(REGISTRATION_FAILURE, properties, userStoreManager);
        return true;
    }

    private void handleEvent(String eventName, HashMap<String, Object> properties, UserStoreManager
            userStoreManager) throws UserStoreException {

        Event identityMgtEvent = new Event(eventName, properties);
        try {
            int tenantId = userStoreManager.getTenantId();
            String userTenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            try {
                RealmService realmService = IdentityTenantUtil.getRealmService();
                TenantManager tenantManager = realmService.getTenantManager();
                userTenantDomain = tenantManager.getDomain(tenantId);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                log.error("Unable to get the get the domain from realmService for tenant: " + tenantId, e);
            }

            properties.put(IdentityEventConstants.EventProperty.USER_STORE_MANAGER, userStoreManager);
            properties.put(IdentityEventConstants.EventProperty.TENANT_ID, PrivilegedCarbonContext
                    .getThreadLocalCarbonContext().getTenantId());
            properties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, userTenantDomain);

            // todo check tenant admin creation failure
            identityEventService.handleEvent(identityMgtEvent);

        } catch (IdentityEventException e) {

            throw new UserStoreException("Error when handling event : " + eventName, e);
        }
    }
}
