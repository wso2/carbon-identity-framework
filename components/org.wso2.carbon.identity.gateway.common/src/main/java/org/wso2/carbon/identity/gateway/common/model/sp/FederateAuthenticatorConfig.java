package org.wso2.carbon.identity.gateway.common.model.sp;

import idp.IdentityProviderConfig;

import java.util.Properties;

public class FederateAuthenticatorConfig {


    private String authenticatorName;
    private String idpName ;
    private IdentityProviderConfig identityProviderConfig ;
    private Properties properties = new Properties();

    public String getAuthenticatorName() {
        return authenticatorName;
    }

    public void setAuthenticatorName(String authenticatorName) {
        this.authenticatorName = authenticatorName;
    }


    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getIdpName() {
        return idpName;
    }

    public void setIdpName(String idpName) {
        this.idpName = idpName;
    }

    public IdentityProviderConfig getIdentityProviderConfig() {
        return identityProviderConfig;
    }

    public void setIdentityProviderConfig(IdentityProviderConfig identityProviderConfig) {
        this.identityProviderConfig = identityProviderConfig;
    }
}