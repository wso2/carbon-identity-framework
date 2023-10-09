/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.SubjectCallback;
import org.wso2.carbon.identity.application.common.model.Property;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MockAuthenticator implements ApplicationAuthenticator {

    private String name;
    private SubjectCallback subjectCallback;

    public MockAuthenticator(String name) {

        this.name = name;
    }

    public MockAuthenticator(String name, SubjectCallback subjectCallback) {

        this(name);
        this.subjectCallback = subjectCallback;
    }

    @Override
    public boolean canHandle(HttpServletRequest request) {

        return false;
    }

    @Override
    public AuthenticatorFlowStatus process(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) throws AuthenticationFailedException, LogoutFailedException {

        if (subjectCallback != null) {
            context.setSubject(subjectCallback.getAuthenticatedUser(context));
        }
        return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
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

        return null;
    }

    @Override
    public String getClaimDialectURI() {

        return null;
    }

    @Override
    public List<Property> getConfigurationProperties() {

        return null;
    }

    @Override
    public String getI18Key() {
        return this.name + ".authenticator";
    }

    @Override
    public List<String> getRequiredParams() {
        return Collections.singletonList(this.name + ".authenticator");
    }

}
