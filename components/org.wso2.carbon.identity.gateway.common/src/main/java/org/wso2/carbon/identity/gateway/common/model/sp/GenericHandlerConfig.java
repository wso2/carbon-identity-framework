package org.wso2.carbon.identity.gateway.common.model.sp;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

class GenericHandlerConfig {
    private List<HandlerConfig> preHandlerConfigs = new ArrayList<>();
    private List<HandlerConfig> postHandlerConfigs = new ArrayList<>();
    private Properties properties = new Properties();

    public List<HandlerConfig> getPostHandlerConfigs() {
        return postHandlerConfigs;
    }

    public void setPostHandlerConfigs(List<HandlerConfig> postHandlerConfigs) {
        this.postHandlerConfigs = postHandlerConfigs;
    }

    public List<HandlerConfig> getPreHandlerConfigs() {
        return preHandlerConfigs;
    }

    public void setPreHandlerConfigs(List<HandlerConfig> preHandlerConfigs) {
        this.preHandlerConfigs = preHandlerConfigs;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}