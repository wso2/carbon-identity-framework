/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.session.extender.processor;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.cache.SessionContextCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.SessionContextCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.SessionContextCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.session.extender.request.SessionExtenderRequest;
import org.wso2.carbon.identity.application.authentication.framework.session.extender.response.SessionExtenderResponse;
import org.wso2.carbon.identity.application.authentication.framwork.test.utils.CommonTestUtils;

import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderTestConstants.IDP_SESSION_KEY;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderTestConstants.TENANT_DOMAIN;

/**
 * Unit test cases for SessionExtenderProcessor.
 */
@PrepareForTest({SessionContextCache.class})
public class SessionExtenderProcessorTest extends PowerMockTestCase {

    private SessionExtenderProcessor sessionExtenderProcessor;

    @BeforeMethod
    public void setUp() throws Exception {
        initMocks(this);
        sessionExtenderProcessor = new SessionExtenderProcessor();
        CommonTestUtils.initPrivilegedCarbonContext();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        PrivilegedCarbonContext.endTenantFlow();
    }

    @Test
    public void testCanHandle() {

        SessionExtenderRequest sessionExtenderRequest = mock(SessionExtenderRequest.class);
        assertTrue(sessionExtenderProcessor.canHandle(sessionExtenderRequest), "Cannot handle valid " +
                "SessionExtenderRequest.");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testProcessWithSessionKey() throws Exception {

        mockStatic(SessionContextCache.class);
        SessionExtenderRequest sessionExtenderRequest = mock(SessionExtenderRequest.class);
        SessionContextCache sessionContextCache = mock(SessionContextCache.class);
        SessionContextCacheKey sessionContextCacheKey = mock(SessionContextCacheKey.class);
        SessionContextCacheEntry sessionContextCacheEntry = mock(SessionContextCacheEntry.class);
        SessionContext sessionContext = mock(SessionContext.class);

        whenNew(SessionContextCacheKey.class).withArguments(anyString()).thenReturn(sessionContextCacheKey);
        when(sessionExtenderRequest.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        when(sessionExtenderRequest.getSessionKey()).thenReturn(IDP_SESSION_KEY);
        when(SessionContextCache.getInstance()).thenReturn(sessionContextCache);
        when(sessionContextCache.getSessionContextCacheEntry(anyObject(), anyString()))
                .thenReturn(sessionContextCacheEntry);
        when(sessionContextCacheEntry.getContext()).thenReturn(sessionContext);

        SessionExtenderResponse.SessionExtenderResponseBuilder responseBuilder =
                (SessionExtenderResponse.SessionExtenderResponseBuilder) sessionExtenderProcessor
                        .process(sessionExtenderRequest);
        SessionExtenderResponse response = responseBuilder.build();
        assertNotNull(response.getTraceId(), "Error creating successful response.");
    }
}
