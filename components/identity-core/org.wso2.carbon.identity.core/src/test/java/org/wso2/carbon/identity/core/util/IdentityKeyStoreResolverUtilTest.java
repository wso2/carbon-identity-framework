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

package org.wso2.carbon.identity.core.util;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.utils.security.KeystoreUtils;

import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;

import static org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverUtil.buildCustomKeyStoreName;
import static org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverUtil.buildTenantKeyStoreName;
import static org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverUtil.getQNameWithIdentityNameSpace;

import javax.xml.namespace.QName;

/**
 * Test cases for IdentityKeyStoreResolverUtil.
 */
public class IdentityKeyStoreResolverUtilTest {

    private MockedStatic<KeystoreUtils> keystoreUtils;

    @BeforeClass
    public void setUp() throws Exception {

        keystoreUtils = mockStatic(KeystoreUtils.class);
    }

    @DataProvider(name = "CorrectTenantKeyStoreNameDataProvider")
    public Object[][] correctTenantKeyStoreNameDataProvider() {

        return new Object[][] {
                {"example", "example.jks"},
                {"example.com", "example-com.jks"}
        };
    }

    @AfterClass
    public void close() {

        keystoreUtils.close();
    }

    @Test(dataProvider = "CorrectTenantKeyStoreNameDataProvider")
    public void testCorrectBuildTenantKeyStoreName(String tenantDomain, String expectedResult) throws IdentityKeyStoreResolverException {

        keystoreUtils.when(() -> KeystoreUtils.getKeyStoreFileExtension(tenantDomain.replace(".", "-"), tenantDomain)).thenReturn(".jks");
        assertEquals(buildTenantKeyStoreName(tenantDomain), expectedResult);
    }

    @DataProvider(name = "IncorrectTenantKeyStoreNameDataProvider")
    public Object[] incorrectTenantKeyStoreNameDataProvider() {

        return new Object[] {
                "",
                null
        };
    }

    @Test(dataProvider = "IncorrectTenantKeyStoreNameDataProvider", expectedExceptions = IdentityKeyStoreResolverException.class)
    public void testIncorrectBuildTenantKeyStoreName(String tenantDomain) throws IdentityKeyStoreResolverException {

        buildTenantKeyStoreName(tenantDomain);
    }

    @DataProvider(name = "CorrectCustomKeyStoreNameDataProvider")
    public Object[][] correctCustomKeyStoreNameDataProvider() {

        return new Object[][] {
                {"example.jks", "CUSTOM/example.jks"},
                {"k$ySt&re.jks", "CUSTOM/k$ySt&re.jks"}
        };
    }

    @Test(dataProvider = "CorrectCustomKeyStoreNameDataProvider")
    public void testCorrectBuildCustomKeyStoreName(String keyStoreName, String expectedResult) throws IdentityKeyStoreResolverException {

        assertEquals(expectedResult, buildCustomKeyStoreName(keyStoreName));
    }

    @DataProvider(name = "IncorrectCustomKeyStoreNameDataProvider")
    public Object[] incorrectCustomKeyStoreNameDataProvider() {

        return new Object[] {
                "",
                null
        };
    }

    @Test(dataProvider = "IncorrectCustomKeyStoreNameDataProvider", expectedExceptions = IdentityKeyStoreResolverException.class)
    public void testIncorrectBuildCustomKeyStoreName(String keyStoreName) throws IdentityKeyStoreResolverException {

        buildCustomKeyStoreName(keyStoreName);
    }

    @Test
    public void testGetQNameWithIdentityNameSpace() {

        QName qName = getQNameWithIdentityNameSpace("localPart");
        assertEquals(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, qName.getNamespaceURI());
        assertEquals("localPart", qName.getLocalPart());
    }
}
