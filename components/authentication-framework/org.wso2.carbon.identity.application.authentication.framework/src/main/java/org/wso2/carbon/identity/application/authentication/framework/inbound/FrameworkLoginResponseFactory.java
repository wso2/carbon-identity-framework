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

package org.wso2.carbon.identity.application.authentication.framework.inbound;

import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import javax.servlet.http.HttpServletResponse;

/**
 * Framework login response factory.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.application.authentication.framework.inbound." +
                        "HttpIdentityResponseFactory",
                "service.scope=singleton"
        }
)
public class FrameworkLoginResponseFactory extends HttpIdentityResponseFactory {

    @Override
    public boolean canHandle(IdentityResponse identityResponse) {
        if (identityResponse instanceof FrameworkLoginResponse) {
            return true;
        }
        return false;
    }

    @Override
    public HttpIdentityResponse.HttpIdentityResponseBuilder create(IdentityResponse identityResponse) {

        HttpIdentityResponse.HttpIdentityResponseBuilder responseBuilder =
                new HttpIdentityResponse.HttpIdentityResponseBuilder();
        create(responseBuilder, identityResponse);
        return responseBuilder;
    }

    @Override
    public void create(
            HttpIdentityResponse.HttpIdentityResponseBuilder builder, IdentityResponse identityResponse) {

        FrameworkLoginResponse response = (FrameworkLoginResponse) identityResponse;

        builder.setStatusCode(HttpServletResponse.SC_FOUND);
        builder.addParameter(InboundConstants.RequestProcessor.AUTH_NAME,
                             new String[]{response.getAuthName()});
        builder.addParameter(FrameworkConstants.SESSION_DATA_KEY,
                             new String[]{response.getContextKey()});
        builder.addParameter(FrameworkConstants.RequestParams.CALLER_PATH,
                             new String[]{response.getCallbackPath()});
        builder.addParameter(FrameworkConstants.RequestParams.ISSUER,
                             new String[]{response.getRelyingParty()});
        builder.addParameter(FrameworkConstants.RequestParams.TYPE,
                             new String[]{response.getAuthType()});
        builder.setRedirectURL(response.getRedirectUrl());

    }

    @Override
    public int getPriority() {
        return 0;
    }
}
