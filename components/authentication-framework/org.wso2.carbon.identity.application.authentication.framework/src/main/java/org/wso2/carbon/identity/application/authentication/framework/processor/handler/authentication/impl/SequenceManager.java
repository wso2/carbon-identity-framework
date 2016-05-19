package org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication.impl;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SequenceContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.processor.authenticator.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.processor.authenticator
        .FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.processor.authenticator
        .LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.processor.authenticator
        .RequestPathApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication
        .AuthenticationHandlerException;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication.impl.model.AbstractSequence;


import org.wso2.carbon.identity.application.common.model.AuthenticationStep;


import java.util.List;

public class SequenceManager extends FrameworkHandler {
    @Override
    public String getName() {
        return null;
    }

    public AuthenticationResponse handleSequence(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        AuthenticationResponse authenticationResponse = null ;
        AbstractSequence abstractSequence = authenticationContext.getSequence();
        if(abstractSequence.isRequestPathAuthenticatorsAvailable()) {
            authenticationResponse = handleRequestPathAuthentication(authenticationContext);
        }
        if(authenticationResponse == null && abstractSequence.isStepAuthenticatorAvailable()) {
            authenticationResponse = handleStepAuthentication(authenticationContext);
        }

        return authenticationResponse;
    }

    protected AuthenticationResponse handleRequestPathAuthentication(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        AuthenticationResponse authenticationResponse = null ;
        List<RequestPathApplicationAuthenticator> requestPathApplicationAuthenticators =
                FrameworkServiceDataHolder.getInstance().getRequestPathApplicationAuthenticators();
        for (RequestPathApplicationAuthenticator requestPathApplicationAuthenticator: requestPathApplicationAuthenticators){
            if(requestPathApplicationAuthenticator.canHandle(authenticationContext)){
                authenticationResponse = requestPathApplicationAuthenticator.process(authenticationContext);
            }
        }
        return authenticationResponse;
    }

    protected AuthenticationResponse handleStepAuthentication(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        AuthenticationResponse authenticationResponse = null ;
        AbstractSequence abstractSequence = authenticationContext.getSequence();
        SequenceContext sequenceContext = authenticationContext.getSequenceContext();
        AuthenticationStep[] stepAuthenticatorConfig = abstractSequence.getStepAuthenticatorConfig();
        if(sequenceContext == null){
            sequenceContext = new SequenceContext();
            authenticationContext.setSequenceContext(sequenceContext);
        }
        if(sequenceContext.getCurrentStepAuthenticator() == null ){

            SequenceContext.StepAuthenticatorContext currentStepAuthenticator = new SequenceContext
                    .StepAuthenticatorContext();
            currentStepAuthenticator.setIsAuthenticated(false);
            currentStepAuthenticator.setStep(1);
            sequenceContext.addCurrentStepAuthenticatorContext(currentStepAuthenticator);

            authenticationResponse = handleLocalAuthenticator(authenticationContext);
            if(authenticationResponse == null){
                authenticationResponse = handleFederatedAuthenticator(authenticationContext);
            }
        }else{
            if(sequenceContext.getCurrentStepAuthenticator().isAuthenticated()){
                if(stepAuthenticatorConfig.length > sequenceContext.getCurrentStepAuthenticator().getStep()){
                    AuthenticationStep authenticationStep =
                            stepAuthenticatorConfig[sequenceContext.getCurrentStepAuthenticator().getStep() + 1];
                    SequenceContext.StepAuthenticatorContext currentStepAuthenticator = new SequenceContext
                            .StepAuthenticatorContext();
                    currentStepAuthenticator.setIsAuthenticated(false);
                    currentStepAuthenticator.setStep(2);
                    sequenceContext.addCurrentStepAuthenticatorContext(currentStepAuthenticator);
                }
            }else{

            }
        }
        return authentticationResponse;
    }

    protected AuthenticationResponse handleLocalAuthenticator(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        AuthenticationResponse authenticationResponse = null ;
        List<LocalApplicationAuthenticator> localApplicationAuthenticators =
                FrameworkServiceDataHolder.getInstance().getLocalApplicationAuthenticators();
        for(LocalApplicationAuthenticator localApplicationAuthenticator: localApplicationAuthenticators){
            if(localApplicationAuthenticator.canHandle(authenticationContext)){
                authenticationResponse = localApplicationAuthenticator.process(authenticationContext);
            }
        }
        return authenticationResponse ;
    }

    protected AuthenticationResponse handleFederatedAuthenticator(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        AuthenticationResponse authenticationResponse = null ;
        List<FederatedApplicationAuthenticator> federatedApplicationAuthenticators =
                FrameworkServiceDataHolder.getInstance().getFederatedApplicationAuthenticators();
        for(FederatedApplicationAuthenticator federatedApplicationAuthenticator: federatedApplicationAuthenticators){
            if(federatedApplicationAuthenticator.canHandle(authenticationContext)){
                authenticationResponse = federatedApplicationAuthenticator.process(authenticationContext);
            }
        }
        return authenticationResponse ;
    }


}
