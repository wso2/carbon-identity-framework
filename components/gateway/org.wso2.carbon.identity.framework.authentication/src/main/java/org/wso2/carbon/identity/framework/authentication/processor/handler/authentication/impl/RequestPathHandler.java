package org.wso2.carbon.identity.framework.authentication.processor.handler.authentication.impl;

import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.framework.authentication.context.AuthenticationContext;
import org.wso2.carbon.identity.framework.authentication.processor.authenticator.RequestPathApplicationAuthenticator;
import org.wso2.carbon.identity.framework.authentication.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.framework.authentication.processor.handler.authentication
        .AuthenticationHandlerException;
import org.wso2.carbon.identity.framework.authentication.processor.handler.authentication.impl.model.AbstractSequence;
import org.wso2.carbon.identity.framework.authentication.processor.handler.authentication.impl.util.Utility;

public class RequestPathHandler extends FrameworkHandler {
    @Override
    public String getName() {
        return null;
    }

    public AuthenticationResponse handleRequestPathAuthentication(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        AuthenticationResponse authenticationResponse = null;
        AbstractSequence sequence = authenticationContext.getSequence();

        RequestPathAuthenticatorConfig[] requestPathAuthenticatorConfigs = sequence.getRequestPathAuthenticatorConfig();
        for (RequestPathAuthenticatorConfig requestPathAuthenticatorConfig : requestPathAuthenticatorConfigs) {
            RequestPathApplicationAuthenticator requestPathApplicationAuthenticator =
                    Utility.getRequestPathApplicationAuthenticator(requestPathAuthenticatorConfig.getName());
            if (requestPathApplicationAuthenticator.canHandle(authenticationContext)) {
                authenticationResponse = requestPathApplicationAuthenticator.process(authenticationContext);
            }
        }
        return authenticationResponse;
    }

    @Override
    public boolean canHandle(MessageContext messageContext) {
        return true ;
    }
}
