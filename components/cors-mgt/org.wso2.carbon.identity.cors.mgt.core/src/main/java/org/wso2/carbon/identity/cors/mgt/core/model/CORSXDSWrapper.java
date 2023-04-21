package org.wso2.carbon.identity.cors.mgt.core.model;

import org.wso2.carbon.identity.xds.common.constant.XDSWrapper;

import java.util.List;

/**
 * CORS XDS wrapper.
 */
public class CORSXDSWrapper implements XDSWrapper {

    private String applicationId;
    private List<String> origins;
    private String tenantDomain;
    private CORSConfiguration corsConfiguration;
    private String timestamp;


    public CORSXDSWrapper(CorsXDSWrapperBuilder builder) {
        this.applicationId = builder.applicationId;
        this.origins = builder.origins;
        this.tenantDomain = builder.tenantDomain;
        this.corsConfiguration = builder.corsConfiguration;
        this.timestamp = builder.timestamp;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public List<String> getOrigins() {
        return origins;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public CORSConfiguration getCORSConfiguration() {
        return corsConfiguration;
    }

    /**
     * Builder class for {@link CORSXDSWrapper}.
     */
    public static class CorsXDSWrapperBuilder {
        String applicationId;
        List<String> origins;
        String tenantDomain;
        CORSConfiguration corsConfiguration;
        String timestamp;

        public CorsXDSWrapperBuilder setApplicationId(String applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        public CorsXDSWrapperBuilder setOrigins(List<String> origins) {
            this.origins = origins;
            return this;
        }

        public CorsXDSWrapperBuilder setTenantDomain(String tenantDomain) {
            this.tenantDomain = tenantDomain;
            return this;
        }

        public CorsXDSWrapperBuilder setCORSConfiguration(CORSConfiguration corsConfiguration) {
            this.corsConfiguration = corsConfiguration;
            return this;
        }

        public CORSXDSWrapper build() {

            this.timestamp = String.valueOf(System.currentTimeMillis());
            return new CORSXDSWrapper(this);
        }
    }
}
