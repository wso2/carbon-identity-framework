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

package org.wso2.carbon.identity.consent.mgt.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.ReceiptListResponse;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.consent.mgt.IdentityConsentMgtUtils;
import org.wso2.carbon.identity.consent.mgt.internal.IdentityConsentDataHolder;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.core.handler.InitConfig;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.bean.IdentityEventMessageContext;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.core.util.LambdaExceptionUtils.rethrowConsumer;

/**
 * Deletes Consents issued against a particular user when a user is deleted from the system.
 */
public class ConsentDeletionUserEventHandler extends AbstractEventHandler {

    private static final Log log = LogFactory.getLog(ConsentDeletionUserEventHandler.class);
    private static int consentSearchLimit = 100;
    private static final String HANDLER_NAME = "user.consent.delete";
    private static final String SEARCH_LIMIT_PROPERTY = HANDLER_NAME + ".receipt.search.limit";

    /**
     * Overridden to check the configuration for this listener enabling and also to check whether globally consent
     * feature enable
     *
     * @return Whether this listener is enabled or not
     */
    @Override
    public boolean isEnabled(MessageContext messageContext) {

        boolean isHandlerEnabledFromConfig = super.isEnabled(messageContext);
        boolean isConsentEnabledSystemWide = IdentityConsentMgtUtils.isConsentEnabled();
        if (log.isDebugEnabled()) {
            log.debug("Is handler enabled from configs: " + isHandlerEnabledFromConfig);
            log.debug("Is consent enabled system wide: " + isConsentEnabledSystemWide);
        }
        if (isConsentEnabledSystemWide && isHandlerEnabledFromConfig) {
            if (log.isDebugEnabled()) {
                log.debug("Handler is enabled and consent is enabled system wide. Hence returning true for " +
                        "isEnabled");
            }
            return true;
        }
        return false;
    }

    /**
     * Consent search limit is configurable and the config is read from identity-event.properties file.
     *
     * @param configuration
     * @throws IdentityRuntimeException
     */
    @Override
    public void init(InitConfig configuration) throws IdentityRuntimeException {

        super.init(configuration);
        String receiptSearchLimit = this.configs.getModuleProperties().getProperty(SEARCH_LIMIT_PROPERTY);
        try {
            consentSearchLimit = Integer.valueOf(receiptSearchLimit);
        } catch (NumberFormatException e) {
            log.error("Configured receipt.search.limit cannot be parsed as an integer. " +
                    "Hence using default value: " + consentSearchLimit);
        }
    }

    /**
     * Delete consents issued against a particular user when a user is deleted.
     *
     * @param event Post User Delete event.
     * @throws IdentityEventException IdentityEventException.
     */
    @Override
    public void handleEvent(Event event) throws IdentityEventException {

        IdentityEventMessageContext eventContext = new IdentityEventMessageContext(event);
        if (!isEnabled(eventContext)) {
            if (log.isDebugEnabled()) {
                log.debug("ConsentDeletionUserEventHandler is disabled. Not handling the " + event.getEventName()
                        + " event.");
            }
            return;
        }

        Map<String, Object> eventProperties = event.getEventProperties();
        String userName = (String) eventProperties.get(IdentityEventConstants.EventProperty.USER_NAME);
        UserStoreManager userStoreManager = (UserStoreManager)
                eventProperties.get(IdentityEventConstants.EventProperty.USER_STORE_MANAGER);

        String domainName = userStoreManager.getRealmConfiguration().
                getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
        String tenantDomain = getUserTenantDomain(eventProperties);
        String usernameWithUserStoreDomain = UserCoreUtil.addDomainToName(userName, domainName);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Deleting consents for user: %s , in tenant domain :%s",
                    usernameWithUserStoreDomain, tenantDomain));
        }
        ConsentManager consentManager = IdentityConsentDataHolder.getInstance().getPrivilegedConsentManager();
        try {
            List<ReceiptListResponse> receiptListResponses = consentManager.searchReceipts(consentSearchLimit, 0,
                    usernameWithUserStoreDomain, null, "*", null);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Found %d receipts issued for user: %s, in tenant domain: %s",
                        receiptListResponses.size(), usernameWithUserStoreDomain, tenantDomain));
            }
            receiptListResponses.forEach(rethrowConsumer(receiptListResponse -> {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Deleting receipt with ID : %d, issued for application %s" +
                            receiptListResponse.getConsentReceiptId(), receiptListResponse.getSpDisplayName()));
                }
                consentManager.deleteReceipt(receiptListResponse.getConsentReceiptId());
            }));
        } catch (ConsentManagementException e) {
            throw new IdentityEventException("Error while deleting consents for user " + userName, e);
        }
    }

    private String getUserTenantDomain(Map<String, Object> eventProperties) {

        return (String) eventProperties.get(IdentityEventConstants.EventProperty.TENANT_DOMAIN);
    }

    /**
     * Returns the name of this handler.
     *
     * @return The name of the handler.
     */
    public String getName() {

        return HANDLER_NAME;
    }
}
