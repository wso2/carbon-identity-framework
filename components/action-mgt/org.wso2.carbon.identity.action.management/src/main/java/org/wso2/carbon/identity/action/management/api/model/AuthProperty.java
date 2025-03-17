/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.management.api.model;

/**
 * Authentication Property.
 */
public class AuthProperty {

    private String name;
    private String value;
    private boolean isConfidential;

    public AuthProperty() {
    }

    public AuthProperty(AuthPropertyBuilder authPropertyBuilder) {

        this.name = authPropertyBuilder.name;
        this.value = authPropertyBuilder.value;
        this.isConfidential = authPropertyBuilder.isConfidential;
    }

    public String getName() {

        return name;
    }

    public String getValue() {

        return value;
    }

    public boolean getIsConfidential() {

        return isConfidential;
    }

    /**
     * Authentication Property Builder.
     */
    public static class AuthPropertyBuilder {

        private String name;
        private String value;
        private boolean isConfidential;

        public AuthPropertyBuilder name(String name) {

            this.name = name;
            return this;
        }

        public AuthPropertyBuilder value(String value) {

            this.value = value;
            return this;
        }

        public AuthPropertyBuilder isConfidential(boolean isConfidential) {

            this.isConfidential = isConfidential;
            return this;
        }

        public AuthProperty build() {

            return new AuthProperty(this);
        }
    }
}
