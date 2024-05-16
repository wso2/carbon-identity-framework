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

package org.wso2.carbon.identity.api.resource.mgt.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.api.resource.mgt.internal.APIResourceManagementServiceComponentHolder;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.services.IdentityEventService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles creating event and publishing events related to api resource management.
 */
public class APIResourceManagerEventPublisherProxy {

    private static final Log log = LogFactory.getLog(APIResourceManagerEventPublisherProxy.class);
    private static final APIResourceManagerEventPublisherProxy instance = new APIResourceManagerEventPublisherProxy();

    private APIResourceManagerEventPublisherProxy() {

    }

    /**
     * Get the instance of APIResourceManagerEventPublisherProxy.
     *
     * @return APIResourceManagerEventPublisherProxy instance.
     */
    public static APIResourceManagerEventPublisherProxy getInstance() {

        return instance;
    }

    /**
     * Publish the pre add API resource event.
     *
     * @param apiResource   API resource.
     * @param tenantDomain  Tenant domain.
     * @throws APIResourceMgtException If an error occurred while publishing the event.
     */
    public void publishPreAddAPIResource(APIResource apiResource, String tenantDomain) throws  APIResourceMgtException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.API_RESOURCE, apiResource);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_ADD_API_RESOURCE);
        doPublishEvent(event);
    }

    /**
     * Publish the post add API resource event.
     *
     * @param apiResource   API resource.
     * @param tenantDomain  Tenant domain.
     * @throws APIResourceMgtException If an error occurred while publishing the event.
     */
    public void publishPostAddAPIResource(APIResource apiResource, String tenantDomain) throws APIResourceMgtException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.API_RESOURCE, apiResource);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_ADD_API_RESOURCE);
        doPublishEvent(event);
    }

    /**
     * Publish the pre delete API resource by id event.
     *
     * @param apiResourceId  API resource id.
     * @param tenantDomain   Tenant domain.
     * @throws APIResourceMgtException If an error occurred while publishing the event.
     */
    public void publishPreDeleteAPIResourceById(String apiResourceId, String tenantDomain)
            throws APIResourceMgtException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.API_ID, apiResourceId);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_DELETE_API_RESOURCE);
        doPublishEvent(event);
    }

    /**
     * Publish the post delete API resource by id event.
     *
     * @param apiResourceId  API resource id.
     * @param tenantDomain   Tenant domain.
     * @throws APIResourceMgtException If an error occurred while publishing the event.
     */
    public void publishPostDeleteAPIResourceById(String apiResourceId, String tenantDomain)
            throws APIResourceMgtException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.API_ID, apiResourceId);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_DELETE_API_RESOURCE);
        doPublishEvent(event);
    }

    /**
     * Publish the pre update API resource event.
     *
     * @param apiResource    API resource.
     * @param addedScopes    Added scopes.
     * @param removedScopes  Removed scopes.
     * @param tenantDomain   Tenant domain.
     * @throws APIResourceMgtException If an error occurred while publishing the event.
     */
    public void publishPreUpdateAPIResource(APIResource apiResource, List<Scope> addedScopes,
                                            List<String> removedScopes, String tenantDomain)
            throws APIResourceMgtException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.API_RESOURCE, apiResource);
        eventProperties.put(IdentityEventConstants.EventProperty.ADDED_SCOPES, addedScopes);
        eventProperties.put(IdentityEventConstants.EventProperty.DELETED_SCOPES, removedScopes);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_UPDATE_API_RESOURCE);
        doPublishEvent(event);
    }

    /**
     * Publish the post update API resource event.
     *
     * @param apiResource    API resource.
     * @param addedScopes    Added scopes.
     * @param removedScopes  Removed scopes.
     * @param tenantDomain   Tenant domain.
     * @throws APIResourceMgtException If an error occurred while publishing the event.
     */
    public void publishPostUpdateAPIResource(APIResource apiResource, List<Scope> addedScopes,
                                             List<String> removedScopes, String tenantDomain)
            throws APIResourceMgtException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.API_RESOURCE, apiResource);
        eventProperties.put(IdentityEventConstants.EventProperty.ADDED_SCOPES, addedScopes);
        eventProperties.put(IdentityEventConstants.EventProperty.DELETED_SCOPES, removedScopes);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_UPDATE_API_RESOURCE);
        doPublishEvent(event);
    }

    /**
     * Publish the pre delete API scopes by API resource id event.
     *
     * @param apiResourceId API resource id.
     * @param tenantDomain  Tenant domain.
     * @throws APIResourceMgtException If an error occurred while publishing the event.
     */
    public void publishPreDeleteAPIScopesById(String apiResourceId, String tenantDomain)
            throws APIResourceMgtException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.API_ID, apiResourceId);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_DELETE_API_RESOURCE_SCOPES);
        doPublishEvent(event);
    }

    /**
     * Publish the post delete API scopes by API resource id event.
     *
     * @param apiResourceId API resource id.
     * @param tenantDomain  Tenant domain.
     * @throws APIResourceMgtException If an error occurred while publishing the event.
     */
    public void publishPostDeleteAPIScopesById(String apiResourceId, String tenantDomain)
            throws APIResourceMgtException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.API_ID, apiResourceId);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_DELETE_API_RESOURCE_SCOPES);
        doPublishEvent(event);
    }

    /**
     * Publish the pre delete API scope by scope name event.
     *
     * @param apiResourceId API resource id.
     * @param scopeName     Scope name.
     * @param tenantDomain  Tenant domain.
     * @throws APIResourceMgtException If an error occurred while publishing the event.
     */
    public void publishPreDeleteAPIScopeByScopeName(String apiResourceId, String scopeName, String tenantDomain)
            throws APIResourceMgtException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.API_ID, apiResourceId);
        eventProperties.put(IdentityEventConstants.EventProperty.SCOPE_NAME, scopeName);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_DELETE_SCOPE);
        doPublishEvent(event);
    }

    /**
     * Publish the post delete API scope by scope name event.
     *
     * @param apiResourceId API resource id.
     * @param scopeName     Scope name.
     * @param tenantDomain  Tenant domain.
     * @throws APIResourceMgtException If an error occurred while publishing the event.
     */
    public void publishPostDeleteAPIScopeByScopeName(String apiResourceId, String scopeName, String tenantDomain)
            throws APIResourceMgtException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.API_ID, apiResourceId);
        eventProperties.put(IdentityEventConstants.EventProperty.SCOPE_NAME, scopeName);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_DELETE_SCOPE);
        doPublishEvent(event);
    }

    /**
     * Publish the pre put API resource scopes event.
     *
     * @param apiResourceId  API resource id.
     * @param currentScopes  Old scopes.
     * @param scopes         New scopes.
     * @param tenantDomain   Tenant domain.
     * @throws APIResourceMgtException If an error occurred while publishing the event.
     */
    public void publishPrePutScopes(String apiResourceId, List<Scope> currentScopes, List<Scope> scopes,
                                    String tenantDomain) throws APIResourceMgtException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.API_ID, apiResourceId);
        eventProperties.put(IdentityEventConstants.EventProperty.OLD_SCOPES, currentScopes);
        eventProperties.put(IdentityEventConstants.EventProperty.NEW_SCOPES, scopes);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_PUT_API_RESOURCE_SCOPES);
        doPublishEvent(event);
    }

    /**
     * Publish the post put API resource scopes event.
     *
     * @param apiResourceId  API resource id.
     * @param currentScopes  Old scopes.
     * @param scopes         New scopes.
     * @param tenantDomain   Tenant domain.
     * @throws APIResourceMgtException If an error occurred while publishing the event.
     */
    public void publishPostPutScopes(String apiResourceId, List<Scope> currentScopes, List<Scope> scopes,
                                     String tenantDomain) throws APIResourceMgtException {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.API_ID, apiResourceId);
        eventProperties.put(IdentityEventConstants.EventProperty.OLD_SCOPES, currentScopes);
        eventProperties.put(IdentityEventConstants.EventProperty.NEW_SCOPES, scopes);
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_PUT_API_RESOURCE_SCOPES);
        doPublishEvent(event);
    }

    private Event createEvent(Map<String, Object> eventProperties, String eventName) {

        return new Event(eventName, eventProperties);
    }

    private void doPublishEvent(Event event) throws APIResourceMgtException {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Event: " + event.getEventName() + " is published for the api resource management " +
                        "operation in the tenant with the tenant domain: "
                        + event.getEventProperties().get(IdentityEventConstants.EventProperty.TENANT_DOMAIN));
            }
            IdentityEventService eventService =
                    APIResourceManagementServiceComponentHolder.getInstance().getIdentityEventService();
            eventService.handleEvent(event);
        } catch (IdentityEventException e) {
            throw new APIResourceMgtException(e.getErrorCode(),
                    "Error while publishing the event: " + event.getEventName() + ".", e);
        }
    }
}
