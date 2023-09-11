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

package org.wso2.carbon.identity.application.tag.common.model;

/**
 * Application Tags List Item.
 */
public class ApplicationTagsListItem {

    private String id;
    private String name;
    private String colour;

    public ApplicationTagsListItem() {
    }

    public ApplicationTagsListItem(ApplicationTagsListItemBuilder applicationTagsListItemBuilder) {

        this.id = applicationTagsListItemBuilder.id;
        this.name = applicationTagsListItemBuilder.name;
        this.colour = applicationTagsListItemBuilder.colour;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getColour() {

        return colour;
    }

    public void setColour(String colour) {

        this.colour = colour;
    }

    /**
     * Application Tags List Item builder.
     */
    public static class ApplicationTagsListItemBuilder {

        private String id;
        private String name;
        private String colour;

        public ApplicationTagsListItemBuilder() {
        }

        public ApplicationTagsListItemBuilder id(String id) {

            this.id = id;
            return this;
        }

        public ApplicationTagsListItemBuilder name(String name) {

            this.name = name;
            return this;
        }

        public ApplicationTagsListItemBuilder colour(String colour) {

            this.colour = colour;
            return this;
        }

        public ApplicationTagsListItem build() {

            return new ApplicationTagsListItem(this);
        }
    }
}
