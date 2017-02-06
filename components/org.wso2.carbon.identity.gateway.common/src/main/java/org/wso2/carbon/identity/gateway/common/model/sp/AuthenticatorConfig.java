package org.wso2.carbon.identity.gateway.common.model.sp;

import java.util.Properties;

public class AuthenticatorConfig {

    private String type;
    private String authenticatorName;
    private Properties properties = new Properties();

    public String getAuthenticatorName() {
        return authenticatorName;
    }

    public void setAuthenticatorName(String authenticatorName) {
        this.authenticatorName = authenticatorName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}