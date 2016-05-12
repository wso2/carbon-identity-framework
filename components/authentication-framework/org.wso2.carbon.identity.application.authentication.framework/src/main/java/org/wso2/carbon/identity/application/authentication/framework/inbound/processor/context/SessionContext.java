package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.context;



import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .context.SequenceContext;

import java.io.Serializable;

public class SessionContext implements Serializable{
    private SequenceContext sequenceContext = null ;

    public SequenceContext getSequenceContext() {
        return sequenceContext;
    }

    public void setSequenceContext(
            SequenceContext sequenceContext) {
        this.sequenceContext = sequenceContext;
    }
}
