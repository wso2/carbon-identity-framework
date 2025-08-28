/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.exception.CookieValidationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserAssertionFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.RequestCoordinator;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link CommonAuthenticationHandler}.
 */
public class CommonAuthenticationHandlerTest {

    @Test(description = "test doPost when cooke validation fails with a non-mismatching tenant error code")
    public void testdoPostCookieValidationFailedWithNonTenantError() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getMaxInactiveInterval()).thenReturn(1800);
        when(request.getParameter("sessionDataKey")).thenReturn("sdkey-123");

        RequestCoordinator coordinator = mock(RequestCoordinator.class);
        CookieValidationFailedException cookieValidationFailedException = mock(CookieValidationFailedException.class);
        when(cookieValidationFailedException.getErrorCode()).thenReturn("NOT_MISMATCHING_TENANT");

        try (MockedStatic<FrameworkUtils> frameworkUtilsMockedStatic = Mockito.mockStatic(FrameworkUtils.class)) {
            frameworkUtilsMockedStatic.when(FrameworkUtils::getMaxInactiveInterval).thenReturn(0);
            frameworkUtilsMockedStatic.when(FrameworkUtils::getRequestCoordinator).thenReturn(coordinator);

            doThrow(cookieValidationFailedException).doNothing()
                    .when(coordinator).handle(any(HttpServletRequest.class), any(HttpServletResponse.class));

            new CommonAuthenticationHandler().doPost(request, response);

            frameworkUtilsMockedStatic.verify(FrameworkUtils::getMaxInactiveInterval);
            frameworkUtilsMockedStatic.verify(() -> FrameworkUtils.setMaxInactiveInterval(1800));
            verify(coordinator, times(2)).handle(request, response);
        }
    }

    @Test(description = "test doPost when user assertion fails")
    public void testdoPostUserAssertionFailed() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getMaxInactiveInterval()).thenReturn(1800);
        when(request.getParameter("sessionDataKey")).thenReturn("sdkey-123");

        RequestCoordinator coordinator = mock(RequestCoordinator.class);
        UserAssertionFailedException userAssertionFailedException = mock(UserAssertionFailedException.class);

        try (MockedStatic<FrameworkUtils> frameworkUtilsMockedStatic = Mockito.mockStatic(FrameworkUtils.class)) {
            frameworkUtilsMockedStatic.when(FrameworkUtils::getMaxInactiveInterval).thenReturn(0);
            frameworkUtilsMockedStatic.when(FrameworkUtils::getRequestCoordinator).thenReturn(coordinator);

            doThrow(userAssertionFailedException).doNothing()
                    .when(coordinator).handle(any(HttpServletRequest.class), any(HttpServletResponse.class));

            new CommonAuthenticationHandler().doPost(request, response);

            frameworkUtilsMockedStatic.verify(FrameworkUtils::getMaxInactiveInterval);
            frameworkUtilsMockedStatic.verify(() -> FrameworkUtils.setMaxInactiveInterval(1800));
            verify(coordinator, times(2)).handle(request, response);
        }
    }
}

