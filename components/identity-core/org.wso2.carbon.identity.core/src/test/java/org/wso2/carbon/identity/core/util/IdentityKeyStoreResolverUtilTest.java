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

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

import static org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverUtil.buildCustomKeyStoreName;
import static org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverUtil.buildTenantKeyStoreName;
import static org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverUtil.getQNameWithIdentityNameSpace;

import javax.xml.namespace.QName;

public class IdentityKeyStoreResolverUtilTest {

    @Test
    public void testBuildTenantKeyStoreName() throws IdentityKeyStoreResolverException {

        assertEquals("example-com.jks", buildTenantKeyStoreName("example.com"));
        assertThrows(IdentityKeyStoreResolverException.class, () -> buildTenantKeyStoreName(""));
        assertThrows(IdentityKeyStoreResolverException.class, () -> buildTenantKeyStoreName(null));
    }

    @Test
    public void testBuildCustomKeyStoreName() throws IdentityKeyStoreResolverException {

        assertEquals("CUSTOM/myKeyStore", buildCustomKeyStoreName("myKeyStore"));
        assertEquals("CUSTOM/@#$_keyStore", buildCustomKeyStoreName("@#$_keyStore"));
        assertThrows(IdentityKeyStoreResolverException.class, () -> buildTenantKeyStoreName(""));
        assertThrows(IdentityKeyStoreResolverException.class, () -> buildTenantKeyStoreName(null));
    }

    @Test
    public void testGetQNameWithIdentityNameSpace() {

        QName qName = getQNameWithIdentityNameSpace("localPart");
        assertEquals(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, qName.getNamespaceURI());
        assertEquals("localPart", qName.getLocalPart());
    }
}