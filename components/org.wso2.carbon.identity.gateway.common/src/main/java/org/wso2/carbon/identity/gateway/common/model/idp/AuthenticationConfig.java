package org.wso2.carbon.identity.gateway.common.model.idp;

import java.util.ArrayList;
import java.util.List;

public class AuthenticationConfig {
    private List<String> requestedClaimUris = new ArrayList<>();
    private List<AuthenticatorConfig> authenticatorConfigs = new ArrayList<>();

    public List<AuthenticatorConfig> getAuthenticatorConfigs() {
        return authenticatorConfigs;
    }

    public void setAuthenticatorConfigs(List<AuthenticatorConfig> authenticatorConfigs) {
        this.authenticatorConfigs = authenticatorConfigs;
    }

    public List<String> getRequestedClaimUris() {
        return requestedClaimUris;
    }

    public void setRequestedClaimUris(List<String> requestedClaimUris) {
        this.requestedClaimUris = requestedClaimUris;
    }
}
