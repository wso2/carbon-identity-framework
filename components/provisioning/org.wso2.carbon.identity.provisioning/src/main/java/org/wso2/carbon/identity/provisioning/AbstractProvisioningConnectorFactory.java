/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.provisioning;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.provisioning.cache.ProvisioningConnectorCache;
import org.wso2.carbon.identity.provisioning.cache.ProvisioningConnectorCacheEntry;
import org.wso2.carbon.identity.provisioning.cache.ProvisioningConnectorCacheKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractProvisioningConnectorFactory {

    private static final Log log = LogFactory.getLog(AbstractProvisioningConnectorFactory.class);

    /**
     * @param identityProviderName
     * @param provisoningProperties
     * @param tenantDomain
     * @return
     * @throws IdentityProvisioningException
     */
    public AbstractOutboundProvisioningConnector getConnector(String identityProviderName,
                                                              Property[] provisoningProperties, String tenantDomain)
            throws IdentityProvisioningException {

        ProvisioningConnectorCacheKey cacheKey = new ProvisioningConnectorCacheKey(identityProviderName);
        ProvisioningConnectorCacheEntry entry = ProvisioningConnectorCache.getInstance()
                                                            .getValueFromCache(cacheKey, tenantDomain);

        if (entry != null) {
            if (log.isDebugEnabled()) {
                log.debug("Provisioning cache HIT for " + identityProviderName + " of " + tenantDomain);
            }
            return entry.getProvisioningConnector();
        }

        AbstractOutboundProvisioningConnector connector;

        Property idpName = new Property();
        idpName.setName("identityProviderName");
        idpName.setValue(identityProviderName);

        List<Property> provisioningPropertiesList = new ArrayList<>(Arrays.asList(provisoningProperties));

        provisioningPropertiesList.add(idpName);

        Property[] provisioningProperties = new Property[provisioningPropertiesList.size()];
        provisioningProperties = provisioningPropertiesList.toArray(provisioningProperties);

        connector = buildConnector(provisioningProperties);
        entry = new ProvisioningConnectorCacheEntry();
        entry.setProvisioningConnector(connector);
        ProvisioningConnectorCache.getInstance().addToCache(cacheKey, entry, tenantDomain);

        return connector;

    }

    /**
     *
     * @param provisoningProperties
     * @return
     * @throws IdentityProvisioningException
     */
    protected abstract AbstractOutboundProvisioningConnector buildConnector(
            Property[] provisoningProperties) throws IdentityProvisioningException;

    /**
     * @param identityProviderName
     * @param tenantDomain
     * @throws IdentityProvisioningException
     */
    public void destroyConnector(String identityProviderName, String tenantDomain)
            throws IdentityProvisioningException {

        ProvisioningConnectorCacheKey cacheKey = new ProvisioningConnectorCacheKey(identityProviderName);
        ProvisioningConnectorCacheEntry entry = ProvisioningConnectorCache.getInstance()
                                                                .getValueFromCache(cacheKey, tenantDomain);

        if (entry != null) {
            ProvisioningConnectorCache.getInstance().clearCacheEntry(cacheKey, tenantDomain);

            if (log.isDebugEnabled()) {
                log.debug("Provisioning cached entry removed for idp " + identityProviderName
                        + " from the connector " + getConnectorType());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Provisioning cached entry not found for idp " + identityProviderName
                        + " from the connector " + getConnectorType());
            }
        }
    }

    /**
     * @return
     */
    public List<Property> getConfigurationProperties() {
        return new ArrayList<>();
    }


    /**
     * @return
     */
    public abstract String getConnectorType();
}
