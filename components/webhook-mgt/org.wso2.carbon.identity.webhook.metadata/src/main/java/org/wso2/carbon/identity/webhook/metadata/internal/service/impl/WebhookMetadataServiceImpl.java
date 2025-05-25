/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.webhook.metadata.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.model.Event;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventProfile;
import org.wso2.carbon.identity.webhook.metadata.api.service.WebhookMetadataService;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.impl.FileBasedWebhookMetadataDAOImpl;
import org.wso2.carbon.identity.webhook.metadata.internal.util.WebhookMetadataExceptionBuilder;
import org.wso2.carbon.identity.webhook.metadata.internal.util.WebhookMetadataExceptionBuilder.ErrorCodes;

import java.util.List;

/**
 * Implementation of WebhookMetadataService.
 */
public class WebhookMetadataServiceImpl implements WebhookMetadataService {

    private static final Log log = LogFactory.getLog(WebhookMetadataServiceImpl.class);
    private static final WebhookMetadataServiceImpl INSTANCE = new WebhookMetadataServiceImpl();

    private FileBasedWebhookMetadataDAOImpl webhookMetadataDAO;

    private WebhookMetadataServiceImpl() {
        webhookMetadataDAO = FileBasedWebhookMetadataDAOImpl.getInstance();
    }

    /**
     * Get the singleton instance of WebhookMetadataServiceImpl.
     *
     * @return Singleton instance
     */
    public static WebhookMetadataServiceImpl getInstance() {
        return INSTANCE;
    }

    /**
     * Initialize the service.
     */
    public void init() {
        webhookMetadataDAO.init();
    }

    @Override
    public List<String> getSupportedEventProfiles() throws WebhookMetadataException {
        try {
            return webhookMetadataDAO.getSupportedEventProfiles();
        } catch (Exception e) {
            String errorMessage = "Error retrieving supported event profiles";
            log.error(errorMessage, e);
            throw WebhookMetadataExceptionBuilder.buildServerException(
                    ErrorCodes.ERROR_RETRIEVING_PROFILES, errorMessage, e);
        }
    }

    @Override
    public EventProfile getEventProfile(String profileName) throws WebhookMetadataException {
        try {
            EventProfile profile = webhookMetadataDAO.getEventProfile(profileName);
            if (profile == null) {
                String errorMessage = "Event profile not found: " + profileName;
                log.error(errorMessage);
                throw WebhookMetadataExceptionBuilder.buildClientException(
                        ErrorCodes.PROFILE_NOT_FOUND, errorMessage);
            }
            return profile;
        } catch (WebhookMetadataException e) {
            throw e;
        } catch (Exception e) {
            String errorMessage = "Error retrieving event profile: " + profileName;
            log.error(errorMessage, e);
            throw WebhookMetadataExceptionBuilder.buildServerException(
                    ErrorCodes.ERROR_RETRIEVING_PROFILE, errorMessage, e);
        }
    }

    @Override
    public List<Event> getEventsBySchema(String schemaUri) throws WebhookMetadataException {
        try {
            List<Event> events = webhookMetadataDAO.getEventsBySchema(schemaUri);
            if (events.isEmpty()) {
                log.warn("No events found for schema URI: " + schemaUri);
            }
            return events;
        } catch (Exception e) {
            String errorMessage = "Error retrieving events for schema: " + schemaUri;
            log.error(errorMessage, e);
            throw WebhookMetadataExceptionBuilder.buildServerException(
                    ErrorCodes.ERROR_RETRIEVING_EVENTS, errorMessage, e);
        }
    }
}
