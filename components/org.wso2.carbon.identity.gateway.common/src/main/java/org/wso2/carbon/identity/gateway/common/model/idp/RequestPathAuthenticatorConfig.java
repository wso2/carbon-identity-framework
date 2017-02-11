package org.wso2.carbon.identity.gateway.common.model.idp;


import java.util.Properties;

public class RequestPathAuthenticatorConfig {
    private String authenticatorName ;
    private Properties properties = new Properties();

    public String getAuthenticatorName() {
        return authenticatorName;
    }

    public void setAuthenticatorName(String authenticatorName) {
        this.authenticatorName = authenticatorName;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
