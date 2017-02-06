package org.wso2.carbon.identity.gateway.common.model.sp;

public class StepHandlerConfig {

    private String subjectStep ;
    private MultiOptionHandlerConfig multiOptionHandlerConfig;
    private AuthenticatorConfig authenticatorConfig;

    public AuthenticatorConfig getAuthenticatorConfig() {
        return authenticatorConfig;
    }

    public void setAuthenticatorConfig(AuthenticatorConfig authenticatorConfig) {
        this.authenticatorConfig = authenticatorConfig;
    }

    public MultiOptionHandlerConfig getMultiOptionHandlerConfig() {
        return multiOptionHandlerConfig;
    }

    public void setMultiOptionHandlerConfig(MultiOptionHandlerConfig multiOptionHandlerConfig) {
        this.multiOptionHandlerConfig = multiOptionHandlerConfig;
    }

    public String getSubjectStep() {
        return subjectStep;
    }

    public void setSubjectStep(String subjectStep) {
        this.subjectStep = subjectStep;
    }
}