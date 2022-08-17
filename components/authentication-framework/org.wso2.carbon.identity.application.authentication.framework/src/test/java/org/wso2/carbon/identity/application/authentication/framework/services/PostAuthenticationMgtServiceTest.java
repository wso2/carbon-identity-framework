/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.services;

import org.powermock.api.mockito.PowerMockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AbstractPostAuthnHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.testutil.IdentityBaseTest;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests for PostAuthenticationManagement Service.
 */
public class PostAuthenticationMgtServiceTest extends IdentityBaseTest {

    private PostAuthenticationMgtService postAuthenticationMgtService = new PostAuthenticationMgtService();
    private TestPostHandlerWithRedirect testPostHandlerWithRedirect = new TestPostHandlerWithRedirect();
    private static final String FIRST_REDIRECT_TRIGGERED = "firstRedirectTriggered";
    private static final String SECOND_REDIRECT_TRIGGERED = "secondRedirectTriggered";
    private static final String ADMIN_USERNAME = "admin";
    private static final String SPECIAL_USER = "specialUser";
    private static final String DUMMY_EXTERNAL_ENDPOINT = "https://localhost/somecontext";

    @BeforeMethod
    void setup() {

        testPostHandlerWithRedirect.setEnabled(true);
        FrameworkServiceDataHolder.getInstance().addPostAuthenticationHandler(testPostHandlerWithRedirect);
    }

    @DataProvider(name = "singlePostAuthenticatorData")
    public Object[][] singlePostAuthenticatorData() {

        return new Object[][]{
                // Sample authenticator is enabled, an admin user authenticated. Hence just a single redirection.
                {true, ADMIN_USERNAME},
                // No post authenticator is enabled.
                {false, ADMIN_USERNAME},
                // Sample authenticator is enabled. A special user is authenticated. Hence two redirections take place.
                {true, SPECIAL_USER}
        };
    }

    @Test(dataProvider = "singlePostAuthenticatorData")
    public void testHandlePostAuthentication(boolean isSampleAuthenticatorEnabled, String userName) throws Exception {

        HttpServletRequest request = PowerMockito.mock(HttpServletRequest.class);
        HttpServletResponse response = PowerMockito.mock(HttpServletResponse.class);
        AuthenticationContext context = new AuthenticationContext();
        context.setContextIdentifier(String.valueOf(UUID.randomUUID()));

        Cookie[] cookies = new Cookie[1];
        doAnswer((mock) -> cookies[0] = (Cookie) mock.getArguments()[0]).when(response).addCookie(any(Cookie.class));
        addSequence(context, true);
        setUser(context, userName);

        if (!isSampleAuthenticatorEnabled) {
            this.testPostHandlerWithRedirect.setEnabled(false);
        }

        postAuthenticationMgtService.handlePostAuthentication(request, response, context);

        if (isSampleAuthenticatorEnabled && !SPECIAL_USER.equalsIgnoreCase(userName)) {
            assertNotNull(context.getParameter(FIRST_REDIRECT_TRIGGERED));
            when(request.getCookies()).thenReturn(cookies);
            postAuthenticationMgtService.handlePostAuthentication(request, response, context);
            assertTrue(Boolean.parseBoolean(context.getParameter(FrameworkConstants
                    .POST_AUTHENTICATION_EXTENSION_COMPLETED).toString()));
        } else if (SPECIAL_USER.equalsIgnoreCase(userName)) {
            assertNotNull(context.getParameter(FIRST_REDIRECT_TRIGGERED));
            when(request.getCookies()).thenReturn(cookies);
            postAuthenticationMgtService.handlePostAuthentication(request, response, context);
            assertNull(context.getParameter(FrameworkConstants
                    .POST_AUTHENTICATION_EXTENSION_COMPLETED));
            assertNotNull(context.getParameter(SECOND_REDIRECT_TRIGGERED));
        } else {
            assertTrue(Boolean.parseBoolean(context.getParameter(FrameworkConstants
                    .POST_AUTHENTICATION_EXTENSION_COMPLETED).toString()));
        }

        postAuthenticationMgtService.handlePostAuthentication(request, response, context);

        if (SPECIAL_USER.equalsIgnoreCase(userName)) {
            assertNotNull(context.getParameter(SECOND_REDIRECT_TRIGGERED));
        }
        postAuthenticationMgtService.handlePostAuthentication(request, response, context);
    }

    @DataProvider(name = "singlePostAuthenticatorUnsuccessData")
    public Object[][] singlePostAuthenticatorUnsuccessData() {

        return new Object[][]{
                // Alter cookie before sending response to first redirection. Hence should fail
                {true, ADMIN_USERNAME},
                // If the user is neither an admin, nor special user, them post authentication should fail.
                {false, ADMIN_USERNAME + "suffix"}
        };
    }

    @Test(dataProvider = "singlePostAuthenticatorUnsuccessData", expectedExceptions =
            PostAuthenticationFailedException.class)
    public void testHandlePostAuthenticationExceptions(boolean alterCookie, String userName) throws Exception {

        HttpServletRequest request = PowerMockito.mock(HttpServletRequest.class);
        HttpServletResponse response = PowerMockito.mock(HttpServletResponse.class);
        AuthenticationContext context = new AuthenticationContext();
        context.setContextIdentifier(String.valueOf(UUID.randomUUID()));

        Cookie[] cookies = new Cookie[1];
        doAnswer((mock) -> cookies[0] = (Cookie) mock.getArguments()[0]).when(response).addCookie(any(Cookie.class));
        addSequence(context, true);
        setUser(context, userName);

        postAuthenticationMgtService.handlePostAuthentication(request, response, context);

        if (alterCookie && ADMIN_USERNAME.equalsIgnoreCase(userName)) {
            cookies[0].setValue(cookies[0].getValue() + "gibberish");
        }

        when(request.getCookies()).thenReturn(cookies);
        postAuthenticationMgtService.handlePostAuthentication(request, response, context);

    }

    private void addSequence(AuthenticationContext context, boolean isCompleted) {

        SequenceConfig sequenceConfig = new SequenceConfig();
        sequenceConfig.setCompleted(isCompleted);
        context.setSequenceConfig(sequenceConfig);

    }

    private void setUser(AuthenticationContext context, String userName) {

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setAuthenticatedSubjectIdentifier(userName);
        context.getSequenceConfig().setAuthenticatedUser(authenticatedUser);
    }

    /**
     * Sample post authentication handler for tests.
     */
    public static class TestPostHandlerWithRedirect extends AbstractPostAuthnHandler {

        private boolean isEnabled = true;

        @Override
        public PostAuthnHandlerFlowStatus handle(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context)
                throws PostAuthenticationFailedException {

            // If not authenticated just return.
            if (context.getSequenceConfig().getAuthenticatedUser() == null) {
                return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;

            } else if (context.getParameter(FIRST_REDIRECT_TRIGGERED) != null && context.getParameter
                    (SECOND_REDIRECT_TRIGGERED) == null) {
                // First redirection has taken place. Decide whether second redirection needs or can finish the flow
                String authenticatedUsername = context.getSequenceConfig().getAuthenticatedUser()
                        .getAuthenticatedSubjectIdentifier();
                if (ADMIN_USERNAME.equalsIgnoreCase(authenticatedUsername)) {
                    // If the user is admin, the flow is success.
                    return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
                } else if (SPECIAL_USER.equalsIgnoreCase(authenticatedUsername)) {
                    // If the user is a special user. then do a second redirection before completing.
                    try {
                        response.sendRedirect(DUMMY_EXTERNAL_ENDPOINT);
                        context.addParameter(SECOND_REDIRECT_TRIGGERED, true);
                        return PostAuthnHandlerFlowStatus.INCOMPLETE;
                    } catch (IOException e) {
                        throw new PostAuthenticationFailedException("Error while checking admin user", "Error while " +
                                "redirecting");
                    }

                } else {
                    throw new PostAuthenticationFailedException("Not an admin user", "User is not an admin");
                }
            }

            if (context.getParameter(SECOND_REDIRECT_TRIGGERED) != null) {
                return PostAuthnHandlerFlowStatus.INCOMPLETE;
            }
            try {
                response.sendRedirect(DUMMY_EXTERNAL_ENDPOINT);
                context.addParameter(FIRST_REDIRECT_TRIGGERED, true);
                return PostAuthnHandlerFlowStatus.INCOMPLETE;
            } catch (IOException e) {
                throw new PostAuthenticationFailedException("Error while checking admin user", "Error while " +
                        "redirecting");
            }
        }

        @Override
        public boolean isEnabled() {

            return this.isEnabled;
        }

        public void setEnabled(boolean isEnabled) {

            this.isEnabled = isEnabled;
        }
    }
}
