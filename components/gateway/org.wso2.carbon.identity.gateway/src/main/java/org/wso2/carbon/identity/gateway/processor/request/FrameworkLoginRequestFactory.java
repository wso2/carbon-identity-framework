package org.wso2.carbon.identity.gateway.processor.request;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.gateway.framework.exception.FrameworkClientException;
import org.wso2.carbon.identity.gateway.framework.request.factory.HttpIdentityRequestFactory;
import org.wso2.carbon.identity.gateway.framework.response.HttpIdentityResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.identity.gateway.processor.request.FrameworkLoginRequest.FrameworkLoginRequestConstants.AUTHENTICATOR_NAME;
import static org.wso2.carbon.identity.gateway.processor.request.FrameworkLoginRequest.FrameworkLoginRequestConstants.IDP_NAME;

public class FrameworkLoginRequestFactory<T extends FrameworkLoginRequest.FrameworkLoginBuilder>
        extends HttpIdentityRequestFactory<T> {

    @Override
    public boolean canHandle(HttpServletRequest request, HttpServletResponse response) {
        String authenticatorName = request.getParameter(AUTHENTICATOR_NAME);
        String idpName = request.getParameter(IDP_NAME);
        return StringUtils.isNotBlank(authenticatorName) && StringUtils.isNotBlank(idpName);
    }

    @Override
    public void create(T builder, HttpServletRequest request, HttpServletResponse response)
            throws FrameworkClientException {

        super.create(builder, request, response);
        builder.setAuthenticatorName(request.getParameter(AUTHENTICATOR_NAME));
        builder.setIdentityProviderName(request.getParameter(IDP_NAME));
    }


    @Override
    public FrameworkLoginRequest.FrameworkLoginBuilder create(HttpServletRequest request, HttpServletResponse response)
            throws FrameworkClientException {

        FrameworkLoginRequest.FrameworkLoginBuilder frameworkLoginBuilder = new FrameworkLoginRequest
                .FrameworkLoginBuilder();

        frameworkLoginBuilder.setAuthenticatorName(request.getParameter(AUTHENTICATOR_NAME));
        frameworkLoginBuilder.setIdentityProviderName(request.getParameter(IDP_NAME));
        return frameworkLoginBuilder;
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
