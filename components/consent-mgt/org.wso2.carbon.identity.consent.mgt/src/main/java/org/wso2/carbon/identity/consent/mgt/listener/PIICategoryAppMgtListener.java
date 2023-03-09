/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.consent.mgt.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.listener.AbstractApplicationMgtListener;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.consent.mgt.IdentityConsentMgtUtils;
import org.wso2.carbon.identity.consent.mgt.internal.IdentityConsentDataHolder;

import java.util.List;

import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.DESCRIPTION_PROPERTY;

/**
 * Takes care of populating PII categories in the database when the service provider
 * is updated with requested claims.
 */
public class PIICategoryAppMgtListener extends AbstractApplicationMgtListener {

    private static final Log log = LogFactory.getLog(PIICategoryAppMgtListener.class);

    private static final String DEFAULT_PURPOSE_CATEGORY = "DEFAULT";

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
     * Overridden the default value.
     *
     * @return the order of the listener.
     */
    @Override
    public int getDefaultOrderId() {

        return 908;
    }

    /**
     * Add PII categories when the requested claims are added after creating an application
     *
     * @param serviceProvider {@link ServiceProvider}
     * @param tenantDomain    Tenant domain
     * @param userName        Username
     * @return true if the operation is successful
     * @throws IdentityApplicationManagementException when an exception occurs while adding PII categories
     */
    public boolean doPostCreateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        addPIICategoryForRequestedClaims(serviceProvider, tenantDomain);
        return true;
    }

    /**
     * Add PII categories when the requested claims are added after updating an application
     *
     * @param serviceProvider {@link ServiceProvider}
     * @param tenantDomain    Tenant domain
     * @param userName        Username
     * @return true if the operation is successful
     * @throws IdentityApplicationManagementException when an exception occurs while adding PII categories
     */
    public boolean doPostUpdateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        addPIICategoryForRequestedClaims(serviceProvider, tenantDomain);
        return true;
    }

    private void addPIICategoryForRequestedClaims(ServiceProvider serviceProvider, String tenantDomain)
            throws IdentityApplicationManagementException {

        if (serviceProvider.getClaimConfig() != null) {
            serviceProvider.getClaimConfig().getClaimMappings();

            try {
                List<LocalClaim> localClaims =
                        IdentityConsentDataHolder.getInstance().getClaimMetadataManagementService()
                                .getLocalClaims(tenantDomain);
                for (ClaimMapping claimMapping : serviceProvider.getClaimConfig().getClaimMappings()) {
                    String claimUri = claimMapping.getLocalClaim().getClaimUri();

                    LocalClaim claim = localClaims.stream()
                            .filter(localClaim -> localClaim.getClaimURI().equals(claimUri))
                            .findFirst()
                            .orElse(null);

                    if (claim != null) {
                        try {
                            PIICategory piiCategoryInput =
                                    new PIICategory(claimUri, claim.getClaimProperty(DESCRIPTION_PROPERTY), false,
                                            claim.getClaimProperty(DESCRIPTION_PROPERTY));
                            if (!getConsentManager().isPIICategoryExists(claimUri)) {
                                getConsentManager().addPIICategory(piiCategoryInput);
                            }
                        } catch (ConsentManagementException e) {
                            throw new IdentityApplicationManagementException("Consent PII category error",
                                    "Error while adding PII category:" + DEFAULT_PURPOSE_CATEGORY, e);
                        }
                    }
                }
            } catch (ClaimMetadataException e) {
                throw new IdentityApplicationManagementException("Error while getting local claims", e);
            }
        }
    }

    private ConsentManager getConsentManager() {

        return IdentityConsentDataHolder.getInstance().getConsentManager();
    }
}
