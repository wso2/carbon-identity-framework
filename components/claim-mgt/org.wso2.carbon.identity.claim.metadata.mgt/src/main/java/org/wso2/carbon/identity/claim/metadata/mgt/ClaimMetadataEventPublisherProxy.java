/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.claim.metadata.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * This class handles creating event and publishing events related to claim management.
 */
public class ClaimMetadataEventPublisherProxy {

    private static final Log log = LogFactory.getLog(ClaimMetadataEventPublisherProxy.class);
    private static final ClaimMetadataEventPublisherProxy proxy = new ClaimMetadataEventPublisherProxy();

    private ClaimMetadataEventPublisherProxy() {

    }

    public static ClaimMetadataEventPublisherProxy getInstance() {

        return proxy;
    }

    public void publishPreAddClaimDialect(int tenantId, ClaimDialect claimDialect) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_ID, tenantId);
        eventProperties.put(IdentityEventConstants.EventProperty.CLAIM_DIALECT_URI, claimDialect.getClaimDialectURI());
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_ADD_CLAIM_DIALECT);
        doPublishEvent(event);
    }

    public void publishPostAddClaimDialect(int tenantId, ClaimDialect claimDialect) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_ID, tenantId);
        eventProperties.put(IdentityEventConstants.EventProperty.CLAIM_DIALECT_URI, claimDialect.getClaimDialectURI());
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_ADD_CLAIM_DIALECT);
        doPublishEvent(event);
    }

    public void publishPreUpdateClaimDialect(int tenantId, ClaimDialect oldClaimDialect, ClaimDialect newClaimDialect) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_ID, tenantId);
        eventProperties.put(IdentityEventConstants.EventProperty.OLD_CLAIM_DIALECT_URI, oldClaimDialect.getClaimDialectURI());
        eventProperties.put(IdentityEventConstants.EventProperty.NEW_CLAIM_DIALECT_URI, newClaimDialect.getClaimDialectURI());
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_UPDATE_CLAIM_DIALECT);
        doPublishEvent(event);
    }

    public void publishPostUpdateClaimDialect(int tenantId, ClaimDialect oldClaimDialect,ClaimDialect newClaimDialect) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_ID, tenantId);
        eventProperties.put(IdentityEventConstants.EventProperty.OLD_CLAIM_DIALECT_URI, oldClaimDialect.getClaimDialectURI());
        eventProperties.put(IdentityEventConstants.EventProperty.NEW_CLAIM_DIALECT_URI, newClaimDialect.getClaimDialectURI());
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_UPDATE_CLAIM_DIALECT);
        doPublishEvent(event);
    }

    public void publishPreDeleteClaimDialect(int tenantId, ClaimDialect claimDialect) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_ID, tenantId);
        eventProperties.put(IdentityEventConstants.EventProperty.CLAIM_DIALECT_URI, claimDialect.getClaimDialectURI());
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_DELETE_CLAIM_DIALECT);
        doPublishEvent(event);
    }

    public void publishPostDeleteClaimDialect(int tenantId, ClaimDialect claimDialect) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_ID, tenantId);
        eventProperties.put(IdentityEventConstants.EventProperty.CLAIM_DIALECT_URI, claimDialect.getClaimDialectURI());
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_DELETE_CLAIM_DIALECT);
        doPublishEvent(event);
    }

    public void publishPreAddLocalClaim(int tenantId, LocalClaim localClaim) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_ID, tenantId);
        eventProperties.put(IdentityEventConstants.EventProperty.LOCAL_CLAIM_URI, localClaim.getClaimURI());
        eventProperties.put(IdentityEventConstants.EventProperty.CLAIM_DIALECT_URI, localClaim.getClaimDialectURI());
        eventProperties.put(IdentityEventConstants.EventProperty.MAPPED_ATTRIBUTES, localClaim.getMappedAttributes());
        eventProperties.put(IdentityEventConstants.EventProperty.LOCAL_CLAIM_PROPERTIES, localClaim.getClaimProperties());
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_ADD_LOCAL_CLAIM);
        doPublishEvent(event);
    }

    public void publishPostAddLocalClaim(int tenantId, LocalClaim localClaim) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_ID, tenantId);
        eventProperties.put(IdentityEventConstants.EventProperty.LOCAL_CLAIM_URI, localClaim.getClaimURI());
        eventProperties.put(IdentityEventConstants.EventProperty.CLAIM_DIALECT_URI, localClaim.getClaimDialectURI());
        eventProperties.put(IdentityEventConstants.EventProperty.LOCAL_CLAIM_PROPERTIES, localClaim.getClaimProperties());
        eventProperties.put(IdentityEventConstants.EventProperty.MAPPED_ATTRIBUTES, localClaim.getMappedAttributes());
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_ADD_LOCAL_CLAIM);
        doPublishEvent(event);
    }

    public void publishPreUpdateLocalClaim(int tenantId, LocalClaim localClaim) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_ID, tenantId);
        eventProperties.put(IdentityEventConstants.EventProperty.LOCAL_CLAIM_URI, localClaim.getClaimURI());
        eventProperties.put(IdentityEventConstants.EventProperty.CLAIM_DIALECT_URI, localClaim.getClaimDialectURI());
        eventProperties.put(IdentityEventConstants.EventProperty.LOCAL_CLAIM_PROPERTIES, localClaim.getClaimProperties());
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_UPDATE_LOCAL_CLAIM);
        doPublishEvent(event);
    }

    public void publishPostUpdateLocalClaim(int tenantId, LocalClaim localClaim) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_ID, tenantId);
        eventProperties.put(IdentityEventConstants.EventProperty.LOCAL_CLAIM_URI, localClaim.getClaimURI());
        eventProperties.put(IdentityEventConstants.EventProperty.CLAIM_DIALECT_URI, localClaim.getClaimDialectURI());
        eventProperties.put(IdentityEventConstants.EventProperty.LOCAL_CLAIM_PROPERTIES, localClaim.getClaimProperties());
        eventProperties.put(IdentityEventConstants.EventProperty.MAPPED_ATTRIBUTES, localClaim.getMappedAttributes());
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_UPDATE_LOCAL_CLAIM);
        doPublishEvent(event);
    }

    public void publishPreDeleteLocalClaim(int tenantId, String localClaim) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_ID, tenantId);
        eventProperties.put(IdentityEventConstants.EventProperty.LOCAL_CLAIM_URI, localClaim);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_DELETE_LOCAL_CLAIM);
        doPublishEvent(event);
    }

    public void publishPostDeleteLocalClaim(int tenantId, String localClaim) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_ID, tenantId);
        eventProperties.put(IdentityEventConstants.EventProperty.LOCAL_CLAIM_URI, localClaim);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_DELETE_LOCAL_CLAIM);
        doPublishEvent(event);
    }

    public void publishPreAddExternalClaim(int tenantId, ExternalClaim externalClaim) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_ID, tenantId);
        eventProperties.put(IdentityEventConstants.EventProperty.CLAIM_DIALECT_URI, externalClaim.getClaimDialectURI());
        eventProperties.put(IdentityEventConstants.EventProperty.EXTERNAL_CLAIM_URI, externalClaim.getClaimURI());
        eventProperties.put(IdentityEventConstants.EventProperty.MAPPED_LOCAL_CLAIM_URI, externalClaim.getMappedLocalClaim());
        eventProperties.put(IdentityEventConstants.EventProperty.EXTERNAL_CLAIM_PROPERTIES, externalClaim.getClaimProperties());

        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_ADD_EXTERNAL_CLAIM);
        doPublishEvent(event);
    }

    public void publishPostAddExternalClaim(int tenantId, ExternalClaim externalClaim) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_ID, tenantId);
        eventProperties.put(IdentityEventConstants.EventProperty.CLAIM_DIALECT_URI, externalClaim.getClaimDialectURI());
        eventProperties.put(IdentityEventConstants.EventProperty.EXTERNAL_CLAIM_URI, externalClaim.getClaimURI());
        eventProperties.put(IdentityEventConstants.EventProperty.MAPPED_LOCAL_CLAIM_URI, externalClaim.getMappedLocalClaim());
        eventProperties.put(IdentityEventConstants.EventProperty.EXTERNAL_CLAIM_PROPERTIES, externalClaim.getClaimProperties());

        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_ADD_EXTERNAL_CLAIM);
        doPublishEvent(event);
    }

    public void publishPreUpdateExternalClaim(int tenantId, ExternalClaim externalClaim) {


        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_ID, tenantId);
        eventProperties.put(IdentityEventConstants.EventProperty.CLAIM_DIALECT_URI, externalClaim.getClaimDialectURI());
        eventProperties.put(IdentityEventConstants.EventProperty.EXTERNAL_CLAIM_URI, externalClaim.getClaimURI());
        eventProperties.put(IdentityEventConstants.EventProperty.MAPPED_LOCAL_CLAIM_URI, externalClaim.getMappedLocalClaim());
        eventProperties.put(IdentityEventConstants.EventProperty.EXTERNAL_CLAIM_PROPERTIES, externalClaim.getClaimProperties());
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_UPDATE_EXTERNAL_CLAIM);
        doPublishEvent(event);
    }

    public void publishPostUpdateExternalClaim(int tenantId, ExternalClaim externalClaim) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_ID, tenantId);
        eventProperties.put(IdentityEventConstants.EventProperty.CLAIM_DIALECT_URI, externalClaim.getClaimDialectURI());
        eventProperties.put(IdentityEventConstants.EventProperty.EXTERNAL_CLAIM_URI, externalClaim.getClaimURI());
        eventProperties.put(IdentityEventConstants.EventProperty.MAPPED_LOCAL_CLAIM_URI, externalClaim.getMappedLocalClaim());
        eventProperties.put(IdentityEventConstants.EventProperty.EXTERNAL_CLAIM_PROPERTIES, externalClaim.getClaimProperties());
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_UPDATE_EXTERNAL_CLAIM);
        doPublishEvent(event);
    }

    public void publishPreDeleteExternalClaim(int tenantId, String externalClaimDialectURI, String externalClaimURI) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_ID, tenantId);
        eventProperties.put(IdentityEventConstants.EventProperty.CLAIM_DIALECT_URI, externalClaimDialectURI);
        eventProperties.put(IdentityEventConstants.EventProperty.EXTERNAL_CLAIM_URI, externalClaimURI);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.PRE_DELETE_EXTERNAL_CLAIM);
        doPublishEvent(event);
    }

    public void publishPostDeleteExternalClaim(int tenantId, String externalClaimDialectURI, String externalClaimURI) {

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(IdentityEventConstants.EventProperty.TENANT_ID, tenantId);
        eventProperties.put(IdentityEventConstants.EventProperty.CLAIM_DIALECT_URI, externalClaimDialectURI);
        eventProperties.put(IdentityEventConstants.EventProperty.EXTERNAL_CLAIM_URI, externalClaimURI);
        Event event = createEvent(eventProperties, IdentityEventConstants.Event.POST_DELETE_EXTERNAL_CLAIM);
        doPublishEvent(event);
    }


    private Event createEvent(Map<String, Object> eventProperties, String eventName) {

        return new Event(eventName, eventProperties);
    }

    private void doPublishEvent(Event event) {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Event: " + event.getEventName() + " is published for the claim management operation in " +
                        "the tenant with the tenantId: " + event.getEventProperties().get(IdentityEventConstants.EventProperty.TENANT_ID));
            }
            IdentityEventService eventService =
                    IdentityClaimManagementServiceDataHolder.getInstance().getIdentityEventService();
            eventService.handleEvent(event);
        } catch (IdentityEventException e) {
            log.error("Error while publishing the event: " + event.getEventName() + ".", e);
        }
    }
}
