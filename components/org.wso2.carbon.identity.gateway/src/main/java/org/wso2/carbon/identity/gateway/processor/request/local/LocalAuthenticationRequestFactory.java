/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.gateway.processor.request.local;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.gateway.api.FrameworkClientException;
import org.wso2.carbon.identity.gateway.api.HttpIdentityRequestFactory;
import org.wso2.carbon.identity.gateway.api.HttpIdentityResponse;
import org.wso2.msf4j.Request;


public class LocalAuthenticationRequestFactory<T extends LocalAuthenticationRequest.LocalAuthenticationRequestBuilder> extends HttpIdentityRequestFactory<T>{
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
