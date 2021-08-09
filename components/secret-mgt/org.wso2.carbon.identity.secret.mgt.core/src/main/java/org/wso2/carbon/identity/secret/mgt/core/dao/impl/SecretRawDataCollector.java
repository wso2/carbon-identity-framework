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

package org.wso2.carbon.identity.secret.mgt.core.dao.impl;

import java.sql.Timestamp;

/**
 * This class is a collector to collect a single row of data from a database call.
 */
public class SecretRawDataCollector {

    private final int tenantId;
    private final String secretId;
    private final String secretName;
    private final String secretValue;
    private final Timestamp createdTime;
    private final Timestamp lastModified;

    public SecretRawDataCollector(SecretRawDataCollectorBuilder builder) {

        this.tenantId = builder.getTenantId();
        this.secretId = builder.getSecretId();
        this.secretName = builder.getSecretName();
        this.secretValue = builder.getSecretValue();
        this.createdTime = builder.getCreatedTime();
        this.lastModified = builder.getLastModified();
    }

    public int getTenantId() {

        return tenantId;
    }

    public String getSecretId() {

        return secretId;
    }

    public String getSecretName() {

        return secretName;
    }

    public String getValue() {

        return secretValue;
    }

    public Timestamp getLastModified() {

        return lastModified;
    }

    public Timestamp getCreatedTime() {

        return createdTime;
    }

    public static class SecretRawDataCollectorBuilder {

        private int tenantId;
        private String secretId;
        private String secretName;
        private String secretValue;
        private Timestamp createdTime;
        private Timestamp lastModified;

        public SecretRawDataCollector build() {

            return new SecretRawDataCollector(this);
        }

        int getTenantId() {

            return tenantId;
        }

        public SecretRawDataCollectorBuilder setTenantId(int tenantId) {

            this.tenantId = tenantId;
            return this;
        }

        String getSecretId() {

            return secretId;
        }

        public SecretRawDataCollectorBuilder setSecretId(String secretId) {

            this.secretId = secretId;
            return this;
        }

        String getSecretName() {

            return secretName;
        }

        public SecretRawDataCollectorBuilder setSecretName(String secretName) {

            this.secretName = secretName;
            return this;
        }

        public Timestamp getCreatedTime() {

            return createdTime;
        }

        public SecretRawDataCollectorBuilder setCreatedTime(Timestamp createdTime) {

            this.createdTime = createdTime;
            return this;
        }

        Timestamp getLastModified() {

            return lastModified;
        }

        public SecretRawDataCollectorBuilder setLastModified(Timestamp lastModified) {

            this.lastModified = lastModified;
            return this;
        }

        String getSecretValue() {

            return secretValue;
        }

        public SecretRawDataCollectorBuilder setSecretValue(String value) {

            this.secretValue = value;
            return this;
        }
    }
}
