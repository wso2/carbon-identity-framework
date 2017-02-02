package org.wso2.carbon.identity.gateway.common.model.sp;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

class HandlerInterceptor {
    private List<Handler> preHandlers = new ArrayList<>();
    private List<Handler> postHandlers = new ArrayList<>();
    private Properties properties = new Properties();

    public List<Handler> getPostHandlers() {
        return postHandlers;
    }

    public void setPostHandlers(List<Handler> postHandlers) {
        this.postHandlers = postHandlers;
    }

    public List<Handler> getPreHandlers() {
        return preHandlers;
    }

    public void setPreHandlers(List<Handler> preHandlers) {
        this.preHandlers = preHandlers;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}