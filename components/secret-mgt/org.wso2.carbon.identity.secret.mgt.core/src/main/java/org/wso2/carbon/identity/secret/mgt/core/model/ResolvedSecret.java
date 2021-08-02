package org.wso2.carbon.identity.secret.mgt.core.model;

public class ResolvedSecret extends Secret {

    private String resolvedSecretValue;

    public void setResolvedSecretValue(String resolvedSecretValue) {

        this.resolvedSecretValue = resolvedSecretValue;
    }

    public String getResolvedSecretValue() {

        return resolvedSecretValue;
    }

}
