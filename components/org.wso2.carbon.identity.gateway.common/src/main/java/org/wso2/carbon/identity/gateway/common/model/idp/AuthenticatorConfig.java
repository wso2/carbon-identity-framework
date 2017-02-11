package org.wso2.carbon.identity.gateway.common.model.idp;

import java.util.Properties;


public class AuthenticatorConfig{
    private String name ;
    private Properties properties = new Properties();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }


}
