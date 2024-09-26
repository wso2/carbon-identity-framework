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

import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.common.testng.WithRegistry;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.PAPStatusDataHandler;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.SimplePAPStatusDataHandler;
import org.wso2.carbon.identity.entitlement.dto.StatusHolder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementConfigHolder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;

import java.util.List;
import java.util.Properties;

import static org.testng.Assert.assertEquals;

/**
 * This class tests the behavior of the Hybrid  PAP Status Data Handler class.
 */
@WithCarbonHome
@WithRegistry(injectToSingletons = {EntitlementServiceComponent.class})
@WithRealmService(injectToSingletons = {EntitlementConfigHolder.class}, initUserStoreManager = true)
@WithH2Database(files = {"dbscripts/h2.sql"})
public class HybridPAPStatusDataHandlerTest extends PAPStatusDataHandlerTest {

    JDBCSimplePAPStatusDataHandler jdbcSimplePAPStatusDataHandler;
    SimplePAPStatusDataHandler registrySimplePAPStatusDataHandler;

    public PAPStatusDataHandler createPAPStatusDataHandler() {

        Properties storeProps = new Properties();
        storeProps.put(PDPConstants.MAX_NO_OF_STATUS_RECORDS, "5");
        jdbcSimplePAPStatusDataHandler = new JDBCSimplePAPStatusDataHandler();
        jdbcSimplePAPStatusDataHandler.init(storeProps);
        registrySimplePAPStatusDataHandler = new SimplePAPStatusDataHandler();
        registrySimplePAPStatusDataHandler.init(storeProps);
        return new HybridPAPStatusDataHandler();
    }

    public SubscriberPersistenceManager createSubscriberPersistenceManager() {

        return new HybridSubscriberPersistenceManager();
    }

    @Test(priority = 5, dataProvider = "papStatusDataProvider")
    public void testHandleStatusForNewPolicy(String about, String key, List<StatusHolder> statusHoldersForAdd,
                                             List<StatusHolder> statusHoldersForModify) throws Exception {

        papStatusDataHandler.handle(about, key, statusHoldersForAdd);

        StatusHolder[] statusDataFromDb = jdbcSimplePAPStatusDataHandler.getStatusData(about, key, null, "*");
        assertEquals(statusDataFromDb.length, 1);
        assertEquals(statusDataFromDb[0].getType(), statusHoldersForAdd.get(0).getType());
        assertEquals(statusDataFromDb[0].getKey(), statusHoldersForAdd.get(0).getKey());
        assertEquals(statusDataFromDb[0].getTarget(), statusHoldersForAdd.get(0).getTarget());
        assertEquals(statusDataFromDb[0].getTargetAction(), statusHoldersForAdd.get(0).getTargetAction());
        assertEquals(statusDataFromDb[0].getUser(), statusHoldersForAdd.get(0).getUser());

        StatusHolder[] statusDataFromRegistry = registrySimplePAPStatusDataHandler.getStatusData(about, key, null, "*");
        assertEquals(statusDataFromRegistry.length, 0);

        papStatusDataHandler.handle(about, key, statusHoldersForModify);
        StatusHolder[] allStatusDataFromDb =
                jdbcSimplePAPStatusDataHandler.getStatusData(about, key, null, "*");
        assertEquals(allStatusDataFromDb.length, 2);
        StatusHolder[] allStatusDataFromRegistry =
                registrySimplePAPStatusDataHandler.getStatusData(about, key, null, "*");
        assertEquals(allStatusDataFromRegistry.length, 0);
    }

    @Test(priority = 6, dataProvider = "papStatusDataProvider")
    public void testHandleStatusWhenStatusExistsInDb(String about, String key, List<StatusHolder> statusHoldersForAdd,
                                                     List<StatusHolder> statusHoldersForModify) throws Exception {

        jdbcSimplePAPStatusDataHandler.handle(about, key, statusHoldersForAdd);
        papStatusDataHandler.handle(about, key, statusHoldersForModify);

        StatusHolder[] allStatusDataFromDb =
                jdbcSimplePAPStatusDataHandler.getStatusData(about, key, null, "*");
        assertEquals(allStatusDataFromDb.length, 2);
        StatusHolder[] allStatusDataFromRegistry =
                registrySimplePAPStatusDataHandler.getStatusData(about, key, null, "*");
        assertEquals(allStatusDataFromRegistry.length, 0);
    }

    @Test(priority = 7, dataProvider = "papStatusDataProvider")
    public void testHandleStatusWhenStatusExistsInRegistry(String about, String key,
                                                           List<StatusHolder> statusHoldersForAdd,
                                                           List<StatusHolder> statusHoldersForModify) throws Exception {

        registrySimplePAPStatusDataHandler.handle(about, key, statusHoldersForAdd);
        papStatusDataHandler.handle(about, key, statusHoldersForModify);

        StatusHolder[] allStatusDataFromRegistry =
                registrySimplePAPStatusDataHandler.getStatusData(about, key, null, "*");
        assertEquals(allStatusDataFromRegistry.length, 2);
        StatusHolder[] allStatusDataFromDb =
                jdbcSimplePAPStatusDataHandler.getStatusData(about, key, null, "*");
        assertEquals(allStatusDataFromDb.length, 0);
    }

    @Test(priority = 8, dataProvider = "papStatusDataProvider")
    public void testGetStatusWhenPolicyStatusExistsInDb(String about, String key,
                                                        List<StatusHolder> statusHoldersForAdd,
                                                        List<StatusHolder> statusHoldersForModify) throws Exception {

        jdbcSimplePAPStatusDataHandler.handle(about, key, statusHoldersForAdd);
        verifyStatusDataFromStorage(about, key, statusHoldersForAdd);
    }

    @Test(priority = 9, dataProvider = "papStatusDataProvider")
    public void testGetStatusWhenPolicyStatusExistsInRegistry(String about, String key,
                                                              List<StatusHolder> statusHoldersForAdd,
                                                              List<StatusHolder> statusHoldersForModify)
            throws Exception {

        registrySimplePAPStatusDataHandler.handle(about, key, statusHoldersForAdd);
        verifyStatusDataFromStorage(about, key, statusHoldersForAdd);
    }

    private void verifyStatusDataFromStorage(String about, String key, List<StatusHolder> statusHoldersForAdd)
            throws EntitlementException {

        StatusHolder[] statusDataFromStorage = papStatusDataHandler.getStatusData(about, key, null, "*");
        assertEquals(statusDataFromStorage.length, 1);
        assertEquals(statusDataFromStorage[0].getType(), statusHoldersForAdd.get(0).getType());
        assertEquals(statusDataFromStorage[0].getKey(), statusHoldersForAdd.get(0).getKey());
        assertEquals(statusDataFromStorage[0].getTarget(), statusHoldersForAdd.get(0).getTarget());
        assertEquals(statusDataFromStorage[0].getTargetAction(), statusHoldersForAdd.get(0).getTargetAction());
        assertEquals(statusDataFromStorage[0].getUser(), statusHoldersForAdd.get(0).getUser());
    }

    @Test(priority = 10, dataProvider = "dataProviderForRemoveStatus")
    public void testHandleRemoveStatusWhenPolicyStatusExistsInDb(String about, String key,
                                                                 List<StatusHolder> statusHoldersForAdd,
                                                                 List<StatusHolder> statusHoldersForRemove)
            throws Exception {

        jdbcSimplePAPStatusDataHandler.handle(about, key, statusHoldersForAdd);
        papStatusDataHandler.handle(about, key, statusHoldersForRemove);
        verifyRemoveStatusDataFromStorage(about, key);
    }

    @Test(priority = 11, dataProvider = "dataProviderForRemoveStatus")
    public void testHandleRemoveStatusWhenPolicyStatusExistsInRegistry(String about, String key,
                                                                       List<StatusHolder> statusHoldersForAdd,
                                                                       List<StatusHolder> statusHoldersForRemove)
            throws Exception {

        registrySimplePAPStatusDataHandler.handle(about, key, statusHoldersForAdd);
        papStatusDataHandler.handle(about, key, statusHoldersForRemove);
        verifyRemoveStatusDataFromStorage(about, key);
    }

    private void verifyRemoveStatusDataFromStorage(String about, String key) throws EntitlementException {

        StatusHolder[] statusDataFromDb = papStatusDataHandler.getStatusData(about, key, null, "*");
        assertEquals(statusDataFromDb.length, 0);
        StatusHolder[] statusDataFromRegistry = papStatusDataHandler.getStatusData(about, key, null, "*");
        assertEquals(statusDataFromRegistry.length, 0);
    }
}
