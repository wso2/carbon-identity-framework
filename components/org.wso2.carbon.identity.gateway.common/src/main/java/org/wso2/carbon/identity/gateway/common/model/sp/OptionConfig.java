package org.wso2.carbon.identity.gateway.common.model.sp;

public class OptionConfig  {
    private FederateAuthenticatorConfig federateAuthenticatorConfig ;
    private LocalAuthenticatorConfig localAuthenticatorConfig ;

    public FederateAuthenticatorConfig getFederateAuthenticatorConfig() {
        return federateAuthenticatorConfig;
    }

    public void setFederateAuthenticatorConfig(FederateAuthenticatorConfig federateAuthenticatorConfig) {
        this.federateAuthenticatorConfig = federateAuthenticatorConfig;
    }

    public LocalAuthenticatorConfig getLocalAuthenticatorConfig() {
        return localAuthenticatorConfig;
    }

    public void setLocalAuthenticatorConfig(LocalAuthenticatorConfig localAuthenticatorConfig) {
        this.localAuthenticatorConfig = localAuthenticatorConfig;
    }
}