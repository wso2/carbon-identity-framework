/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.api.resource.mgt.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.api.resource.mgt.cache.APIResourceCacheById;
import org.wso2.carbon.identity.api.resource.mgt.cache.APIResourceCacheEntry;
import org.wso2.carbon.identity.api.resource.mgt.cache.APIResourceIdCacheKey;
import org.wso2.carbon.identity.api.resource.mgt.dao.AuthorizationDetailsTypeMgtDAO;
import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;
import org.wso2.carbon.identity.core.model.ExpressionNode;

import java.sql.Connection;
import java.util.List;

/**
 * Cache-backed implementation of the {@link AuthorizationDetailsTypeMgtDAO} interface for
 * managing Authorization Detail Types with improved performance through caching.
 *
 * <p>This class wraps a standard {@link AuthorizationDetailsTypeMgtDAOImpl} implementation
 * and provides caching functionality to reduce the number of database lookups for commonly
 * requested authorization detail types. It uses a cache to store frequently accessed data,
 * improving the performance of read operations by avoiding redundant queries to the database.</p>
 */
public class CacheBackedAuthorizationDetailsTypeMgtDAOImpl implements AuthorizationDetailsTypeMgtDAO {

    private static final Log log = LogFactory.getLog(CacheBackedAuthorizationDetailsTypeMgtDAOImpl.class);
    private final AuthorizationDetailsTypeMgtDAO authorizationDetailsTypeMgtDAO;
    private final APIResourceCacheById apiResourceCacheById;

    public CacheBackedAuthorizationDetailsTypeMgtDAOImpl(final AuthorizationDetailsTypeMgtDAO typeMgtDAO) {

        this.authorizationDetailsTypeMgtDAO = typeMgtDAO;
        apiResourceCacheById = APIResourceCacheById.getInstance();
    }

    @Override
    public List<AuthorizationDetailsType> addAuthorizationDetailsTypes(
            final String apiId, final List<AuthorizationDetailsType> authorizationDetailsTypes, final Integer tenantId)
            throws APIResourceMgtException {

        this.clearAPIResourceCache(apiId, tenantId);
        return this.authorizationDetailsTypeMgtDAO
                .addAuthorizationDetailsTypes(apiId, authorizationDetailsTypes, tenantId);
    }

    @Override
    public List<AuthorizationDetailsType> addAuthorizationDetailsTypes(
            final Connection connection, final String apiId,
            final List<AuthorizationDetailsType> authorizationDetailsTypes,
            final Integer tenantId) throws APIResourceMgtException {

        this.clearAPIResourceCache(apiId, tenantId);
        return this.authorizationDetailsTypeMgtDAO
                .addAuthorizationDetailsTypes(connection, apiId, authorizationDetailsTypes, tenantId);
    }

    @Override
    public void deleteAuthorizationDetailsTypeByApiIdAndType(final String apiId, final String type,
                                                             final Integer tenantId) throws APIResourceMgtException {
        this.clearAPIResourceCache(apiId, tenantId);
        this.authorizationDetailsTypeMgtDAO.deleteAuthorizationDetailsTypeByApiIdAndType(apiId, type, tenantId);

    }

    @Override
    public void deleteAuthorizationDetailsTypeByApiIdAndTypeId(final String apiId, final String typeId,
                                                               final Integer tenantId) throws APIResourceMgtException {
        this.clearAPIResourceCache(apiId, tenantId);
        this.authorizationDetailsTypeMgtDAO.deleteAuthorizationDetailsTypeByApiIdAndTypeId(apiId, typeId, tenantId);

    }

    @Override
    public void deleteAuthorizationDetailsTypesByApiId(final String apiId, final Integer tenantId)
            throws APIResourceMgtException {

        this.clearAPIResourceCache(apiId, tenantId);
        this.authorizationDetailsTypeMgtDAO.deleteAuthorizationDetailsTypesByApiId(apiId, tenantId);
    }

    @Override
    public void deleteAuthorizationDetailsTypesByApiId(final Connection connection, final String apiId,
                                                       final Integer tenantId) throws APIResourceMgtException {

        this.clearAPIResourceCache(apiId, tenantId);
        this.authorizationDetailsTypeMgtDAO.deleteAuthorizationDetailsTypesByApiId(connection, apiId, tenantId);
    }

    @Override
    public AuthorizationDetailsType getAuthorizationDetailsTypeByApiIdAndType(final String apiId, final String type,
                                                                              final Integer tenantId)
            throws APIResourceMgtException {

        return this.authorizationDetailsTypeMgtDAO.getAuthorizationDetailsTypeByApiIdAndType(apiId, type, tenantId);
    }

    @Override
    public AuthorizationDetailsType getAuthorizationDetailsTypeByApiIdAndTypeId(final String apiId, final String typeId,
                                                                                final Integer tenantId)
            throws APIResourceMgtException {

        return this.authorizationDetailsTypeMgtDAO.getAuthorizationDetailsTypeByApiIdAndTypeId(apiId, typeId, tenantId);
    }

    @Override
    public List<AuthorizationDetailsType> getAuthorizationDetailsTypes(final List<ExpressionNode> expressionNodes,
                                                                       final Integer tenantId)
            throws APIResourceMgtException {

        return this.authorizationDetailsTypeMgtDAO.getAuthorizationDetailsTypes(expressionNodes, tenantId);
    }

    @Override
    public List<AuthorizationDetailsType> getAuthorizationDetailsTypesByApiId(final String apiId,
                                                                              final Integer tenantId)
            throws APIResourceMgtException {

        APIResourceCacheEntry entry =
                apiResourceCacheById.getValueFromCache(new APIResourceIdCacheKey(apiId), tenantId);
        if (entry != null) {
            if (log.isDebugEnabled()) {
                log.debug("Cache entry found for API Resource " + apiId);
            }
            return entry.getAPIResource().getAuthorizationDetailsTypes();
        }
        if (log.isDebugEnabled()) {
            log.debug("Cache entry not found for API Resource " + apiId + ". Fetching entry from DB");
        }
        return this.authorizationDetailsTypeMgtDAO.getAuthorizationDetailsTypesByApiId(apiId, tenantId);
    }

    @Override
    public List<AuthorizationDetailsType> getAuthorizationDetailsTypesByApiId(final Connection connection,
                                                                              final String apiId,
                                                                              final Integer tenantId)
            throws APIResourceMgtException {

        APIResourceCacheEntry entry =
                apiResourceCacheById.getValueFromCache(new APIResourceIdCacheKey(apiId), tenantId);
        if (entry != null) {
            if (log.isDebugEnabled()) {
                log.debug("Cache entry found for API Resource " + apiId);
            }
            return entry.getAPIResource().getAuthorizationDetailsTypes();
        }
        if (log.isDebugEnabled()) {
            log.debug("Cache entry not found for API Resource " + apiId + ". Fetching entry from DB");
        }
        return this.authorizationDetailsTypeMgtDAO.getAuthorizationDetailsTypesByApiId(connection, apiId, tenantId);
    }

    @Override
    public boolean isAuthorizationDetailsTypeExists(final String apiId, final String type, final Integer tenantId)
            throws APIResourceMgtException {

        return this.authorizationDetailsTypeMgtDAO.isAuthorizationDetailsTypeExists(apiId, type, tenantId);
    }

    @Override
    public void updateAuthorizationDetailsTypes(final String apiId,
                                                final List<AuthorizationDetailsType> authorizationDetailsTypes,
                                                final Integer tenantId) throws APIResourceMgtException {

        this.clearAPIResourceCache(apiId, tenantId);
        this.authorizationDetailsTypeMgtDAO
                .updateAuthorizationDetailsTypes(apiId, authorizationDetailsTypes, tenantId);
    }

    @Override
    public void updateAuthorizationDetailsTypes(final Connection connection, final String apiId,
                                                final List<AuthorizationDetailsType> authorizationDetailsTypes,
                                                final Integer tenantId) throws APIResourceMgtException {

        this.clearAPIResourceCache(apiId, tenantId);
        this.authorizationDetailsTypeMgtDAO
                .updateAuthorizationDetailsTypes(connection, apiId, authorizationDetailsTypes, tenantId);
    }

    private void clearAPIResourceCache(String apiId, int tenantId) {

        if (log.isDebugEnabled()) {
            log.debug("Removing entry for API Id: " + apiId + " of tenantId:" + tenantId + " from cache.");
        }

        APIResourceIdCacheKey apiResourceIdCacheKey = new APIResourceIdCacheKey(apiId);
        apiResourceCacheById.clearCacheEntry(apiResourceIdCacheKey, tenantId);
    }
}
