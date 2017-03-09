package org.wso2.carbon.identity.gateway.authentication.authenticator.impl;

import org.wso2.carbon.identity.gateway.api.request.GatewayRequest;
import org.wso2.carbon.identity.gateway.authentication.AuthenticationResponse;
import org.wso2.carbon.identity.gateway.authentication.authenticator.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.authentication.authenticator.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.authentication.local.LocalAuthenticationRequest;
import org.wso2.carbon.identity.gateway.authentication.local.LocalAuthenticationResponse;
import org.wso2.carbon.identity.gateway.common.model.sp.IdentityProvider;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.exception.AuthenticationHandlerException;

import java.util.List;
import java.util.Properties;

/**
 * Created by harshat on 3/9/17.
 */
public class BasicAuthenticator extends AbstractApplicationAuthenticator implements LocalApplicationAuthenticator{
    @Override
    public boolean canHandle(AuthenticationContext authenticationContext) {
        return true;
    }

    @Override
    public String getClaimDialectURI() {
        return null;
    }

    @Override
    public List<Properties> getConfigurationProperties() {
        return null;
    }

    @Override
    public String getContextIdentifier(AuthenticationContext authenticationContext) {
        return null;
    }

    @Override
    public String getFriendlyName() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    protected boolean isInitialRequest(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        LocalAuthenticationRequest localAuthenticationRequest = (LocalAuthenticationRequest)authenticationContext
                .getIdentityRequest();
        if(localAuthenticationRequest.getUserName() == null || localAuthenticationRequest.getPassword() ==null){
            return true ;
        }
        return false;
    }

    @Override
    protected AuthenticationResponse processRequest(AuthenticationContext context)
            throws AuthenticationHandlerException {
        return null;
    }

    @Override
    protected AuthenticationResponse processResponse(AuthenticationContext context)
            throws AuthenticationHandlerException {
        return null;
    }

    private AuthenticationResponse buildResponse(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        LocalAuthenticationResponse.LocalAuthenticationResponseBuilder
                localAuthenticationResponseBuilder = new LocalAuthenticationResponse
                .LocalAuthenticationResponseBuilder();
        localAuthenticationResponseBuilder.setRelayState(authenticationContext
                                                                 .getInitialAuthenticationRequest()
                                                                 .getRequestKey());
        List<IdentityProvider> identityProviders = authenticationContext.getSequence()
                .getIdentityProviders(authenticationContext.getSequenceContext().getCurrentStep());
        StringBuilder idpList = new StringBuilder();
        identityProviders.forEach(identityProvider -> idpList.append(identityProvider
                                                                             .getAuthenticatorName() +
                                                                     ":" + identityProvider
                                                                             .getIdentityProviderName()
                                                                     + ","));
        AuthenticationResponse authenticationResponse = AuthenticationResponse.INCOMPLETE;
        authenticationResponse.setGatewayResponseBuilder(localAuthenticationResponseBuilder);
        return authenticationResponse;
    }
}
