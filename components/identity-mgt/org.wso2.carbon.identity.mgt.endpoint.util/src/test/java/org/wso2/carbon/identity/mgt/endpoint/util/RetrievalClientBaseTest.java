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

package org.wso2.carbon.identity.mgt.endpoint.util;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID;

@Listeners(MockitoTestNGListener.class)
public class RetrievalClientBaseTest {

    @Mock
    IdentityManagementServiceUtil identityManagementServiceUtil;
    @Mock
    HttpClientBuilder httpClientBuilder;
    @Mock
    CloseableHttpClient httpClient;
    @Mock
    HttpEntity httpEntity;
    @Mock
    ClassicHttpResponse httpResponse;

    private String mockJsonResponse = "{}";
    public static final String SUPER_TENANT_DOMAIN = "carbon.super";
    private static final String SERVICE_URL = "https://wso2.org:9443";
    private static final String APP_NAME = "admin";
    private static final String APP_PASSWORD = "p@ssw0rd";

    public String getMockJsonResponse() {

        return mockJsonResponse;
    }

    public void setMockJsonResponse(String mockJsonResponse) {

        this.mockJsonResponse = mockJsonResponse;
    }

    @BeforeMethod
    public void setup() throws IOException {
        setupConfiguration();
        when(httpClientBuilder.build()).thenReturn(httpClient);
        when(httpClient.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class)))
                .thenAnswer(invocation -> {
                    HttpClientResponseHandler<?> handler = invocation.getArgument(1);
                    return handler.handleResponse(httpResponse);
                });

        when(httpResponse.getCode()).thenReturn(200);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        InputStream inputStream = new ByteArrayInputStream(mockJsonResponse.getBytes());
        when(httpEntity.getContent()).thenReturn(inputStream);

        when(identityManagementServiceUtil.getContextURLFromFile()).thenReturn(SERVICE_URL);
        when(identityManagementServiceUtil.getAppName()).thenReturn(APP_NAME);
        when(identityManagementServiceUtil.getAppPassword()).thenReturn(APP_PASSWORD.toCharArray());
    }

    public void setupConfiguration() {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes", "repository").
                toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(SUPER_TENANT_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext();
    }

    /**
     * Read a resource in the responses directory.
     *
     * @param path File path to be read.
     * @return Content of the file as a String.
     * @throws IOException Exception when file reading fails.
     */
    public String readResource(String path) throws IOException {

        path = "responses/" + path;
        try (InputStream resourceAsStream = RetrievalClientBaseTest.class.getClassLoader().getResourceAsStream(path);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(resourceAsStream)) {
            StringBuilder resourceFile = new StringBuilder();

            int character;
            while ((character = bufferedInputStream.read()) != -1) {
                char value = (char) character;
                resourceFile.append(value);
            }
            return resourceFile.toString();
        }
    }
}
