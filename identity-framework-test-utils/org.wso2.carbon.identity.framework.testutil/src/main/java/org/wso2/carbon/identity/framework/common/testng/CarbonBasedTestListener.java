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

package org.wso2.carbon.identity.framework.common.testng;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.IClassListener;
import org.testng.IMethodInstance;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.framework.common.testng.ms.MicroserviceServer;
import org.wso2.carbon.identity.framework.common.testng.realm.InMemoryRealmService;
import org.wso2.carbon.identity.framework.common.testng.realm.InMemoryTenantManager;
import org.wso2.carbon.identity.framework.common.testng.realm.MockUserStoreManager;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceComponent;
import org.wso2.carbon.identity.core.persistence.JDBCPersistenceManager;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.framework.testutil.ReadCertStoreSampleUtil;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.jdbc.dataaccess.JDBCDataAccessManager;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

/**
 * Common TestNg Listener to provide common functions in Identity Testing.
 */
public class CarbonBasedTestListener implements ITestListener, IClassListener {

    private Log log = LogFactory.getLog(CarbonBasedTestListener.class);
    private static final String REG_DB_JNDI_NAME = "jdbc/WSO2RegDB";
    private static final String REG_DB_SQL_FILE = "dbScripts/registry.sql";

    private RealmService testSessionRealmService;
    private RegistryService registryService;
    private Map<Object, MicroserviceServer> microserviceServerMap = new HashMap<>();

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

        return c.isAnnotationPresent(clazz);
    }

    private boolean annotationPresent(Field f, Class clazz) {

        return f.isAnnotationPresent(clazz) ? true : false;
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
            copyDefaultConfigsIfNotExists(realClass);
        }
        if (annotationPresent(realClass, WithAxisConfiguration.class)) {
            AxisConfiguration axisConfiguration = new AxisConfiguration();
            ConfigurationContext configurationContext = new ConfigurationContext(axisConfiguration);
            setInternalState(IdentityCoreServiceComponent.class, "configurationContextService",
                             new ConfigurationContextService(configurationContext, configurationContext));
        }
        if (annotationPresent(realClass, WithH2Database.class)) {
            System.setProperty("java.naming.factory.initial",
                               "org.wso2.carbon.identity.framework.common.testng.MockInitialContextFactory");
            Annotation annotation = realClass.getAnnotation(WithH2Database.class);
            WithH2Database withH2Database = (WithH2Database) annotation;
            MockInitialContextFactory
                    .initializeDatasource(withH2Database.jndiName(), this.getClass(), withH2Database.files());
            setInternalState(JDBCPersistenceManager.class, "instance", null);
        }
        if (annotationPresent(realClass, WithRealmService.class)) {
            Annotation annotation = realClass.getAnnotation(WithRealmService.class);
            WithRealmService withRealmService = (WithRealmService) annotation;
            try {
                createRealmService(withRealmService, false);
            } catch (UserStoreException e) {
                log.error("Could not initialize the realm service for test. Test class:  " + realClass
                        .getName(), e);
            }
        }
        if (annotationPresent(realClass, WithRegistry.class)) {
            Annotation annotation = realClass.getAnnotation(WithRegistry.class);
            WithRegistry withRegistry = (WithRegistry) annotation;
            createRegistryService(realClass, withRegistry);
        }
        if (annotationPresent(realClass, WithKeyStore.class)) {
            Annotation annotation = realClass.getAnnotation(WithKeyStore.class);
            WithKeyStore withKeyStore = (WithKeyStore) annotation;
            createKeyStore(realClass, withKeyStore);
        }
        if (annotationPresent(realClass, WithMicroService.class) && !microserviceServerInitialized(
                iMethodInstance.getInstance())) {
            MicroserviceServer microserviceServer = initMicroserviceServer(iMethodInstance.getInstance());
            scanAndLoadClasses(microserviceServer, realClass, iMethodInstance.getInstance());
        }
        Field[] fields = realClass.getDeclaredFields();
        processFields(fields, iMethodInstance.getInstance());
    }

    private boolean microserviceServerInitialized(Object instance) {

        MicroserviceServer microserviceServer = microserviceServerMap.get(instance);
        if (microserviceServer != null) {
            return microserviceServer.isActive();
        }
        return false;
    }

    private void copyDefaultConfigsIfNotExists(Class callerClass) {

        URL carbonXmlUrl = callerClass.getClassLoader().getResource("repository/conf/carbon.xml");
        boolean needToCopyCarbonXml = false;
        if (carbonXmlUrl == null) {
            needToCopyCarbonXml = true;
        } else {
            File file = new File(carbonXmlUrl.getPath());
            needToCopyCarbonXml = !file.exists();
        }
        if (needToCopyCarbonXml) {
            try {
                //Copy default identity xml into temp location and use it.
                URL url = CarbonBasedTestListener.class.getClassLoader().getResource("repository/conf/carbon.xml");
                InputStream inputStream = url.openStream();
                ReadableByteChannel inputChannel = Channels.newChannel(inputStream);

                Path tmpDirPath = Paths.get(System.getProperty("java.io.tmpdir"), "tests", callerClass.getSimpleName());
                Path repoConfPath = Paths.get(tmpDirPath.toUri().getPath(), "repository", "conf");
                Path carbonXMLPath = repoConfPath.resolve("carbon.xml");
                Path carbonXML = Files.createFile(carbonXMLPath);
                FileOutputStream fos = new FileOutputStream(carbonXML.toFile());
                WritableByteChannel targetChannel = fos.getChannel();
                //Transfer data from input channel to output channel
                ((FileChannel) targetChannel).transferFrom(inputChannel, 0, Short.MAX_VALUE);
                inputStream.close();
                targetChannel.close();
                fos.close();
                System.setProperty(TestConstants.CARBON_CONFIG_DIR_PATH, tmpDirPath.toString());
            } catch (IOException e) {
                log.error("Failed to copy carbon.xml", e);
            }
        } else {
            try {
                System.setProperty(TestConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonXmlUrl.toURI()).getParent()
                        .toString());
            } catch (URISyntaxException e) {
                log.error("Failed to copy path for carbon.xml", e);
            }
        }

    }

    private void createKeyStore(Class realClass, WithKeyStore withKeyStore) {

        try {
            RegistryService registryService = createRegistryService(realClass, withKeyStore.tenantId(),
                                                                    withKeyStore.tenantDomain());
            ServerConfiguration serverConfigurationService = ServerConfiguration.getInstance();
            serverConfigurationService.init(realClass.getResourceAsStream("/repository/conf/carbon.xml"));
            KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(withKeyStore.tenantId(),
                                                                          serverConfigurationService,
                                                                          registryService);
            if (!Proxy.isProxyClass(keyStoreManager.getClass()) &&
                    !keyStoreManager.getClass().getName().contains("EnhancerByMockitoWithCGLIB")  ) {
                KeyStore keyStore = ReadCertStoreSampleUtil.createKeyStore(getClass());
                org.wso2.carbon.identity.framework.testutil.Whitebox.setInternalState(keyStoreManager, "primaryKeyStore",
                                                                            keyStore);
                org.wso2.carbon.identity.framework.testutil.Whitebox.setInternalState(keyStoreManager, "registryKeyStore",
                                                                            keyStore);
            }
            CarbonCoreDataHolder.getInstance().setRegistryService(registryService);
            CarbonCoreDataHolder.getInstance().setServerConfigurationService(serverConfigurationService);
        } catch (Exception e) {
            throw new TestCreationException(
                    "Unhandled error while reading cert for test class:  " + realClass.getName(), e);
        }
    }

    private RealmService createRealmService(WithRealmService withRealmService, boolean reuseIfAvailable) throws
            UserStoreException {

        if (testSessionRealmService != null && reuseIfAvailable) {
            return testSessionRealmService;
        }
        try {
            int tenantId = withRealmService.tenantId();
            String tenantDomain = withRealmService.tenantDomain();

            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            testSessionRealmService = new InMemoryRealmService(tenantId);
            UserStoreManager userStoreManager = testSessionRealmService.getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            ((MockUserStoreManager) userStoreManager)
                    .addSecondaryUserStoreManager("PRIMARY", (MockUserStoreManager) userStoreManager);
            IdentityTenantUtil.setRealmService(testSessionRealmService);
            TenantManager tenantManager = new InMemoryTenantManager();
            testSessionRealmService.setTenantManager(tenantManager);
            RegistryDataHolder.getInstance().setRealmService(testSessionRealmService);
            OSGiDataHolder.getInstance().setUserRealmService(testSessionRealmService);

            Class[] singletonClasses = withRealmService.injectToSingletons();
            for (Class singletonClass : singletonClasses) {
                Object instance = getSingletonInstance(singletonClass);
                if (instance != null) {
                    setInstanceValue(testSessionRealmService, RealmService.class, singletonClass, instance);
                } else {
                    setInstanceValue(testSessionRealmService, RealmService.class, singletonClass, null);
                }

            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return testSessionRealmService;
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
            RegistryService registryService = createRegistryService(realClass, withRegistry.tenantId(),
                                                                    withRegistry.tenantDomain());
            Class[] singletonClasses = withRegistry.injectToSingletons();
            injectSingletonVariables(registryService, RegistryService.class, singletonClasses);
        } catch (RegistryException e) {
            log.error("Error creating the registry, for test case : " + realClass.getName(), e);
        }
    }

    /**
     * Creates the regostry service if not available.
     *
     * @param realClass
     * @return
     * @throws RegistryException
     */
    private RegistryService createRegistryService(Class realClass, int tenantID, String tenantDomain) throws
            RegistryException {

        if (registryService != null) {
            return registryService;
        }

        try {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantID);

            RegistryContext registryContext = RegistryContext.getBaseInstance(IdentityTenantUtil.getRealmService());
            DataSource dataSource = MockInitialContextFactory
                    .initializeDatasource(REG_DB_JNDI_NAME, realClass, new String[]{REG_DB_SQL_FILE});
            registryContext.setDataAccessManager(new JDBCDataAccessManager(dataSource));
            registryService = new EmbeddedRegistryService(registryContext);

            OSGiDataHolder.getInstance().setRegistryService(registryService);
            CarbonCoreDataHolder.getInstance().setRegistryService(registryService);
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setRegistry(RegistryType.USER_GOVERNANCE, registryService.getRegistry());

            return registryService;
        } catch (TestCreationException e) {
            log.error("Could not load registry data", e);
            throw new RegistryException("Could not load registry data", e);
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
        MicroserviceServer microserviceServer = microserviceServerMap.get(iMethodInstance.getInstance());
        if (microserviceServer != null) {
            microserviceServer.stop();
            microserviceServer.destroy();
            microserviceServerMap.remove(iMethodInstance.getInstance());
        }
    }

    private void processFields(Field[] fields, Object realInstance) {

        for (Field field : fields) {
            if (annotationPresent(field, WithRealmService.class)) {
                field.setAccessible(true);
                Annotation annotation = field.getAnnotation(WithRealmService.class);
                WithRealmService withRealmService = (WithRealmService) annotation;
                try {
                    RealmService realmService = createRealmService(withRealmService, true);
                    field.set(realInstance, realmService);
                    IdentityTenantUtil.setRealmService(realmService);
                    CarbonCoreDataHolder.getInstance().setRealmService(realmService);
                } catch (IllegalAccessException e) {
                    log.error("Error in setting field value: " + field.getName() + ", Class: " + field
                            .getDeclaringClass(), e);
                } catch (UserStoreException e) {
                    log.error("Error in setting user store value: " + field.getName() + ", Class: " + field
                            .getDeclaringClass(), e);
                }

            }
            if (annotationPresent(field, InjectMicroservicePort.class)) {
                MicroserviceServer microserviceServer = microserviceServerMap.get(realInstance);
                if (microserviceServer != null) {
                    field.setAccessible(true);
                    try {
                        field.set(realInstance, microserviceServer.getPort());
                    } catch (IllegalAccessException e) {
                        log.error("Error in setting micro-service port: " + field.getName() + ", Class: " + field
                                .getDeclaringClass(), e);
                    }
                }
            }
        }
    }

    /**
     * Scans for the real class under test and loads all the microservices on that class into microservice server.
     *
     * @param realClass
     * @param instance
     */
    private void scanAndLoadClasses(MicroserviceServer microserviceServer, Class realClass, Object instance) {

        if (microserviceServer != null) {
            microserviceServer.addService(instance);
            microserviceServer.start();
        }

    }

    /**
     * Initializes the micro-service server.
     * Detects an available port from the system and use that for the microservice server.
     */
    private MicroserviceServer initMicroserviceServer(Object realInstance) throws TestCreationException {

        try {

            ServerSocket s = new ServerSocket(0);
            int port = s.getLocalPort();
            s.close();

            MicroserviceServer microserviceServer = new MicroserviceServer(port);
            microserviceServer.init();
            microserviceServerMap.put(realInstance, microserviceServer);
            return microserviceServer;

        } catch (IOException e) {
            throw new TestCreationException("Could not get an aviailable port for micro-service", e);
        }
    }
}
