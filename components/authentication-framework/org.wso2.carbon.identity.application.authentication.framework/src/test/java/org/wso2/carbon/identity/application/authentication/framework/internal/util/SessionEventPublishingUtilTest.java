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

package org.wso2.carbon.identity.application.authentication.framework.internal.util;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.services.IdentityEventService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link SessionEventPublishingUtil}.
 */
public class SessionEventPublishingUtilTest {

    @Mock
    private IdentityEventService identityEventService;
    @Mock
    FrameworkServiceDataHolder frameworkServiceDataHolder;
    private MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolderMockedStatic;

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        frameworkServiceDataHolderMockedStatic = mockStatic(FrameworkServiceDataHolder.class);
        frameworkServiceDataHolderMockedStatic.when(FrameworkServiceDataHolder::getInstance)
                .thenReturn(frameworkServiceDataHolder);
        when(frameworkServiceDataHolder.getIdentityEventService()).thenReturn(identityEventService);
    }

    @AfterMethod
    public void tearDown() {

        frameworkServiceDataHolderMockedStatic.close();
    }

    @Test
    public void testPublishSessionTerminationEventWithValidInputs() throws IdentityEventException {

        String sessionId = "session123";
        String userId = "user123";

        SessionEventPublishingUtil.publishSessionTerminationEvent(userId, sessionId);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(identityEventService, times(1)).handleEvent(eventCaptor.capture());

        Event capturedEvent = eventCaptor.getValue();

        // Verify event properties.
        assertEquals(capturedEvent.getEventName(), IdentityEventConstants.Event.SESSION_TERMINATE_V2);
        assertEquals(capturedEvent.getEventProperties().get(IdentityEventConstants.EventProperty.USER_ID),
                userId);
        assertTrue(
                capturedEvent.getEventProperties().get(IdentityEventConstants.EventProperty.PARAMS) instanceof HashMap);
        HashMap<String, Object> params = (HashMap<String, Object>) capturedEvent.getEventProperties()
                .get(IdentityEventConstants.EventProperty.PARAMS);
        assertEquals(params.entrySet().size(), 1);
        assertEquals(params.get(FrameworkConstants.AnalyticsAttributes.SESSION_ID), sessionId);
    }

    @Test
    public void testPublishSessionTerminationEventWithInvalidInputs() throws IdentityEventException {

        String sessionId = "";
        String userId = "user123";

        SessionEventPublishingUtil.publishSessionTerminationEvent(userId, sessionId);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(identityEventService, never()).handleEvent(eventCaptor.capture());
    }

    @Test
    public void testPublishSessionTerminationEventForAuthAndSessionContextsWithValidInputs()
            throws IdentityEventException {

        String sessionId = "session123";
        AuthenticatedUser authenticatedUser = mock(AuthenticatedUser.class);
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        SessionContext sessionContext = mock(SessionContext.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

        SessionEventPublishingUtil.publishSessionTerminationEvent(sessionId, authenticatedUser, httpServletRequest,
                authenticationContext, sessionContext);

        verify(identityEventService, times(1)).handleEvent(any(Event.class));
    }

    @Test
    public void testPublishSessionTerminationEventForAuthAndSessionContextsWithInvalidInputs()
            throws IdentityEventException {

        String sessionId = "session123";
        AuthenticatedUser authenticatedUser = null;
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        SessionContext sessionContext = mock(SessionContext.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

        SessionEventPublishingUtil.publishSessionTerminationEvent(sessionId, authenticatedUser, httpServletRequest,
                authenticationContext, sessionContext);

        verify(identityEventService, never()).handleEvent(any(Event.class));
    }

    @Test
    public void testPublishSessionTerminationEventForMultipleSessions() throws IdentityEventException {

        String userId = "user123";
        List<String> sessionIds = Arrays.asList("session1", "session2");

        SessionEventPublishingUtil.publishSessionTerminationEvent(userId, sessionIds);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(identityEventService, times(1)).handleEvent(eventCaptor.capture());
        Event capturedEvent = eventCaptor.getValue();

        // Verify event properties.
        assertEquals(capturedEvent.getEventName(), IdentityEventConstants.Event.SESSION_TERMINATE_V2);
        assertEquals(capturedEvent.getEventProperties().get(IdentityEventConstants.EventProperty.USER_ID),
                userId);
        assertTrue(
                capturedEvent.getEventProperties().get(IdentityEventConstants.EventProperty.PARAMS) instanceof HashMap);
        HashMap<String, Object> params = (HashMap<String, Object>) capturedEvent.getEventProperties()
                .get(IdentityEventConstants.EventProperty.PARAMS);
        assertEquals(params.entrySet().size(), 1);
        assertEquals(params.get(IdentityEventConstants.EventProperty.SESSION_IDS), sessionIds);
    }

    @Test
    public void testDoPublishEventWithException() throws IdentityEventException {

        doThrow(new IdentityEventException("Error")).when(identityEventService).handleEvent(any(Event.class));

        String sessionId = "session123";
        String userId = "user123";

        SessionEventPublishingUtil.publishSessionTerminationEvent(userId, sessionId);

        verify(identityEventService, times(1)).handleEvent(any(Event.class));
    }
}
