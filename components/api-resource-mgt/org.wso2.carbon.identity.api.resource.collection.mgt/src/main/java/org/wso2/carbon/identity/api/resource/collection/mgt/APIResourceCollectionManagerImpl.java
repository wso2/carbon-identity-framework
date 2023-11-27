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

import org.apache.commons.collections.CollectionUtils;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    public APIResourceCollectionSearchResult getAPIResourceCollections(String filter, List<String> requiredAttributes,
                                                                       String tenantDomain)
            throws APIResourceCollectionMgtException {

        Map<String, APIResourceCollection> filteredMap =
                FilterUtil.filterAPIResourceCollections(apiResourceCollectionMap, filter);
        if (requiredAttributes != null &&
                requiredAttributes.contains(APIResourceCollectionManagementConstants.API_RESOURCES)) {
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

        if (collection == null) {
            return null;
        }
        try {
            APIResourceCollection clonedCollection = cloneAPIResourceCollection(collection);

            // Combine read and write scopes for a single fetch.
            Set<String> combinedScopes = new HashSet<>();
            Optional.ofNullable(clonedCollection.getReadScopes()).ifPresent(combinedScopes::addAll);
            Optional.ofNullable(clonedCollection.getWriteScopes()).ifPresent(combinedScopes::addAll);
            List<APIResource> allAPIResources = APIResourceCollectionMgtServiceDataHolder.getInstance()
                    .getAPIResourceManagementService().getScopeMetadata(new ArrayList<>(combinedScopes), tenantDomain);

            List<APIResource> readAPIResources =
                    filterAPIResources(allAPIResources, clonedCollection.getReadScopes());
            List<APIResource> writeAPIResources =
                    filterAPIResources(allAPIResources, new ArrayList<>(combinedScopes));

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

    private List<APIResource> filterAPIResources(List<APIResource> apiResources, List<String> scopes) {

        if (CollectionUtils.isEmpty(scopes)) {
            return Collections.emptyList();
        }
        Set<String> scopeSet = new HashSet<>(scopes);
        return apiResources.stream()
                .filter(apiResource ->
                        apiResource.getScopes().stream().anyMatch(scope -> scopeSet.contains(scope.getName()))
                       )
                .map(apiResource -> filterAPIResourceScopes(apiResource, scopeSet))
                .collect(Collectors.toList());
    }

    private APIResource filterAPIResourceScopes(APIResource apiResource, Set<String> allowedScopes) {

        APIResource clonedResource = cloneAPIResource(apiResource);
        if (clonedResource.getScopes() != null) {
            List<Scope> filteredScopes = clonedResource.getScopes().stream()
                    .filter(scope -> allowedScopes.contains(scope.getName()))
                    .collect(Collectors.toList());
            clonedResource.setScopes(filteredScopes);
        } else {
            clonedResource.setScopes(Collections.emptyList());
        }
        return clonedResource;
    }

    private APIResource cloneAPIResource(APIResource apiResource) {

        return new APIResource.APIResourceBuilder()
                .id(apiResource.getId())
                .name(apiResource.getName())
                .type(apiResource.getType())
                .identifier(apiResource.getIdentifier())
                .description(apiResource.getDescription())
                .scopes(apiResource.getScopes())
                .build();
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
