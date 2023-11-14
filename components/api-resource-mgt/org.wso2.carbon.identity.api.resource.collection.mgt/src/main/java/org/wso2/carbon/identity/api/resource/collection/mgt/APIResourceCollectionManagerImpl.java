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

import org.wso2.carbon.identity.api.resource.collection.mgt.constant.APIResourceCollectionManagementConstants;
import org.wso2.carbon.identity.api.resource.collection.mgt.exception.APIResourceCollectionMgtException;
import org.wso2.carbon.identity.api.resource.collection.mgt.internal.APIResourceCollectionMgtServiceDataHolder;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollection;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollectionSearchResult;
import org.wso2.carbon.identity.api.resource.collection.mgt.util.APIResourceCollectionMgtConfigBuilder;
import org.wso2.carbon.identity.api.resource.collection.mgt.util.FilterUtil;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.Scope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.api.resource.collection.mgt.util.APIResourceCollectionManagementUtil.convertListToMap;
import static org.wso2.carbon.identity.api.resource.collection.mgt.util.APIResourceCollectionManagementUtil.handleServerException;

/**
 * API Resource Collection Manager Implementation.
 */
public class APIResourceCollectionManagerImpl implements APIResourceCollectionManager {

    private static final APIResourceCollectionManagerImpl INSTANCE;
    private static final APIResourceCollectionMgtConfigBuilder configBuilder;
    private static Map<String, APIResourceCollection> apiResourceCollectionMap;

    static {
        configBuilder = APIResourceCollectionMgtConfigBuilder.getInstance();
        apiResourceCollectionMap = configBuilder.getApiResourceCollectionMgtConfigurations();
        INSTANCE = new APIResourceCollectionManagerImpl();
    }

    private APIResourceCollectionManagerImpl() {
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
                FilterUtil.filterAPIResourceCollections(apiResourceCollectionMap, filter));
    }

    /**
     * Get API Resource Collections.
     *
     * @param filter Filter expression.
     * @return API resource collection search result.
     * @throws APIResourceCollectionMgtException If an error occurred while retrieving API Resource Collections.
     */
    @Override
    public APIResourceCollectionSearchResult
    getAPIResourceCollectionsWithRequiredAttributes(String filter, List<String> requiredAttributes, String tenantDomain)
            throws APIResourceCollectionMgtException {

        List<APIResourceCollection> filteredList =
                FilterUtil.filterAPIResourceCollections(apiResourceCollectionMap, filter);
        Map<String, APIResourceCollection> filteredMap = convertListToMap(filteredList);

        if (requiredAttributes.contains(APIResourceCollectionManagementConstants.API_RESOURCES)) {
            for (Map.Entry<String, APIResourceCollection> entry : filteredMap.entrySet()) {
                entry.setValue(populateAPIResourcesForCollection(entry.getValue(), tenantDomain));
            }
        }
        return new APIResourceCollectionSearchResult(new ArrayList<>(filteredMap.values()));
    }

    /**
     * Get API Resource Collection by id.
     *
     * @param collectionId Collection id.
     * @return API resource collection.
     * @throws APIResourceCollectionMgtException If an error occurred while retrieving API Resource Collection.
     */
    @Override
    public APIResourceCollection getAPIResourceCollectionById(String collectionId, String tenantDomain)
            throws APIResourceCollectionMgtException {

        APIResourceCollection apiResourceCollection = apiResourceCollectionMap.get(collectionId);
        if (apiResourceCollection == null) {
            return null;
        }
        return populateAPIResourcesForCollection(apiResourceCollection, tenantDomain);
    }

    /**
     * Populate API resources for the given API resource collection.
     *
     * @param collection   API resource collection.
     * @param tenantDomain Tenant domain.
     * @throws APIResourceCollectionMgtException If an error occurred while retrieving API resource metadata.
     */
    private APIResourceCollection populateAPIResourcesForCollection(APIResourceCollection collection,
                                                                    String tenantDomain)
            throws APIResourceCollectionMgtException {

        try {
            APIResourceCollection clonedCollection = cloneAPIResourceCollection(collection);
            // Fetch and filter read API resources.
            List<APIResource> readAPIResources = APIResourceCollectionMgtServiceDataHolder.getInstance()
                    .getAPIResourceManagementService().getScopeMetadata(clonedCollection.getReadScopes(), tenantDomain);
            filterAndSetScopes(readAPIResources, clonedCollection,
                    APIResourceCollectionManagementConstants.READ_SCOPES);

            // Fetch and filter write API resources.
            List<APIResource> writeAPIResources = APIResourceCollectionMgtServiceDataHolder.getInstance()
                    .getAPIResourceManagementService()
                    .getScopeMetadata(clonedCollection.getWriteScopes(), tenantDomain);
            filterAndSetScopes(writeAPIResources, clonedCollection,
                    APIResourceCollectionManagementConstants.WRITE_SCOPES);

            // Set read and write resources.
            Map<String, List<APIResource>> apiResourcesMap = new HashMap<>();
            apiResourcesMap.put(APIResourceCollectionManagementConstants.READ, readAPIResources);
            apiResourcesMap.put(APIResourceCollectionManagementConstants.WRITE, writeAPIResources);
            clonedCollection.setApiResources(apiResourcesMap);
            return clonedCollection;
        } catch (APIResourceMgtException e) {
            throw handleServerException(
                    APIResourceCollectionManagementConstants.ErrorMessages
                            .ERROR_CODE_ERROR_WHILE_RETRIEVING_SCOPE_METADATA,
                    e);
        }
    }

    /**
     * Filter scopes of API resources based on the scopes defined in the API resource collection.
     *
     * @param apiResources          List of API resources.
     * @param apiResourceCollection API resource collection.
     * @param scopeType             Scope type.
     */
    private void filterAndSetScopes(List<APIResource> apiResources, APIResourceCollection apiResourceCollection,
                                    String scopeType) {

        if (apiResources == null || apiResources.isEmpty()) {
            return;
        }
        Set<String> scopeSet = getScopeSet(apiResourceCollection, scopeType);
        if (!scopeSet.isEmpty()) {
            filterAPIResourceScopes(apiResources, scopeSet);
        }
    }

    private Set<String> getScopeSet(APIResourceCollection apiResourceCollection, String scopeType) {

        List<String> scopes = APIResourceCollectionManagementConstants.READ_SCOPES.equals(scopeType) ?
                apiResourceCollection.getReadScopes() : apiResourceCollection.getWriteScopes();
        return scopes == null ? Collections.emptySet() : new HashSet<>(scopes);
    }

    private void filterAPIResourceScopes(List<APIResource> apiResources, Set<String> allowedScopes) {

        apiResources.forEach(apiResource -> {
            if (apiResource.getScopes() != null) {
                List<Scope> filteredScopes = apiResource.getScopes().stream()
                        .filter(scope -> allowedScopes.contains(scope.getName()))
                        .collect(Collectors.toList());
                apiResource.setScopes(filteredScopes);
            } else {
                apiResource.setScopes(Collections.emptyList());
            }
        });
    }

    private APIResourceCollection cloneAPIResourceCollection(APIResourceCollection apiResourceCollection) {

        return new APIResourceCollection.APIResourceCollectionBuilder()
                .id(apiResourceCollection.getId())
                .name(apiResourceCollection.getName())
                .displayName(apiResourceCollection.getDisplayName())
                .type(apiResourceCollection.getType())
                .readScopes(apiResourceCollection.getReadScopes())
                .writeScopes(apiResourceCollection.getWriteScopes())
                .apiResources(apiResourceCollection.getApiResources())
                .build();
    }
}
