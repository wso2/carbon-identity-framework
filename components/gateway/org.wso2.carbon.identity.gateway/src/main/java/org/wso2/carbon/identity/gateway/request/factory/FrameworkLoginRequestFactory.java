/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.gateway.request.factory;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.framework.exception.FrameworkClientException;
import org.wso2.carbon.identity.framework.request.factory.HttpIdentityRequestFactory;
import org.wso2.carbon.identity.framework.response.HttpIdentityResponse;
import org.wso2.carbon.identity.gateway.request.FrameworkLoginRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FrameworkLoginRequestFactory<T extends FrameworkLoginRequest.FrameworkLoginBuilder>
        extends HttpIdentityRequestFactory<T> {

    @Override
    public boolean canHandle(HttpServletRequest request, HttpServletResponse response) {
        String authenticatorName = request.getParameter(FrameworkLoginRequest.FrameworkLoginRequestConstants.AUTHENTICATOR_NAME);
        String idpName = request.getParameter(FrameworkLoginRequest.FrameworkLoginRequestConstants.IDP_NAME);
        return StringUtils.isNotBlank(authenticatorName) && StringUtils.isNotBlank(idpName);
    }

    @Override
    public void create(T builder, HttpServletRequest request, HttpServletResponse response)
            throws FrameworkClientException {

        super.create(builder, request, response);
        builder.setAuthenticatorName(request.getParameter(FrameworkLoginRequest.FrameworkLoginRequestConstants.AUTHENTICATOR_NAME));
        builder.setIdentityProviderName(request.getParameter(FrameworkLoginRequest.FrameworkLoginRequestConstants.IDP_NAME));
    }


    @Override
    public FrameworkLoginRequest.FrameworkLoginBuilder create(HttpServletRequest request, HttpServletResponse response)
            throws FrameworkClientException {

        FrameworkLoginRequest.FrameworkLoginBuilder frameworkLoginBuilder = new FrameworkLoginRequest
                .FrameworkLoginBuilder();

        frameworkLoginBuilder.setAuthenticatorName(request.getParameter(FrameworkLoginRequest.FrameworkLoginRequestConstants.AUTHENTICATOR_NAME));
        frameworkLoginBuilder.setIdentityProviderName(request.getParameter(FrameworkLoginRequest.FrameworkLoginRequestConstants.IDP_NAME));
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
