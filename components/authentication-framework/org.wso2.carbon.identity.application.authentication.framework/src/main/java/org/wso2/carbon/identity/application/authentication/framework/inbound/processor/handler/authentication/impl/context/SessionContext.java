package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl.context;



import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SessionContext implements Serializable{

    //Cookie->Sp->SequenceContext
    private Map<String, Map<String, SequenceContext>> sequenceContexts = new HashMap<>();

    public SequenceContext getSequenceContext(String cookie, String serviceProvider){
        SequenceContext sequenceContext = null ;
        Map<String, SequenceContext> sequenceContextMap = getSequenceContext(cookie);
        if(sequenceContextMap != null){
            sequenceContext = sequenceContextMap.get(serviceProvider);
        }
        return sequenceContext ;
    }
    public Map<String,SequenceContext> getSequenceContext(String cookie){
        return this.sequenceContexts.get(cookie);
    }

    public void addSequenceContext(String cookie, String serviceProvider, SequenceContext sequenceContext){
        Map<String, SequenceContext> sequenceContextMap = getSequenceContext(cookie);
        if(sequenceContextMap == null){
            sequenceContextMap = new HashMap<>();
            this.sequenceContexts.put(cookie, sequenceContextMap);
        }

        SequenceContext sequenceContextForSP = sequenceContextMap.put(serviceProvider, sequenceContext);
    }
}
