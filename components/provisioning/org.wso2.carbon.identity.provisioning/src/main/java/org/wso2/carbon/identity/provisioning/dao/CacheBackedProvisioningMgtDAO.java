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

package org.wso2.carbon.identity.provisioning.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.provisioning.ProvisionedIdentifier;
import org.wso2.carbon.identity.provisioning.ProvisioningEntity;
import org.wso2.carbon.identity.provisioning.cache.ProvisioningEntityCache;
import org.wso2.carbon.identity.provisioning.cache.ProvisioningEntityCacheEntry;
import org.wso2.carbon.identity.provisioning.cache.ProvisioningEntityCacheKey;

public class CacheBackedProvisioningMgtDAO {

    private static final Log log = LogFactory.getLog(CacheBackedProvisioningMgtDAO.class);

    private ProvisioningManagementDAO provisioningMgtDAO = null;

    private ProvisioningEntityCache provisioningEntityCache = null;

    /**
     * @param provisioningManagementDAO
     */
    public CacheBackedProvisioningMgtDAO(ProvisioningManagementDAO provisioningManagementDAO) {
        this.provisioningMgtDAO = provisioningManagementDAO;
        this.provisioningEntityCache = ProvisioningEntityCache.getInstance();
    }

    /**
     * @param identityProviderName
     * @param connectorType
     * @param provisioningEntity
     * @param tenantId
     * @throws IdentityApplicationManagementException
     */
    public void addProvisioningEntity(String identityProviderName, String connectorType,
                                      ProvisioningEntity provisioningEntity, int tenantId, String tenantDomain)
            throws IdentityApplicationManagementException {


        provisioningMgtDAO.addProvisioningEntity(identityProviderName, connectorType, provisioningEntity, tenantId);

        if (log.isDebugEnabled()) {
            log.debug("Caching newly added Provisioning Entity : " +
                    "identityProviderName=" + identityProviderName +
                    "&& connectorType=" + connectorType +
                    "&& provisioningEntityType=" + provisioningEntity.getEntityType() +
                    "&& provisioningEntityName=" + provisioningEntity.getEntityName() +
                    "&& provisioningIdentifier=" + provisioningEntity.getIdentifier().getIdentifier());
        }


        ProvisioningEntityCacheKey cacheKey = new ProvisioningEntityCacheKey(identityProviderName, connectorType,
                provisioningEntity);
        ProvisioningEntityCacheEntry entry = new ProvisioningEntityCacheEntry();

        ProvisioningEntity cachedProvisioningEntity = new ProvisioningEntity(provisioningEntity.getEntityType(),
                provisioningEntity.getOperation());
        ProvisionedIdentifier provisionedIdentifier = provisioningEntity.getIdentifier();
        cachedProvisioningEntity.setIdentifier(provisionedIdentifier);
        entry.setProvisioningEntity(cachedProvisioningEntity);
        provisioningEntityCache.addToCache(cacheKey, entry, tenantDomain);

    }

    /**
     * @param identityProviderName
     * @param connectorType
     * @param provisioningEntity
     * @param tenantId
     * @throws IdentityApplicationManagementException
     */
    public ProvisionedIdentifier getProvisionedIdentifier(String identityProviderName, String connectorType,
                                                          ProvisioningEntity provisioningEntity, int tenantId, String tenantDomain)
            throws IdentityApplicationManagementException {

        ProvisioningEntityCacheKey cacheKey = new ProvisioningEntityCacheKey(identityProviderName, connectorType,
                provisioningEntity);
        ProvisioningEntityCacheEntry entry = provisioningEntityCache.getValueFromCache(cacheKey, tenantDomain);

        if (entry != null) {
            if (log.isDebugEnabled()) {
                log.debug("Cache entry found for Provisioning Entity : " +
                        "identityProviderName=" + identityProviderName +
                        "&& connectorType=" + connectorType +
                        "&& provisioningEntityType=" + provisioningEntity.getEntityType() +
                        "&& provisioningEntityName=" + provisioningEntity.getEntityName());
            }
            provisioningEntity = entry.getProvisioningEntity();
            return provisioningEntity.getIdentifier();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cache entry not found for Provisioning Entity : " +
                        "identityProviderName=" + identityProviderName +
                        "&& connectorType=" + connectorType +
                        "&& provisioningEntityType=" + provisioningEntity.getEntityType() +
                        "&& provisioningEntityName=" + provisioningEntity.getEntityName() +
                        ". Fetching entity from DB");
            }

            ProvisionedIdentifier provisionedIdentifier = provisioningMgtDAO.getProvisionedIdentifier(identityProviderName, connectorType, provisioningEntity, tenantId);

            if (provisionedIdentifier != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Entry fetched from DB for Provisioning Entity : " +
                            "identityProviderName=" + identityProviderName +
                            "&& connectorType=" + connectorType +
                            "&& provisioningEntityType=" + provisioningEntity.getEntityType() +
                            "&& provisioningEntityName=" + provisioningEntity.getEntityName() +
                            ". Updating cache");
                }

                ProvisioningEntity cachedProvisioningEntity = new ProvisioningEntity(provisioningEntity.getEntityType(), provisioningEntity.getOperation());
                cachedProvisioningEntity.setIdentifier(provisionedIdentifier);

                entry = new ProvisioningEntityCacheEntry();
                entry.setProvisioningEntity(cachedProvisioningEntity);
                provisioningEntityCache.addToCache(cacheKey, entry, tenantDomain);

                return provisionedIdentifier;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Entry for Provisioning Entity : " + "identityProviderName=" +
                            identityProviderName + "&& connectorType=" + connectorType +
                            "&& provisioningEntityType=" + provisioningEntity.getEntityType() +
                            "&& provisioningEntityName=" + provisioningEntity.getEntityName() +
                            " not found in cache or DB");
                }
            }
        }

        return null;
    }

    /**
     * @param identityProviderName
     * @param connectorType
     * @param provisioningEntity
     * @param tenantId
     * @throws IdentityApplicationManagementException
     */
    public void deleteProvisioningEntity(String identityProviderName, String connectorType,
                                         ProvisioningEntity provisioningEntity, int tenantId, String tenantDomain)
            throws IdentityApplicationManagementException {

        ProvisioningEntityCacheKey cacheKey = new ProvisioningEntityCacheKey(identityProviderName, connectorType,
                provisioningEntity);
        ProvisioningEntityCacheEntry entry = provisioningEntityCache.getValueFromCache(cacheKey, tenantDomain);
        if (entry != null) {
            if (log.isDebugEnabled()) {
                log.debug("Cache entry found for Provisioning Entity : " +
                        "identityProviderName=" + identityProviderName +
                        "&& connectorType=" + connectorType +
                        "&& provisioningEntityType=" + provisioningEntity.getEntityType() +
                        "&& provisioningEntityName=" + provisioningEntity.getEntityName() +
                        ". Hence remove from cache");
            }
            provisioningEntityCache.clearCacheEntry(cacheKey, tenantDomain);
        }

        provisioningMgtDAO.deleteProvisioningEntity(identityProviderName, connectorType, provisioningEntity, tenantId);

        if (log.isDebugEnabled()) {
            log.debug("Entry removed from DB for Provisioning Entity : " +
                    "identityProviderName=" + identityProviderName +
                    "&& connectorType=" + connectorType +
                    "&& provisioningEntityType=" + provisioningEntity.getEntityType() +
                    "&& provisioningEntityName=" + provisioningEntity.getEntityName());
        }
    }

    public void updateProvisionedEntityName(ProvisioningEntity provisioningEntity) throws
                                                                                IdentityApplicationManagementException {
        //todo: as an improvement cache implementation need to be done
        provisioningMgtDAO.updateProvisioningEntityName(provisioningEntity);
    }

    public String getProvisionedEntityNameByLocalId(String localId) throws IdentityApplicationManagementException {
        //todo: as an improvement cache implementation need to be done
        return provisioningMgtDAO.getProvisionedEntityNameByLocalId(localId);
    }
}
