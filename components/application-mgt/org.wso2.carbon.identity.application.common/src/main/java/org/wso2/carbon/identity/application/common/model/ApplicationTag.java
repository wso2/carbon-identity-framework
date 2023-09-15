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

package org.wso2.carbon.identity.application.common.model;

/**
 * Application Tag POST.
 */
public class ApplicationTag {
    private String name;
    private String colour;

    public ApplicationTag() {
    }

    public ApplicationTag(ApplicationTagBuilder applicationTagBuilder) {

        this.name = applicationTagBuilder.name;
        this.colour = applicationTagBuilder.colour;
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
     * Application Tag POST builder.
     */
    public static class ApplicationTagBuilder {
        private String name;
        private String colour;

        public ApplicationTagBuilder() {
        }

        public ApplicationTagBuilder name(String name) {

            this.name = name;
            return this;
        }

        public ApplicationTagBuilder colour(String colour) {

            this.colour = colour;
            return this;
        }

        public ApplicationTag build() {

            return new ApplicationTag(this);
        }
    }
}
