package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl;


import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .model.Sequence;


public abstract class SequenceBuildFactory extends FrameworkHandler {
    public Sequence buildSequence(IdentityMessageContext identityMessageContext){
        return null ;
    }
}
