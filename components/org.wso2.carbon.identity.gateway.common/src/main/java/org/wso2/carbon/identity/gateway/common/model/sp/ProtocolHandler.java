package org.wso2.carbon.identity.gateway.common.model.sp;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

class ProtocolHandler extends HandlerInterceptor {
    private String type;
    private List<Properties> protocolHandler = new ArrayList<>();

    public List<Properties> getProtocolHandler() {
        return protocolHandler;
    }

    public void setProtocolHandler(List<Properties> protocolHandler) {
        this.protocolHandler = protocolHandler;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}