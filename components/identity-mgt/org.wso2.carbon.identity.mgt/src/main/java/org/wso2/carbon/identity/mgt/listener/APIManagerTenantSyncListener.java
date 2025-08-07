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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
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

/**
 * Listener class to create, update, activate and deactivate tenants in API Manager when tenant related task is
 * triggered in Identity server
 */

public class APIManagerTenantSyncListener implements TenantMgtListener {

    private static final Log log = LogFactory.getLog(APIManagerTenantSyncListener.class);

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onTenantCreate(TenantInfoBean tenantInfo) throws StratosException {
             
        if (canCreateTenantInAPIM(tenantInfo.getTenantId())) {
            sendEvent(tenantInfo, TenantManagement.ACTION_CREATE, TenantManagement.EVENT_CREATE_TENANT_URI);
        }
        
    }

    @Override
    public void onTenantUpdate(TenantInfoBean tenantInfo) throws StratosException {

        if (canCreateTenantInAPIM(tenantInfo.getTenantId())) {
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

    @Override
    public void onTenantActivation(int tenantId) throws StratosException {
            
        if (canCreateTenantInAPIM(tenantId)) {
            TenantInfoBean tenantInfo = new TenantInfoBean();
            try {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

                RealmService realmService = IdentityMgtServiceComponent.getRealmService();
                String domain = realmService.getTenantManager().getTenant(tenantId).getDomain();
                
                tenantInfo.setTenantDomain(domain);
                tenantInfo.setTenantId(tenantId);
                tenantInfo.setActive(true);
                sendEvent(tenantInfo, TenantManagement.ACTION_ACTIVATE, TenantManagement.EVENT_ACTIVATE_TENANT_URI);
            } catch (UserStoreException e) {
                log.error("Error while activating tenant in API Manager ", e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    @Override
    public void onTenantDeactivation(int tenantId) throws StratosException {
        
        if (canCreateTenantInAPIM(tenantId)) {
            TenantInfoBean tenantInfo = new TenantInfoBean();
            try {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

                RealmService realmService = IdentityMgtServiceComponent.getRealmService();
                String domain = realmService.getTenantManager().getTenant(tenantId).getDomain();
                
                tenantInfo.setTenantDomain(domain);
                tenantInfo.setTenantId(tenantId);
                tenantInfo.setActive(false);
                sendEvent(tenantInfo, TenantManagement.ACTION_DEACTIVATE, TenantManagement.EVENT_ACTIVATE_TENANT_URI);
            } catch (UserStoreException e) {
                log.error("Error while deactivating tenant in API Manager ", e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
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
     * @param tenantId id of the tenant
     * @return boolean
     */
    private boolean canCreateTenantInAPIM(int tenantId) {
        
        boolean canCreateTenant = false;
        
        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty(
                UserOperationEventListener.class.getName(), APIManagerTenantSyncListener.class.getName());

        if (StringUtils.isNotBlank(identityEventListenerConfig.getEnable())
                && Boolean.parseBoolean(identityEventListenerConfig.getEnable())) {
            try {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

                RealmService realmService = IdentityMgtServiceComponent.getRealmService();

                Tenant tenant = realmService.getTenantManager().getTenant(tenantId);
                String organizationID = tenant.getAssociatedOrganizationUUID();

                // check if the Organization Depth in the Hierarchy is 0. only then create the root org.
                if (organizationID == null || IdentityMgtServiceDataHolder.getInstance().getOrganizationManager()
                        .getOrganizationDepthInHierarchy(organizationID) == 0) {

                    canCreateTenant = true;

                } else {
                    log.debug("Skipping creating the tenant in APIM since the triggered Event is not related "
                            + "to a root org creation.");
                }

                // if there was an exception thrown here, tenant activation won't happen
            } catch (UserStoreException | OrganizationManagementServerException e) {
                log.error("Error while creating tenant in API Manager ", e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        } else {
            log.debug(
                    "Tenant sharing is disabled. Skipping tenant creation in API Manager for tenant ID : " + tenantId);
        }
        return canCreateTenant;
    }
    
    /**
     * Method to build the payload and send it to the external server. Event is sent asynchronously
     * 
     * @param tenantInfo
     * @param type
     * @param eventURI
     */
    private void sendEvent(TenantInfoBean tenantInfo, String type, String eventURI) {

        TenantManagementEventDTO eventDTO = buildPayload(tenantInfo, type, eventURI, IdentityUtil.getServerURL(null, false, false));
        
        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty(
                UserOperationEventListener.class.getName(), APIManagerTenantSyncListener.class.getName());
        String password = Utils.replaceSystemProperty(identityEventListenerConfig.getProperties().getProperty(TenantManagement.PASSWORD));
        String username = Utils.replaceSystemProperty(identityEventListenerConfig.getProperties().getProperty(TenantManagement.USER_NAME));
             
        String notificationEndpoint = identityEventListenerConfig.getProperties()
                .getProperty(TenantManagement.NOTIFICATION_ENDPOINT);
        
        // Any headers will be defined in the config as header. prefex
        HashMap<String, String> headers = new HashMap<String, String>();
        for (Map.Entry<Object, Object> propertiesEntry : identityEventListenerConfig.getProperties().entrySet()) {
            String key = (String) propertiesEntry.getKey();
            String value = (String) propertiesEntry.getValue();
            if (key.startsWith(TenantManagement.HEADER_PROPERTY)) {
                headers.put(key.split(TenantManagement.HEADER_PROPERTY)[1], value);
            }
        }
        EventRunner eventRunner = new EventRunner(notificationEndpoint, username, password,
                headers, eventDTO);
        executor.execute(eventRunner);

    }

    protected TenantManagementEventDTO buildPayload(TenantInfoBean tenantInfo, String type, String eventURI, String serverURL) {

        TenantManagementEventDTO eventDTO = new TenantManagementEventDTO();

        eventDTO.setIss(serverURL); 
        eventDTO.setJti(UUID.randomUUID().toString());
        eventDTO.setIat(System.currentTimeMillis() / 1000L);

        Map<String, TenantManagementEventDTO.EventDetail> events = new HashMap<>();

        TenantManagementEventDTO.EventDetail createEventDetail = new TenantManagementEventDTO.EventDetail();
        createEventDetail.setInitiatorType(TenantManagement.EVENT_INITIATOR);
        createEventDetail.setAction(type);

        TenantManagementEventDTO.Tenant tenant = new TenantManagementEventDTO.Tenant();
        tenant.setId(Integer.toString(tenantInfo.getTenantId()));
        tenant.setDomain(tenantInfo.getTenantDomain());
        tenant.setRef(serverURL + "/api/server/v1/tenants/"
                + tenantInfo.getTenantId()); 

        if (TenantManagement.EVENT_CREATE_TENANT_URI.equals(eventURI) || TenantManagement.EVENT_UPDATE_TENANT_URI.equals(eventURI)) {
            List<TenantManagementEventDTO.Owner> owners = new ArrayList<>();
            TenantManagementEventDTO.Owner owner = new TenantManagementEventDTO.Owner();
            
            if (TenantManagement.EVENT_CREATE_TENANT_URI.equals(eventURI)) {
                // set username only it is a creation request
                owner.setUsername(tenantInfo.getAdmin()); 
            }
            owner.setPassword(tenantInfo.getAdminPassword()); 
            owner.setEmail(tenantInfo.getEmail());
            owner.setFirstname(tenantInfo.getFirstname());
            owner.setLastname(tenantInfo.getLastname());
            owners.add(owner); 
            tenant.setOwners(owners);
        }

        if (TenantManagement.EVENT_ACTIVATE_TENANT_URI.equals(eventURI)) {
            TenantManagementEventDTO.LifecycleStatus lifecycleStatus = new TenantManagementEventDTO.LifecycleStatus();
            lifecycleStatus.setActivated(tenantInfo.isActive());
            tenant.setLifecycleStatus(lifecycleStatus);
        }        
    
        createEventDetail.setTenant(tenant);
        events.put(eventURI, createEventDetail);
        eventDTO.setEvents(events);
        return eventDTO;
    }

    /**
     * Runnable Thread to send Event
     */
    public static class EventRunner implements Runnable {

        private String notificationEndpoint;
        private String username;
        private String password;
        private Map<String, String> headers;
        private TenantManagementEventDTO event;

        public EventRunner(String notificationEndpoint, String username, String password, Map<String, String> headers,
                TenantManagementEventDTO event) {

            this.notificationEndpoint = notificationEndpoint;
            this.username = username;
            this.password = password;
            this.headers = headers;
            this.event = event;
        }

        @Override
        public void run() {
            
            try (CloseableHttpClient httpClient = HTTPClientUtils.createClientWithCustomHostnameVerifier().build()) {

                HttpPost httpPost = new HttpPost(notificationEndpoint);
                if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                    byte[] credentials = Base64
                            .encodeBase64((username + ":" + password).getBytes(StandardCharsets.UTF_8));
                    httpPost.addHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
                }

                headers.forEach((key, value) -> {
                    httpPost.addHeader(key, value);
                });

                String content = new Gson().toJson(event);
                httpPost.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));
                httpClient.execute(httpPost, response -> {
                    if (response.getCode() != HttpStatus.SC_OK) {
                        log.error("Error while notifying API Manger for tenant creation. Status code: "
                                + response.getCode());
                    }
                    return null;
                });

            } catch (IOException e) {
                log.error("An error occurred while sending the HTTP request: ", e);
            }
        }
    }

}
