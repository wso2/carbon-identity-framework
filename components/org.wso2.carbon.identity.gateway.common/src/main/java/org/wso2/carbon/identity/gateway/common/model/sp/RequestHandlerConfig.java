package org.wso2.carbon.identity.gateway.common.model.sp;

import java.util.Properties;

class RequestHandlerConfig {
    private String type;
    private String uniquePropertyName ;

    private Properties properties = new Properties();

    public String getUniquePropertyName() {
        return uniquePropertyName;
    }

    public void setUniquePropertyName(String uniquePropertyName) {
        this.uniquePropertyName = uniquePropertyName;
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