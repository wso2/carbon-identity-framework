package org.wso2.carbon.identity.api.resource.collection.mgt.model;

import java.util.List;

public class APIResourceCollectionSearchResult {
   private int totalCount;
   List<APIResourceCollection> APIResourceCollections;

    public int getTotalCount() {

        return totalCount;
    }

    public void setTotalCount(int totalCount) {

        this.totalCount = totalCount;
    }

    public List<APIResourceCollection> getAPIResourceCollections() {

        return APIResourceCollections;
    }

    public void setAPIResourceCollections(List<APIResourceCollection> APIResourceCollections) {

        this.APIResourceCollections = APIResourceCollections;
    }

}
