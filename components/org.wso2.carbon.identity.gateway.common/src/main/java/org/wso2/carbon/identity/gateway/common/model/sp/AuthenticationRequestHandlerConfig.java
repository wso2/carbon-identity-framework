package org.wso2.carbon.identity.gateway.common.model.sp;

import java.util.ArrayList;
import java.util.List;

class AuthenticationRequestHandlerConfig extends GenericHandlerConfig {
    private List<ProtocolHandlerConfig> protocolHandler = new ArrayList<>();

    public List<ProtocolHandlerConfig> getProtocolHandler() {
        return protocolHandler;
    }

    public void setProtocolHandler(List<ProtocolHandlerConfig> protocolHandler) {
        this.protocolHandler = protocolHandler;
    }
}