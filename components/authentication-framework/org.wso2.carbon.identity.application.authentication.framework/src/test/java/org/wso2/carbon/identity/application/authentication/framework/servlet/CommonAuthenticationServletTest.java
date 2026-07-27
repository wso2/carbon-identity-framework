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

package org.wso2.carbon.identity.application.authentication.framework.servlet;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.event.IdentityEventConfigBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CommonAuthenticationServlet}.
 */
public class CommonAuthenticationServletTest {

    /**
     * Sets up the test environment by configuring the carbon home system property
     * and initializing the {@link CommonAuthenticationServlet} instance.
     */
    @BeforeMethod
    public void setUp() {
        configureCarbonHome();
    }

    /**
     * Tests the {@code doHead} method to ensure it sets the HTTP status to 200 (OK).
     */
    @Test
    public void testDoHead() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getMethod()).thenReturn("HEAD");
        HttpServletResponse spyResponse = spy(HttpServletResponse.class);
        CommonAuthenticationServlet servlet = new CommonAuthenticationServlet();
        servlet.doHead(mockRequest, spyResponse);
        verify(spyResponse, times(1)).setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Configures the carbon home system property for the test environment.
     */
    private void configureCarbonHome() {
        String carbonHome = IdentityEventConfigBuilder.class.getResource("/").getFile();
        System.setProperty("carbon.home", carbonHome);
    }
}
