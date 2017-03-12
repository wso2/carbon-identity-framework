package org.wso2.carbon.identity.gateway.authentication.authenticator.impl;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequest;
import org.wso2.carbon.identity.gateway.authentication.AbstractSequence;
import org.wso2.carbon.identity.gateway.authentication.AuthenticationResponse;
import org.wso2.carbon.identity.gateway.authentication.authenticator.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.authentication.authenticator.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.authentication.local.LocalAuthenticationRequest;
import org.wso2.carbon.identity.gateway.authentication.local.LocalAuthenticationResponse;
import org.wso2.carbon.identity.gateway.common.model.idp.AuthenticationConfig;
import org.wso2.carbon.identity.gateway.common.model.idp.AuthenticatorConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.AuthenticationStepConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.IdentityProvider;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.context.SequenceContext;
import org.wso2.carbon.identity.gateway.exception.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.internal.GatewayActivator;
import org.wso2.carbon.identity.gateway.internal.GatewayServiceHolder;
import org.wso2.carbon.identity.gateway.model.LocalUser;
import org.wso2.carbon.identity.gateway.request.AuthenticationRequest;
import org.wso2.carbon.identity.gateway.request.ClientAuthenticationRequest;
import org.wso2.carbon.identity.mgt.IdentityStore;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.impl.IdentityStoreImpl;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by harshat on 3/9/17.
 */
public class BasicAuthenticator extends AbstractApplicationAuthenticator implements LocalApplicationAuthenticator{

    public  static final String IS_RETRY_ENABLE = "isRetryEnable" ;
    private Logger log = LoggerFactory.getLogger(BasicAuthenticator.class);
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
        return "BasicAuthenticator";
    }

    @Override
    public String getName() {
        return "BasicAuthenticator";
    }




    @Override
    protected AuthenticationResponse processRequest(AuthenticationContext context)
            throws AuthenticationHandlerException {


        if(context.getIdentityRequest() instanceof LocalAuthenticationRequest){
            LocalAuthenticationRequest  localAuthenticationRequest = (LocalAuthenticationRequest)context.getIdentityRequest();
            SequenceContext sequenceContext = context.getSequenceContext();
            SequenceContext.StepContext currentStepContext = sequenceContext.getCurrentStepContext();

            RealmService realmService = GatewayServiceHolder.getInstance().getRealmService();
            IdentityStore identityStore = realmService.getIdentityStore();
            Claim claim = new Claim("http://wso2.org/claims", "http://wso2.org/claims/username",
                    localAuthenticationRequest.getUserName());
            PasswordCallback passwordCallback = new PasswordCallback("psasword", false);
            passwordCallback.setPassword(localAuthenticationRequest.getPassword().toCharArray());

            org.wso2.carbon.identity.mgt.AuthenticationContext context1 = null;
            try {
                context1 = identityStore.authenticate(claim,new
                        PasswordCallback[]{passwordCallback},"PRIMARY");
            } catch (IdentityStoreException e) {
                String error = "Error occurred while authetnicating the user," + e.getMessage() ;
                log.error(error, e);
                throw new GatewayRuntimeException(error , e);
            }  catch (AuthenticationFailure authenticationFailure) {
                throw new AuthenticationHandlerException("Authentication Failed.");
            }

            User user = context1.getUser();
            LocalUser localUser = new LocalUser(user);
            currentStepContext.setUser(localUser);
            return AuthenticationResponse.AUTHENTICATED;
        }
        LocalAuthenticationResponse.LocalAuthenticationResponseBuilder
                localAuthenticationResponseBuilder = new LocalAuthenticationResponse
                .LocalAuthenticationResponseBuilder();
        localAuthenticationResponseBuilder.setRelayState(context
                .getInitialAuthenticationRequest()
                .getRequestKey());
        localAuthenticationResponseBuilder.setEndpointURL("https://localhost:9292/gateway/endpoint");
        List<IdentityProvider> identityProviders = context.getSequence()
                .getIdentityProviders(context.getSequenceContext().getCurrentStep());
        StringBuilder idpList = new StringBuilder();
        identityProviders.forEach(identityProvider -> idpList.append(identityProvider
                .getAuthenticatorName() +
                ":" + identityProvider
                .getIdentityProviderName()
                +","));
        localAuthenticationResponseBuilder.setIdentityProviderList(idpList.toString());
        AuthenticationResponse authenticationResponse = AuthenticationResponse.INCOMPLETE ;
        authenticationResponse.setGatewayResponseBuilder(localAuthenticationResponseBuilder);
        return authenticationResponse;
    }

    @Override
    protected AuthenticationResponse processResponse(AuthenticationContext context)
            throws AuthenticationHandlerException {
        LocalAuthenticationRequest  localAuthenticationRequest = (LocalAuthenticationRequest)context.getIdentityRequest();
        try {
            SequenceContext sequenceContext = context.getSequenceContext();
            SequenceContext.StepContext currentStepContext = sequenceContext.getCurrentStepContext();

            RealmService realmService = GatewayServiceHolder.getInstance().getRealmService();
            IdentityStore identityStore = realmService.getIdentityStore();
            Claim claim = new Claim("http://wso2.org/claims", "http://wso2.org/claims/username",
                    localAuthenticationRequest.getUserName());
            PasswordCallback passwordCallback = new PasswordCallback("psasword", false);
            passwordCallback.setPassword(localAuthenticationRequest.getPassword().toCharArray());

            org.wso2.carbon.identity.mgt.AuthenticationContext context1 = identityStore.authenticate(claim,new
                    PasswordCallback[]{passwordCallback},"PRIMARY");
            if(context1.isAuthenticated()){
                User user = context1.getUser();
                LocalUser localUser = new LocalUser(user);
                currentStepContext.setUser(localUser);
                return AuthenticationResponse.AUTHENTICATED;
            }
            throw new AuthenticationHandlerException("Authentication Failed.");
        } catch (IdentityStoreException e) {
            String error = "Error occurred while authetnicating the user," + e.getMessage() ;
            log.error(error, e);
            throw new GatewayRuntimeException(error , e);
        }  catch (AuthenticationFailure authenticationFailure) {
            throw new AuthenticationHandlerException("Authentication Failed.", authenticationFailure);
        }
    }

    @Override
    public boolean isRetryEnable(AuthenticationContext context) {
        AbstractSequence sequence = context.getSequence();
        SequenceContext.StepContext currentStepContext = context.getSequenceContext().getCurrentStepContext();
        AuthenticatorConfig authenticatorConfig = sequence.getAuthenticatorConfig(currentStepContext.getStep(), currentStepContext
                        .getAuthenticatorName(),
                currentStepContext.getIdentityProviderName());
        String retryEnableString = (String)authenticatorConfig.getProperties().get(IS_RETRY_ENABLE);
        boolean isRetryEnable = BooleanUtils.toBoolean(retryEnableString);
        return isRetryEnable ;
    }
}
