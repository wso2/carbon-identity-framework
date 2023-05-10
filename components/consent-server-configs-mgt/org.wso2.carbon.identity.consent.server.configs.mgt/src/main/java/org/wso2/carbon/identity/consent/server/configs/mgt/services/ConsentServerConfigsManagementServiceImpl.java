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

package org.wso2.carbon.identity.consent.server.configs.mgt.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;
import org.wso2.carbon.identity.configuration.mgt.core.search.ComplexCondition;
import org.wso2.carbon.identity.configuration.mgt.core.search.Condition;
import org.wso2.carbon.identity.configuration.mgt.core.search.PrimitiveCondition;
import org.wso2.carbon.identity.configuration.mgt.core.search.constant.ConditionType;
import org.wso2.carbon.identity.consent.server.configs.mgt.exceptions.ConsentServerConfigsMgtClientException;
import org.wso2.carbon.identity.consent.server.configs.mgt.exceptions.ConsentServerConfigsMgtException;
import org.wso2.carbon.identity.consent.server.configs.mgt.exceptions.ConsentServerConfigsMgtServerException;
import org.wso2.carbon.identity.consent.server.configs.mgt.internal.ConsentServerConfigsManagementDataHolder;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.configuration.mgt.core.search.constant.ConditionType.PrimitiveOperator.EQUALS;
import static org.wso2.carbon.identity.consent.server.configs.mgt.utils.Constants.EXTERNAL_CONSENT_PAGE;
import static org.wso2.carbon.identity.consent.server.configs.mgt.utils.Constants.EXTERNAL_CONSENT_PAGE_CONFIGURATIONS;
import static org.wso2.carbon.identity.consent.server.configs.mgt.utils.Constants.EXTERNAL_CONSENT_PAGE_URL;
import static org.wso2.carbon.identity.consent.server.configs.mgt.utils.Constants.ErrorMessages.ERROR_GETTING_EXISTING_CONFIGURATIONS;
import static org.wso2.carbon.identity.consent.server.configs.mgt.utils.Constants.ErrorMessages.ERROR_NO_EXTERNAL_CONSENT_PAGE_CONFIGURATIONS_FOUND;
import static org.wso2.carbon.identity.consent.server.configs.mgt.utils.Constants.ErrorMessages.ERROR_NO_EXTERNAL_CONSENT_PAGE_URL_FOUND;
import static org.wso2.carbon.identity.consent.server.configs.mgt.utils.Constants.RESOURCE_NAME;
import static org.wso2.carbon.identity.consent.server.configs.mgt.utils.Constants.RESOURCE_TYPE_NAME;

/**
 * Class for Consent Server Configs Management Service Implementation.
 */
public class ConsentServerConfigsManagementServiceImpl implements ConsentServerConfigsManagementService {

    private static final Log LOG = LogFactory.getLog(ConsentServerConfigsManagementServiceImpl.class);

    /**
     * Method to get external consent page url.
     *
     * @param tenantDomain  Tenant domain.
     * @return External consent page url.
     * @throws ConsentServerConfigsMgtException If an error occurred in getting the consent url.
     */
    public String getExternalConsentPageUrl(String tenantDomain) throws ConsentServerConfigsMgtException {

        Condition condition = getExternalConsentPageUrlSearchCondition();
        List<Attribute> resourceAttributes = getResourceAttributesByTenant(tenantDomain, condition);

        String externalConsentPageUrl = "";
        if (resourceAttributes != null && resourceAttributes.size() > 0) {

            externalConsentPageUrl = resourceAttributes.stream()
                    .filter(attribute -> attribute.getKey().equals(EXTERNAL_CONSENT_PAGE_URL))
                    .map(Attribute::getValue).findFirst().orElse(null);
        }

        if (externalConsentPageUrl.isEmpty()) {
            throw new ConsentServerConfigsMgtClientException(
                    ERROR_NO_EXTERNAL_CONSENT_PAGE_URL_FOUND.getCode(),
                    String.format(ERROR_NO_EXTERNAL_CONSENT_PAGE_URL_FOUND.getMessage(), tenantDomain));
        }
        return externalConsentPageUrl;
    }

    /**
     * Method to get the resource attributes for the tenant which matches with the condition.
     *
     * @param tenantDomain  Tenant domain.
     * @param condition     Condition to search the resource.
     * @return Resource attributes list.
     * @throws ConsentServerConfigsMgtException If an error occurred in getting the consent url.
     */
    private List<Attribute> getResourceAttributesByTenant(String tenantDomain, Condition condition)
            throws ConsentServerConfigsMgtException {

        try {
            List<Attribute> resourceAttributes = new ArrayList<>();

            Resources resources = getConfigurationManager()
                    .getTenantResources(tenantDomain, condition);

            if (resources == null || resources.getResources() == null) {
                throw new ConsentServerConfigsMgtClientException(
                        ERROR_NO_EXTERNAL_CONSENT_PAGE_CONFIGURATIONS_FOUND.getCode(),
                        String.format(ERROR_NO_EXTERNAL_CONSENT_PAGE_CONFIGURATIONS_FOUND.getMessage(),
                                tenantDomain));
            }

            if (resources.getResources().size() > 0) {
                for (Resource resource : resources.getResources()) {
                    resourceAttributes = resource.getAttributes();
                }
            }

           return resourceAttributes;
        } catch (ConfigurationManagementException e) {
            throw new ConsentServerConfigsMgtServerException(ERROR_GETTING_EXISTING_CONFIGURATIONS.getCode(),
                    String.format(ERROR_GETTING_EXISTING_CONFIGURATIONS.getMessage(), tenantDomain));
        }
    }

    /**
     * Method to get the search condition to get the external consent url resources.
     *
     * @return Condition to search the external consent page configuration resource.
     */
    private Condition getExternalConsentPageUrlSearchCondition() {

        Condition resourceTypeCondition = new PrimitiveCondition(
                RESOURCE_TYPE_NAME, EQUALS, EXTERNAL_CONSENT_PAGE_CONFIGURATIONS);
        Condition resourceNameCondition = new PrimitiveCondition(
                RESOURCE_NAME, EQUALS, EXTERNAL_CONSENT_PAGE);
        List<Condition> list = new ArrayList<>();
        list.add(resourceTypeCondition);
        list.add(resourceNameCondition);

        Condition searchCondition = new ComplexCondition(ConditionType.ComplexOperator.AND, list);
        return searchCondition;
    }
    /**
     * Get configuration manger.
     *
     * @return configuration manger.
     */
    private ConfigurationManager getConfigurationManager() {

        return ConsentServerConfigsManagementDataHolder.getConfigurationManager();
    }
}
