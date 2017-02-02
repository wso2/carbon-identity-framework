package org.wso2.carbon.identity.gateway.common.model.sp;

import java.util.Properties;

class HandlerConfig {
    private String name;
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