package org.wso2.carbon.identity.configuration.mgt.core.model;

import org.wso2.carbon.identity.xds.common.constant.XDSWrapper;

import java.io.InputStream;

public class ConfigurationXDSWrapper implements XDSWrapper {

    private String resourceName;
    private String resourceTypeName;
    private ResourceAdd resourceAdd;
    private Resource resource;
    private ResourceTypeAdd resourceTypeAdd;
    private String attributeKey;
    private Attribute attribute;
    private String fileName;
    private InputStream fileStream;
    private String resourceType;
    private String fileId;
    private String resourceId;
    private String resourceTypeId;
    private String timestamp;

    public ConfigurationXDSWrapper(ConfigurationXDSWrapperBuilder builder) {
        this.resourceName = builder.resourceName;
        this.resourceTypeName = builder.resourceTypeName;
        this.resourceAdd = builder.resourceAdd;
        this.resource = builder.resource;
        this.resourceTypeAdd = builder.resourceTypeAdd;
        this.attributeKey = builder.attributeKey;
        this.attribute = builder.attribute;
        this.fileName = builder.fileName;
        this.timestamp = builder.timestamp;
        this.fileStream = builder.fileStream;
        this.resourceType = builder.resourceType;
        this.fileId = builder.fileId;
        this.resourceId = builder.resourceId;
        this.resourceTypeId = builder.resourceTypeId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getResourceTypeName() {
        return resourceTypeName;
    }

    public ResourceAdd getResourceAdd() {
        return resourceAdd;
    }

    public Resource getResource() {
        return resource;
    }

    public ResourceTypeAdd getResourceTypeAdd() {
        return resourceTypeAdd;
    }

    public String getAttributeKey() {
        return attributeKey;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public String getFileName() {
        return fileName;
    }

    public InputStream getFileStream() {
        return fileStream;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getFileId() {
        return fileId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getResourceTypeId() {
        return resourceTypeId;
    }

    public static class ConfigurationXDSWrapperBuilder {

        private String resourceName;
        private String resourceTypeName;
        private ResourceAdd resourceAdd;
        private Resource resource;
        private ResourceTypeAdd resourceTypeAdd;
        private String attributeKey;
        private Attribute attribute;
        private String fileName;
        private InputStream fileStream;
        private String resourceType;
        private String timestamp;
        private String fileId;
        private String resourceId;
        private String resourceTypeId;


        public ConfigurationXDSWrapperBuilder setResourceName(String resourceName) {
            this.resourceName = resourceName;
            return this;
        }

        public ConfigurationXDSWrapperBuilder setResourceTypeName(String resourceTypeName) {
            this.resourceTypeName = resourceTypeName;
            return this;
        }

        public ConfigurationXDSWrapperBuilder setResourceAdd(ResourceAdd resourceAdd) {
            this.resourceAdd = resourceAdd;
            return this;
        }

        public ConfigurationXDSWrapperBuilder setResource(Resource resource) {
            this.resource = resource;
            return this;
        }

        public ConfigurationXDSWrapperBuilder setResourceTypeAdd(ResourceTypeAdd resourceTypeAdd) {
            this.resourceTypeAdd = resourceTypeAdd;
            return this;
        }

        public ConfigurationXDSWrapperBuilder setAttributeKey(String attributeKey) {
            this.attributeKey = attributeKey;
            return this;
        }

        public ConfigurationXDSWrapperBuilder setAttribute(Attribute attribute) {
            this.attribute = attribute;
            return this;
        }

        public ConfigurationXDSWrapperBuilder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public ConfigurationXDSWrapperBuilder setFileStream(InputStream fileStream) {
            this.fileStream = fileStream;
            return this;
        }

        public ConfigurationXDSWrapperBuilder setResourceType(String resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public ConfigurationXDSWrapperBuilder setFileId(String fileId) {
            this.fileId = fileId;
            return this;
        }

        public ConfigurationXDSWrapperBuilder setResourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public ConfigurationXDSWrapperBuilder setResourceTypeId(String resourceTypeId) {
            this.resourceTypeId = resourceTypeId;
            return this;
        }

        public ConfigurationXDSWrapper build() {

            this.timestamp = String.valueOf(System.currentTimeMillis());
            return new ConfigurationXDSWrapper(this);
        }
    }
}
