package org.wso2.carbon.identity.gateway.common.model.sp;

import java.util.ArrayList;
import java.util.List;

public class StepHandlerConfig {

    private String subjectStep ;
    private List<AuthenticatorHandlerConfig> authenticatorHandlerConfigs = new ArrayList<>();
    private AuthenticatorHandlerConfig authenticatorHandlerConfig ;

    public String getSubjectStep() {
        return subjectStep;
    }

    public void setSubjectStep(String subjectStep) {
        this.subjectStep = subjectStep;
    }

    public List<AuthenticatorHandlerConfig> getAuthenticatorHandlerConfigs() {
        return authenticatorHandlerConfigs;
    }

    public void setAuthenticatorHandlerConfigs(List<AuthenticatorHandlerConfig> authenticatorHandlerConfigs) {
        this.authenticatorHandlerConfigs = authenticatorHandlerConfigs;
    }

    public AuthenticatorHandlerConfig getAuthenticatorHandlerConfig() {
        return authenticatorHandlerConfig;
    }

    public void setAuthenticatorHandlerConfig(AuthenticatorHandlerConfig authenticatorHandlerConfig) {
        this.authenticatorHandlerConfig = authenticatorHandlerConfig;
    }
}