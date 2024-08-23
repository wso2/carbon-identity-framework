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

package org.wso2.carbon.identity.entitlement.persistence;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.identity.entitlement.PAPStatusDataHandler;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.SimplePAPStatusDataHandler;
import org.wso2.carbon.identity.entitlement.internal.EntitlementConfigHolder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;

import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Properties;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertTrue;

/**
 * This class tests the behavior of the PersistenceManagerFactory class.
 */
public class PersistenceManagerFactoryTest {

    MockedStatic<EntitlementServiceComponent> entitlementServiceComponent;

    @BeforeMethod
    public void setUp() {

        initMocks(this);
        setUpCarbonHome();

        Properties engineProperties = new Properties();
        engineProperties.put(PDPConstants.MAX_NO_OF_POLICY_VERSIONS, "0");

        EntitlementConfigHolder mockEntitlementConfigHolder = mock(EntitlementConfigHolder.class);
        when(mockEntitlementConfigHolder.getEngineProperties()).thenReturn(engineProperties);

        entitlementServiceComponent = mockStatic(EntitlementServiceComponent.class);
        entitlementServiceComponent.when(EntitlementServiceComponent::getEntitlementConfig).
                thenReturn(mockEntitlementConfigHolder);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        entitlementServiceComponent.close();
    }

    @Test
    public void shouldReturnDBBasedPersistenceManagerWhenConfigIsDatabase() throws Exception {

        setPrivateStaticField(PersistenceManagerFactory.class, "POLICY_STORAGE_TYPE", "database");

        // Validate Policy Persistence Manager.
        PolicyPersistenceManager policyPersistenceManager = PersistenceManagerFactory.getPolicyPersistenceManager();
        assertTrue(policyPersistenceManager instanceof JDBCPolicyPersistenceManager);

        // Validate Config Persistence Manager.
        ConfigPersistenceManager configPersistenceManager = PersistenceManagerFactory.getConfigPersistenceManager();
        assertTrue(configPersistenceManager instanceof JDBCConfigPersistenceManager);

        // Validate Subscriber Persistence Manager.
        SubscriberPersistenceManager subscriberPersistenceManager =
                PersistenceManagerFactory.getSubscriberPersistenceManager();
        assertTrue(subscriberPersistenceManager instanceof JDBCSubscriberPersistenceManager);

        // Validate the PAP Status Data Handler.
        PAPStatusDataHandler papStatusDataHandler = PersistenceManagerFactory.getPAPStatusDataHandler();
        assertTrue(papStatusDataHandler instanceof JDBCSimplePAPStatusDataHandler);
    }

    @Test
    public void shouldReturnHybridPersistenceManagerWhenConfigIsOnMigration() throws Exception {

        setPrivateStaticField(PersistenceManagerFactory.class, "POLICY_STORAGE_TYPE", "hybrid");

        // Validate Policy Persistence Manager.
        PolicyPersistenceManager policyPersistenceManager = PersistenceManagerFactory.getPolicyPersistenceManager();
        assertTrue(policyPersistenceManager instanceof HybridPolicyPersistenceManager);

        // Validate Config Persistence Manager.
        ConfigPersistenceManager configPersistenceManager = PersistenceManagerFactory.getConfigPersistenceManager();
        assertTrue(configPersistenceManager instanceof HybridConfigPersistenceManager);

        // Validate Subscriber Persistence Manager.
        SubscriberPersistenceManager subscriberPersistenceManager =
                PersistenceManagerFactory.getSubscriberPersistenceManager();
        assertTrue(subscriberPersistenceManager instanceof HybridSubscriberPersistenceManager);

        // Validate the PAP Status Data Handler.
        PAPStatusDataHandler papStatusDataHandler = PersistenceManagerFactory.getPAPStatusDataHandler();
        assertTrue(papStatusDataHandler instanceof HybridPAPStatusDataHandler);
    }

    @Test
    public void shouldReturnRegistryBasedPersistenceManagerWhenConfigIsRegistry() throws Exception {


        setPrivateStaticField(PersistenceManagerFactory.class, "POLICY_STORAGE_TYPE", "registry");

        // Validate Policy Persistence Manager.
        PolicyPersistenceManager policyPersistenceManager = PersistenceManagerFactory.getPolicyPersistenceManager();
        assertTrue(policyPersistenceManager instanceof RegistryPolicyPersistenceManager);

        // Validate Config Persistence Manager.
        ConfigPersistenceManager configPersistenceManager = PersistenceManagerFactory.getConfigPersistenceManager();
        assertTrue(configPersistenceManager instanceof RegistryConfigPersistenceManager);

        // Validate Subscriber Persistence Manager.
        SubscriberPersistenceManager subscriberPersistenceManager =
                PersistenceManagerFactory.getSubscriberPersistenceManager();
        assertTrue(subscriberPersistenceManager instanceof RegistrySubscriberPersistenceManager);

        // Validate the PAP Status Data Handler.
        PAPStatusDataHandler papStatusDataHandler = PersistenceManagerFactory.getPAPStatusDataHandler();
        assertTrue(papStatusDataHandler instanceof SimplePAPStatusDataHandler);
    }

    @Test
    public void shouldReturnDBBasedPersistenceManagerWhenConfigIsInvalid() throws Exception {

        setPrivateStaticField(PersistenceManagerFactory.class, "POLICY_STORAGE_TYPE", "invalid");

        // Validate Policy Persistence Manager.
        PolicyPersistenceManager policyPersistenceManager = PersistenceManagerFactory.getPolicyPersistenceManager();
        assertTrue(policyPersistenceManager instanceof JDBCPolicyPersistenceManager);

        // Validate Config Persistence Manager.
        ConfigPersistenceManager configPersistenceManager = PersistenceManagerFactory.getConfigPersistenceManager();
        assertTrue(configPersistenceManager instanceof JDBCConfigPersistenceManager);

        // Validate Subscriber Persistence Manager.
        SubscriberPersistenceManager subscriberPersistenceManager =
                PersistenceManagerFactory.getSubscriberPersistenceManager();
        assertTrue(subscriberPersistenceManager instanceof JDBCSubscriberPersistenceManager);

        // Validate the PAP Status Data Handler.
        PAPStatusDataHandler papStatusDataHandler = PersistenceManagerFactory.getPAPStatusDataHandler();
        assertTrue(papStatusDataHandler instanceof JDBCSimplePAPStatusDataHandler);
    }

    @Test
    public void shouldReturnDBBasedPersistenceManagerWhenConfigIsEmpty() throws Exception {

        setPrivateStaticField(PersistenceManagerFactory.class, "POLICY_STORAGE_TYPE", "");

        // Validate Policy Persistence Manager.
        PolicyPersistenceManager policyPersistenceManager = PersistenceManagerFactory.getPolicyPersistenceManager();
        assertTrue(policyPersistenceManager instanceof JDBCPolicyPersistenceManager);

        // Validate Config Persistence Manager.
        ConfigPersistenceManager configPersistenceManager = PersistenceManagerFactory.getConfigPersistenceManager();
        assertTrue(configPersistenceManager instanceof JDBCConfigPersistenceManager);

        // Validate Subscriber Persistence Manager.
        SubscriberPersistenceManager subscriberPersistenceManager =
                PersistenceManagerFactory.getSubscriberPersistenceManager();
        assertTrue(subscriberPersistenceManager instanceof JDBCSubscriberPersistenceManager);

        // Validate the PAP Status Data Handler.
        PAPStatusDataHandler papStatusDataHandler = PersistenceManagerFactory.getPAPStatusDataHandler();
        assertTrue(papStatusDataHandler instanceof JDBCSimplePAPStatusDataHandler);
    }

    @Test
    public void shouldReturnDBBasedPersistenceManagerWhenConfigIsNull() throws Exception {

        setPrivateStaticField(PersistenceManagerFactory.class, "POLICY_STORAGE_TYPE", null);

        // Validate Policy Persistence Manager.
        PolicyPersistenceManager policyPersistenceManager = PersistenceManagerFactory.getPolicyPersistenceManager();
        assertTrue(policyPersistenceManager instanceof JDBCPolicyPersistenceManager);

        // Validate Config Persistence Manager.
        ConfigPersistenceManager configPersistenceManager = PersistenceManagerFactory.getConfigPersistenceManager();
        assertTrue(configPersistenceManager instanceof JDBCConfigPersistenceManager);

        // Validate Subscriber Persistence Manager.
        SubscriberPersistenceManager subscriberPersistenceManager =
                PersistenceManagerFactory.getSubscriberPersistenceManager();
        assertTrue(subscriberPersistenceManager instanceof JDBCSubscriberPersistenceManager);

        // Validate the PAP Status Data Handler.
        PAPStatusDataHandler papStatusDataHandler = PersistenceManagerFactory.getPAPStatusDataHandler();
        assertTrue(papStatusDataHandler instanceof JDBCSimplePAPStatusDataHandler);
    }


    private static void setUpCarbonHome() {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome,
                "repository/conf").toString());
    }

    private void setPrivateStaticField(Class<?> clazz, String fieldName, Object newValue)
            throws NoSuchFieldException, IllegalAccessException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, newValue);
    }
}
