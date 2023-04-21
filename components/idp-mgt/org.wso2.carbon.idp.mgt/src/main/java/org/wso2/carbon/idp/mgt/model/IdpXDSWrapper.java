package org.wso2.carbon.idp.mgt.model;

import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.xds.common.constant.XDSWrapper;

public class IdpXDSWrapper implements XDSWrapper {

    private IdentityProvider identityProvider;
    private String tenantDomain;
    private String idpName;
    private String resourceId;
    private String timestamp;

    public IdpXDSWrapper(IdpXDSWrapperBuilder builder) {
        this.identityProvider = builder.identityProvider;
        this.tenantDomain = builder.tenantDomain;
        this.idpName = builder.idpName;
        this.resourceId = builder.resourceId;
        this.timestamp = builder.timestamp;
    }

    public IdentityProvider getIdentityProvider() {
        return identityProvider;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public String getIdpName() {
        return idpName;
    }

    public String getResourceId() {
        return resourceId;
    }

    public static class IdpXDSWrapperBuilder {
        private IdentityProvider identityProvider;
        private String tenantDomain;

        private String idpName;
        private String resourceId;
        private String timestamp;

        public IdpXDSWrapperBuilder setIdentityProvider(IdentityProvider identityProvider) {
            this.identityProvider = identityProvider;
            return this;
        }

        public IdpXDSWrapperBuilder setTenantDomain(String tenantDomain) {
            this.tenantDomain = tenantDomain;
            return this;
        }

        public IdpXDSWrapper build() {

            this.timestamp = String.valueOf(System.currentTimeMillis());
            return new IdpXDSWrapper(this);
        }

        public IdpXDSWrapperBuilder setIdpName(String idpName) {
            this.idpName = idpName;
            return this;
        }

        public IdpXDSWrapperBuilder setResourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

    }
}
