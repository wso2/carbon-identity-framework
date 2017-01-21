package org.wso2.carbon.identity.gateway.context;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SessionContext implements Serializable {

    //Sp->SequenceContext
    private Map<String, SequenceContext> sequenceContexts = new HashMap<>();

    public SequenceContext getSequenceContext(String serviceProvider) {
        SequenceContext sequenceContext = sequenceContexts.get(serviceProvider);
        return sequenceContext;
    }

    public void addSequenceContext(String serviceProvider, SequenceContext sequenceContext) {
        if (sequenceContext != null) {
            this.sequenceContexts.put(serviceProvider, sequenceContext);
        }
    }
}
