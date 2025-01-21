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

package org.wso2.carbon.identity.core.dao;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;

import static org.testng.Assert.assertTrue;

/**
 * This class tests the methods of the SAMLServiceProviderPersistenceManagerFactory class.
 */
public class SAMLServiceProviderPersistenceManagerFactoryTest {

    private SAMLServiceProviderPersistenceManagerFactory factory;

    @BeforeMethod
    public void setUp() {

        URL root = this.getClass().getClassLoader().getResource(".");
        File file = new File(root.getPath());
        System.setProperty("carbon.home", file.getAbsolutePath());
        factory = new SAMLServiceProviderPersistenceManagerFactory();
    }

    @AfterMethod
    public void tearDown() throws Exception {

        setPrivateStaticField(SAMLServiceProviderPersistenceManagerFactory.class, "SAML_STORAGE_TYPE", "");
        factory = null;
    }

    @Test
    public void testGetSAMLServiceProviderPersistenceManagerWithDefaultStorage() throws Exception {

        setPrivateStaticField(SAMLServiceProviderPersistenceManagerFactory.class, "SAML_STORAGE_TYPE", "database");
        SAMLSSOServiceProviderDAO samlSSOServiceProviderDAO = factory.getSAMLServiceProviderPersistenceManager();
        assertTrue(samlSSOServiceProviderDAO instanceof CacheBackedSAMLSSOServiceProviderDAO);
    }

    @Test
    public void testGetSAMLServiceProviderPersistenceManagerWithRegistryStorage() throws Exception {

        setPrivateStaticField(SAMLServiceProviderPersistenceManagerFactory.class, "SAML_STORAGE_TYPE", "registry");
        SAMLSSOServiceProviderDAO samlSSOServiceProviderDAO = factory.getSAMLServiceProviderPersistenceManager();
        assertTrue(samlSSOServiceProviderDAO instanceof RegistrySAMLSSOServiceProviderDAOImpl);
    }

    @Test
    public void testGetSAMLServiceProviderPersistenceManagerWithHybridStorage() throws Exception {

        setPrivateStaticField(SAMLServiceProviderPersistenceManagerFactory.class, "SAML_STORAGE_TYPE", "hybrid");
        SAMLSSOServiceProviderDAO samlSSOServiceProviderDAO = factory.getSAMLServiceProviderPersistenceManager();
        assertTrue(samlSSOServiceProviderDAO instanceof HybridSAMLSSOServiceProviderDAOImpl);
    }

    private void setPrivateStaticField(Class<?> clazz, String fieldName, Object newValue)
            throws NoSuchFieldException, IllegalAccessException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, newValue);
    }
}
