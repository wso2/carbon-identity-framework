package org.wso2.carbon.identity.gateway.common.model.sp;

import java.util.Properties;

public class ProtocolHandlerConfig{

    private String type;
    private Properties properties = new Properties();

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