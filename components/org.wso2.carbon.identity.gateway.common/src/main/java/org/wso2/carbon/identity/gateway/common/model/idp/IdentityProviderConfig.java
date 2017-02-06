package org.wso2.carbon.identity.gateway.common.model.idp;


public class IdentityProviderConfig {

    private String name ;
    private IDPMetaData idpMetaData ;
    private AuthenticationConfig authenticationConfig ;
    private ProvisioningConfig provisioningConfig ;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IDPMetaData getIdpMetaData() {
        return idpMetaData;
    }

    public void setIdpMetaData(IDPMetaData idpMetaData) {
        this.idpMetaData = idpMetaData;
    }

    public AuthenticationConfig getAuthenticationConfig() {
        return authenticationConfig;
    }

    public void setAuthenticationConfig(AuthenticationConfig authenticationConfig) {
        this.authenticationConfig = authenticationConfig;
    }

    public ProvisioningConfig getProvisioningConfig() {
        return provisioningConfig;
    }

    public void setProvisioningConfig(ProvisioningConfig provisioningConfig) {
        this.provisioningConfig = provisioningConfig;
    }
}
