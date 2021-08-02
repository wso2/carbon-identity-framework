/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.secret.mgt.core.model;

/**
 * A model class representing a secret.
 */
public class Secret {

    private String secretId;
    private String tenantDomain;
    private String secretName;
    private String lastModified;
    private String created;
    private String secretValue;

    /**
     * Initialize a Secret object.
     *
     * @param secretName Name of the secret.
     */
    public Secret(String secretName) {

        this.secretName = secretName;
    }

    public Secret() {

    }

    public String getSecretName() {

        return secretName;
    }

    public void setSecretName(String secretName) {

        this.secretName = secretName;
    }

    public String getCreatedTime() {

        return created;
    }

    public void setCreatedTime(String createdTime) {

        this.created = createdTime;
    }

    public String getSecretId() {

        return secretId;
    }

    public void setSecretId(String secretIdId) {

        this.secretId = secretIdId;
    }

    public String getTenantDomain() {

        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    public String getLastModified() {

        return lastModified;
    }

    public void setLastModified(String lastModified) {

        this.lastModified = lastModified;
    }

    public String getSecretValue() {

        return secretValue;
    }

    public void setSecretValue(String value) {

        this.secretValue = value;
    }
}
