/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.gateway.api;

public class FrameworkLoginResponseFactory extends HttpIdentityResponseFactory {

    @Override
    public String getName() {
        return "FrameworkLoginResponseFactory";
    }

    @Override
    public boolean canHandle(IdentityResponse identityResponse) {
        if (identityResponse instanceof FrameworkLoginResponse) {
            return true;
        }
        return false;
    }

    @Override
    public HttpIdentityResponse.HttpIdentityResponseBuilder create(IdentityResponse identityResponse) {


        FrameworkLoginResponse response = (FrameworkLoginResponse) identityResponse;

        HttpIdentityResponse.HttpIdentityResponseBuilder responseBuilder =
                new HttpIdentityResponse.HttpIdentityResponseBuilder();
        responseBuilder.addParameter(InboundConstants.RequestProcessor.AUTH_NAME,
                                     new String[]{response.getAuthName()});
        responseBuilder.addParameter(InboundConstants.RequestProcessor.CONTEXT_KEY,
                                     new String[]{response.getContextKey()});
        responseBuilder.addParameter(InboundConstants.RequestProcessor.CALL_BACK_PATH,
                                     new String[]{response.getCallbackPath()});
        responseBuilder.addParameter(InboundConstants.RequestProcessor.RELYING_PARTY,
                                     new String[]{response.getRelyingParty()});
        responseBuilder.addParameter(InboundConstants.RequestProcessor.AUTH_TYPE,
                                     new String[]{response.getAuthType()});
        responseBuilder.setRedirectURL(response.getRedirectUrl());
        responseBuilder.setStatusCode(302);
        return responseBuilder;
    }

    @Override
    public HttpIdentityResponse.HttpIdentityResponseBuilder create(
            HttpIdentityResponse.HttpIdentityResponseBuilder builder, IdentityResponse identityResponse) {

        FrameworkLoginResponse response = (FrameworkLoginResponse)identityResponse;

        builder.addParameter(InboundConstants.RequestProcessor.AUTH_NAME,
                             new String[]{response.getAuthName()});
        builder.addParameter(InboundConstants.RequestProcessor.CONTEXT_KEY,
                             new String[]{response.getContextKey()});
        builder.addParameter(InboundConstants.RequestProcessor.CALL_BACK_PATH,
                             new String[]{response.getCallbackPath()});
        builder.addParameter(InboundConstants.RequestProcessor.RELYING_PARTY,
                             new String[]{response.getRelyingParty()});
        builder.addParameter(InboundConstants.RequestProcessor.AUTH_TYPE,
                             new String[]{response.getAuthType()});
        builder.setRedirectURL(response.getRedirectUrl());

        return builder;
    }
}
