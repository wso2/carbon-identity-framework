package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl.handler;


import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .model.Sequence;


public class SequenceConfigBuildHandler extends FrameworkHandler {
    @Override
    public String getName() {
        return null;
    }

    public Sequence buildSequenceConfig(IdentityMessageContext identityMessageContext){
        //identityMessageContext.getIdentityRequest().getParameter()
        //SequenceConfig sequenceConfig = new SequenceConfig();
        return null ;
    }
}
