package org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication.impl;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.processor.authenticator.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication
        .AuthenticationHandlerException;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication.impl.model.AbstractSequence;


import java.util.List;

public class SequenceManager extends FrameworkHandler {
    @Override
    public String getName() {
        return null;
    }

    public AuthenticationResponse handleSequence(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        AuthenticationResponse authenticationResponse = null ;
        AbstractSequence abstractSequence = authenticationContext.getAbstractSequence();
        if(abstractSequence.isRequestPathAuthenticatorsAvailable) {
            authenticationResponse = handleRequestPathAuthentication(authenticationContext);
        }
        if(authenticationResponse == null) {
            authenticationResponse = handleStepAuthentication(authenticationContext);
        }

        return authenticationResponse;
    }

    protected AuthenticationResponse handleRequestPathAuthentication(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        AuthenticationResponse authenticationResponse = null ;
        List<ApplicationAuthenticator> requestPathApplicationAuthenticators =
                FrameworkServiceDataHolder.getInstance().getRequestPathApplicationAuthenticators();
        for (ApplicationAuthenticator applicationAuthenticator: requestPathApplicationAuthenticators){
            if(applicationAuthenticator.canHandle(authenticationContext)){
                authenticationResponse = applicationAuthenticator.process(authenticationContext);
            }
        }
        return authenticationResponse;
    }

    protected AuthenticationResponse handleStepAuthentication(AuthenticationContext authenticationContext) {
        AuthenticationResponse authenticationResponse = null ;
        List<ApplicationAuthenticator> applicationAuthenticators =
                FrameworkServiceDataHolder.getInstance().getLocalApplicationAuthenticators();
        List<ApplicationAuthenticator> federatedApplicationAuthenticators =
                FrameworkServiceDataHolder.getInstance().getFederatedApplicationAuthenticators();
        applicationAuthenticators.addAll(federatedApplicationAuthenticators);
        for(ApplicationAuthenticator applicationAuthenticator: applicationAuthenticators){
            if(applicationAuthenticator.canHandle(authenticationContext)){
                authenticationResponse = applicationAuthenticator.process(authenticationContext);
            }
        }
        return authenticationResponse;
    }


}
