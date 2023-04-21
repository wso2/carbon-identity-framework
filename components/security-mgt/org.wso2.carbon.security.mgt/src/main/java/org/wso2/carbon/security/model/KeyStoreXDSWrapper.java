package org.wso2.carbon.security.model;

import org.wso2.carbon.identity.xds.common.constant.XDSWrapper;

public class KeyStoreXDSWrapper implements XDSWrapper {

    private String alias;
    private String certificate;
    private String tenantDomain;
    private String timestamp;

    public KeyStoreXDSWrapper(KeyStoreXDSWrapperBuilder builder) {
        this.alias = builder.alias;
        this.certificate = builder.certificate;
        this.tenantDomain = builder.tenantDomain;
        this.timestamp = builder.timestamp;
    }

    public String getAlias() {
        return alias;
    }

    public String getCertificate() {
        return certificate;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public static class KeyStoreXDSWrapperBuilder {

        private String alias;
        private String certificate;
        private String tenantDomain;
        private String timestamp;


        public KeyStoreXDSWrapperBuilder setAlias(String alias) {
            this.alias = alias;
            return this;
        }

        public KeyStoreXDSWrapperBuilder setCertificate(String certificate) {
            this.certificate = certificate;
            return this;
        }

        public KeyStoreXDSWrapperBuilder setTenantDomain(String tenantDomain) {
            this.tenantDomain = tenantDomain;
            return this;
        }

        public KeyStoreXDSWrapper build() {

            this.timestamp = String.valueOf(System.currentTimeMillis());
            return new KeyStoreXDSWrapper(this);
        }
    }
}
