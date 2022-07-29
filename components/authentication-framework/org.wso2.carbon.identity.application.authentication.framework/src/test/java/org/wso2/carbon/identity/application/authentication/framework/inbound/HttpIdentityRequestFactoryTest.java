/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.inbound;

import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.HttpIdentityResponse.HttpIdentityResponseBuilder;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest.IdentityRequestBuilder;
import org.wso2.carbon.identity.application.authentication.framwork.test.utils.CommonTestUtils;
import org.wso2.carbon.identity.core.handler.InitConfig;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link HttpIdentityRequestFactory}
 */
@PrepareForTest(IdentityUtil.class)
public class HttpIdentityRequestFactoryTest extends PowerMockTestCase {

    private static final String ERROR_MESSAGE = "CLIENT_ERROR_MESSAGE";

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    private HttpIdentityRequestFactory httpIdentityRequestFactory;

    @BeforeMethod
    public void setUp() throws Exception {
        initMocks(this);
        httpIdentityRequestFactory = new HttpIdentityRequestFactory();
        CommonTestUtils.initPrivilegedCarbonContext();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        PrivilegedCarbonContext.endTenantFlow();
    }

    @Test
    public void testInit() throws Exception {
        // Mock returning a null after reading event listener configs
        Util.mockReturnNullEventListenerConfig();
        InitConfig initConfig = new InitConfig();
        httpIdentityRequestFactory.init(initConfig);
        assertEquals(httpIdentityRequestFactory.properties.size(), 0);
    }

    @DataProvider(name = "initConfigDataProvider")
    public Object[][] initConfigDataProvider() {
        return Util.getEventListenerPropertyData();
    }

    @Test(dataProvider = "initConfigDataProvider")
    public void testInitWithDuplicateEventListenerProperties(Properties eventListenerProperties,
                                                             Properties expectedListenerProperties) throws Exception {

        Util.mockReturnEventListenerConfigWithProperties(eventListenerProperties);
        httpIdentityRequestFactory.init(new InitConfig());
        Util.assertPropertiesEqual(httpIdentityRequestFactory.properties, expectedListenerProperties);
    }

    @Test
    public void testCanHandle() throws Exception {
        assertTrue(httpIdentityRequestFactory.canHandle(request, response));
    }

    @DataProvider(name = "tenantDomainProvider")
    public Object[][] provideTenantDomain() {
        return new Object[][]{
                {
                        null,
                        MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
                },
                {
                        "foo.com",
                        "foo.com"
                }
        };
    }

    @Test(dataProvider = "tenantDomainProvider")
    public void testCreate(String tenantDomainInContext,
                           String expectedTenantDomain) throws Exception {

        final Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put("header1", "headerValue1");
        headers.put("header2", "headerValue2");

        when(request.getHeaderNames()).thenReturn(Collections.enumeration(headers.keySet()));
        when(request.getHeader(anyString())).then(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                return headers.get(invocationOnMock.getArgument(0));
            }
        });

        Map<String, String[]> paramMap = new ConcurrentHashMap<>();
        paramMap.put("param1", new String[]{"value1", "value2"});

        when(request.getParameterMap()).thenReturn(paramMap);

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("attribute1", "attributeValue1");
        attributes.put("attribute2", "attributeValue2");
        attributes.put("non_serializable", mock(Object.class));


        when(request.getAttributeNames()).thenReturn(Collections.enumeration(attributes.keySet()));
        when(request.getAttribute(anyString())).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return attributes.get(invocationOnMock.getArgument(0));
            }
        });

        Cookie[] cookies = new Cookie[]{
                new Cookie("cookie1", "cookieValue1")
        };
        when(request.getCookies()).thenReturn(cookies);
        when(request.getRequestURI()).thenReturn("https://localhost/identity");

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomainInContext);

        IdentityRequestBuilder identityRequestBuilder = httpIdentityRequestFactory.create(request, response);

        assertEquals(identityRequestBuilder.headers.size(), 2);
        for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
            assertEquals(identityRequestBuilder.headers.get(headerEntry.getKey()), headerEntry.getValue());
        }

        // Check whether the non-serializable attributes were added into the request builder
        assertEquals(identityRequestBuilder.attributes.size(), 2);
        attributes.remove("non_serializable");
        for (Map.Entry<String, Object> attributeEntry : attributes.entrySet()) {
            assertEquals(identityRequestBuilder.attributes.get(attributeEntry.getKey()), attributeEntry.getValue());
            assertTrue(identityRequestBuilder.attributes.get(attributeEntry.getKey()) instanceof Serializable);
        }

        assertEquals(identityRequestBuilder.cookies.size(), 1);
        for (Cookie cookie : cookies) {
            assertTrue(identityRequestBuilder.cookies.containsKey(cookie.getName()));
            assertEquals(identityRequestBuilder.cookies.get(cookie.getName()).getValue(), cookie.getValue());
        }

        assertEquals(identityRequestBuilder.tenantDomain, expectedTenantDomain);
        // TODO write asserts for other attributes of the builder
    }

    @Test
    public void testHandleFrameworkClientException() throws Exception {
        FrameworkClientException clientException = new FrameworkClientException(ERROR_MESSAGE);

        HttpIdentityResponseBuilder responseBuilder =
                httpIdentityRequestFactory.handleException(clientException, request, response);

        assertEquals(responseBuilder.body, ERROR_MESSAGE);
        assertEquals(responseBuilder.statusCode, 400);
    }

    @Test
    public void testHandleRuntimeException() throws Exception {
        RuntimeException runtimeException = new RuntimeException();

        HttpIdentityResponseBuilder responseBuilder =
                httpIdentityRequestFactory.handleException(runtimeException, request, response);

        assertEquals(responseBuilder.statusCode, 500);
    }
}
