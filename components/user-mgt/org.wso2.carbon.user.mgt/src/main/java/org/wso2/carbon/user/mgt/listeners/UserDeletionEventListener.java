/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.user.mgt.listeners;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.mgt.internal.UserMgtDSComponent;
import org.wso2.carbon.user.mgt.recorder.RecorderException;
import org.wso2.carbon.user.mgt.recorder.UserDeletionEventRecorder;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 * This event listener's main purpose is to listen to user deletion and persist those data to using a suitable appender.
 */
public class UserDeletionEventListener extends AbstractIdentityUserOperationEventListener {

    private static final Log log = LogFactory.getLog(UserDeletionEventListener.class);

    private static final String EVENT_RECORDERS_ELEMENT = "UserDeleteEventRecorders";
    private static final String ENABLE_ATTRIBUTE = "enable";
    private static final String NAME_ATTRIBUTE = "name";

    @Override
    public int getExecutionOrderId() {
        int orderId = getOrderId();
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }
        return 98;
    }

    @Override
    public boolean doPostDeleteUser(String userName, UserStoreManager userStoreManager) throws UserStoreException {

        // Check whether the listener is enabled.
        if (!isEnable()) {
            return true;
        }

        IdentityConfigParser identityConfigParser = IdentityConfigParser.getInstance();
        Map<UserDeletionEventRecorder, Map<String, String>> userDeleteEventRecorders =
                readDeletionEventRecorders(identityConfigParser);

        try {
            String userStoreDomain = UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration());
            int tenantId = userStoreManager.getTenantId();
            String tenantDomain = UserMgtDSComponent.getRealmService().getTenantManager()
                    .getDomain(userStoreManager.getTenantId());

            // We are calling all the event recorders with the values we have. (Including properties we read.)
            for (Map.Entry<UserDeletionEventRecorder, Map<String, String>> entry : userDeleteEventRecorders.entrySet
                    ()) {
                UserDeletionEventRecorder userDeletionEventRecorder = entry.getKey();
                Map<String, String> stringMap = entry.getValue();

                userDeletionEventRecorder.recordUserDeleteEvent(userName, userStoreDomain, tenantDomain, tenantId,
                        new Date(System.currentTimeMillis()), stringMap);
                if (log.isDebugEnabled()) {
                    log.debug("Event recorder with name: " + userDeletionEventRecorder.getClass().getName() +
                            " invoked with values. " + userName + ", " + userStoreDomain + ", " + tenantId);
                }
            }
        } catch (RecorderException | org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }

        return true;
    }

    /**
     * Read the identity.xml file and get all the Delete Event recorders.
     * @param identityConfigParser Instance to parse the identity.xml.
     * @return Map of DeletionEventRecorders with there properties.
     */
    private Map<UserDeletionEventRecorder, Map<String, String>> readDeletionEventRecorders(
            IdentityConfigParser identityConfigParser) {

        Map<UserDeletionEventRecorder, Map<String, String>> userDeleteEventRecorders = new HashMap<>();

        // All the delete event recorders are listed under this tag.
        OMElement deleteEventRecorders = identityConfigParser.getConfigElement(EVENT_RECORDERS_ELEMENT);

        // No deleted event recorder element found in the identity.xml.
        if (deleteEventRecorders == null) {
            return userDeleteEventRecorders;
        }

        // We have to iterate each child and get every recorder.
        Iterator deleteEventRecordersChildElements = deleteEventRecorders.getChildElements();
        while (deleteEventRecordersChildElements.hasNext()) {

            Object recorderElement = deleteEventRecordersChildElements.next();
            if (recorderElement instanceof OMElement) {
                OMAttribute enabled = ((OMElement) recorderElement).getAttribute(new QName(ENABLE_ATTRIBUTE));

                // We can ignore rest if this is not enabled or enabled attribute is missing.
                if (enabled == null || !Boolean.parseBoolean(enabled.getAttributeValue())) {
                    continue;
                }

                OMAttribute className = ((OMElement) recorderElement).getAttribute(new QName(NAME_ATTRIBUTE));
                if (className != null) {
                    UserDeletionEventRecorder userDeletionEventRecorder = UserMgtDSComponent
                            .getUserDeleteEventRecorders().get(className.getAttributeValue());

                    // Read the properties if this has the element and add it to the map. This is optional. There can
                    // be instances where recorder element without properties.
                    Map<String, String> propertiesMap = new HashMap<>();
                    Iterator properties = ((OMElement) recorderElement).getChildElements();
                    while (properties.hasNext()) {
                        Object propertyElement = properties.next();
                        if (propertyElement instanceof OMElement) {
                            String nameAttribute = ((OMElement) propertyElement).getAttributeValue(
                                    new QName(NAME_ATTRIBUTE));
                            String value = ((OMElement) propertyElement).getText();
                            propertiesMap.put(nameAttribute, value);
                            if (log.isDebugEnabled()) {
                                log.debug("Property " + value + " added to the recorder: " + className
                                        .getAttributeValue());
                            }
                        }
                    }
                    if (userDeletionEventRecorder != null) {
                        userDeleteEventRecorders.put(userDeletionEventRecorder, propertiesMap);
                        if (log.isDebugEnabled()) {
                            log.debug("Event recorder with name: " + className.getAttributeValue() + " added.");
                        }
                    }
                }
            }
        }

        return userDeleteEventRecorders;
    }
}
