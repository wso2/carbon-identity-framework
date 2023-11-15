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

package org.wso2.carbon.identity.api.resource.collection.mgt;

import org.wso2.carbon.identity.api.resource.collection.mgt.exception.APIResourceCollectionMgtException;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollection;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollectionSearchResult;

import java.util.List;

/**
 * API Resource Collection Manager Interface.
 */
public interface APIResourceCollectionManager {

    /**
     * Get API Resource Collections with required attributes.
     *
     * @param filter             Filter expression.
     * @param requiredAttributes Required attributes.
     * @return API resource collection search result with required attributes.
     * @throws APIResourceCollectionMgtException If an error occurred while retrieving API Resource Collections.
     */
    APIResourceCollectionSearchResult getAPIResourceCollections(String filter, List<String> requiredAttributes,
                                                                String tenantDomain)
            throws APIResourceCollectionMgtException;

    /**
     * Get API Resource Collection by id.
     *
     * @param collectionId Collection id.
     * @param tenantDomain Tenant domain.
     * @return API resource collection.
     * @throws APIResourceCollectionMgtException If an error occurred while retrieving API Resource Collection.
     */
    APIResourceCollection getAPIResourceCollectionById(String collectionId, String tenantDomain)
            throws APIResourceCollectionMgtException;
}
