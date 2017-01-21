package org.wso2.carbon.identity.gateway.processor.request;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.gateway.api.FrameworkClientException;
import org.wso2.carbon.identity.gateway.api.HttpIdentityRequestFactory;
import org.wso2.carbon.identity.gateway.api.HttpIdentityResponse;
import org.wso2.carbon.identity.gateway.api.IdentityRequest;
import org.wso2.msf4j.Request;


public class FrameworkLoginRequestFactory<T extends  FrameworkLoginRequest.FrameworkLoginBuilder> extends HttpIdentityRequestFactory<T>{
    @Override
    public boolean canHandle(Request request) {
        String authenticatorName = (String) request.getProperty(FrameworkLoginRequest.FrameworkLoginRequestConstants
                                                        .AUTHENTICATOR_NAME);
        String idpName = (String)request.getProperty(FrameworkLoginRequest.FrameworkLoginRequestConstants.IDP_NAME);
        if(StringUtils.isNotBlank(authenticatorName) && StringUtils.isNotBlank(idpName)) {
            return true;
        }
        return false ;
    }

    @Override
    public void create(T builder, Request request)
            throws FrameworkClientException {

        super.create(builder, request);
        FrameworkLoginRequest.FrameworkLoginBuilder frameworkLoginBuilder = (FrameworkLoginRequest.FrameworkLoginBuilder)builder ;

        frameworkLoginBuilder.setAuthenticatorName((String) request.getProperty(FrameworkLoginRequest
                                                                                .FrameworkLoginRequestConstants.AUTHENTICATOR_NAME));
        frameworkLoginBuilder.setIdentityProviderName((String)request.getProperty(FrameworkLoginRequest
                                                                                   .FrameworkLoginRequestConstants
                                                                                   .IDP_NAME));
    }


    @Override
    public FrameworkLoginRequest.FrameworkLoginBuilder create(Request request)
            throws FrameworkClientException {

        FrameworkLoginRequest.FrameworkLoginBuilder frameworkLoginBuilder =  new FrameworkLoginRequest
                .FrameworkLoginBuilder();

        frameworkLoginBuilder.setAuthenticatorName((String)request.getProperty(FrameworkLoginRequest
                                                                                .FrameworkLoginRequestConstants
                                                                                .AUTHENTICATOR_NAME));
        frameworkLoginBuilder.setIdentityProviderName((String)request.getProperty(FrameworkLoginRequest
                                                                                   .FrameworkLoginRequestConstants
                                                                                   .IDP_NAME));
        return frameworkLoginBuilder ;
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
