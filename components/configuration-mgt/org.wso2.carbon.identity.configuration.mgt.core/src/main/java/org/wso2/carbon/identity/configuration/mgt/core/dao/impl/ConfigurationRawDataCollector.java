/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.configuration.mgt.core.dao.impl;

import java.sql.Timestamp;

/**
 * This class is a collector to collect a single row of data from a database call.
 */
class ConfigurationRawDataCollector {

    private int tenantId;
    private String resourceId;
    private String resourceName;
    private Timestamp createdTime;
    private Timestamp lastModified;
    private String resourceTypeName;
    private String resourceTypeDescription;
    private String attributeKey;
    private String attributeValue;
    private String attributeId;
    private String fileId;
    private boolean hasFile;
    private boolean hasAttribute;

    public ConfigurationRawDataCollector(ConfigurationRawDataCollectorBuilder builder) {

        this.tenantId = builder.getTenantId();
        this.resourceId = builder.getResourceId();
        this.resourceName = builder.getResourceName();
        this.createdTime = builder.getCreatedTime();
        this.lastModified = builder.getLastModified();
        this.resourceTypeName = builder.getResourceTypeName();
        this.resourceTypeDescription = builder.getResourceTypeDescription();
        this.attributeKey = builder.getAttributeKey();
        this.attributeValue = builder.getAttributeValue();
        this.fileId = builder.getFileId();
        this.hasFile = builder.isHasFile();
        this.hasAttribute = builder.isHasAttribute();
        this.attributeId = builder.getAttributeId();
    }

    public String getAttributeId() {

        return attributeId;
    }

    public int getTenantId() {

        return tenantId;
    }

    public String getResourceId() {

        return resourceId;
    }

    public String getResourceName() {

        return resourceName;
    }

    public Timestamp getLastModified() {

        return lastModified;
    }

    public String getResourceTypeName() {

        return resourceTypeName;
    }

    public String getResourceTypeDescription() {

        return resourceTypeDescription;
    }

    public String getAttributeKey() {

        return attributeKey;
    }

    public String getAttributeValue() {

        return attributeValue;
    }

    public String getFileId() {

        return fileId;
    }

    public boolean isHasFile() {

        return hasFile;
    }

    public boolean isHasAttribute() {

        return hasAttribute;
    }

    public Timestamp getCreatedTime() {

        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {

        this.createdTime = createdTime;
    }

    public static class ConfigurationRawDataCollectorBuilder {

        private int tenantId;
        private String resourceId;
        private String resourceName;
        private Timestamp createdTime;
        private Timestamp lastModified;
        private String resourceTypeName;
        private String resourceTypeDescription;
        private String attributeKey;
        private String attributeValue;
        private String attributeId;
        private String fileId;
        private boolean hasFile;
        private boolean hasAttribute;

        public String getAttributeId() {

            return attributeId;
        }

        public ConfigurationRawDataCollectorBuilder setAttributeId(String attributeId) {

            this.attributeId = attributeId;
            return this;
        }

        public boolean isHasFile() {

            return hasFile;
        }

        public ConfigurationRawDataCollectorBuilder setHasFile(boolean hasFile) {

            this.hasFile = hasFile;
            return this;
        }

        public boolean isHasAttribute() {

            return hasAttribute;
        }

        public ConfigurationRawDataCollectorBuilder setHasAttribute(boolean hasAttribute) {

            this.hasAttribute = hasAttribute;
            return this;
        }

        public ConfigurationRawDataCollector build() {

            return new ConfigurationRawDataCollector(this);
        }

        int getTenantId() {

            return tenantId;
        }

        public ConfigurationRawDataCollectorBuilder setTenantId(int tenantId) {

            this.tenantId = tenantId;
            return this;
        }

        String getResourceId() {

            return resourceId;
        }

        public ConfigurationRawDataCollectorBuilder setResourceId(String resourceId) {

            this.resourceId = resourceId;
            return this;
        }

        String getResourceName() {

            return resourceName;
        }

        public ConfigurationRawDataCollectorBuilder setResourceName(String resourceName) {

            this.resourceName = resourceName;
            return this;
        }

        public Timestamp getCreatedTime() {

            return createdTime;
        }

        public ConfigurationRawDataCollectorBuilder setCreatedTime(Timestamp createdTime) {

            this.createdTime = createdTime;
            return this;
        }


        Timestamp getLastModified() {

            return lastModified;
        }

        public ConfigurationRawDataCollectorBuilder setLastModified(Timestamp lastModified) {

            this.lastModified = lastModified;
            return this;
        }

        String getResourceTypeName() {

            return resourceTypeName;
        }

        public ConfigurationRawDataCollectorBuilder setResourceTypeName(String resourceTypeName) {

            this.resourceTypeName = resourceTypeName;
            return this;
        }

        String getResourceTypeDescription() {

            return resourceTypeDescription;
        }

        public ConfigurationRawDataCollectorBuilder setResourceTypeDescription(String resourceTypeDescription) {

            this.resourceTypeDescription = resourceTypeDescription;
            return this;
        }

        String getAttributeKey() {

            return attributeKey;
        }

        public ConfigurationRawDataCollectorBuilder setAttributeKey(String attributeKey) {

            this.attributeKey = attributeKey;
            return this;
        }

        String getAttributeValue() {

            return attributeValue;
        }

        public ConfigurationRawDataCollectorBuilder setAttributeValue(String attributeValue) {

            this.attributeValue = attributeValue;
            return this;
        }

        String getFileId() {

            return fileId;
        }

        public ConfigurationRawDataCollectorBuilder setFileId(String fileId) {

            this.fileId = fileId;
            return this;
        }
    }
}
