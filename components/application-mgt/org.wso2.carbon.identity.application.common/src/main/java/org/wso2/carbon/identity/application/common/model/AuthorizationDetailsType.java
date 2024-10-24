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

package org.wso2.carbon.identity.application.common.model;

import java.util.Map;

/**
 * Authorized authorization details types model class.
 */
public class AuthorizationDetailsType {

    private String id;
    private String type;
    private String name;
    private String description;
    private Map<String, Object> schema;

    public AuthorizationDetailsType() {
    }

    public AuthorizationDetailsType(final String id, final String type, final String name,
                                    final String description, final Map<String, Object> schema) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.description = description;
        this.schema = schema;
    }

    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getType() {
        return this.type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Map<String, Object> getSchema() {
        return this.schema;
    }

    public void setSchema(final Map<String, Object> schema) {
        this.schema = schema;
    }

    /**
     * Builder class for {@link AuthorizationDetailsType}.
     */
    public static class AuthorizationDetailsTypesBuilder {

        private String id;
        private String type;
        private String name;
        private String description;
        private Map<String, Object> schema;

        public AuthorizationDetailsType.AuthorizationDetailsTypesBuilder id(String id) {

            this.id = id;
            return this;
        }

        public AuthorizationDetailsType.AuthorizationDetailsTypesBuilder type(String type) {

            this.type = type;
            return this;
        }

        public AuthorizationDetailsType.AuthorizationDetailsTypesBuilder name(String name) {

            this.name = name;
            return this;
        }

        public AuthorizationDetailsType.AuthorizationDetailsTypesBuilder description(String description) {

            this.description = description;
            return this;
        }

        public AuthorizationDetailsType.AuthorizationDetailsTypesBuilder schema(Map<String, Object> schema) {

            this.schema = schema;
            return this;
        }

        public AuthorizationDetailsType build() {

            return new AuthorizationDetailsType(this.id, this.type, this.name, this.description, this.schema);
        }
    }
}
