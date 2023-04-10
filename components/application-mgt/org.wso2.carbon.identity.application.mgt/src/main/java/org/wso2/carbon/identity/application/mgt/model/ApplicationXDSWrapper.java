package org.wso2.carbon.identity.application.mgt.model;

import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.SpTemplate;
import org.wso2.carbon.identity.xds.common.constant.XDSWrapper;

/**
 * This class is used to update the application.
 */
public class ApplicationXDSWrapper implements XDSWrapper {

    private ServiceProvider serviceProvider;
    private String tenantDomain;
    private String username;
    private String templateName;
    private String applicationName;
    private int tenantId;
    private SpTemplate spTemplate;
    private String oldTemplateName;
    private String resourceId;

    public ApplicationXDSWrapper(ApplicationXDSWrapperBuilder builder) {

        this.serviceProvider = builder.serviceProvider;
        this.tenantDomain = builder.tenantDomain;
        this.username = builder.username;
        this.templateName = builder.templateName;
        this.applicationName = builder.applicationName;
        this.tenantId = builder.tenantId;
        this.spTemplate = builder.spTemplate;
        this.oldTemplateName = builder.oldTemplateName;
        this.resourceId = builder.resourceId;
    }
    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public String getUsername() {
        return username;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public int getTenantId() {
        return tenantId;
    }

    public SpTemplate getSpTemplate() {
        return spTemplate;
    }

    public String getOldTemplateName() {
        return oldTemplateName;
    }

    public String getResourceId() {
        return resourceId;
    }

    /**
     * This class is used to build the ApplicationXDSWrapper.
     */
    public static class ApplicationXDSWrapperBuilder {

        private ServiceProvider serviceProvider;
        private String tenantDomain;
        private String username;
        private String templateName;
        private String applicationName;
        private int tenantId;
        private SpTemplate spTemplate;
        private String oldTemplateName;
        private String resourceId;

        public ApplicationXDSWrapperBuilder setServiceProvider(ServiceProvider serviceProvider) {
            this.serviceProvider = serviceProvider;
            return this;
        }

        public ApplicationXDSWrapperBuilder setTenantDomain(String tenantDomain) {
            this.tenantDomain = tenantDomain;
            return this;
        }

        public ApplicationXDSWrapperBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        public ApplicationXDSWrapperBuilder setTemplateName(String templateName) {
            this.templateName = templateName;
            return this;
        }

        public ApplicationXDSWrapperBuilder setApplicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        public ApplicationXDSWrapperBuilder setTenantId(int tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public ApplicationXDSWrapperBuilder setSpTemplate(SpTemplate spTemplate) {
            this.spTemplate = spTemplate;
            return this;
        }

        public ApplicationXDSWrapperBuilder setOldTemplateName(String oldTemplateName) {
            this.oldTemplateName = oldTemplateName;
            return this;
        }

        public ApplicationXDSWrapperBuilder setResourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public ApplicationXDSWrapper build() {
            return new ApplicationXDSWrapper(this);
        }

    }
}
