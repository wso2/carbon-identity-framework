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