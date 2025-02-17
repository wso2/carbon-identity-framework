/*
 * Copyright (c) 2017-2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MockAuthenticator implements ApplicationAuthenticator {

    private String name;
    private SubjectCallback subjectCallback;
    private String claimDialectURI;

    public MockAuthenticator(String name) {

        this.name = name;
    }

    public MockAuthenticator(String name, SubjectCallback subjectCallback) {

        this(name);
        this.subjectCallback = subjectCallback;
    }

    public MockAuthenticator(String name, SubjectCallback subjectCallback, String claimDialectURI) {

        this(name);
        this.subjectCallback = subjectCallback;
        this.claimDialectURI = claimDialectURI;
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

        return claimDialectURI;
    }

    @Override
    public List<Property> getConfigurationProperties() {

        return null;
    }

    @Override
    public String getI18nKey() {

        return this.name + ".authenticator";
    }

    public static class MockLocalAuthenticator extends MockAuthenticator implements LocalApplicationAuthenticator {

        public MockLocalAuthenticator(String name) {
            super(name);
        }
    }

    public static class MockFederatedAuthenticator extends MockAuthenticator
            implements FederatedApplicationAuthenticator {

        public MockFederatedAuthenticator(String name) {
            super(name);
        }
    }
}
