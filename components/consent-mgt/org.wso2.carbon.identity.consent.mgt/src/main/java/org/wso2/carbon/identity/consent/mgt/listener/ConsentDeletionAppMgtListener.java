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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.consent.mgt.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.ReceiptListResponse;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.listener.AbstractApplicationMgtListener;
import org.wso2.carbon.identity.application.mgt.listener.ApplicationMgtListener;
import org.wso2.carbon.identity.consent.mgt.IdentityConsentMgtUtils;
import org.wso2.carbon.identity.consent.mgt.internal.IdentityConsentDataHolder;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.core.util.LambdaExceptionUtils;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.wso2.carbon.identity.core.util.LambdaExceptionUtils.rethrowConsumer;

/**
 * Takes care of deleting consents / receipts which are issued against a service provider. When the service provider
 * is deleted, consents issued against the service provider will be deleted through this listener.
 */
public class ConsentDeletionAppMgtListener extends AbstractApplicationMgtListener {

    private static final Log log = LogFactory.getLog(ConsentDeletionAppMgtListener.class);
    private static final String CONSENT_SEARCH_LIMIT_PROPERTY = "Consent.Search.Limit";
    protected final Properties properties = new Properties();
    private static int consentSearchLimit = 100;

    /**
     * Overridden to check the configuration for this listener enabling and also to check whether globally consent
     * feature enable
     *
     * @return Whether this listener is enabled or not
     */
    public boolean isEnable() {

        boolean isListenerEnabledFromConfigs = super.isEnable();
        boolean isConsentEnabledSystemWide = IdentityConsentMgtUtils.isConsentEnabled();
        if (log.isDebugEnabled()) {
            log.debug("Is listener enabled from configs: " + isListenerEnabledFromConfigs);
            log.debug("Is consent enabled system wide: " + isConsentEnabledSystemWide);
        }
        if (isConsentEnabledSystemWide && isListenerEnabledFromConfigs) {
            if (log.isDebugEnabled()) {
                log.debug("Listener is enabled and consent is enabled system wide. Hence returning true for " +
                        "isEnabled");
            }
            return true;
        }
        return false;
    }

    /**
     * Reads configurations from identity.xml
     */
    public ConsentDeletionAppMgtListener() {

        if (buildConfig()) {
            return;
        }
        setConsentLimit();
    }

    /**
     * Overridden the default value.
     *
     * @return
     */
    @Override
    public int getDefaultOrderId() {

        return 907;
    }

    /**
     * When an application is deleted, it will delete all relevant receipts issued againsed that application.
     *
     * @param applicationName Name of the application which is getting deleted.
     * @param tenantDomain    Tenant domain of the application.
     * @param userName        Username of the person who does the deletion.
     * @return true.
     * @throws IdentityApplicationManagementException IdentityApplicationManagementException.
     */
    @Override
    public boolean doPostDeleteApplication(String applicationName, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        ConsentManager consentManager = IdentityConsentDataHolder.getInstance().getConsentManager();

        if (log.isDebugEnabled()) {
            log.debug(String.format("Deleting consents on deletion of application: %s, in tenant domain", applicationName,
                    tenantDomain));
        }
        try {
            List<ReceiptListResponse> receiptListResponses = consentManager.searchReceipts(consentSearchLimit, 0,
                    "*", tenantDomain, applicationName, null, null);
            if (log.isDebugEnabled()) {
                log.debug(String.format("%d number of consents found for application %s", receiptListResponses.size(),
                        applicationName));
            }
            receiptListResponses.forEach(rethrowConsumer(receiptListResponse -> {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Deleting receipt with id : %s, issued for user: ", receiptListResponse
                            .getConsentReceiptId(), receiptListResponse.getPiiPrincipalId()));
                }
                consentManager.deleteReceipt(receiptListResponse.getConsentReceiptId());
            }));

        } catch (ConsentManagementException e) {
            throw new IdentityApplicationManagementException("Error while deleting user consents for application "
                    + applicationName, e);
        }
        return true;
    }

    private void setConsentLimit() {

        Object consentSearchLimitProp = properties.getProperty(CONSENT_SEARCH_LIMIT_PROPERTY);
        if (consentSearchLimitProp != null) {
            try {
                consentSearchLimit = Integer.parseInt(consentSearchLimitProp.toString());
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Consent search limit is set to: %d", consentSearchLimit));
                }
            } catch (NumberFormatException e) {
                log.warn("consentSearchLimit for ConsentDeletionAppMgtListener is not a parsable integer. Hence " +
                        "using default value: " + consentSearchLimit);
            }
        }
    }

    private boolean buildConfig() {

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (ApplicationMgtListener.class.getName(), this.getClass().getName());

        if (identityEventListenerConfig == null) {
            return true;
        }

        if (identityEventListenerConfig.getProperties() != null) {
            for (Map.Entry<Object, Object> property : identityEventListenerConfig.getProperties().entrySet()) {
                String key = (String) property.getKey();
                String value = (String) property.getValue();
                if (!properties.containsKey(key)) {
                    properties.setProperty(key, value);
                } else {
                    log.warn("Property key " + key + " already exists. Cannot add property!!");
                }
            }
        }
        return false;
    }
}
