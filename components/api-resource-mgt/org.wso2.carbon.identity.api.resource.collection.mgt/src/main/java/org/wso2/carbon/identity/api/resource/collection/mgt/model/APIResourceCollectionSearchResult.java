package org.wso2.carbon.identity.api.resource.collection.mgt.model;

import java.util.List;

/**
 * API Resource Collection Search Result.
 */
public class APIResourceCollectionSearchResult {
   private int totalCount;
   List<APIResourceCollectionBasicInfo> apiResourceCollections;

    public APIResourceCollectionSearchResult(List<APIResourceCollectionBasicInfo> apiResourceCollections) {

        this.apiResourceCollections = apiResourceCollections;
        this.totalCount = apiResourceCollections.size();
    }

    public int getTotalCount() {

        return totalCount;
    }

    public void setTotalCount(int totalCount) {

        this.totalCount = totalCount;
    }

    public List<APIResourceCollectionBasicInfo> getAPIResourceCollections() {

        return apiResourceCollections;
    }

    public void setAPIResourceCollections(List<APIResourceCollectionBasicInfo> apiResourceCollectionBasicInfoList) {

        this.apiResourceCollections = apiResourceCollectionBasicInfoList;
    }

}
