/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.mgt.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.mgt.EventMgtConstants;
import org.wso2.carbon.identity.mgt.EventMgtException;
import org.wso2.carbon.identity.mgt.EventMgtConfigBuilder;
import org.wso2.carbon.identity.mgt.event.Event;
import org.wso2.carbon.identity.mgt.internal.EventMgtServiceDataHolder;
import org.wso2.carbon.identity.mgt.services.EventMgtService;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserOperationEventListener;

import java.util.HashMap;


/**
 * This is an implementation of UserOperationEventListener. This defines
 * additional operations
 * for some of the core user management operations
 */
public class EventMgtListener extends AbstractUserOperationEventListener {

    private static final Log log = LogFactory.getLog(EventMgtListener.class);
//    private UserIdentityDataStore module;
    EventMgtService eventMgtService = EventMgtServiceDataHolder.getInstance().getEventMgtService();

    public EventMgtListener() {

//        module = IdentityMgtConfigGlobal.getInstance().getUserIdentityDataStore();
//        String adminUserName =
//                IdentityMgtServiceComponent.getRealmService()
//                        .getBootstrapRealmConfiguration()
//                        .getAdminUserName();
//        try {
//            UserStoreManager userStoreMng = IdentityMgtServiceComponent.getRealmService()
//                    .getBootstrapRealm().getUserStoreManager();
//            if (!userStoreMng.isReadOnly()) {
//                Map<String, String> claimMap = new HashMap<>();
//                claimMap.put(IdentityMgtConstants.Claim.ACCOUNT_LOCK, Boolean.toString(false));
//                userStoreMng.setUserClaimValues(adminUserName, claimMap, null);
//            }
//
//        } catch (UserStoreException e) {
//            log.error("Error while init identity listener", e);
//        }
    }

    /**
     * This method checks if the user account exist or is locked. If the account is
     * locked, the authentication process will be terminated after this method
     * returning false.
     */
    @Override
    public boolean doPreAuthenticate(String userName, Object credential,
                                     UserStoreManager userStoreManager) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Pre authenticator is called in IdentityMgtEventListener");
        }
        try {
            String eventName = EventMgtConstants.Event.PRE_AUTHENTICATION;

            HashMap<String, Object> properties = new HashMap<>();
//        properties.put(IdentityMgtConstants.EventProperty.MODULE, module);
            properties.put(EventMgtConstants.EventProperty.USER_NAME, userName);
            properties.put(EventMgtConstants.EventProperty.USER_STORE_MANAGER, userStoreManager);
            properties.put(EventMgtConstants.EventProperty.IDENTITY_MGT_CONFIG, EventMgtConfigBuilder
                    .getInstance());

            Event identityMgtEvent = new Event(eventName, properties);

            eventMgtService.handleEvent(identityMgtEvent);
        } catch (EventMgtException e) {
            throw new UserStoreException("Error when authenticating user", e);
        }

        return true;
    }

    /**
     * This method retrieves the configurations for the tenant ID of the user
     */
//    protected IdentityMgtConfig getConfiguration(int tenantId) {
//        IdentityMgtConfig identityMgtConfig = null;
//        try {
//            identityMgtConfig = new IdentityMgtConfig();
//            Properties properties = identityMgtConfig.getConfiguration(tenantId);
//            identityMgtConfig.setConfiguration(properties);
//        } catch (IdentityMgtException ex) {
//            log.error("Error when retrieving configurations of tenant: " + tenantId, ex);
//        }
//        return identityMgtConfig;
//        return null;
//    }
}
