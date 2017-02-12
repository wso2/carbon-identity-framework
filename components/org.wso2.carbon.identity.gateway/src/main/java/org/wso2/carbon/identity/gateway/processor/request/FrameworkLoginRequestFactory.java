package org.wso2.carbon.identity.gateway.processor.request;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.gateway.api.FrameworkClientException;
import org.wso2.carbon.identity.gateway.api.HttpIdentityRequestFactory;
import org.wso2.carbon.identity.gateway.api.HttpIdentityResponse;
import org.wso2.msf4j.Request;


public class FrameworkLoginRequestFactory<T extends LocalAuthenticationRequest.LocalAuthenticationRequestBuilder> extends HttpIdentityRequestFactory<T>{
    @Override
    public boolean canHandle(Request request) {
        String authenticatorName = (String) request.getProperty(LocalAuthenticationRequest.FrameworkLoginRequestConstants
                                                        .AUTHENTICATOR_NAME);
        String idpName = (String)request.getProperty(LocalAuthenticationRequest.FrameworkLoginRequestConstants.IDP_NAME);
        if(StringUtils.isNotBlank(authenticatorName) && StringUtils.isNotBlank(idpName)) {
            return true;
        }
        return false ;
    }

    @Override
    public void create(T builder, Request request)
            throws FrameworkClientException {

        super.create(builder, request);
        LocalAuthenticationRequest.LocalAuthenticationRequestBuilder localAuthenticationRequestBuilder = (LocalAuthenticationRequest.LocalAuthenticationRequestBuilder)builder ;

        localAuthenticationRequestBuilder.setAuthenticatorName((String) request.getProperty(LocalAuthenticationRequest
                                                                                .FrameworkLoginRequestConstants.AUTHENTICATOR_NAME));
        localAuthenticationRequestBuilder.setIdentityProviderName((String)request.getProperty(LocalAuthenticationRequest
                                                                                   .FrameworkLoginRequestConstants
                                                                                   .IDP_NAME));
    }


    @Override
    public LocalAuthenticationRequest.LocalAuthenticationRequestBuilder create(Request request)
            throws FrameworkClientException {

        LocalAuthenticationRequest.LocalAuthenticationRequestBuilder localAuthenticationRequestBuilder =  new LocalAuthenticationRequest.LocalAuthenticationRequestBuilder();

        localAuthenticationRequestBuilder.setAuthenticatorName((String)request.getProperty(LocalAuthenticationRequest
                                                                                .FrameworkLoginRequestConstants
                                                                                .AUTHENTICATOR_NAME));
        localAuthenticationRequestBuilder.setIdentityProviderName((String)request.getProperty(LocalAuthenticationRequest
                                                                                   .FrameworkLoginRequestConstants
                                                                                   .IDP_NAME));
        return localAuthenticationRequestBuilder;
    }

    @Override
    public HttpIdentityResponse.HttpIdentityResponseBuilder handleException(FrameworkClientException exception) {
        return super.handleException(exception);
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public int getPriority() {
        return super.getPriority();
    }

}
