/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * Class for listening to modifications of tenants and sending the tenant list to receiving endpoints.
 */
public class AuthenticationEndpointTenantActivityListener implements TenantMgtListener {

    /**
     * Logger for the class
     */
    private static final Log log = LogFactory.getLog(AuthenticationEndpointTenantActivityListener.class);

    /**
     * Tenant list query parameter
     */
    private static final String TENANT_LIST_QUERY_PARAM = "isTenantListModified";

    /**
     * Tenant list data separator
     */
    private static final String TENANT_LIST_DATA_SEPARATOR = ",";

    /**
     * List of tenant list receiving URLs
     */
    private List<String> tenantDataReceiveURLs;

    /**
     * Listener initialization status
     */
    private boolean initialized;

    /**
     * CARBON SERVER URL
     */
    private String serverURL;

    /**
     * Initialize listener
     */
    private synchronized void init() {
        try {
            tenantDataReceiveURLs = ConfigurationFacade.getInstance().getTenantDataEndpointURLs();

            if (!tenantDataReceiveURLs.isEmpty()) {

                serverURL = IdentityUtil.getServerURL("", true, true);
                int index = 0;

                for (String tenantDataReceiveUrl : tenantDataReceiveURLs) {
                    URI tenantDataReceiveURI = new URI(tenantDataReceiveUrl);

                    if (log.isDebugEnabled()) {
                        log.debug("Tenant list receiving url added : " + tenantDataReceiveUrl);
                    }

                    if (!tenantDataReceiveURI.isAbsolute()) {
                        // Set the absolute URL for tenant list receiving endpoint
                        tenantDataReceiveURLs.set(index, serverURL + tenantDataReceiveUrl);
                    }
                    index++;
                }

                initialized = true;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("TenantDataListenerURLs are not set in configuration");
                }
            }
        } catch (URISyntaxException e) {
            log.error("Error while getting TenantDataListenerURLs", e);
        }
    }

    /**
     * Send the list of tenants to the receiving endpoints
     */
    private void sendTenantList() {

        if (!initialized) {
            init();
        }

        if (!initialized) {
            if (log.isDebugEnabled()) {
                log.debug("AuthenticationEndpointTenantActivityListener is not initialized. Tenant list not sent " +
                          "to authentication endpoint");
            }
            return;
        }

        if (tenantDataReceiveURLs != null && !tenantDataReceiveURLs.isEmpty()) {

            StringBuilder paramsBuilder = new StringBuilder();
            paramsBuilder.append("?").append(TENANT_LIST_QUERY_PARAM).append("=true");

            InputStream inputStream = null;
            for (String tenantDataReceiveURL : tenantDataReceiveURLs) {
                try {
                    // Send tenant list to the receiving endpoint
                    inputStream = new URL(tenantDataReceiveURL + paramsBuilder.toString()).openStream();

                } catch (IOException e) {
                    log.error("Sending tenant domain list to " + tenantDataReceiveURL + " failed.", e);

                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            log.error("Error while closing the tenant data receiving stream", e);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onTenantInitialActivation(int tenantId) throws StratosException {
        sendTenantList();
    }

    @Override
    public void onTenantActivation(int tenantId) throws StratosException {
        sendTenantList();
    }

    @Override
    public void onTenantDeactivation(int tenantId) throws StratosException {
        sendTenantList();
    }

    @Override
    public void onTenantCreate(TenantInfoBean tenantInfoBean) throws StratosException {
        /* Method not implemented */
    }

    @Override
    public void onTenantUpdate(TenantInfoBean tenantInfoBean) throws StratosException {
        /* Method not implemented */
    }

    @Override
    public void onPreDelete(int tenantId) throws StratosException {
        /* Method not implemented */
    }

    @Override
    public void onTenantDelete(int tenantId) {
        /* Method not implemented */
    }

    @Override
    public void onTenantRename(int tenantId, String oldDomainName, String newDomainName) throws StratosException {
        /* Method not implemented */
    }

    @Override
    public void onSubscriptionPlanChange(int tenentId, String oldPlan, String newPlan) throws StratosException {
        /* Method not implemented */
    }

    @Override
    public int getListenerOrder() {
        return 0;
    }

}
