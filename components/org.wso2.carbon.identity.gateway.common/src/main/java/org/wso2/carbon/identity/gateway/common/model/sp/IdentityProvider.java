package org.wso2.carbon.identity.gateway.common.model.sp;

import org.wso2.carbon.identity.gateway.common.model.idp.IdentityProviderConfig;

public class IdentityProvider {

    private String identityProviderName ;
    private String authenticatorName ;
    private IdentityProviderConfig identityProviderConfig ;

    public String getIdentityProviderName() {
        return identityProviderName;
    }

    public void setIdentityProviderName(String identityProviderName) {
        this.identityProviderName = identityProviderName;
    }

    public String getAuthenticatorName() {
        return authenticatorName;
    }

    public void setAuthenticatorName(String authenticatorName) {
        this.authenticatorName = authenticatorName;
    }

    public IdentityProviderConfig getIdentityProviderConfig() {
        return identityProviderConfig;
    }

    public void setIdentityProviderConfig(IdentityProviderConfig identityProviderConfig) {
        this.identityProviderConfig = identityProviderConfig;
    }
}
