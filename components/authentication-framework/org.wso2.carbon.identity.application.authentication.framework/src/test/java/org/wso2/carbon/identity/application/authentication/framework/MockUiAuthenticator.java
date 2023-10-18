/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.SubjectCallback;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A Mock Authenticator which simulates presenting page to browser and getting a response (HTTP Request) from the user
 */
public class MockUiAuthenticator extends AbstractApplicationAuthenticator {

    private String name;
    private SubjectCallback subjectCallback;

    public MockUiAuthenticator(String name, SubjectCallback subjectCallback) {

        this.name = name;
        this.subjectCallback = subjectCallback;
    }

    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) throws AuthenticationFailedException {

        super.initiateAuthenticationRequest(request, response, context);
    }

    @Override
    protected void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) throws AuthenticationFailedException {

        if (subjectCallback != null) {
            context.setSubject(subjectCallback.getAuthenticatedUser(context));
        }
    }

    @Override
    public boolean canHandle(HttpServletRequest request) {

        return request.getParameter("returning") != null;
    }

    @Override
    public String getContextIdentifier(HttpServletRequest request) {

        return null;
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public String getFriendlyName() {

        return name;
    }

    @Override
    public String getI18nKey() {

        return this.name + ".authenticator";
    }
}
