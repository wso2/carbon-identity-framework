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

public class AuthorizationDetailsType {

    private String id;
    private String type;
    private String name;
    private String description;
    private String schema;
    private String apiId;
    private String orgId;

    public AuthorizationDetailsType() {

    }

    public AuthorizationDetailsType(final String id, final String type, final String name,
                                    final String description, final String schema) {

        this(id, type, name, description, schema, null, null);
    }

    public AuthorizationDetailsType(final String id, final String type, final String name, final String description,
                                    final String schema, final String apiId, final String orgId) {

        this.id = id;
        this.type = type;
        this.name = name;
        this.description = description;
        this.schema = schema;
        this.apiId = apiId;
        this.orgId = orgId;
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

    public String getSchema() {
        return this.schema;
    }

    public void setSchema(final String schema) {
        this.schema = schema;
    }

    public String getApiId() {
        return this.apiId;
    }

    public void setApiID(final String apiId) {
        this.apiId = apiId;
    }

    public String getOrgId() {
        return this.orgId;
    }

    public void setOrgId(final String orgId) {
        this.orgId = orgId;
    }
}

