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
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollectionBasicInfo;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollectionSearchResult;
import org.wso2.carbon.identity.api.resource.collection.mgt.util.APIResourceCollectionMgtConfigBuilder;

import java.util.List;
import java.util.Map;

/**
 * API Resource Collection Manager Implementation.
 */
public class APIResourceCollectionManagerImpl implements APIResourceCollectionManager {

    private static final APIResourceCollectionManagerImpl INSTANCE = new APIResourceCollectionManagerImpl();
    private static final APIResourceCollectionMgtConfigBuilder configBuilder =
            APIResourceCollectionMgtConfigBuilder.getInstance();
    private static final Map<String, APIResourceCollectionBasicInfo> apiResourceCollectionsBasicInfoMap =
            configBuilder.getApiResourceCollectionMgtConfigurations();

    private APIResourceCollectionManagerImpl () {

    }

    public static APIResourceCollectionManagerImpl getInstance() {

        return INSTANCE;
    }

    /**
     * Get API Resource Collections.
     *
     * @param filter Filter expression.
     * @return API resource collection search result.
     * @throws APIResourceCollectionMgtException If an error occurred while retrieving API Resource Collections.
     */
    @Override
    public APIResourceCollectionSearchResult getAPIResourceCollections(String filter)
            throws APIResourceCollectionMgtException {

        return new APIResourceCollectionSearchResult(
                (List<APIResourceCollectionBasicInfo>) apiResourceCollectionsBasicInfoMap.values());
    }

    /**
     * Get API Resource Collection by id.
     *
     * @param collectionId Collection id.
     * @return API resource collection.
     * @throws APIResourceCollectionMgtException If an error occurred while retrieving API Resource Collection.
     */
    @Override
    public APIResourceCollection getAPIResourceCollectionById(String collectionId)
            throws APIResourceCollectionMgtException {

        return null;
    }
}
