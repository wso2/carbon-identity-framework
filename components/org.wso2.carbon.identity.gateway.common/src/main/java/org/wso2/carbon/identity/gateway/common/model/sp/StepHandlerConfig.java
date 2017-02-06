package org.wso2.carbon.identity.gateway.common.model.sp;

public class StepHandlerConfig {
    private String isSubjectStep ;

    private MultiOptionHandlerConfig multiOptionHandler;
    private AuthenticatorConfig authenticatorConfig;

    public AuthenticatorConfig getAuthenticatorConfig() {
        return authenticatorConfig;
    }

    public void setAuthenticatorConfig(AuthenticatorConfig authenticatorConfig) {
        this.authenticatorConfig = authenticatorConfig;
    }

    public MultiOptionHandlerConfig getMultiOptionHandler() {
        return multiOptionHandler;
    }

    public void setMultiOptionHandler(MultiOptionHandlerConfig multiOptionHandler) {
        this.multiOptionHandler = multiOptionHandler;
    }

    public String getIsSubjectStep() {
        return isSubjectStep;
    }

    public void setIsSubjectStep(String isSubjectStep) {
        this.isSubjectStep = isSubjectStep;
    }
}