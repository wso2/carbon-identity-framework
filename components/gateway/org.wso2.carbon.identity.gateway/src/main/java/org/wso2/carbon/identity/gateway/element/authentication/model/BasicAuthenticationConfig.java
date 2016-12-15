package org.wso2.carbon.identity.gateway.element.authentication.model;

import org.wso2.carbon.identity.framework.handler.HandlerConfig;

import java.io.Serializable;

public class BasicAuthenticationConfig extends HandlerConfig implements Serializable{
    private String callbackURL = null ;

    public String getCallbackURL() {
        return callbackURL;
    }

    public void setCallbackURL(String callbackURL) {
        this.callbackURL = callbackURL;
    }
}

