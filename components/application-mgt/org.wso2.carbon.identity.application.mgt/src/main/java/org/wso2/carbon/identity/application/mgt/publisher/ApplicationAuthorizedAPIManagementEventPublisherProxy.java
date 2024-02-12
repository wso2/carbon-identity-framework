/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
 *
 */

package org.wso2.carbon.identity.application.mgt.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.services.IdentityEventService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles creating event and publishing events related to application authorized api management.
 */
public class ApplicationAuthorizedAPIManagementEventPublisherProxy {

    private static final Log log = LogFactory.getLog(ApplicationAuthorizedAPIManagementEventPublisherProxy.class);
    private static final ApplicationAuthorizedAPIManagementEventPublisherProxy proxy =
            new ApplicationAuthorizedAPIManagementEventPublisherProxy();

    private ApplicationAuthorizedAPIManagementEventPublisherProxy() {

    }

    public static ApplicationAuthorizedAPIManagementEventPublisherProxy getInstance() {

        return proxy;
    }
    public void publishPreUpdateAuthorizedAPIForApplication(String appId, String apiId, List<String> addedScopes,
                                                            List<String> removedScopes, String tenantDomain)
            throws IdentityApplicationManagementException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.APPLICATION_ID, appId);
        eventProperties.put(IdentityEventConstants.EventProperty.API_ID, apiId);
        eventProperties.put(IdentityEventConstants.EventProperty.ADDED_SCOPES, addedScopes);
        eventProperties.put(IdentityEventConstants.EventProperty.DELETED_SCOPES, removedScopes);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties,
                IdentityEventConstants.Event.PRE_UPDATE_AUTHORIZED_API_FOR_APPLICATION_EVENT);
        doPublishEvent(event);
    }

    public void publishPostUpdateAuthorizedAPIForApplication(String appId, String apiId, List<String> addedScopes,
                                                             List<String> removedScopes, String tenantDomain)
            throws IdentityApplicationManagementException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.APPLICATION_ID, appId);
        eventProperties.put(IdentityEventConstants.EventProperty.API_ID, apiId);
        eventProperties.put(IdentityEventConstants.EventProperty.ADDED_SCOPES, addedScopes);
        eventProperties.put(IdentityEventConstants.EventProperty.DELETED_SCOPES, removedScopes);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties,
                IdentityEventConstants.Event.POST_UPDATE_AUTHORIZED_API_FOR_APPLICATION_EVENT);
        doPublishEvent(event);

    }

    public void publishPreDeleteAuthorizedAPIForApplication(String appId, String apiId, String tenantDomain)
            throws IdentityApplicationManagementException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.APPLICATION_ID, appId);
        eventProperties.put(IdentityEventConstants.EventProperty.API_ID, apiId);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties,
                IdentityEventConstants.Event.PRE_DELETE_AUTHORIZED_API_FOR_APPLICATION_EVENT);
        doPublishEvent(event);
    }

    public void publishPostDeleteAuthorizedAPIForApplication(String appId, String apiId, String tenantDomain)
            throws IdentityApplicationManagementException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.APPLICATION_ID, appId);
        eventProperties.put(IdentityEventConstants.EventProperty.API_ID, apiId);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties,
                IdentityEventConstants.Event.POST_DELETE_AUTHORIZED_API_FOR_APPLICATION_EVENT);
        doPublishEvent(event);
    }

    private Event createEvent(Map<String, Object> eventProperties, String eventName) {

        return new Event(eventName, eventProperties);
    }

    private void doPublishEvent(Event event) throws IdentityApplicationManagementException {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Event: " + event.getEventName() + " is published for the application management " +
                        "operation in the tenant with the tenantId: "
                        + event.getEventProperties().get(IdentityEventConstants.EventProperty.TENANT_ID));
            }
            IdentityEventService eventService =
                    ApplicationManagementServiceComponentHolder.getInstance().getIdentityEventService();
            eventService.handleEvent(event);
        } catch (IdentityEventException e) {
            throw new IdentityApplicationManagementException(e.getErrorCode(),
                        "Error while publishing the event: " + event.getEventName() + ".", e);
        }
    }
}
