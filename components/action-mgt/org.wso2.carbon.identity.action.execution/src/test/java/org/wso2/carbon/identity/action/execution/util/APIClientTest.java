/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.execution.util;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationResponse;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class APIClientTest {

//    @Mock
    /*private CloseableHttpClient mockHttpClient;

    private APIClient apiClient;

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.initMocks(this);
        apiClient = new APIClient(mockHttpClient);
    }

    @Test
    public void testCallAPISuccess() throws Exception {

        HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.getStatusLine()).thenReturn(
                new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
        String json = "{\"message\":\"success\"}";
        when(mockResponse.getEntity()).thenReturn(new StringEntity(json, StandardCharsets.UTF_8));

        when(mockHttpClient.execute(any(HttpPost.class))).thenReturn(mockResponse);

        ActionInvocationResponse response = apiClient.callAPI("http://example.com/api", null, "{}");

        Assert.assertNotNull(response);
//        Assert.assertTrue(response instanceof ActionInvocationSuccessResponse);
//        Assert.assertEquals(((ActionInvocationSuccessResponse) response).getMessage(), "success");
    }*/
}