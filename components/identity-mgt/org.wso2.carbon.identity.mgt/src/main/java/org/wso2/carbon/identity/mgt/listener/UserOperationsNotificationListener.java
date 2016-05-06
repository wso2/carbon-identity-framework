/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.mgt.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.identity.notification.mgt.NotificationManagementException;
import org.wso2.carbon.identity.notification.mgt.NotificationSender;
import org.wso2.carbon.identity.notification.mgt.bean.PublisherEvent;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

import java.util.Map;

/**
 * This listener is registered as a user operation listener. Whenever a user operation takes place
 * this listener fires message sending module, So that registered modules with registered events
 * will send messages to endpoints.
 */
public class UserOperationsNotificationListener extends AbstractIdentityUserOperationEventListener {

    private static final Log log = LogFactory.getLog(UserOperationsNotificationListener.class);
    private final String eventName = "userOperation";
    private final String usernameLabel = "username";
    private final String operationLabel = "operation";
    private final String EVENT_TYPE_PROFILE_UPDATE = "profileUpdate";
    private final String EVENT_TYPE_ROLE_UPDATE = "roleUpdate";

    @Override
    public int getExecutionOrderId() {
        int orderId = getOrderId();
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }
        return 80;
    }

    /**
     * Overridden to trigger Notification sending module to send messages to registered modules
     * on doPostDeleteOperation
     *
     * @param username         Username of the deleted user
     * @param userStoreManager Instance of user store manager called
     * @return Always returns true, even if message sending fails there is no major effect on
     * further operations.
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    @Override
    public boolean doPostDeleteUser(String username, UserStoreManager userStoreManager)
            throws UserStoreException {
        if (!isEnable()) {
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("Sending user delete notification for user " + username);
        }
        sendNotification(EVENT_TYPE_PROFILE_UPDATE, username);
        // Returns true since no major effect on upcoming listeners
        return true;
    }

    /**
     * Overridden to trigger Notification Sending module to send messages to registered modules
     * on doPostDeleteUserClaimValues
     *
     * @param username         Username of the deleted user
     * @param userStoreManager Instance of user store manager called
     * @return Always returns true, even if message sending fails there is no major effect on
     * further operations.
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    @Override
    public boolean doPostDeleteUserClaimValues(String username, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("Sending user claim value update notification for user " + username);
        }
        sendNotification(EVENT_TYPE_PROFILE_UPDATE, username);
        // Returns true since no major effect on upcoming listeners
        return true;
    }

    /**
     * Overridden to trigger Notification Sending module to send messages to registered modules
     * on
     * doPostDeleteUserClaimValues
     *
     * @param username         Username of the deleted user
     * @param userStoreManager Instance of user store manager called
     * @return Always returns true, even if message sending fails there is no major effect on
     * further operations.
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    @Override
    public boolean doPostDeleteUserClaimValue(String username, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("Sending user delete update notification for user " + username);
        }
        sendNotification(EVENT_TYPE_PROFILE_UPDATE, username);
        // Returns true since no major effect on upcoming listeners
        return true;
    }

    /**
     * Overridden to trigger Notification Sending module to send messages to registered modules
     * on PostUpdateRoleListOfUser
     *
     * @param username         Username of role updated user
     * @param deletedRoles     List of roles deleted
     * @param newRoles         list of roles added
     * @param userStoreManager Instance of user store manager called
     * @return always returns true since no major effect on further operations.
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    public boolean doPostUpdateRoleListOfUser(String username,
                                              String[] deletedRoles, String[] newRoles,
                                              UserStoreManager userStoreManager)
            throws UserStoreException {
        if (!isEnable()) {
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("Sending user role list update notification for user " + username);
        }
        sendNotification(EVENT_TYPE_ROLE_UPDATE, username);
        // Returns true since no major effect on upcoming listeners
        return true;
    }

    /**
     * Overridden to trigger Notification Sending module to send messages to registered modules
     * on doPostSetUserClaimValues
     *
     * @param username         username of user whose claim values are updated
     * @param claims           set of claims
     * @param profileName      profile name
     * @param userStoreManager instance of user store manager called
     * @return always returns true since no major effect on further operations
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    @Override
    public boolean doPostSetUserClaimValues(String username,
                                            Map<String, String> claims, String profileName,
                                            UserStoreManager userStoreManager)
            throws UserStoreException {
        if (!isEnable()) {
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("Sending user claim values update notification for user " + username);
        }
        sendNotification(EVENT_TYPE_PROFILE_UPDATE, username);
        // Returns true since no major effect on upcoming listeners
        return true;
    }

    /**
     * This function builds the required configuration object for Notification sender and pass it
     * to the notification sender with the relevant event.
     *
     * @param operation Type or operation took place in user operation listener
     * @param username  username of the subjected user for attribute change
     */
    private void sendNotification(String operation, String username) {
        NotificationSender notificationSender = IdentityMgtServiceComponent.getNotificationSender();
        if (notificationSender != null) {
            try {
                PublisherEvent event = new PublisherEvent(eventName);
                event.addEventProperty(operationLabel, operation);
                event.addEventProperty(usernameLabel, username);
                if (log.isDebugEnabled()) {
                    log.debug("Invoking notification sender");
                }
                notificationSender.invoke(event);
            } catch (NotificationManagementException e) {
                log.error("Error while sending notifications on user operations", e);
            }
        } else {
            log.error("No registered notification sender found. Notification sending aborted");
        }
    }
}
