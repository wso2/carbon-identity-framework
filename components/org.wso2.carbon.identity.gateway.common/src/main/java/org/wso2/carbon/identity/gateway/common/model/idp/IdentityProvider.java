package org.wso2.carbon.identity.gateway.common.model.idp;

public class IdentityProvider{
    private IdentityProviderConfig identityProviderConfig ;

    public IdentityProviderConfig getIdentityProviderConfig() {
        return identityProviderConfig;
    }

    public void setIdentityProviderConfig(IdentityProviderConfig identityProviderConfig) {
        this.identityProviderConfig = identityProviderConfig;
    }
}
