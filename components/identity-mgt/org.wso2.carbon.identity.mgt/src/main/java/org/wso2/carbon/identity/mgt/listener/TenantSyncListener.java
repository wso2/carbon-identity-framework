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

package org.wso2.carbon.identity.mgt.listener;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.Utils;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants.TenantManagement;
import org.wso2.carbon.identity.mgt.dto.TenantManagementEventDTO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceDataHolder;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementServerException;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.httpclient5.HTTPClientUtils;

import com.google.gson.Gson;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;

/**
 * Listener class to create, update, activate and deactivate tenants when tenant related task is
 * triggered in Identity server.
 */
public class TenantSyncListener implements TenantMgtListener {

    private static final Log LOG = LogFactory.getLog(TenantSyncListener.class);

    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(2, 5, 100L,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>() {
            });

    @Override
    public void onTenantCreate(TenantInfoBean tenantInfo) throws StratosException {

        if (isTenantEventFiringEnabled(tenantInfo.getTenantId())) {
            sendEvent(tenantInfo, TenantManagement.ACTION_CREATE, TenantManagement.EVENT_CREATE_TENANT_URI);
        }
    }

    @Override
    public void onTenantUpdate(TenantInfoBean tenantInfo) throws StratosException {

        if (isTenantEventFiringEnabled(tenantInfo.getTenantId())) {
            sendEvent(tenantInfo, TenantManagement.ACTION_UPDATE, TenantManagement.EVENT_UPDATE_TENANT_URI);
        }
    }

    @Override
    public void onTenantDelete(int tenantId) {
        // Not implemented.
    }

    @Override
    public void onTenantRename(int tenantId, String oldDomainName, String newDomainName) throws StratosException {
        // Not implemented.
    }

    @Override
    public void onTenantInitialActivation(int tenantId) throws StratosException {
        // Not implemented.
    }

    /**
     * A private helper method that fires a tenant lifecycle event (activation or deactivation).
     * It handles the common logic for setting the tenant context, creating the event payload,
     * and sending the event.
     *
     * @param tenantId The ID of the tenant.
     * @param isActive The desired active status for the tenant (true for activation, false for deactivation).
     * @param action   The event action string (e.g., "ACTIVATE", "DEACTIVATE").
     * @param eventUri The URI for the event.
     */
    private void fireTenantLifecycleEvent(int tenantId, boolean isActive, String action, String eventUri) {

        if (!isTenantEventFiringEnabled(tenantId)) {
            return;
        }

        try {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

            RealmService realmService = IdentityMgtServiceComponent.getRealmService();
            String domain = realmService.getTenantManager().getTenant(tenantId).getDomain();

            TenantInfoBean tenantInfo = new TenantInfoBean();
            tenantInfo.setTenantDomain(domain);
            tenantInfo.setTenantId(tenantId);
            tenantInfo.setActive(isActive);

            sendEvent(tenantInfo, action, eventUri);
        } catch (UserStoreException e) {
            String actionVerb = isActive ? "activating" : "deactivating";
            LOG.error("Error while " + actionVerb + " tenant " + tenantId, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void onTenantActivation(int tenantId) throws StratosException {

        fireTenantLifecycleEvent(tenantId, true, TenantManagement.ACTION_ACTIVATE,
                TenantManagement.EVENT_ACTIVATE_TENANT_URI);
    }

    @Override
    public void onTenantDeactivation(int tenantId) throws StratosException {

        fireTenantLifecycleEvent(tenantId, false, TenantManagement.ACTION_DEACTIVATE,
                TenantManagement.EVENT_ACTIVATE_TENANT_URI);
    }

    @Override
    public void onSubscriptionPlanChange(int tenentId, String oldPlan, String newPlan) throws StratosException {
        // Not implemented.
    }

    @Override
    public int getListenerOrder() {

        return 0;
    }

    @Override
    public void onPreDelete(int tenantId) throws StratosException {
        // Not implemented.
    }

    /**
     * Check whether tenant sharing is enabled and whether the creation is only a root organization.
     *
     * @param tenantId id of the tenant.
     * @return boolean Tenant creation firing is enable or not.
     */
    private boolean isTenantEventFiringEnabled(int tenantId) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking if tenant can be created. Tenant ID: " + tenantId);
        }

        boolean isTenantEventFiringEnabled = false;

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty(
                UserOperationEventListener.class.getName(), TenantSyncListener.class.getName());

        if (identityEventListenerConfig != null && StringUtils.isNotBlank(identityEventListenerConfig.getEnable())
                && Boolean.parseBoolean(identityEventListenerConfig.getEnable())) {
            try {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

                RealmService realmService = IdentityMgtServiceComponent.getRealmService();

                Tenant tenant = realmService.getTenantManager().getTenant(tenantId);
                String organizationID = tenant.getAssociatedOrganizationUUID();

                // check if the Organization Depth in the Hierarchy is 0. only then create the root org.
                if (StringUtils.isEmpty(organizationID) || IdentityMgtServiceDataHolder.getInstance().getOrganizationManager()
                        .getOrganizationDepthInHierarchy(organizationID) == 0) {

                    isTenantEventFiringEnabled = true;

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Tenant is a root organization and can be created. Tenant ID: "
                                + tenantId);
                    }

                } else {
                    LOG.debug("Skipping creating the tenant since the triggered Event is not related "
                            + "to a root org creation.");
                }

                // if there was an exception thrown here, tenant activation won't happen.
            } catch (UserStoreException | OrganizationManagementServerException e) {
                LOG.error("Error while creating tenant ", e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                        "Tenant sharing is disabled. Skipping tenant creation for tenant ID : " + tenantId);
            }
        }
        return isTenantEventFiringEnabled;
    }

    /**
     * Method to build the payload and send it to the external server. Event is sent asynchronously.
     *
     * @param tenantInfo TenantInfoBean containing tenant details.
     * @param type       Type of the event.
     * @param eventURI   URI of the event.
     */
    private void sendEvent(TenantInfoBean tenantInfo, String type, String eventURI) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending event. Event type: " + type + ", Tenant ID: " + tenantInfo.getTenantId());
        }

        TenantManagementEventDTO eventDTO = buildPayload(tenantInfo, type, eventURI,
                IdentityUtil.getServerURL(null, false, false));

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty(
                UserOperationEventListener.class.getName(), TenantSyncListener.class.getName());
        Properties properties = identityEventListenerConfig.getProperties();
        resolveSecrets(properties);
        char[] password = Utils.replaceSystemProperty(properties.getProperty(TenantManagement.PASSWORD)).toCharArray();
        String username = Utils.replaceSystemProperty(properties.getProperty(TenantManagement.USER_NAME));

        String notificationEndpoint = properties.getProperty(TenantManagement.NOTIFICATION_ENDPOINT);

        // Any headers will be defined in the config as header. prefex
        HashMap<String, String> headers = new HashMap<String, String>();
        for (Map.Entry<Object, Object> propertiesEntry : properties.entrySet()) {
            String key = (String) propertiesEntry.getKey();
            String value = (String) propertiesEntry.getValue();
            if (key.startsWith(TenantManagement.HEADER_PROPERTY)) {
                headers.put(key.split(TenantManagement.HEADER_PROPERTY)[1], value);
            }
        }
        EventRunner eventRunner = new EventRunner(notificationEndpoint, username, password, headers, eventDTO);
        EXECUTOR.execute(eventRunner);
    }

    /**
     * There can be sensitive information like passwords in configuration file. If they are encrypted using secure
     * vault, this method will resolve them and replace with original values.
     *
     * @param properties listener properties.
     */
    protected void resolveSecrets(Properties properties) {

        SecretResolver secretResolver = SecretResolverFactory.create(properties);
        if (secretResolver != null && secretResolver.isInitialized()) {
            for(Map.Entry<Object, Object> entry : properties.entrySet()) {
                String key = entry.getKey().toString();
                String value = entry.getValue().toString();
                if (value != null) {
                    value = MiscellaneousUtil.resolve(value, secretResolver);
                }
                properties.put(key, value);
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("Secret Resolver is not present. Will not resolve encryptions in config file.");
        }
    }

    protected TenantManagementEventDTO buildPayload(TenantInfoBean tenantInfo, String type, String eventURI,
                                                    String serverURL) {

        // Start building the Tenant object first, as it's nested.
        TenantManagementEventDTO.Tenant.Builder tenantBuilder = new TenantManagementEventDTO.Tenant.Builder()
                .id(Integer.toString(tenantInfo.getTenantId()))
                .domain(tenantInfo.getTenantDomain())
                .ref(serverURL + "/api/server/v1/tenants/" + tenantInfo.getTenantId());

        // Conditionally build and add owner details for create/update events.
        if (TenantManagement.EVENT_CREATE_TENANT_URI.equals(eventURI)
                || TenantManagement.EVENT_UPDATE_TENANT_URI.equals(eventURI)) {

            TenantManagementEventDTO.Owner.Builder ownerBuilder = new TenantManagementEventDTO.Owner.Builder()
                    .password(tenantInfo.getAdminPassword())
                    .email(tenantInfo.getEmail())
                    .firstname(tenantInfo.getFirstname())
                    .lastname(tenantInfo.getLastname());

            // Username is only set during creation.
            if (TenantManagement.EVENT_CREATE_TENANT_URI.equals(eventURI)) {
                ownerBuilder.username(tenantInfo.getAdmin());
            }

            // Build the owner and add it to the tenant builder.
            tenantBuilder.owners(Collections.singletonList(ownerBuilder.build()));
        }

        // Conditionally build and add lifecycle status for activation events.
        if (TenantManagement.EVENT_ACTIVATE_TENANT_URI.equals(eventURI)) {
            TenantManagementEventDTO.LifecycleStatus lifecycleStatus =
                    new TenantManagementEventDTO.LifecycleStatus.Builder()
                            .activated(tenantInfo.isActive())
                            .build();
            tenantBuilder.lifecycleStatus(lifecycleStatus);
        }

        // Build the final, immutable Tenant object.
        TenantManagementEventDTO.Tenant tenant = tenantBuilder.build();

        // Build the EventDetail object, including the tenant.
        TenantManagementEventDTO.EventDetail createEventDetail = new TenantManagementEventDTO.EventDetail.Builder()
                .initiatorType(TenantManagement.EVENT_INITIATOR)
                .action(type)
                .tenant(tenant)
                .build();

        // Create the events map.
        Map<String, TenantManagementEventDTO.EventDetail> events = new HashMap<>();
        events.put(eventURI, createEventDetail);

        // Build the final TenantManagementEventDTO and return it.
        return new TenantManagementEventDTO.Builder()
                .iss(serverURL)
                .jti(UUID.randomUUID().toString())
                .iat(System.currentTimeMillis() / 1000L)
                .events(events)
                .build();
    }

    /**
     * Runnable Thread to send Event.
     */
    public static class EventRunner implements Runnable {

        private String notificationEndpoint;
        private String username;
        private char[] password;
        private Map<String, String> headers;
        private TenantManagementEventDTO event;
        private static final int MAX_RETRIES = 5;
        private static final long INITIAL_RETRY_DELAY_MS = 1000L;
        private static final long MAX_RETRY_DELAY_MS = 120000L;
        private static final double BACKOFF_FACTOR = 2.0;

        public EventRunner(String notificationEndpoint, String username, char[] password, Map<String, String> headers,
                           TenantManagementEventDTO event) {

            this.notificationEndpoint = notificationEndpoint;
            this.username = username;
            this.password = password;
            this.headers = headers;
            this.event = event;
        }

        @Override
        public void run() {

            if (LOG.isDebugEnabled()) {
                LOG.debug("Sending HTTP request to notification endpoint: " + notificationEndpoint);
            }

            HttpPost httpPost = new HttpPost(notificationEndpoint);
            if (StringUtils.isNotEmpty(username) && !(password == null || password.length == 0)) {
                byte[] credentials = Base64
                        .encodeBase64((username + ":" + new String(password)).getBytes(StandardCharsets.UTF_8));
                httpPost.addHeader(TenantManagement.AUTHORIZATION_HEADER,
                        TenantManagement.BASIC_PREFIX + new String(credentials, StandardCharsets.UTF_8));
            }

            headers.forEach((key, value) -> {
                httpPost.addHeader(key, value);
            });

            String content = new Gson().toJson(event);
            httpPost.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));

            for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Sending notification... (Attempt " + attempt + "/" + MAX_RETRIES + ")");
                }

                try (CloseableHttpClient httpClient = HTTPClientUtils.createClientWithCustomHostnameVerifier()
                        .build()) {

                    boolean success = httpClient.execute(httpPost, response -> {
                        int responseCode = response.getCode();
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Received HTTP response code: " + responseCode);
                        }
                        if (responseCode >= 400 && responseCode < 500) {
                            // Client errors — unauthorized, forbidden, bad request, etc.
                            LOG.warn("Client error: Unauthorized or invalid request. No retry will be attempted.");
                        } else if (responseCode >= 300 && responseCode < 400) {
                            // Redirects — usually not retried automatically unless handled.
                            LOG.warn("Redirection response received. No retry will be attempted.");
                        } else if (responseCode >= 200 && responseCode < 300) {
                            // Successful responses — shouldn't be treated as unauthorized. 
                        } else {
                            // Other cases (1xx or >= 500).
                            LOG.error("Unexpected response code: " + responseCode);
                        }

                        return true;
                    });

                    if (success) {
                        return;
                    }

                } catch (IOException e) {
                    LOG.error("An error occurred while sending the HTTP request: ", e);
                }

                if (attempt < MAX_RETRIES) {
                    long delay = calculateNextDelay(attempt);
                    try {
                        LOG.info("Will retry sending notification in " + delay + "ms.");
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOG.error("Notification thread was interrupted during retry delay. Aborting.", e);
                        break;
                    }
                }
            }
        }

        private long calculateNextDelay(int attempt) {
            double exponent = attempt - 1.0;
            long exponentialDelay = (long) (INITIAL_RETRY_DELAY_MS * Math.pow(BACKOFF_FACTOR, exponent));
            // Return the calculated delay, ensuring it does not exceed the maximum cap.
            return Math.min(exponentialDelay, MAX_RETRY_DELAY_MS);
        }
    }

}
