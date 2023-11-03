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

import org.wso2.carbon.identity.application.common.model.Scope;

import java.util.List;

/**
 * API Resource Collection Basic Info.
 */
public class APIResourceCollectionBasicInfo {

    private String id;
    private String name;
    private String displayName;
    private String type;
    private List<Scope> scopes;

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

    public List<Scope> getScopes() {

        return scopes;
    }

    public void setScopes(List<Scope> scopes) {

        this.scopes = scopes;
    }

    public APIResourceCollectionBasicInfo() {
    }

    public APIResourceCollectionBasicInfo(APIResourceCollectionBasicInfoBuilder apiResourceCollectionBasicInfoBuilder) {

        this.id = apiResourceCollectionBasicInfoBuilder.id;
        this.name = apiResourceCollectionBasicInfoBuilder.name;
        this.displayName = apiResourceCollectionBasicInfoBuilder.displayName;
        this.type = apiResourceCollectionBasicInfoBuilder.type;
        this.scopes = apiResourceCollectionBasicInfoBuilder.scopes;
    }

    /**
     * Builder class for API Resource Collection Basic Info.
     */
    public static class APIResourceCollectionBasicInfoBuilder {

        private String id;
        private String name;
        private String displayName;
        private String type;
        private List<Scope> scopes;

        public APIResourceCollectionBasicInfoBuilder id(String id) {

            this.id = id;
            return this;
        }

        public APIResourceCollectionBasicInfoBuilder name(String name) {

            this.name = name;
            return this;
        }

        public APIResourceCollectionBasicInfoBuilder displayName(String displayName) {

            this.displayName = displayName;
            return this;
        }

        public APIResourceCollectionBasicInfoBuilder type(String type) {

            this.type = type;
            return this;
        }

        public APIResourceCollectionBasicInfoBuilder scopes(List<Scope> scopes) {

            this.scopes = scopes;
            return this;
        }

        public APIResourceCollectionBasicInfo build() {

            return new APIResourceCollectionBasicInfo(this);
        }
    }

}
