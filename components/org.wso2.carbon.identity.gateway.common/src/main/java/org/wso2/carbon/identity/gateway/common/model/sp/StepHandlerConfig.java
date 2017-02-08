package org.wso2.carbon.identity.gateway.common.model.sp;

public class StepHandlerConfig {

    private String subjectStep ;
    private MultiOptionHandlerConfig multiOptionHandlerConfig;
    private LocalAuthenticatorConfig localAuthenticatorConfig ;
    private FederateAuthenticatorConfig federateAuthenticatorConfig ;


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

    public LocalAuthenticatorConfig getLocalAuthenticatorConfig() {
        return localAuthenticatorConfig;
    }

    public void setLocalAuthenticatorConfig(LocalAuthenticatorConfig localAuthenticatorConfig) {
        this.localAuthenticatorConfig = localAuthenticatorConfig;
    }

    public FederateAuthenticatorConfig getFederateAuthenticatorConfig() {
        return federateAuthenticatorConfig;
    }

    public void setFederateAuthenticatorConfig(FederateAuthenticatorConfig federateAuthenticatorConfig) {
        this.federateAuthenticatorConfig = federateAuthenticatorConfig;
    }
}