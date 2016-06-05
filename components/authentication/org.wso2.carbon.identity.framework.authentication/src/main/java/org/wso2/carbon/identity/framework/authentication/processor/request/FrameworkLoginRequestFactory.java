package org.wso2.carbon.identity.framework.authentication.processor.request;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.framework.FrameworkClientException;
import org.wso2.carbon.identity.framework.HttpIdentityRequestFactory;
import org.wso2.carbon.identity.framework.HttpIdentityResponse;
import org.wso2.carbon.identity.framework.IdentityRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FrameworkLoginRequestFactory<T extends  FrameworkLoginRequest.FrameworkLoginBuilder> extends HttpIdentityRequestFactory<T>{
    @Override
    public boolean canHandle(HttpServletRequest request, HttpServletResponse response) {
        String authenticatorName = request.getParameter(FrameworkLoginRequest.FrameworkLoginRequestConstants.AUTHENTICATOR_NAME);
        String idpName = request.getParameter(FrameworkLoginRequest.FrameworkLoginRequestConstants.IDP_NAME);
        if(StringUtils.isNotBlank(authenticatorName) && StringUtils.isNotBlank(idpName)) {
            return true;
        }
        return false ;
    }

    @Override
    public void create(T builder, HttpServletRequest request, HttpServletResponse response)
            throws FrameworkClientException {

        super.create(builder, request, response);
        FrameworkLoginRequest.FrameworkLoginBuilder frameworkLoginBuilder = (FrameworkLoginRequest.FrameworkLoginBuilder)builder ;

        frameworkLoginBuilder.setAuthenticatorName(request.getParameter(FrameworkLoginRequest.FrameworkLoginRequestConstants.AUTHENTICATOR_NAME));
        frameworkLoginBuilder.setIdentityProviderName(request.getParameter(FrameworkLoginRequest
                                                                                   .FrameworkLoginRequestConstants
                                                                                   .IDP_NAME));
    }


    @Override
    public FrameworkLoginRequest.FrameworkLoginBuilder create(HttpServletRequest request, HttpServletResponse response)
            throws FrameworkClientException {

        FrameworkLoginRequest.FrameworkLoginBuilder frameworkLoginBuilder =  new FrameworkLoginRequest
                .FrameworkLoginBuilder();

        frameworkLoginBuilder.setAuthenticatorName(request.getParameter(FrameworkLoginRequest
                                                                                .FrameworkLoginRequestConstants
                                                                                .AUTHENTICATOR_NAME));
        frameworkLoginBuilder.setIdentityProviderName(request.getParameter(FrameworkLoginRequest
                                                                                   .FrameworkLoginRequestConstants
                                                                                   .IDP_NAME));
        return frameworkLoginBuilder ;
    }

    @Override
    public HttpIdentityResponse.HttpIdentityResponseBuilder handleException(FrameworkClientException exception,
                                                                            HttpServletRequest request,
                                                                            HttpServletResponse response) {
        return super.handleException(exception, request, response);
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
