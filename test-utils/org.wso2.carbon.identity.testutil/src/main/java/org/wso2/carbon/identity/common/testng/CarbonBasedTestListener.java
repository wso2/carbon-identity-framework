/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.common.testng;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.internal.util.reflection.Whitebox;
import org.testng.IClassListener;
import org.testng.IMethodInstance;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.common.testng.realm.InMemoryRealmService;
import org.wso2.carbon.identity.common.testng.realm.MockUserStoreManager;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceComponent;
import org.wso2.carbon.identity.core.persistence.JDBCPersistenceManager;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.testutil.ReadCertStoreSampleUtil;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.jdbc.dataaccess.JDBCDataAccessManager;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Common TestNg Listener to provide common functions in Identity Testing.
 */
public class CarbonBasedTestListener implements ITestListener, IClassListener {

    private Log log = LogFactory.getLog(CarbonBasedTestListener.class);
    private static final String REG_DB_JNDI_NAME = "jdbc/WSO2RegDB";
    private static final String REG_DB_SQL_FILE = "dbScripts/registry.sql";

    @Override
    public void onTestStart(ITestResult iTestResult) {
    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {
    }

    @Override
    public void onTestFailure(ITestResult iTestResult) {
    }

    @Override
    public void onTestSkipped(ITestResult iTestResult) {

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {

    }

    @Override
    public void onStart(ITestContext iTestContext) {
    }

    @Override
    public void onFinish(ITestContext iTestContext) {
    }

    private boolean annotationPresent(Class c, Class clazz) {
        boolean retVal = c.isAnnotationPresent(clazz) ? true : false;
        return retVal;
    }

    private boolean annotationPresent(Field f, Class clazz) {
        boolean retVal = f.isAnnotationPresent(clazz) ? true : false;
        return retVal;
    }

    public static void setInternalState(Class c, String field, Object value) {

        try {
            Field f = c.getDeclaredField(field);
            f.setAccessible(true);
            f.set(null, value);
        } catch (Exception e) {
            throw new RuntimeException("Unable to set internal state on a private field.", e);
        }
    }

    public static void setInternalState(Object target, String field, Object value) {
        Class c = target.getClass();

        try {
            Field f = c.getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Unable to set internal state on a private field.", e);
        }
    }

    private void callInternalMethod(Object target, String method, Class[] types, Object... values) {

        Class c = target.getClass();

        try {
            Method m = c.getDeclaredMethod(method, types);
            m.setAccessible(true);
            m.invoke(target, values);
        } catch (Exception e) {
            throw new RuntimeException("Unable to set internal state on a private field.", e);
        }
    }

    @Override
    public void onBeforeClass(ITestClass iTestClass, IMethodInstance iMethodInstance) {
        Class realClass = iTestClass.getRealClass();
        if (annotationPresent(realClass, WithCarbonHome.class)) {
            System.setProperty(CarbonBaseConstants.CARBON_HOME, realClass.getResource("/").getFile());
            System.setProperty(TestConstants.CARBON_PROTOCOL, TestConstants.CARBON_PROTOCOL_HTTPS);
            System.setProperty(TestConstants.CARBON_HOST, TestConstants.CARBON_HOST_LOCALHOST);
            System.setProperty(TestConstants.CARBON_MANAGEMENT_PORT, TestConstants.CARBON_DEFAULT_HTTPS_PORT);
        }
        if (annotationPresent(realClass, WithAxisConfiguration.class)) {
            AxisConfiguration axisConfiguration = new AxisConfiguration();
            ConfigurationContext configurationContext = new ConfigurationContext(axisConfiguration);
            setInternalState(IdentityCoreServiceComponent.class, "configurationContextService",
                    new ConfigurationContextService(configurationContext, configurationContext));
        }
        if (annotationPresent(realClass, WithH2Database.class)) {
            System.setProperty("java.naming.factory.initial",
                    "org.wso2.carbon.identity.common.testng.MockInitialContextFactory");
            Annotation annotation = realClass.getAnnotation(WithH2Database.class);
            WithH2Database withH2Database = (WithH2Database) annotation;
            MockInitialContextFactory
                    .initializeDatasource(withH2Database.jndiName(), this.getClass(), withH2Database.files());
            setInternalState(JDBCPersistenceManager.class, "instance", null);
        }
        if (annotationPresent(realClass, WithRealmService.class)) {
            Annotation annotation = realClass.getAnnotation(WithRealmService.class);
            WithRealmService withRealmService = (WithRealmService) annotation;
            createRealmService(withRealmService);
        }
        if (annotationPresent(realClass, WithRegistry.class)) {
            Annotation annotation = realClass.getAnnotation(WithRegistry.class);
            WithRegistry withRegistry = (WithRegistry) annotation;
            createRegistryService(realClass, withRegistry);
        }
        if (annotationPresent(realClass, WithKeyStore.class)) {
            Annotation annotation = realClass.getAnnotation(WithKeyStore.class);
            WithKeyStore withKeyStore = (WithKeyStore) annotation;
            createKeyStore(withKeyStore);
        }
        Field[] fields = realClass.getDeclaredFields();
        processFields(fields, iMethodInstance.getInstance());
    }

    private void createKeyStore(WithKeyStore withKeyStore) {
        try {
            KeyStoreManager keyStoreManager = mock(KeyStoreManager.class);
            ServerConfigurationService serverConfigurationService = mock(ServerConfigurationService.class);
            RegistryService registryService = mock(RegistryService.class);
            ConcurrentHashMap<String, KeyStoreManager> mtKeyStoreManagers = new ConcurrentHashMap();
            mtKeyStoreManagers.put(String.valueOf(withKeyStore.tenantId()), keyStoreManager);
            Whitebox.setInternalState(keyStoreManager, "mtKeyStoreManagers", mtKeyStoreManagers);
            KeyStore keyStore = ReadCertStoreSampleUtil.createKeyStore(getClass());
            Whitebox.setInternalState(keyStoreManager, "primaryKeyStore", keyStore);
            Whitebox.setInternalState(keyStoreManager, "registryKeyStore", keyStore);
            CarbonCoreDataHolder carbonCoreDataHolder = mock(CarbonCoreDataHolder.class);
            CarbonCoreDataHolder.getInstance().setRegistryService(registryService);
            CarbonCoreDataHolder.getInstance().setServerConfigurationService(serverConfigurationService);
            carbonCoreDataHolder.setServerConfigurationService(serverConfigurationService);
            carbonCoreDataHolder.setRegistryService(registryService);
            when(keyStoreManager.getDefaultPrimaryCertificate())
                    .thenReturn((X509Certificate) keyStore.getCertificate(withKeyStore.alias()));
        } catch (NoSuchAlgorithmException e) {
            log.error("Error while reading cert.", e);
        } catch (UnrecoverableKeyException e) {
            log.error("Error while reading cert.", e);
        } catch (IllegalAccessException e) {
            log.error("Error while reading cert.", e);
        } catch (KeyStoreException e) {
            log.error("Error while reading cert.", e);
        } catch (Exception e) {
            log.error("Error while reading cert.", e);
        }
    }

    private void createRealmService(WithRealmService withRealmService) {
        try {
            int tenantId = withRealmService.tenantId();
            String tenantDomain = withRealmService.tenantDomain();

            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            RealmService realmService = new InMemoryRealmService(tenantId);
            UserStoreManager userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            ((MockUserStoreManager) userStoreManager)
                    .addSecondaryUserStoreManager("PRIMARY", (MockUserStoreManager) userStoreManager);
            IdentityTenantUtil.setRealmService(realmService);
            RegistryDataHolder.getInstance().setRealmService(realmService);

            Class[] singletonClasses = withRealmService.injectToSingletons();
            for (Class singletonClass : singletonClasses) {
                Object instance = getSingletonInstance(singletonClass);
                if (instance != null) {
                    setInstanceValue(realmService, RealmService.class, singletonClass, instance);
                } else {
                    setInstanceValue(realmService, RealmService.class, singletonClass, null);
                }

            }
        } catch (UserStoreException e) {
            log.error("Error setting the realm.", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private void injectSingletonVariables(Object value, Class valueType, Class[] singletonClasses) {
        for (Class singletonClass : singletonClasses) {
            Object instance = getSingletonInstance(singletonClass);
            if (instance != null) {
                setInstanceValue(value, valueType, singletonClass, instance);
            } else {
                setInstanceValue(value, valueType, singletonClass, null);
            }

        }
    }

    private void createRegistryService(Class realClass, WithRegistry withRegistry) {
        try {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(withRegistry.tenantDomain());
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(withRegistry.tenantId());
            RegistryContext registryContext = RegistryContext.getBaseInstance(IdentityTenantUtil.getRealmService());
            DataSource dataSource = MockInitialContextFactory
                    .initializeDatasource(REG_DB_JNDI_NAME, realClass, new String[] { REG_DB_SQL_FILE });
            registryContext.setDataAccessManager(new JDBCDataAccessManager(dataSource));
            RegistryService registryService = new EmbeddedRegistryService(registryContext);

            OSGiDataHolder.getInstance().setRegistryService(registryService);
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setRegistry(RegistryType.USER_GOVERNANCE, registryService.getRegistry());
            Class[] singletonClasses = withRegistry.injectToSingletons();
            injectSingletonVariables(registryService, RegistryService.class, singletonClasses);
        } catch (RegistryException e) {
            log.error("Error creating the registry.", e);
        }
    }

    private Object getSingletonInstance(Class singletonClass) {
        for (Method m : singletonClass.getDeclaredMethods()) {
            if (m.getName().equals("getInstance")) {
                try {
                    return m.invoke(null, null);
                } catch (IllegalAccessException e) {
                    log.error("Error in invoking singleton class", e);
                } catch (InvocationTargetException e) {
                    log.error("Error getting singleton class", e);
                }
            }
        }
        return null;
    }

    private void setInstanceValue(Object value, Class valueType, Class clazz, Object instance) {
        for (Field field1 : clazz.getDeclaredFields()) {
            if (field1.getType().isAssignableFrom(valueType)) {
                field1.setAccessible(true);

                if (java.lang.reflect.Modifier.isStatic(field1.getModifiers())) {
                    setInternalState(clazz, field1.getName(), value);
                } else if (instance != null) {
                    setInternalState(instance, field1.getName(), value);
                }
            }
        }
    }

    @Override
    public void onAfterClass(ITestClass iTestClass, IMethodInstance iMethodInstance) {
        MockInitialContextFactory.destroy();
    }

    private void processFields(Field[] fields, Object realInstance) {
        for (Field field : fields) {
            if (annotationPresent(field, WithRealmService.class)) {
                field.setAccessible(true);
                Annotation annotation = field.getAnnotation(WithRealmService.class);
                WithRealmService withRealmService = (WithRealmService) annotation;
                try {
                    RealmService realmService = mock(RealmService.class);
                    RealmConfiguration realmConfiguration = mock(RealmConfiguration.class);
                    TenantManager tenantManager = mock(TenantManager.class);
                    when(realmService.getTenantManager()).thenReturn(tenantManager);
                    when(realmService.getBootstrapRealmConfiguration()).thenReturn(realmConfiguration);
                    when(tenantManager.getTenantId(anyString())).thenReturn(withRealmService.tenantId());
                    field.set(realInstance, realmService);
                    IdentityTenantUtil.setRealmService(realmService);
                } catch (IllegalAccessException e) {
                    log.error("Error in setting field value: " + field.getName() + ", Class: " + field
                            .getDeclaringClass(), e);
                } catch (UserStoreException e) {
                    log.error("Error in setting user store value: " + field.getName() + ", Class: " + field
                            .getDeclaringClass(), e);
                }

            }
        }
    }
}
