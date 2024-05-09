/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.application.authentication.endpoint.util;

import org.apache.axiom.om.util.Base64;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mockStatic;

public class MutualSSLManagerTest {

    private String clientKeyStore = "./repository/resources/security/wso2carbon.jks";
    private String clientTrustStore = "./repository/resources/security/client-truststore.jks";
    private String endpointConfigFileName = "./repository/conf/identity/EndpointConfig.properties";
    private String endpointConfigFileName1 = "./repository/conf/identity/EndpointConfig1.properties";
    private String endpointConfigFileName2 = "./repository/conf/identity/EndpointConfig2.properties";

    @BeforeMethod
    public void setUp() throws Exception {

        setPrivateStaticField(MutualSSLManager.class, "initialized", false);
        setPrivateStaticField(MutualSSLManager.class, "carbonLogin", "");
    }

    @DataProvider(name = "configData")
    public Object[][] configData() throws UnsupportedEncodingException {

        String carbonLogin = Base64.encode("admin".getBytes(Constants.TenantConstants.CHARACTER_ENCODING));
        return new Object[][]{
                {endpointConfigFileName, ""},
                {endpointConfigFileName1, carbonLogin},
                {endpointConfigFileName2, carbonLogin}
        };
    }

    @Test(dataProvider = "configData")
    public void testInit(String configFilePath, String carbonLogin) throws Exception {

        try (MockedStatic<MutualSSLManager> mutualSSLManager = mockStatic(MutualSSLManager.class,
                Mockito.CALLS_REAL_METHODS)) {
            mutualSSLManager.when(() -> MutualSSLManager.buildFilePath(clientKeyStore)).thenReturn(buildFilePath(clientKeyStore));
            mutualSSLManager.when(() -> MutualSSLManager.buildFilePath(clientTrustStore)).thenReturn(buildFilePath(clientTrustStore));
            mutualSSLManager.when(() -> MutualSSLManager.buildFilePath(Constants.TenantConstants.CONFIG_RELATIVE_PATH)).thenReturn(buildFilePath(configFilePath));
            mutualSSLManager.when(() -> MutualSSLManager.initMutualSSLConnection(anyBoolean())).thenAnswer(invocation -> null);

            MutualSSLManager.init();
            Assert.assertEquals(MutualSSLManager.getCarbonLogin(), carbonLogin);
        }
    }

    private String buildFilePath(String path) {
        String filepath = System.getProperty("user.dir")
                + File.separator + "src"
                + File.separator + "test"
                + File.separator + "resources"
                + File.separator + path;
        return filepath;
    }

    private void setPrivateStaticField(Class<?> clazz, String fieldName, Object newValue)
            throws NoSuchFieldException, IllegalAccessException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, newValue);
    }
}
