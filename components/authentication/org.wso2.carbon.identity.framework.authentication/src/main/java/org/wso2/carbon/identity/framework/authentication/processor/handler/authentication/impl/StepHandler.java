package org.wso2.carbon.identity.framework.authentication.processor.handler.authentication.impl;


import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.framework.IdentityRequest;
import org.wso2.carbon.identity.framework.authentication.context.AuthenticationContext;
import org.wso2.carbon.identity.framework.authentication.context.SequenceContext;
import org.wso2.carbon.identity.framework.authentication.processor.authenticator.ApplicationAuthenticator;
import org.wso2.carbon.identity.framework.authentication.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.framework.authentication.processor.handler.authentication
        .AuthenticationHandlerException;
import org.wso2.carbon.identity.framework.authentication.processor.handler.authentication.impl.model.AbstractSequence;
import org.wso2.carbon.identity.framework.authentication.processor.handler.authentication.impl.util.Utility;
import org.wso2.carbon.identity.framework.authentication.processor.request.LocalAuthenticationRequest;

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
                        Utility.getLocalApplicationAuthenticator(currentStepContext.getName());
                if (applicationAuthenticator == null) {
                    applicationAuthenticator =
                            Utility.getFederatedApplicationAuthenticator(currentStepContext.getName());
                }
            } else {
                authenticationResponse = AuthenticationResponse.AUTHENTICATED;
            }
        } else {
            if (sequence.getStep(sequenceContext.getCurrentStep()).isMultiOption()) {
                IdentityRequest identityRequest = authenticationContext.getIdentityRequest();
                String authenticatorName = null;
                if (identityRequest instanceof LocalAuthenticationRequest) {
                    LocalAuthenticationRequest localAuthenticationRequest =
                            (LocalAuthenticationRequest) identityRequest;
                    authenticatorName = localAuthenticationRequest.getAuthenticatorName();
                }

                if (StringUtils.isNotBlank(authenticatorName)) {
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
                } else {
                    IdentityProvider federatedIdentityProviderForSingleOption =
                            sequence.getFederatedIdentityProviderForSingleOption(sequenceContext.getCurrentStep());
                    applicationAuthenticator =
                            Utility.getFederatedApplicationAuthenticator(federatedIdentityProviderForSingleOption
                                                                                 .getDefaultAuthenticatorConfig()
                                                                                 .getName());
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


}
