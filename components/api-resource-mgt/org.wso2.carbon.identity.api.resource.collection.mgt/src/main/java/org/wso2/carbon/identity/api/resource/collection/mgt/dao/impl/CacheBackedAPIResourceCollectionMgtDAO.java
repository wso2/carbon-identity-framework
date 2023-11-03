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

package org.wso2.carbon.identity.api.resource.collection.mgt.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.api.resource.collection.mgt.APIResourceCollectionManager;
import org.wso2.carbon.identity.api.resource.collection.mgt.cache.APIResourceCollectionCacheById;
import org.wso2.carbon.identity.api.resource.collection.mgt.cache.APIResourceCollectionCacheEntry;
import org.wso2.carbon.identity.api.resource.collection.mgt.cache.APIResourceCollectionIdCacheKey;
import org.wso2.carbon.identity.api.resource.collection.mgt.dao.APIResourceCollectionManagementDAO;
import org.wso2.carbon.identity.api.resource.collection.mgt.exception.APIResourceCollectionMgtException;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollection;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollectionBasicInfo;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollectionSearchResult;
import org.wso2.carbon.identity.core.model.ExpressionNode;

import java.util.List;

/**
 * This class implements the {@link APIResourceCollectionManager} interface.
 */
public class CacheBackedAPIResourceCollectionMgtDAO implements APIResourceCollectionManagementDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedAPIResourceCollectionMgtDAO.class);
    private final APIResourceCollectionCacheById apiResourceCollectionCacheById;
    private final APIResourceCollectionManagementDAO apiResourceCollectionManagementDAO;

    public CacheBackedAPIResourceCollectionMgtDAO(APIResourceCollectionManagementDAO apiResourceCollectionManagementDAO) {

        this.apiResourceCollectionManagementDAO = apiResourceCollectionManagementDAO;
        apiResourceCollectionCacheById = APIResourceCollectionCacheById.getInstance();
    }

    /**
     * Get API resource collections under given tenantId.
     *
     * @param expressionNode Expression nodes.
     * @return List of <code>APIResourceCollection</code>
     * @throws APIResourceCollectionMgtException If an error occurs while retrieving the API resource collections.
     */
    @Override
    public List<APIResourceCollectionBasicInfo> getAPIResourceCollections(List<ExpressionNode> expressionNode,
                                                                          Integer tenantId)
            throws APIResourceCollectionMgtException {

        return apiResourceCollectionManagementDAO.getAPIResourceCollections(expressionNode, tenantId);
    }

    /**
     * Get API resource collection by collectionId.
     *
     * @param collectionId ID of the API resource collection.
     * @return APIResourceCollection.
     * @throws APIResourceCollectionMgtException If an error occurs while retrieving the API resource collection.
     */
    @Override
    public APIResourceCollection getAPIResourceCollectionById(String collectionId, Integer tenantId)
            throws APIResourceCollectionMgtException {

        APIResourceCollectionIdCacheKey cacheKey = new APIResourceCollectionIdCacheKey(collectionId);
        APIResourceCollectionCacheEntry cacheEntry =
                apiResourceCollectionCacheById.getValueFromCache(cacheKey, tenantId);

        if (cacheEntry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for API Resource " + collectionId);
            }
            return cacheEntry.getAPIResourceCollection();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for API Resource " + collectionId + ". Fetching entry from DB");
        }

        APIResourceCollection apiResourceCollection = apiResourceCollectionManagementDAO
                .getAPIResourceCollectionById(collectionId, tenantId);

        if (apiResourceCollection != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry fetched from DB for API Resource " + collectionId + ". Updating cache");
            }
            apiResourceCollectionCacheById.addToCache(cacheKey,
                    new APIResourceCollectionCacheEntry(apiResourceCollection),
                    tenantId);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry not found in DB for API Resource " + collectionId);
            }
        }
        return apiResourceCollection;
    }
}
