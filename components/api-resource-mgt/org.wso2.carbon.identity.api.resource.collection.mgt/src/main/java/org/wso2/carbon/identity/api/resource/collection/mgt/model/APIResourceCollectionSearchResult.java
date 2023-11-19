package org.wso2.carbon.identity.api.resource.collection.mgt.model;

import java.util.List;

/**
 * API Resource Collection Search Result.
 */
public class APIResourceCollectionSearchResult {
   private int totalCount;
   List<APIResourceCollection> apiResourceCollections;

    public APIResourceCollectionSearchResult(List<APIResourceCollection> apiResourceCollections) {

        this.apiResourceCollections = apiResourceCollections;
        this.totalCount = apiResourceCollections.size();
    }

    public int getTotalCount() {

        return totalCount;
    }

    public List<APIResourceCollection> getAPIResourceCollections() {

        return apiResourceCollections;
    }

    public void setAPIResourceCollections(List<APIResourceCollection> apiResourceCollectionBasicInfoList) {

        this.apiResourceCollections = apiResourceCollectionBasicInfoList;
    }
}
