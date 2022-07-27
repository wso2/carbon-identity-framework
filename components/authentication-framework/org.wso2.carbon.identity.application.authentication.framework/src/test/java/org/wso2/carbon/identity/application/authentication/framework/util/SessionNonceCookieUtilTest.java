/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.util;

import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils.REQUEST_PARAM_APPLICATION;
import static org.wso2.carbon.identity.application.authentication.framework.util.SessionNonceCookieUtil.NONCE_COOKIE;
import static org.wso2.carbon.identity.application.authentication.framework.util.SessionNonceCookieUtil.NONCE_COOKIE_CONFIG;

@PrepareForTest({IdentityUtil.class, IdentityTenantUtil.class, FrameworkUtils.class, LoginContextManagementUtil.class})
@PowerMockIgnore("org.mockito.*")
public class SessionNonceCookieUtilTest {

    private static final String SESSION_DATA_KEY_VALUE = "SessionDataKeyValue";
    private static final String SESSION_DATA_KEY_NAME = "sessionDataKey";
    private static final String RELYING_PARTY_NAME = "relyingParty";
    private static final String RELYING_PARTY_VALUE = "relyingPartyValue";
    private static final String TENANT_DOMAIN_NAME = "tenantDomain";
    private static final String TENANT_DOMAIN_VALUE = "carbon.super";
    private static final String APP_ACCESS_URL = "https://test.mock.relyingParty.com";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationContext context;

    @Captor
    ArgumentCaptor<Cookie> cookieCaptor;

    @Captor
    ArgumentCaptor<String> responseCaptor;

    @Test
    public void nonceCookieTest() {

        mockNonceCookieUtils("true", "40");
        assertTrue(SessionNonceCookieUtil.isNonceCookieEnabled());

        // Creating old session nonce and authentication cookie.
        Cookie[] cookies = getAuthenticationCookies();
        Mockito.when(request.getCookies()).thenReturn(cookies);
        Mockito.when(context.getContextIdentifier()).thenReturn(SESSION_DATA_KEY_VALUE);

        // Validating old session nonce cookie.
        Mockito.when(context.getProperty(cookies[1].getName())).thenReturn(cookies[1].getValue());
        boolean validateNonceCookie = SessionNonceCookieUtil.validateNonceCookie(request, context);
        assertTrue(validateNonceCookie);

        // Stimulate the flow where a new authentication flow is created when there is existing session nonce cookie.
        Mockito.when(context.getContextIdentifier()).thenReturn(SESSION_DATA_KEY_VALUE);
        SessionNonceCookieUtil.addNonceCookie(request, response, context);

        Mockito.verify(response, times(1)).addCookie(cookieCaptor.capture());
        List<Cookie> capturedCookies = cookieCaptor.getAllValues();

        // First, the old cookie has to be cleared with max age 0.
        Cookie removedOldSessionNonce = capturedCookies.get(0);
        assertEquals(removedOldSessionNonce.getName(), NONCE_COOKIE + "-" + SESSION_DATA_KEY_VALUE);
        assertEquals(removedOldSessionNonce.getMaxAge(), TimeUnit.MINUTES.toSeconds(40) * 2);
    }

    @Test
    public void missingNonceCookieTest() throws Exception {

        mockUtils();
        Cookie[] cookies = getAuthenticationCookies();
        Mockito.when(request.getCookies()).thenReturn(cookies);
        Mockito.when(request.getParameter(SESSION_DATA_KEY_NAME)).thenReturn(SESSION_DATA_KEY_VALUE);
        Mockito.when(request.getParameter(RELYING_PARTY_NAME)).thenReturn(RELYING_PARTY_VALUE);
        Mockito.when(request.getParameter(REQUEST_PARAM_APPLICATION)).thenReturn(RELYING_PARTY_VALUE);
        Mockito.when(request.getParameter(TENANT_DOMAIN_NAME)).thenReturn(TENANT_DOMAIN_VALUE);
        Mockito.when(context.getContextIdentifier()).thenReturn(SESSION_DATA_KEY_VALUE);

        PrintWriter mockPrintWriter = Mockito.mock(PrintWriter.class);
        Mockito.when(response.getWriter()).thenReturn(mockPrintWriter);

        // Request contains an authentication contex but missing the session nonce cookie.
        LoginContextManagementUtil.handleLoginContext(request, response);

        Mockito.verify(mockPrintWriter).write(responseCaptor.capture());
        List<String> responses = responseCaptor.getAllValues();
        Assert.assertNotNull(responses.get(0));

        JSONObject response = new JSONObject(responses.get(0));
        Assert.assertEquals(response.get("status"), "redirect");
        Assert.assertEquals(response.get("redirectUrl"), APP_ACCESS_URL);
    }

    private Cookie[] getAuthenticationCookies() {

        Cookie[] cookies = new Cookie[2];
        cookies[0] = new Cookie(FrameworkConstants.COMMONAUTH_COOKIE, "commonAuthIdValue");
        cookies[1] = new Cookie(NONCE_COOKIE + "-" + SESSION_DATA_KEY_VALUE, "sessionNonceValue");
        return cookies;
    }

    private void mockUtils() throws Exception {

        mockNonceCookieUtils("true", "40");

        mockStatic(IdentityTenantUtil.class);
        Mockito.when(IdentityTenantUtil.isTenantQualifiedUrlsEnabled()).thenReturn(false);

        mockStatic(FrameworkUtils.class);
        Mockito.when(FrameworkUtils.getAuthenticationContextFromCache(any())).thenReturn(context);
        Mockito.when(FrameworkUtils.getCookie(any(), any())).thenReturn(null);

        PowerMockito.spy(LoginContextManagementUtil.class);
        PowerMockito.doReturn(APP_ACCESS_URL).when(LoginContextManagementUtil.class, "getAccessURLFromApplication",
                anyString(), anyString());
    }

    private void mockNonceCookieUtils(String isNonceCookieEnabled, String tempDataCleanupTimeout) {

        mockStatic(IdentityUtil.class);
        Mockito.when(IdentityUtil.getProperty(NONCE_COOKIE_CONFIG)).thenReturn(isNonceCookieEnabled);
        Mockito.when(IdentityUtil.getTempDataCleanUpTimeout()).thenReturn(Long.parseLong(tempDataCleanupTimeout));
    }
}
