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

package org.wso2.carbon.identity.api.resource.collection.mgt.model;

import org.wso2.carbon.identity.application.common.model.APIResource;

import java.util.List;
import java.util.Map;

/**
 * API Resource Collection.
 */
public class APIResourceCollection {

    private String id;
    private String name;
    private String displayName;
    private String type;
    private List<String> readScopes;
    private List<String> writeScopes;
    private List<String> legacyReadScopes;
    private List<String> legacyWriteScopes;
    private String viewFeatureScope;
    private String editFeatureScope;
    private Map<String, List<APIResource>> apiResources;

    public APIResourceCollection() {
    }

    public APIResourceCollection(APIResourceCollectionBuilder apiResourceCollectionBuilder) {

            this.id = apiResourceCollectionBuilder.id;
            this.name = apiResourceCollectionBuilder.name;
            this.displayName = apiResourceCollectionBuilder.displayName;
            this.type = apiResourceCollectionBuilder.type;
            this.apiResources = apiResourceCollectionBuilder.apiResources;
            this.readScopes = apiResourceCollectionBuilder.readScopes;
            this.writeScopes = apiResourceCollectionBuilder.writeScopes;
            this.legacyReadScopes = apiResourceCollectionBuilder.legacyReadScopes;
            this.legacyWriteScopes = apiResourceCollectionBuilder.legacyWriteScopes;
            this.viewFeatureScope = apiResourceCollectionBuilder.viewFeatureScope;
            this.editFeatureScope = apiResourceCollectionBuilder.editFeatureScope;
    }

    public String getId() {

        return id;
    }

    public String getName() {

        return name;
    }

    public String getDisplayName() {

        return displayName;
    }

    public String getType() {

        return type;
    }

    public Map<String, List<APIResource>> getApiResources() {

        return apiResources;
    }

    public void setApiResources(Map<String, List<APIResource>> apiResources) {

        this.apiResources = apiResources;
    }

    public List<String> getReadScopes() {

        return readScopes;
    }

    public void setReadScopes(List<String> readScopes) {

        this.readScopes = readScopes;
    }

    public List<String> getWriteScopes() {

        return writeScopes;
    }

    public void setWriteScopes(List<String> writeScopes) {

        this.writeScopes = writeScopes;
    }

    public List<String> getLegacyReadScopes() {

        return legacyReadScopes;
    }

    public void setLegacyReadScopes(List<String> legacyReadScopes) {

        this.legacyReadScopes = legacyReadScopes;
    }

    public List<String> getLegacyWriteScopes() {

        return legacyWriteScopes;
    }

    public void setLegacyWriteScopes(List<String> legacyWriteScopes) {

        this.legacyWriteScopes = legacyWriteScopes;
    }

    public String getViewFeatureScope() {

        return viewFeatureScope;
    }

    public void setViewFeatureScope(String viewFeatureScope) {

        this.viewFeatureScope = viewFeatureScope;
    }

    public String getEditFeatureScope() {

        return editFeatureScope;
    }

    public void setEditFeatureScope(String editFeatureScope) {

        this.editFeatureScope = editFeatureScope;
    }

    /**
     * Builder class for API Resource Collection.
     */
    public static class APIResourceCollectionBuilder {

        private String id;
        private String name;
        private String displayName;
        private String type;
        private List<String> readScopes;
        private List<String> writeScopes;
        private List<String> legacyReadScopes;
        private List<String> legacyWriteScopes;
        private String viewFeatureScope;
        private String editFeatureScope;
        private Map<String, List<APIResource>> apiResources;

        public APIResourceCollectionBuilder() {
        }

        public APIResourceCollectionBuilder id(String id) {

            this.id = id;
            return this;
        }

        public APIResourceCollectionBuilder name(String name) {

            this.name = name;
            return this;
        }

        public APIResourceCollectionBuilder displayName(String displayName) {

            this.displayName = displayName;
            return this;
        }

        public APIResourceCollectionBuilder type(String type) {

            this.type = type;
            return this;
        }

        public APIResourceCollectionBuilder readScopes(List<String> readScopes) {

            this.readScopes = readScopes;
            return this;
        }

        public APIResourceCollectionBuilder writeScopes(List<String> writeScopes) {

            this.writeScopes = writeScopes;
            return this;
        }


        public APIResourceCollectionBuilder legacyReadScopes(List<String> legacyReadScopes) {

            this.legacyReadScopes = legacyReadScopes;
            return this;
        }

        public APIResourceCollectionBuilder legacyWriteScopes(List<String> legacyWriteScopes) {

            this.legacyWriteScopes = legacyWriteScopes;
            return this;
        }

        public APIResourceCollectionBuilder viewFeatureScope(String viewFeatureScope) {

            this.viewFeatureScope = viewFeatureScope;
            return this;
        }

        public APIResourceCollectionBuilder editFeatureScope(String editFeatureScope) {

            this.editFeatureScope = editFeatureScope;
            return this;
        }

        public APIResourceCollectionBuilder apiResources(Map<String, List<APIResource>> apiResources) {
            
            this.apiResources = apiResources;
            return this;
        }

        public APIResourceCollection build() {

            return new APIResourceCollection(this);
        }
    }
}
