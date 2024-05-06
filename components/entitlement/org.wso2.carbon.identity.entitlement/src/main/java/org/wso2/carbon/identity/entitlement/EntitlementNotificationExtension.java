/*
 *Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
*/
package org.wso2.carbon.identity.entitlement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dao.StatusDataDAO;
import org.wso2.carbon.identity.entitlement.dto.StatusHolder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.notification.mgt.NotificationManagementException;
import org.wso2.carbon.identity.notification.mgt.NotificationSender;
import org.wso2.carbon.identity.notification.mgt.bean.PublisherEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This is an extension module that can be used to send policy update statuses as notifications.
 * This extension will trigger notifications on policy changes only if this extension is
 * registered in entitlement.properties file.
 */
@SuppressWarnings("unused")
public class EntitlementNotificationExtension implements StatusDataDAO {

    private static final Log log = LogFactory.getLog(EntitlementNotificationExtension.class);
    private final String eventName = "policyUpdate";
    private boolean pdpUpdate = true;
    private boolean papUpdate = false;
    private List<String> pdpActions = new ArrayList<String>();

    /**
     * At the initialization a property map which carries relevant properties to this extension
     * will be passed and class variables will be set from those properties.
     *
     * @param properties properties
     */
    @Override
    public void init(Properties properties) {

        if (log.isDebugEnabled()) {
            log.debug("Initiating Entitlement Notification Extension");
        }
        // Reading properties and setting to default values if properties are not found
        String pdpUpdateProperty = properties.getProperty(NotificationConstants
                .PDP_NOTIFICATION_PROPERTY_LABEL);
        String papNotificationProperty = properties.getProperty(NotificationConstants
                .PAP_NOTIFICATION_PROPERTY_LABEL);

        if (pdpUpdateProperty != null && !pdpUpdateProperty.trim().isEmpty()) {
            pdpUpdate = Boolean.parseBoolean(pdpUpdateProperty);
        }   // Else default value of false

        // only pap policy updates
        if (papNotificationProperty != null && !papNotificationProperty.trim().isEmpty()) {
            papUpdate = Boolean.parseBoolean(papNotificationProperty);
        }
        //Else default value of false

        // pdp action
        String pdpActionUpdate = properties.getProperty(NotificationConstants
                .PDP_NOTIFICATION_ACTION_PROPERTY_LABEL);
        if (pdpActionUpdate != null) {
            String[] pdpActionUpdates = pdpActionUpdate.split(";");
            for (String update : pdpActionUpdates) {
                pdpActions.add(update.trim());
            }
        } // Else will have an empty list
    }

    @Override
    public void handle(String about, String key, List<StatusHolder> statusHolder) throws
            EntitlementException {
        // If status is about policy return.
        if (EntitlementConstants.Status.ABOUT_POLICY.equalsIgnoreCase(about)) {
            return;
        }
        if (statusHolder != null) {
            for (StatusHolder holder : statusHolder) {
                handle(about, holder);
            }
        }
    }

    /**
     * handler will decide the process depending on the status in status holder
     *
     * @param about        indicates what is related with this admin status action
     * @param statusHolder <code>StatusHolder</code>
     * @throws EntitlementException
     */
    @Override
    public void handle(String about, StatusHolder statusHolder) throws EntitlementException {

        if (!EntitlementConstants.Status.ABOUT_POLICY.equalsIgnoreCase(about)) {
            return;
        }

        String action = null;
        String typeOfAction = statusHolder.getType();

        //If papUpdate notifications are enabled through entitlement.properties
        if (papUpdate) {
            if (EntitlementConstants.StatusTypes.UPDATE_POLICY.equals(typeOfAction)) {
                action = NotificationConstants.ACTION_LABEL_UPDATE;
            } else if (EntitlementConstants.StatusTypes.DELETE_POLICY.equals(typeOfAction)) {
                action = NotificationConstants.ACTION_LABEL_DELETE;
            } else if (EntitlementConstants.StatusTypes.ADD_POLICY.equals(typeOfAction)) {
                action = NotificationConstants.ACTION_LABEL_CREATE;
            }
        }

        //if pdpUpdate properties are enabled through entitlement.properties
        if (pdpUpdate && action == null) {

            if (EntitlementConstants.StatusTypes.PUBLISH_POLICY.equals(typeOfAction)) {
                action = statusHolder.getTargetAction();
            }
            if (action == null || (pdpActions.size() > 0 && !pdpActions.contains(action))) {
                return;
            }
            if (EntitlementConstants.PolicyPublish.ACTION_CREATE.equals(action) ||
                    EntitlementConstants.PolicyPublish.ACTION_UPDATE.equals(action)) {
                action = NotificationConstants.ACTION_LABEL_UPDATE;
            }
        }

        if (action == null) {
            return;
        }
        // Setting up properties and configuration object to be sent to the NotificationSender,
        // which is consumed by all subscribed Message Sending Modules
        NotificationSender notificationSender = EntitlementServiceComponent.getNotificationSender();

        if (notificationSender != null) {
            try {
                PublisherEvent event = new PublisherEvent(eventName);
                event.addEventProperty(NotificationConstants.TARGET_ID_PROPERTY_LABEL, statusHolder.getKey());
                event.addEventProperty(NotificationConstants.USERNAME_PROPERTY_LABEL, statusHolder.getUser());
                event.addEventProperty(NotificationConstants.TARGET_PROPERTY_LABEL, statusHolder.getTarget());
                event.addEventProperty(NotificationConstants.ACTION_PROPERTY_LABEL, action);
                if (log.isDebugEnabled()) {
                    log.debug("Invoking notification sender");
                }
                notificationSender.invoke(event);
            } catch (NotificationManagementException e) {
                log.error("Error while invoking notification sender", e);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.error("No registered notification sending service found");
            }
        }
    }

    @Override
    public StatusHolder[] getStatusData(String about, String key, String type,
                                        String searchString) throws EntitlementException {
        return new StatusHolder[0];
    }
}
