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

package org.wso2.carbon.identity.trusted.app.mgt.servlet;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.trusted.app.mgt.exceptions.TrustedAppMgtException;
import org.wso2.carbon.identity.trusted.app.mgt.internal.TrustedAppMgtDataHolder;
import org.wso2.carbon.identity.trusted.app.mgt.model.TrustedAndroidApp;
import org.wso2.carbon.identity.trusted.app.mgt.services.TrustedAppMgtService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.ANDROID_CREDENTIAL_PERMISSION;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.ANDROID_HANDLE_URLS_PERMISSION;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.CT_APPLICATION_JSON;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.HTTP_RESP_HEADER_CACHE_CONTROL;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.HTTP_RESP_HEADER_PRAGMA;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.HTTP_RESP_HEADER_VAL_CACHE_CONTROL_NO_STORE;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.HTTP_RESP_HEADER_VAL_PRAGMA_NO_CACHE;

/**
 * Test class for AndroidTrustedAppDiscoveryServlet.
 */
public class AndroidTrustedAppDiscoveryServletTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    private MockedStatic<TrustedAppMgtDataHolder> trustedAppMgtDataHolder;
    private TrustedAppMgtService trustedAppMgtService;

    @BeforeMethod
    public void setup() {

        TrustedAppMgtDataHolder mockTrustedAppMgtDataHolder = mock(TrustedAppMgtDataHolder.class);
        trustedAppMgtDataHolder = mockStatic(TrustedAppMgtDataHolder.class);
        trustedAppMgtDataHolder.when(TrustedAppMgtDataHolder::getInstance)
                .thenReturn(mockTrustedAppMgtDataHolder);
        trustedAppMgtService = mock(TrustedAppMgtService.class);
        when(mockTrustedAppMgtDataHolder.getTrustedAppMgtService())
                .thenReturn(trustedAppMgtService);

        httpServletResponse = mock(HttpServletResponse.class);
        httpServletRequest = mock(HttpServletRequest.class);
    }

    @AfterMethod
    public void tearDown() {

        trustedAppMgtDataHolder.close();
    }

    @DataProvider(name = "doGetDataProvider")
    public Object[][] doGetDataProvider() {

        Set<String> permissions = new HashSet<>();
        permissions.add(ANDROID_HANDLE_URLS_PERMISSION);
        permissions.add(ANDROID_CREDENTIAL_PERMISSION);

        TrustedAndroidApp trustedApp1 = new TrustedAndroidApp();
        trustedApp1.setPackageName("com.wso2.sample.mobile1");
        trustedApp1.setThumbprints(new String[] {"thumbprint1"});
        trustedApp1.setPermissions(permissions);

        TrustedAndroidApp trustedApp2 = new TrustedAndroidApp();
        trustedApp2.setPackageName("com.wso2.sample.mobile2");
        trustedApp2.setThumbprints(new String[] {"thumbprint1", "thumbprint2"});
        trustedApp2.setPermissions(permissions);

        List<TrustedAndroidApp> trustedApps1 = new ArrayList<>();
        trustedApps1.add(trustedApp1);

        List<TrustedAndroidApp> trustedApps2 = new ArrayList<>();
        trustedApps2.add(trustedApp2);

        List<TrustedAndroidApp> trustedApps3 = new ArrayList<>();
        trustedApps3.add(trustedApp1);
        trustedApps3.add(trustedApp2);

        return new Object[][]{

                // Single trusted app with single thumbprint.
                {trustedApps1, "[{\"relation\":[\"" + ANDROID_HANDLE_URLS_PERMISSION + "\"," +
                        "\"" + ANDROID_CREDENTIAL_PERMISSION + "\"],\"target\":{\"namespace\":\"android_app\"," +
                        "\"package_name\":\"com.wso2.sample.mobile1\",\"sha256_cert_fingerprints\":" +
                        "[\"thumbprint1\"]}}]"},

                // Single trusted app with multiple thumbprints.
                {trustedApps2, "[{\"relation\":[\"" + ANDROID_HANDLE_URLS_PERMISSION + "\"," +
                        "\"" + ANDROID_CREDENTIAL_PERMISSION + "\"],\"target\":{\"namespace\":\"android_app\"," +
                        "\"package_name\":\"com.wso2.sample.mobile2\",\"sha256_cert_fingerprints\":" +
                        "[\"thumbprint1\",\"thumbprint2\"]}}]"},

                // Multiple trusted apps.
                {trustedApps3, "[{\"relation\":[\"" + ANDROID_HANDLE_URLS_PERMISSION + "\"," +
                        "\"" + ANDROID_CREDENTIAL_PERMISSION + "\"],\"target\":{\"namespace\":\"android_app\"," +
                        "\"package_name\":\"com.wso2.sample.mobile1\",\"sha256_cert_fingerprints\":" +
                        "[\"thumbprint1\"]}},{\"relation\":[\"" + ANDROID_HANDLE_URLS_PERMISSION + "\"," +
                        "\"" + ANDROID_CREDENTIAL_PERMISSION + "\"],\"target\":{\"namespace\":\"android_app\"," +
                        "\"package_name\":\"com.wso2.sample.mobile2\",\"sha256_cert_fingerprints\":" +
                        "[\"thumbprint1\",\"thumbprint2\"]}}]"}
        };
    }

    @Test(dataProvider = "doGetDataProvider")
    public void testDoGet(List<TrustedAndroidApp> trustedAndroidApps, String expectedServletResponse)
            throws TrustedAppMgtException, NoSuchMethodException, IOException,
            InvocationTargetException, IllegalAccessException {

        when(trustedAppMgtService.getTrustedAndroidApps()).thenReturn(trustedAndroidApps);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(httpServletResponse.getWriter()).thenReturn(writer);

        AndroidTrustedAppDiscoveryServlet androidTrustedAppDiscoveryServlet = new AndroidTrustedAppDiscoveryServlet();
        Method doGet = AndroidTrustedAppDiscoveryServlet.class.getDeclaredMethod(
                "doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(androidTrustedAppDiscoveryServlet, httpServletRequest, httpServletResponse);

        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
        verify(httpServletResponse).setContentType(CT_APPLICATION_JSON);
        verify(httpServletResponse).setHeader(HTTP_RESP_HEADER_CACHE_CONTROL,
                HTTP_RESP_HEADER_VAL_CACHE_CONTROL_NO_STORE);
        verify(httpServletResponse).setHeader(HTTP_RESP_HEADER_PRAGMA, HTTP_RESP_HEADER_VAL_PRAGMA_NO_CACHE);

        Assert.assertEquals(stringWriter.toString(), expectedServletResponse);
        writer.flush();
    }
}
