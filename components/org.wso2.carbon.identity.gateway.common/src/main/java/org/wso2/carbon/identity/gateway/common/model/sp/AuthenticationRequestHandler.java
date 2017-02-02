package org.wso2.carbon.identity.gateway.common.model.sp;

import java.util.ArrayList;
import java.util.List;

class AuthenticationRequestHandler extends HandlerInterceptor {
    private List<ProtocolHandler> protocolHandler = new ArrayList<>();

    public List<ProtocolHandler> getProtocolHandler() {
        return protocolHandler;
    }

    public void setProtocolHandler(List<ProtocolHandler> protocolHandler) {
        this.protocolHandler = protocolHandler;
    }
}