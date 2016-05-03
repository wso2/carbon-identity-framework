package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.extension;

import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandlerStatus;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;

public abstract class AbstractExtensionHandler extends FrameworkHandler {

    private ExtensionExecutionPoint extensionExecutionPoint = null ;

    protected AbstractExtensionHandler(ExtensionExecutionPoint extensionExecutionPoint){
        this.extensionExecutionPoint = extensionExecutionPoint ;
    }

    protected abstract FrameworkHandlerStatus handle(IdentityMessageContext identityMessageContext) throws ExtensionHandlerException ;
}
