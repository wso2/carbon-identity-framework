package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl.context;



import java.io.Serializable;

public class SessionContext implements Serializable{
    private SessionContext sessionContext = null ;

    public SessionContext getSessionContext() {
        return sessionContext;
    }

    public void setSessionContext(
            SessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }
}
