/*
 * Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.idp.mgt.dao;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementClientException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementServerException;
import org.wso2.carbon.idp.mgt.cache.IdPAuthPropertyCacheKey;
import org.wso2.carbon.idp.mgt.cache.IdPCacheByAuthProperty;
import org.wso2.carbon.idp.mgt.cache.IdPCacheByHRI;
import org.wso2.carbon.idp.mgt.cache.IdPCacheByName;
import org.wso2.carbon.idp.mgt.cache.IdPCacheByResourceId;
import org.wso2.carbon.idp.mgt.cache.IdPCacheEntry;
import org.wso2.carbon.idp.mgt.cache.IdPHomeRealmIdCacheKey;
import org.wso2.carbon.idp.mgt.cache.IdPNameCacheKey;
import org.wso2.carbon.idp.mgt.cache.IdPResourceIdCacheKey;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.idp.mgt.model.ConnectedAppsResult;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheBackedIdPMgtDAO {

    private static final Log log = LogFactory.getLog(CacheBackedIdPMgtDAO.class);

    private IdPManagementDAO idPMgtDAO = null;

    private IdPCacheByName idPCacheByName = null;
    private IdPCacheByHRI idPCacheByHRI = null;
    private IdPCacheByAuthProperty idPCacheByAuthProperty = null;
    private IdPCacheByResourceId idPCacheByResourceId = null;

    /**
     * @param idPMgtDAO
     */
    public CacheBackedIdPMgtDAO(IdPManagementDAO idPMgtDAO) {
        this.idPMgtDAO = idPMgtDAO;
        idPCacheByName = IdPCacheByName.getInstance();
        idPCacheByHRI = IdPCacheByHRI.getInstance();
        idPCacheByAuthProperty = IdPCacheByAuthProperty.getInstance();
        idPCacheByResourceId = IdPCacheByResourceId.getInstance();
    }

    /**
     * @param dbConnection
     * @param tenantId
     * @param tenantDomain
     * @return
     * @throws IdentityProviderManagementException
     */
    public List<IdentityProvider> getIdPs(Connection dbConnection, int tenantId,
                                          String tenantDomain) throws IdentityProviderManagementException {

        return idPMgtDAO.getIdPs(dbConnection, tenantId, tenantDomain);
    }

    /**
     * @param dbConnection
     * @param tenantId
     * @param tenantDomain
     * @param filter
     * @return
     * @throws IdentityProviderManagementException
     */
    public List<IdentityProvider> getIdPsSearch(Connection dbConnection,
			int tenantId, String tenantDomain, String filter)
			throws IdentityProviderManagementException {
		return idPMgtDAO.getIdPsSearch(dbConnection, tenantId, tenantDomain,
				filter);
	}

    /**
     * Get all basic identity provider information for a matching filter.
     *
     * @param tenantId             tenant Id of the identity provider.
     * @param expressionConditions filter value list for IdP search.
     * @param limit                limit per page.
     * @param offset               offset value.
     * @param sortOrder            order of IdP ASC/DESC.
     * @param sortBy               the attribute need to sort.
     * @return Identity Provider's basic information array.
     * @throws IdentityProviderManagementServerException Error when getting list of Identity Providers.
     * @throws IdentityProviderManagementClientException Error when append the filer string.
     */
    public List<IdentityProvider> getPaginatedIdPsSearch(int tenantId, List<ExpressionNode> expressionConditions,
                                                         int limit, int offset, String sortOrder, String sortBy)
            throws IdentityProviderManagementServerException, IdentityProviderManagementClientException {

        return idPMgtDAO.getIdPsSearch(tenantId, expressionConditions, limit, offset, sortOrder, sortBy);
    }

    /**
     * Get number of IdP count for a matching filter.
     *
     * @param tenantId             Tenant Id of the identity provider.
     * @param expressionConditions filter value list for IdP search.
     * @return number of IdP count for a given filter.
     * @throws IdentityProviderManagementServerException Error when getting count of Identity Providers.
     * @throws IdentityProviderManagementClientException Error when append the filer string.
     */
    public int getTotalIdPCount(int tenantId, List<ExpressionNode> expressionConditions)
            throws IdentityProviderManagementServerException, IdentityProviderManagementClientException {

        return idPMgtDAO.getCountOfFilteredIdPs(tenantId, expressionConditions);
    }

    /**
     * @param dbConnection
     * @param idPName
     * @param tenantId
     * @param tenantDomain
     * @return
     * @throws IdentityProviderManagementException
     */
    public IdentityProvider getIdPByName(Connection dbConnection, String idPName,
                                         int tenantId, String tenantDomain) throws
            IdentityProviderManagementException {

        IdPNameCacheKey cacheKey = new IdPNameCacheKey(idPName, tenantDomain);
        IdPCacheEntry entry = idPCacheByName.getValueFromCache(cacheKey);

        if (entry != null) {
            log.debug("Cache entry found for Identity Provider " + idPName);
            IdentityProvider identityProvider = entry.getIdentityProvider();
            IdPManagementUtil.removeRandomPasswords(identityProvider, false);
            return identityProvider;
        } else {
            log.debug("Cache entry not found for Identity Provider " + idPName
                    + ". Fetching entry from DB");
        }

        IdentityProvider identityProvider = idPMgtDAO.getIdPByName(dbConnection, idPName,
                                                                   tenantId, tenantDomain);

        if (identityProvider != null) {
            log.debug("Entry fetched from DB for Identity Provider " + idPName + ". Updating cache");
            idPCacheByName.addToCache(cacheKey, new IdPCacheEntry(identityProvider));
            if (identityProvider.getHomeRealmId() != null) {
                IdPHomeRealmIdCacheKey homeRealmIdCacheKey = new IdPHomeRealmIdCacheKey(
                        identityProvider.getHomeRealmId(), tenantDomain);
                idPCacheByHRI.addToCache(homeRealmIdCacheKey, new IdPCacheEntry(identityProvider));
            }
        } else {
            log.debug("Entry for Identity Provider " + idPName + " not found in cache or DB");
        }

        return identityProvider;
    }

    /**
     * @param dbConnection Database connection.
     * @param id Id of the identity provider.
     * @param tenantId Tenant Id of the identity provider.
     * @param tenantDomain Tenant domain of the identity provider.
     * @return Identity provider with given ID.
     * @throws IdentityProviderManagementException
     */
    public IdentityProvider getIdPById(Connection dbConnection, int id,
                                       int tenantId, String tenantDomain) throws IdentityProviderManagementException {

        IdentityProvider identityProvider = idPMgtDAO.getIDPbyId(dbConnection, id,
                tenantId, tenantDomain);

        if (identityProvider != null) {
            if (log.isDebugEnabled()) {
                log.debug("Entry fetched from DB for Identity Provider " + identityProvider.getIdentityProviderName()
                        + ". Updating cache");
            }
            IdPNameCacheKey cacheKey = new IdPNameCacheKey(identityProvider.getIdentityProviderName(), tenantDomain);
            idPCacheByName.addToCache(cacheKey, new IdPCacheEntry(identityProvider));
            if (identityProvider.getHomeRealmId() != null) {
                IdPHomeRealmIdCacheKey homeRealmIdCacheKey = new IdPHomeRealmIdCacheKey(
                        identityProvider.getHomeRealmId(), tenantDomain);
                idPCacheByHRI.addToCache(homeRealmIdCacheKey, new IdPCacheEntry(identityProvider));
            }
        } else {
            if(log.isDebugEnabled()) {
                log.debug(String.format("No IDP found with ID: %d either in cache or DB", id));
            }
        }

        return identityProvider;
    }

    /**
     * @param resourceId   resource ID of the identity provider.
     * @param tenantId     Tenant ID of the identity provider.
     * @param tenantDomain Tenant domain of the identity provider.
     * @return Identity provider with given resource ID.
     * @throws IdentityProviderManagementException
     */
    public IdentityProvider getIdPByResourceId(String resourceId, int tenantId, String tenantDomain) throws
            IdentityProviderManagementException {

        IdentityProvider identityProvider;
        IdPResourceIdCacheKey cacheKey = new IdPResourceIdCacheKey(resourceId);
        IdPCacheEntry entry = idPCacheByResourceId.getValueFromCache(cacheKey);

        if (entry != null) {
            if (log.isDebugEnabled()) {
                log.debug("Cache entry found for Identity Provider with resource ID:" + resourceId);
            }
            identityProvider = entry.getIdentityProvider();
            IdPManagementUtil.removeRandomPasswords(identityProvider, false);
            return identityProvider;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cache entry not found for Identity Provider with resource ID: " + resourceId
                        + ". Fetching entry from DB");
            }
            identityProvider = idPMgtDAO.getIDPbyResourceId(null, resourceId,
                    tenantId, tenantDomain);

            if (identityProvider != null) {
                addIdPCache(identityProvider, tenantDomain);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("No IDP found with resource ID: %s either in cache or DB", resourceId));
                }
            }
        }

        return identityProvider;
    }

    /**
     * @param dbConnection
     * @param property
     * @param value
     * @param tenantId
     * @param tenantDomain
     * @return
     * @throws IdentityProviderManagementException
     */
    public IdentityProvider getIdPByAuthenticatorPropertyValue(Connection dbConnection, String property, String value,
                                                               int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        IdPAuthPropertyCacheKey cacheKey = new IdPAuthPropertyCacheKey(property, value, tenantDomain);
        IdPCacheEntry entry = idPCacheByAuthProperty.getValueFromCache(cacheKey);

        if (entry != null) {
            log.debug("Cache entry found for Identity Provider with authenticator property " + property
                    + " and with value " + value);
            IdentityProvider identityProvider = entry.getIdentityProvider();
            return identityProvider;
        } else {
            log.debug("Cache entry not found for Identity Provider with authenticator property " + property
                    + " and with value " + value + ". Fetching entry from DB");
        }

        IdentityProvider identityProvider = idPMgtDAO.getIdPByAuthenticatorPropertyValue(dbConnection, property, value,
                                                                                         tenantId, tenantDomain);

        if (identityProvider != null) {
            log.debug("Entry fetched from DB for Identity Provider with authenticator property " + property
                    + " and with value " + value + ". Updating cache");

            IdPNameCacheKey idPNameCacheKey = new IdPNameCacheKey(identityProvider.getIdentityProviderName(),
                    tenantDomain);
            idPCacheByName.addToCache(idPNameCacheKey, new IdPCacheEntry(identityProvider));
            if (identityProvider.getHomeRealmId() != null) {
                IdPHomeRealmIdCacheKey homeRealmIdCacheKey = new IdPHomeRealmIdCacheKey(
                        identityProvider.getHomeRealmId(), tenantDomain);
                idPCacheByHRI.addToCache(homeRealmIdCacheKey, new IdPCacheEntry(identityProvider));
            }
        } else {
            log.debug("Entry for Identity Provider with authenticator property " + property + " and with value "
                    + value + " not found in cache or DB");
        }

        return identityProvider;
    }

    /**
     * @param dbConnection
     * @param property
     * @param value
     * @param authenticator
     * @param tenantId
     * @param tenantDomain
     * @return
     * @throws IdentityProviderManagementException
     */
    public IdentityProvider getIdPByAuthenticatorPropertyValue(Connection dbConnection, String property, String value,
                                                               String authenticator, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        IdPAuthPropertyCacheKey cacheKey = new IdPAuthPropertyCacheKey(property, value, tenantDomain);
        IdPCacheEntry entry = idPCacheByAuthProperty.getValueFromCache(cacheKey);

        if (entry != null) {
            log.debug("Cache entry found for Identity Provider with authenticator property " + property
                    + " and with value " + value);
            IdentityProvider identityProvider = entry.getIdentityProvider();
            return identityProvider;
        } else {
            log.debug("Cache entry not found for Identity Provider with authenticator property " + property
                    + " and with value " + value + ". Fetching entry from DB");
        }

        IdentityProvider identityProvider = idPMgtDAO.getIdPByAuthenticatorPropertyValue(dbConnection, property,
                value, authenticator,tenantId, tenantDomain);

        if (identityProvider != null) {
            log.debug("Entry fetched from DB for Identity Provider with authenticator property " + property
                    + " and with value " + value + ". Updating cache");

            IdPNameCacheKey idPNameCacheKey = new IdPNameCacheKey(identityProvider.getIdentityProviderName(),
                    tenantDomain);
            idPCacheByName.addToCache(idPNameCacheKey, new IdPCacheEntry(identityProvider));
            if (identityProvider.getHomeRealmId() != null) {
                IdPHomeRealmIdCacheKey homeRealmIdCacheKey = new IdPHomeRealmIdCacheKey(
                        identityProvider.getHomeRealmId(), tenantDomain);
                idPCacheByHRI.addToCache(homeRealmIdCacheKey, new IdPCacheEntry(identityProvider));
            }
        } else {
            log.debug("Entry for Identity Provider with authenticator property " + property + " and with value "
                    + value + " not found in cache or DB");
        }

        return identityProvider;
    }

    /**
     * @param realmId
     * @param tenantId
     * @param tenantDomain
     * @return
     * @throws IdentityProviderManagementException
     */
    public IdentityProvider getIdPByRealmId(String realmId, int tenantId,
                                            String tenantDomain) throws IdentityProviderManagementException {

        IdPHomeRealmIdCacheKey cacheKey = new IdPHomeRealmIdCacheKey(realmId, tenantDomain);
        IdPCacheEntry entry = idPCacheByHRI.getValueFromCache(cacheKey);
        if (entry != null) {
            log.debug("Cache entry found for Identity Provider with Home Realm ID " + realmId);
            return entry.getIdentityProvider();
        } else {
            log.debug("Cache entry not found for Identity Provider with Home Realm ID " + realmId
                    + ". Fetching entry from DB");
        }

        IdentityProvider identityProvider = idPMgtDAO.getIdPByRealmId(realmId, tenantId, tenantDomain);

        if (identityProvider != null) {
            log.debug("Entry fetched from DB for Identity Provider with Home Realm ID " + realmId
                    + ". Updating cache");
            idPCacheByHRI.addToCache(cacheKey, new IdPCacheEntry(identityProvider));
            IdPNameCacheKey idPNameCacheKey = new IdPNameCacheKey(
                    identityProvider.getIdentityProviderName(), tenantDomain);
            idPCacheByName.addToCache(idPNameCacheKey, new IdPCacheEntry(identityProvider));
        } else {
            log.debug("Entry for Identity Provider with Home Realm ID " + realmId
                    + " not found in cache or DB");
        }

        return identityProvider;
    }

    /**
     * Adds a new Identity Provider and cache it.
     *
     * @param identityProvider  new Identity Provider information.
     * @param tenantId          tenant ID of IDP.
     * @param tenantDomain      tenant domain of IDP.
     * @throws IdentityProviderManagementException IdentityProviderManagementException
     */
    public String addIdP(IdentityProvider identityProvider, int tenantId, String
            tenantDomain) throws IdentityProviderManagementException {

        return idPMgtDAO.addIdPWithResourceId(identityProvider, tenantId);
    }

    /**
     * @param newIdentityProvider     New Identity Provider information.
     * @param currentIdentityProvider Current Identity Provider information.
     * @param tenantId                Tenant ID of IDP.
     * @param tenantDomain            Tenant Domain of IDP.
     * @throws IdentityProviderManagementException IdentityProviderManagementException
     */
    public void updateIdP(IdentityProvider newIdentityProvider,
                                      IdentityProvider currentIdentityProvider, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Removing entry for Identity Provider "
                    + currentIdentityProvider.getIdentityProviderName() + " from cache");
        }
        clearIdpCache(currentIdentityProvider.getIdentityProviderName(), currentIdentityProvider.getResourceId(),
                tenantId, tenantDomain);
        idPMgtDAO.updateIdPWithResourceId(currentIdentityProvider.getResourceId(),
                newIdentityProvider, currentIdentityProvider, tenantId);
    }

    /**
     * @param idPName
     * @param tenantId
     * @param tenantDomain
     * @throws IdentityProviderManagementException
     */
    public void deleteIdP(String idPName, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        if (idPMgtDAO.isIdpReferredBySP(idPName, tenantId)) {
            throw new IdentityProviderManagementException("Identity Provider '" + idPName + "' " +
                    "cannot be deleted as it is referred by Service Providers.");
        }

        IdentityProvider identityProvider = this.getIdPByName(null, idPName, tenantId, tenantDomain);
        if (identityProvider != null) {
            idPMgtDAO.deleteIdP(idPName, tenantId, tenantDomain);
            clearIdpCache(idPName, tenantId, tenantDomain);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("IDP:%s of tenantDomain:%s is not found is cache or DB", idPName, tenantDomain));
            }
        }



    }

    /**
     * @param resourceId
     * @param tenantId
     * @param tenantDomain
     * @throws IdentityProviderManagementException
     */
    public void deleteIdPByResourceId(String resourceId, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        IdentityProvider identityProvider = this.getIdPByResourceId(resourceId, tenantId, tenantDomain);
        if (identityProvider != null) {
            String idPName = identityProvider.getIdentityProviderName();
            if (idPMgtDAO.isIdpReferredBySP(idPName, tenantId)) {
                String data = "Identity Provider '" + idPName + "' cannot be deleted as it is referred by Service " +
                        "Providers.";
                throw IdPManagementUtil.handleClientException(IdPManagementConstants.ErrorMessage
                        .ERROR_CODE_DELETE_IDP, data);
            }

            idPMgtDAO.deleteIdPByResourceId(resourceId, tenantId, tenantDomain);
            clearIdpCache(idPName, resourceId, tenantId, tenantDomain);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("IDP with resource ID: %s of tenantDomain:%s is not found is cache or DB",
                                resourceId, tenantDomain));
            }
        }
    }

    public void forceDeleteIdP(String idPName, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Force deleting IDP:%s of tenantDomain:%s started.", idPName, tenantDomain));
        }

        // Remove cache entries related to the force deleted idps.
        IdentityProvider identityProvider = this.getIdPByName(null, idPName, tenantId, tenantDomain);
        if (identityProvider != null) {
            idPMgtDAO.forceDeleteIdP(idPName, tenantId, tenantDomain);
            clearIdpCache(idPName, tenantId, tenantDomain);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("IDP:%s of tenantDomain:%s is not found is cache or DB", idPName, tenantDomain));
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Force deleting IDP:%s of tenantDomain:%s completed.", idPName,
                    tenantDomain));
        }
    }

    public void forceDeleteIdPByResourceId(String resourceId, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Force deleting IDP with resource ID:%s of tenantDomain:%s started.", resourceId,
                    tenantDomain));
        }

        // Remove cache entries related to the force deleted idps.
        IdentityProvider identityProvider = this.getIdPByResourceId(resourceId, tenantId, tenantDomain);
        if (identityProvider != null) {
            idPMgtDAO.forceDeleteIdPByResourceId(resourceId, tenantId, tenantDomain);
            clearIdpCache(identityProvider.getIdentityProviderName(), resourceId, tenantId, tenantDomain);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("IDP with resource ID:%s of tenantDomain:%s is not found is cache or DB",
                        resourceId, tenantDomain));
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Force deleting IDP with resource ID:%s of tenantDomain:%s completed.", resourceId,
                    tenantDomain));
        }
    }

    /**
     * Add new cache entries for IDP against cache keys: name, home-realm-id and resource-id.
     *
     * @param identityProvider  Identity Provider information.
     * @param tenantDomain      Tenant domain of IDP.
     */
    public void addIdPCache(IdentityProvider identityProvider, String tenantDomain) {

        if (identityProvider != null) {
            if (log.isDebugEnabled()) {
                log.debug("Adding new entry for Identity Provider: '" + identityProvider.getIdentityProviderName() +
                        "' to cache.");
            }
            IdPNameCacheKey idPNameCacheKey = new IdPNameCacheKey(
                    identityProvider.getIdentityProviderName(), tenantDomain);
            idPCacheByName.addToCache(idPNameCacheKey, new IdPCacheEntry(identityProvider));
            if (identityProvider.getHomeRealmId() != null) {
                IdPHomeRealmIdCacheKey idPHomeRealmIdCacheKey = new IdPHomeRealmIdCacheKey(
                        identityProvider.getHomeRealmId(), tenantDomain);
                idPCacheByHRI.addToCache(idPHomeRealmIdCacheKey,
                        new IdPCacheEntry(identityProvider));
            }
            IdPResourceIdCacheKey idPResourceIdCacheKey = new IdPResourceIdCacheKey(identityProvider.getResourceId());
            idPCacheByResourceId.addToCache(idPResourceIdCacheKey, new IdPCacheEntry(identityProvider));
        }
    }

    public void clearIdpCache(String idPName, int tenantId, String tenantDomain) throws IdentityProviderManagementException {

        clearIdpCache(idPName, null, tenantId, tenantDomain);
    }

    public void clearIdpCache(String idPName, String resourceId, int tenantId, String tenantDomain) throws
            IdentityProviderManagementException {


        // clearing cache entries related to the IDP.
        IdentityProvider identityProvider;
        if (StringUtils.isNotBlank(resourceId)) {
            identityProvider = this.getIdPByResourceId(resourceId, tenantId, tenantDomain);
        } else {
            identityProvider = this.getIdPByName(null, idPName, tenantId, tenantDomain);
        }
        if (identityProvider != null) {
            if (log.isDebugEnabled()) {
                log.debug("Removing entry for Identity Provider " + idPName + " of tenantDomain:" + tenantDomain +
                        " from cache.");
            }

            IdPNameCacheKey idPNameCacheKey = new IdPNameCacheKey(idPName, tenantDomain);
            idPCacheByName.clearCacheEntry(idPNameCacheKey);

            if (identityProvider.getHomeRealmId() != null) {
                IdPHomeRealmIdCacheKey idPHomeRealmIdCacheKey = new IdPHomeRealmIdCacheKey(
                        identityProvider.getHomeRealmId(), tenantDomain);
                idPCacheByHRI.clearCacheEntry(idPHomeRealmIdCacheKey);
            }
            if (StringUtils.isNotBlank(resourceId)) {
                IdPResourceIdCacheKey idPResourceIdCacheKey = new IdPResourceIdCacheKey(resourceId);
                idPCacheByResourceId.clearCacheEntry(idPResourceIdCacheKey);
            }
        } else {
            log.debug("Entry for Identity Provider " + idPName + " not found in cache or DB");
        }
    }



    /**
     * @param tenantId
     * @param role
     * @param tenantDomain
     * @throws IdentityProviderManagementException
     */
    public void deleteTenantRole(int tenantId, String role, String tenantDomain)
            throws IdentityProviderManagementException {

        log.debug("Removing all cached Identity Provider entries for tenant Domain " + tenantDomain);
        List<IdentityProvider> identityProviders = this.getIdPs(null, tenantId,
                tenantDomain);
        for (IdentityProvider identityProvider : identityProviders) {
            identityProvider = this.getIdPByName(null, identityProvider.getIdentityProviderName(),
                    tenantId, tenantDomain);
            IdPNameCacheKey idPNameCacheKey = new IdPNameCacheKey(
                    identityProvider.getIdentityProviderName(), tenantDomain);
            idPCacheByName.clearCacheEntry(idPNameCacheKey);
            if (identityProvider.getHomeRealmId() != null) {
                IdPHomeRealmIdCacheKey idPHomeRealmIdCacheKey = new IdPHomeRealmIdCacheKey(
                        identityProvider.getHomeRealmId(), tenantDomain);
                idPCacheByHRI.clearCacheEntry(idPHomeRealmIdCacheKey);
            }
        }

        idPMgtDAO.deleteTenantRole(tenantId, role, tenantDomain);
    }

    /**
     * @param newRoleName
     * @param oldRoleName
     * @param tenantId
     * @param tenantDomain
     * @throws IdentityProviderManagementException
     */
    public void renameTenantRole(String newRoleName, String oldRoleName, int tenantId,
                                 String tenantDomain) throws IdentityProviderManagementException {

        log.debug("Removing all cached Identity Provider entries for tenant Domain " + tenantDomain);
        List<IdentityProvider> identityProviders = this.getIdPs(null, tenantId,
                tenantDomain);
        for (IdentityProvider identityProvider : identityProviders) {
            identityProvider = this.getIdPByName(null, identityProvider.getIdentityProviderName(),
                    tenantId, tenantDomain);
            IdPNameCacheKey idPNameCacheKey = new IdPNameCacheKey(
                    identityProvider.getIdentityProviderName(), tenantDomain);
            idPCacheByName.clearCacheEntry(idPNameCacheKey);
            if (identityProvider.getHomeRealmId() != null) {
                IdPHomeRealmIdCacheKey idPHomeRealmIdCacheKey = new IdPHomeRealmIdCacheKey(
                        identityProvider.getHomeRealmId(), tenantDomain);
                idPCacheByHRI.clearCacheEntry(idPHomeRealmIdCacheKey);
            }
        }

        idPMgtDAO.renameTenantRole(newRoleName, oldRoleName, tenantId, tenantDomain);
    }

    /**
     * @param tenantId
     * @param claimURI
     * @param tenantDomain
     * @throws IdentityProviderManagementException
     */
    public void deleteTenantClaimURI(int tenantId, String claimURI, String tenantDomain)
            throws IdentityProviderManagementException {

        log.debug("Removing all cached Identity Provider entries for tenant Domain " + tenantDomain);
        List<IdentityProvider> identityProviders = this.getIdPs(null, tenantId,
                tenantDomain);
        for (IdentityProvider identityProvider : identityProviders) {
            identityProvider = this.getIdPByName(null, identityProvider.getIdentityProviderName(),
                    tenantId, tenantDomain);
            IdPNameCacheKey idPNameCacheKey = new IdPNameCacheKey(
                    identityProvider.getIdentityProviderName(), tenantDomain);
            idPCacheByName.clearCacheEntry(idPNameCacheKey);
            if (identityProvider.getHomeRealmId() != null) {
                IdPHomeRealmIdCacheKey idPHomeRealmIdCacheKey = new IdPHomeRealmIdCacheKey(
                        identityProvider.getHomeRealmId(), tenantDomain);
                idPCacheByHRI.clearCacheEntry(idPHomeRealmIdCacheKey);
            }
        }

        idPMgtDAO.deleteTenantRole(tenantId, claimURI, tenantDomain);
    }

    /**
     * @param newClaimURI
     * @param oldClaimURI
     * @param tenantId
     * @param tenantDomain
     * @throws IdentityProviderManagementException
     */
    public void renameTenantClaimURI(String newClaimURI, String oldClaimURI, int tenantId,
                                     String tenantDomain) throws IdentityProviderManagementException {

        log.debug("Removing all cached Identity Provider entries for tenant Domain " + tenantDomain);
        List<IdentityProvider> identityProviders = this.getIdPs(null, tenantId,
                tenantDomain);
        for (IdentityProvider identityProvider : identityProviders) {
            identityProvider = this.getIdPByName(null, identityProvider.getIdentityProviderName(),
                    tenantId, tenantDomain);
            IdPNameCacheKey idPNameCacheKey = new IdPNameCacheKey(
                    identityProvider.getIdentityProviderName(), tenantDomain);
            idPCacheByName.clearCacheEntry(idPNameCacheKey);
            if (identityProvider.getHomeRealmId() != null) {
                IdPHomeRealmIdCacheKey idPHomeRealmIdCacheKey = new IdPHomeRealmIdCacheKey(
                        identityProvider.getHomeRealmId(), tenantDomain);
                idPCacheByHRI.clearCacheEntry(idPHomeRealmIdCacheKey);
            }
        }

        idPMgtDAO.renameTenantRole(newClaimURI, oldClaimURI, tenantId, tenantDomain);
    }

    /**
     * @param idPEntityId
     * @param tenantId
     * @return
     * @throws IdentityProviderManagementException
     */
    public boolean isIdPAvailableForAuthenticatorProperty(String authenticatorName, String propertyName, String idPEntityId, int tenantId)
            throws IdentityProviderManagementException {

        return idPMgtDAO.isIdPAvailableForAuthenticatorProperty(authenticatorName, propertyName, idPEntityId, tenantId);
    }

    /**
     * Retrieve Identity provider connected applications.
     *
     * @param resourceId Resource ID.
     * @param limit      Limit parameter for pagination.
     * @param offset     Offset parameter for pagination.
     * @return ConnectedAppsResult.
     * @throws IdentityProviderManagementException IdentityProviderManagementException.
     */
    public ConnectedAppsResult getConnectedApplications(String resourceId, int limit, int offset) throws
            IdentityProviderManagementException {

        return idPMgtDAO.getConnectedApplications(resourceId, limit, offset);
    }

}
