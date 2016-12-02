package org.wso2.carbon.identity.gateway.processor.handler.extension;


import org.wso2.carbon.identity.framework.context.IdentityMessageContext;
import org.wso2.carbon.identity.framework.response.FrameworkHandlerResponse;
import org.wso2.carbon.identity.gateway.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.gateway.processor.handler.FrameworkHandlerException;

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
