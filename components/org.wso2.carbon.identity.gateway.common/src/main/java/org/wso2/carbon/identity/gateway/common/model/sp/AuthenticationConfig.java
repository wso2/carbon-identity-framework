package org.wso2.carbon.identity.gateway.common.model.sp;

import java.util.ArrayList;
import java.util.List;

public class AuthenticationConfig {

    List<AuthenticationStepConfig> authenticationStepConfigs = new ArrayList<>();

    public List<AuthenticationStepConfig> getAuthenticationStepConfigs() {
        return authenticationStepConfigs;
    }

    public void setAuthenticationStepConfigs(List<AuthenticationStepConfig> authenticationStepConfigs) {
        this.authenticationStepConfigs = authenticationStepConfigs;
    }
}