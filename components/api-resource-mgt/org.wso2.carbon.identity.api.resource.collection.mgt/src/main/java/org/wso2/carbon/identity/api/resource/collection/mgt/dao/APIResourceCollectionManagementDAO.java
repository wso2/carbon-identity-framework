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

package org.wso2.carbon.identity.api.resource.collection.mgt.dao;

import org.wso2.carbon.identity.api.resource.collection.mgt.exception.APIResourceCollectionMgtException;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollection;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollectionBasicInfo;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollectionSearchResult;
import org.wso2.carbon.identity.core.model.ExpressionNode;

import java.util.List;

/**
 * This interface performs CRUD operations for {@link APIResourceCollection}
 */
public interface APIResourceCollectionManagementDAO {

    /**
     * Get API resource collections under given tenantId.
     *
     * @param expressionNode Expression nodes.
     * @return List of <code>APIResourceCollection</code>
     * @throws APIResourceCollectionMgtException If an error occurs while retrieving the API resource collections.
     */
    List<APIResourceCollectionBasicInfo> getAPIResourceCollections(List<ExpressionNode> expressionNode, Integer tenantId)
            throws APIResourceCollectionMgtException;

    /**
     * Get API resource collection by collectionId.
     *
     * @param collectionId ID of the API resource collection.
     * @return APIResourceCollection.
     * @throws APIResourceCollectionMgtException If an error occurs while retrieving the API resource collection.
     */
    APIResourceCollection getAPIResourceCollectionById(String collectionId, Integer tenantId)
            throws APIResourceCollectionMgtException;
}
