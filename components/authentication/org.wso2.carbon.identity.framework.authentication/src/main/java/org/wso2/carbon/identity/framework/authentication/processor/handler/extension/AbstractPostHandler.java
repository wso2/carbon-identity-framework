package org.wso2.carbon.identity.framework.authentication.processor.handler.extension;


import org.wso2.carbon.identity.framework.authentication.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.framework.authentication.processor.handler.FrameworkHandlerException;
import org.wso2.carbon.identity.gateway.context.IdentityMessageContext;
import org.wso2.carbon.identity.gateway.response.FrameworkHandlerResponse;

public abstract class AbstractPostHandler extends FrameworkHandler {
    private ExtensionHandlerPoints extensionHandlerPoints;

    protected AbstractPostHandler(ExtensionHandlerPoints extensionHandlerPoints) {
        this.extensionHandlerPoints = extensionHandlerPoints;
    }

    public ExtensionHandlerPoints getExtensionHandlerPoints() {
        return extensionHandlerPoints;
    }

    public abstract FrameworkHandlerResponse handle(IdentityMessageContext identityMessageContext) throws
            FrameworkHandlerException;
}
