package org.wso2.carbon.identity.gateway.processor.handler.authentication.impl;


import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.gateway.common.model.IdentityProvider;
import org.wso2.carbon.identity.gateway.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.gateway.api.IdentityRequest;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.context.SequenceContext;
import org.wso2.carbon.identity.gateway.processor.authenticator.ApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.model.AbstractSequence;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.util.Utility;
import org.wso2.carbon.identity.gateway.processor.request.FrameworkLoginRequest;

public class StepHandler extends FrameworkHandler {
    @Override
    public String getName() {
        return null;
    }

    public AuthenticationResponse handleStepAuthentication(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {

        AuthenticationResponse authenticationResponse = null;
        ApplicationAuthenticator applicationAuthenticator = null;
        AbstractSequence sequence = authenticationContext.getSequence();

        SequenceContext sequenceContext = authenticationContext.getSequenceContext();
        SequenceContext.StepContext currentStepContext = sequenceContext.getCurrentStepContext();

        if (currentStepContext != null) {
            if (!currentStepContext.isAuthenticated()) {
                applicationAuthenticator =
                        Utility.getLocalApplicationAuthenticator(currentStepContext.getAuthenticatorName());
                if (applicationAuthenticator == null) {
                    applicationAuthenticator =
                            Utility.getFederatedApplicationAuthenticator(currentStepContext.getAuthenticatorName());
                }
            } else {
                authenticationResponse = AuthenticationResponse.AUTHENTICATED;
            }
        } else {
            currentStepContext = sequenceContext.addStepContext();
            if (sequence.getStep(sequenceContext.getCurrentStep()).isMultiOption()) {
                IdentityRequest identityRequest = authenticationContext.getIdentityRequest();
                String authenticatorName = null;
                if (identityRequest instanceof FrameworkLoginRequest) {
                    FrameworkLoginRequest frameworkLoginRequest =
                            (FrameworkLoginRequest) identityRequest;
                    authenticatorName = frameworkLoginRequest.getAuthenticatorName();
                    currentStepContext.setIdentityProviderName(frameworkLoginRequest.getIdentityProviderName());
                }

                if (StringUtils.isNotBlank(authenticatorName)) {
                    currentStepContext.setAuthenticatorName(authenticatorName);
                    applicationAuthenticator =
                            Utility.getLocalApplicationAuthenticator(authenticatorName);
                    if (applicationAuthenticator == null) {
                        applicationAuthenticator = Utility.getFederatedApplicationAuthenticator(authenticatorName);
                    }
                } else {
                    authenticationResponse = AuthenticationResponse.INCOMPLETE;
                    //Should set redirect URL ;
                }

            } else {
                LocalAuthenticatorConfig localAuthenticatorConfigForSingleOption =
                        sequence.getLocalAuthenticatorConfigForSingleOption(sequenceContext.getCurrentStep());
                if (localAuthenticatorConfigForSingleOption != null) {
                    applicationAuthenticator =
                            Utility.getLocalApplicationAuthenticator(localAuthenticatorConfigForSingleOption.getName());
                    currentStepContext.setAuthenticatorName(localAuthenticatorConfigForSingleOption.getName());
                } else {
                    IdentityProvider federatedIdentityProviderForSingleOption =
                            sequence.getFederatedIdentityProviderForSingleOption(sequenceContext.getCurrentStep());
                    applicationAuthenticator =
                            Utility.getFederatedApplicationAuthenticator(federatedIdentityProviderForSingleOption
                                                                                 .getDefaultAuthenticatorConfig()
                                                                                 .getName());
                    currentStepContext.setAuthenticatorName(applicationAuthenticator.getName());
                    currentStepContext.setIdentityProviderName(federatedIdentityProviderForSingleOption.getIdentityProviderName());
                }
            }
        }
        if (applicationAuthenticator != null) {
            authenticationResponse = applicationAuthenticator.process(authenticationContext);
        }

        if (AuthenticationResponse.AUTHENTICATED.equals(authenticationResponse)) {
            currentStepContext.setIsAuthenticated(true);
            int nextStep = sequenceContext.getCurrentStep() + 1;
            if (sequence.hasNext(nextStep)) {
                authenticationResponse = handleStepAuthentication(authenticationContext);
            }
        }

        return authenticationResponse;
    }

    @Override
    public boolean canHandle(MessageContext messageContext) {
        return true ;
    }
}
