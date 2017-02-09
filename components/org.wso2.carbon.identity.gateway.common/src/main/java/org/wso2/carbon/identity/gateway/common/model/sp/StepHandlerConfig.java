package org.wso2.carbon.identity.gateway.common.model.sp;

import java.util.ArrayList;
import java.util.List;

public class StepHandlerConfig {

    private String subjectStep ;
    private String idpName ;
    private List<AuthenticatorConfig> authenticatorConfigs = new ArrayList<>();
    private AuthenticatorConfig authenticatorConfig ;

    public String getSubjectStep() {
        return subjectStep;
    }

    public void setSubjectStep(String subjectStep) {
        this.subjectStep = subjectStep;
    }

    public List<AuthenticatorConfig> getAuthenticatorConfigs() {
        return authenticatorConfigs;
    }

    public void setAuthenticatorConfigs(List<AuthenticatorConfig> authenticatorConfigs) {
        this.authenticatorConfigs = authenticatorConfigs;
    }

    public AuthenticatorConfig getAuthenticatorConfig() {
        return authenticatorConfig;
    }

    public void setAuthenticatorConfig(AuthenticatorConfig authenticatorConfig) {
        this.authenticatorConfig = authenticatorConfig;
    }

    public String getIdpName() {
        return idpName;
    }

    public void setIdpName(String idpName) {
        this.idpName = idpName;
    }
}