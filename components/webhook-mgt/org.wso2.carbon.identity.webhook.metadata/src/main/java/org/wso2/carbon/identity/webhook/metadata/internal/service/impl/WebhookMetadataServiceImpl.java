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
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.constant.PolicyEnum;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataServerException;
import org.wso2.carbon.identity.webhook.metadata.api.model.Event;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventProfile;
import org.wso2.carbon.identity.webhook.metadata.api.model.WebhookMetadataProperties;
import org.wso2.carbon.identity.webhook.metadata.api.service.WebhookMetadataService;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.WebhookMetadataDAO;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.impl.CacheBackedWebhookMetadataDAO;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.impl.FileBasedEventProfileMetadataDAOImpl;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.impl.WebhookMetadataDAOImpl;
import org.wso2.carbon.identity.webhook.metadata.internal.util.WebhookMetadataExceptionHandler;
import org.wso2.carbon.identity.webhook.metadata.internal.util.WebhookMetadataValidator;

import java.util.List;

import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_CODE_EVENTS_RETRIEVE_ERROR;
import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_CODE_PROFILES_RETRIEVE_ERROR;
import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_CODE_PROFILE_RETRIEVE_ERROR;

/**
 * Implementation of WebhookMetadataService.
 */
public class WebhookMetadataServiceImpl implements WebhookMetadataService {

    private static final Log log = LogFactory.getLog(WebhookMetadataServiceImpl.class);
    private static final WebhookMetadataServiceImpl INSTANCE = new WebhookMetadataServiceImpl();

    private final FileBasedEventProfileMetadataDAOImpl eventProfileMetadataDAO;
    private final WebhookMetadataDAO webhookMetadataDAO;
    private static final WebhookMetadataValidator WEBHOOK_METADATA_VALIDATOR = new WebhookMetadataValidator();

    private WebhookMetadataServiceImpl() {

        eventProfileMetadataDAO = FileBasedEventProfileMetadataDAOImpl.getInstance();
        webhookMetadataDAO = new CacheBackedWebhookMetadataDAO(new WebhookMetadataDAOImpl());
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
    public void init() throws WebhookMetadataServerException {

        eventProfileMetadataDAO.init();
    }

    @Override
    public List<EventProfile> getSupportedEventProfiles() throws WebhookMetadataException {

        try {
            return eventProfileMetadataDAO.getSupportedEventProfiles();
        } catch (Exception e) {
            throw WebhookMetadataExceptionHandler.handleServerException(
                    ERROR_CODE_PROFILES_RETRIEVE_ERROR, e);
        }
    }

    @Override
    public EventProfile getEventProfile(String profileName) throws WebhookMetadataException {

        try {
            return eventProfileMetadataDAO.getEventProfile(profileName);
        } catch (WebhookMetadataException e) {
            throw e;
        } catch (Exception e) {
            throw WebhookMetadataExceptionHandler.handleServerException(
                    ERROR_CODE_PROFILE_RETRIEVE_ERROR, e, profileName);
        }
    }

    @Override
    public List<Event> getEventsByProfileURI(String profileUri) throws WebhookMetadataException {

        try {
            List<Event> events = eventProfileMetadataDAO.getEventsByProfile(profileUri);
            if (events.isEmpty()) {
                log.warn("No events found for profile URI: " + profileUri);
            }
            return events;
        } catch (Exception e) {
            throw WebhookMetadataExceptionHandler.handleServerException(
                    ERROR_CODE_EVENTS_RETRIEVE_ERROR, e, profileUri);
        }
    }

    @Override
    public WebhookMetadataProperties getWebhookMetadataProperties(String tenantDomain) throws WebhookMetadataException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        log.debug("Retrieving Webhook Metadata properties for tenant " + tenantDomain);
        WebhookMetadataProperties properties = webhookMetadataDAO.getWebhookMetadataProperties(tenantId);

        if (properties == null || properties.getOrganizationPolicy() == null) {
            return new WebhookMetadataProperties.Builder()
                    .organizationPolicy(PolicyEnum.ALL_EXISTING_AND_FUTURE_ORGS)
                    .build();
        }
        return properties;
    }

    @Override
    public WebhookMetadataProperties updateWebhookMetadataProperties(
            WebhookMetadataProperties webhookMetadataProperties, String tenantDomain) throws WebhookMetadataException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        log.debug("Updating Webhook Metadata properties for tenant " + tenantDomain);
        validateWebhookMetadataProperties(webhookMetadataProperties);
        webhookMetadataDAO.updateWebhookMetadataProperties(webhookMetadataProperties, tenantId);
        return webhookMetadataDAO.getWebhookMetadataProperties(tenantId);

    }

    private void validateWebhookMetadataProperties(WebhookMetadataProperties properties)
            throws WebhookMetadataException {

        WEBHOOK_METADATA_VALIDATOR.validateOrganizationPolicy(properties.getOrganizationPolicy());
    }
}
