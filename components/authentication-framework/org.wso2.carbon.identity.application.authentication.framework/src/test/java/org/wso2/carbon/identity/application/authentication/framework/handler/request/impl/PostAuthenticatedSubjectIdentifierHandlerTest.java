/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.AdaptiveAuthentication.ALLOW_AUTHENTICATED_SUB_UPDATE;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.PROP_USERNAME_UPDATED_EXTERNALLY;

/**
 * Unit tests for PostAuthenticatedSubjectIdentifierHandler.
 */
public class PostAuthenticatedSubjectIdentifierHandlerTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private AuthenticationContext context;
    @Mock
    private SequenceConfig sequenceConfig;
    @Mock
    private ApplicationConfig applicationConfig;
    @Mock
    private AuthenticatedUser authenticatedUser;
    private MockedStatic<IdentityUtil> identityUtil;

    private static final String USERNAME_CLAIM = "http://wso2.org/claims/username";
    private static final String TEST_USER_USERNAME = "testUser";

    @Before
    public void setUp() {

        MockitoAnnotations.initMocks(this);
        when(context.getSequenceConfig()).thenReturn(sequenceConfig);
        when(sequenceConfig.getApplicationConfig()).thenReturn(applicationConfig);
        when(sequenceConfig.getAuthenticatedUser()).thenReturn(authenticatedUser);
        identityUtil = Mockito.mockStatic(IdentityUtil.class);
    }

    @After
    public void tearDown() {

        identityUtil.close();
    }

    /**
     * Test handle returns SUCCESS_COMPLETED when step-based sequence handler is not executed.
     */
    @Test
    public void testHandleWhenStepBasedSequenceHandlerNotExecuted() {

        when(FrameworkUtils.isStepBasedSequenceHandlerExecuted(context)).thenReturn(false);

        PostAuthenticatedSubjectIdentifierHandler handler = PostAuthenticatedSubjectIdentifierHandler.getInstance();
        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED, status);
    }

    /**
     * Test handle sets subject identifier when subject claim URI and value are present.
     */
    @Test
    public void testHandleWhenSubjectClaimAndValueArePresent() {

        when(FrameworkUtils.isStepBasedSequenceHandlerExecuted(context)).thenReturn(true);
        when(applicationConfig.getSubjectClaimUri()).thenReturn(USERNAME_CLAIM);
        when(context.getProperty(FrameworkConstants.SERVICE_PROVIDER_SUBJECT_CLAIM_VALUE)).
                thenReturn(TEST_USER_USERNAME);

        PostAuthenticatedSubjectIdentifierHandler handler = PostAuthenticatedSubjectIdentifierHandler.getInstance();
        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED, status);
        verify(authenticatedUser, atLeastOnce()).setAuthenticatedSubjectIdentifier(TEST_USER_USERNAME);
    }

    /**
     * Test handle sets subject identifier based on user id when subject claim is missing.
     */
    @Test
    public void testHandleWhenSubjectClaimIsMissing() {

        when(FrameworkUtils.isStepBasedSequenceHandlerExecuted(context)).thenReturn(true);
        when(applicationConfig.getSubjectClaimUri()).thenReturn("");
        when(authenticatedUser.getUserName()).thenReturn(TEST_USER_USERNAME);
        when(context.getProperty(PROP_USERNAME_UPDATED_EXTERNALLY)).thenReturn("true");
        identityUtil.when(() -> IdentityUtil.getProperty(ALLOW_AUTHENTICATED_SUB_UPDATE)).thenReturn("true");

        PostAuthenticatedSubjectIdentifierHandler handler = spy(PostAuthenticatedSubjectIdentifierHandler.
                getInstance());
        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED, status);
    }
}
