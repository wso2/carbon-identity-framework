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
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.UnsupportedEncodingException;

import static org.mockito.ArgumentMatchers.anyBoolean;

@PrepareForTest({MutualSSLManager.class})
@PowerMockIgnore("org.mockito.*")
public class MutualSSLManagerTest extends PowerMockTestCase {

    private String clientKeyStore = "./repository/resources/security/wso2carbon.jks";
    private String clientTrustStore = "./repository/resources/security/client-truststore.jks";
    private String endpointConfigFileName = "./repository/conf/identity/EndpointConfig.properties";
    private String endpointConfigFileName1 = "./repository/conf/identity/EndpointConfig1.properties";
    private String endpointConfigFileName2 = "./repository/conf/identity/EndpointConfig2.properties";

    @BeforeMethod
    public void setUp() throws Exception {

        PowerMockito.spy(MutualSSLManager.class);
        PowerMockito.doReturn(buildFilePath(clientKeyStore))
                .when(MutualSSLManager.class, "buildFilePath", clientKeyStore);
        PowerMockito.doReturn(buildFilePath(clientTrustStore))
                .when(MutualSSLManager.class, "buildFilePath", clientTrustStore);

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

        PowerMockito.doNothing().when(MutualSSLManager.class, "initMutualSSLConnection", anyBoolean());

        PowerMockito.doReturn(buildFilePath(configFilePath))
                .when(MutualSSLManager.class, "buildFilePath", Constants.TenantConstants.CONFIG_RELATIVE_PATH);
        MutualSSLManager.init();
        Assert.assertEquals(carbonLogin, MutualSSLManager.getCarbonLogin());
    }

    private String buildFilePath(String path) {
        String filepath = System.getProperty("user.dir")
                + File.separator + "src"
                + File.separator + "test"
                + File.separator + "resources"
                + File.separator + path;
        return filepath;
    }

}
