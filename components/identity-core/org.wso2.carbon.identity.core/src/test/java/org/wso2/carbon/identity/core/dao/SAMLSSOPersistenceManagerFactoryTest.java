package org.wso2.carbon.identity.core.dao;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

import static org.testng.Assert.assertTrue;

public class SAMLSSOPersistenceManagerFactoryTest {

    private SAMLSSOPersistenceManagerFactory factory;

    @BeforeMethod
    public void setUp() {

        factory = new SAMLSSOPersistenceManagerFactory();

    }

    @AfterMethod
    public void tearDown() throws Exception {

        setPrivateStaticField(SAMLSSOPersistenceManagerFactory.class, "SAML_STORAGE_TYPE", "");
        factory = null;
    }

    @Test
    public void testBuildSSOServiceProviderManagerWithDefaultStorage() throws Exception {

        setPrivateStaticField(SAMLSSOPersistenceManagerFactory.class, "SAML_STORAGE_TYPE", "database");
        SAMLSSOServiceProviderDAO samlSSOServiceProviderDAO = factory.buildSSOServiceProviderManager();
        assertTrue(samlSSOServiceProviderDAO instanceof JDBCSAMLSSOServiceProviderDAOImpl);
    }

    @Test
    public void testBuildSSOServiceProviderManagerWithRegistryStorage() throws Exception {

        setPrivateStaticField(SAMLSSOPersistenceManagerFactory.class, "SAML_STORAGE_TYPE", "registry");
        SAMLSSOServiceProviderDAO samlSSOServiceProviderDAO = factory.buildSSOServiceProviderManager();
        assertTrue(samlSSOServiceProviderDAO instanceof RegistrySAMLSSOServiceProviderDAOImpl);
    }

    @Test
    public void testBuildSSOServiceProviderManagerWithHybridStorage() throws Exception {

        setPrivateStaticField(SAMLSSOPersistenceManagerFactory.class, "SAML_STORAGE_TYPE", "hybrid");
        SAMLSSOServiceProviderDAO samlSSOServiceProviderDAO = factory.buildSSOServiceProviderManager();
        assertTrue(samlSSOServiceProviderDAO instanceof JDBCSAMLSSOServiceProviderDAOImpl);
    }

    private void setPrivateStaticField(Class<?> clazz, String fieldName, Object newValue)
            throws NoSuchFieldException, IllegalAccessException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, newValue);
    }
}