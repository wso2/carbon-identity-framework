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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.entitlement.PAPStatusDataHandler;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;
import org.wso2.carbon.identity.entitlement.dto.PublisherPropertyDTO;
import org.wso2.carbon.identity.entitlement.dto.StatusHolder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementConfigHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.identity.entitlement.common.EntitlementConstants.PROP_USE_LAST_STATUS_ONLY;
import static org.wso2.carbon.identity.entitlement.common.EntitlementConstants.PolicyPublish.ACTION_CREATE;
import static org.wso2.carbon.identity.entitlement.common.EntitlementConstants.PolicyPublish.ACTION_DELETE;
import static org.wso2.carbon.identity.entitlement.common.EntitlementConstants.PolicyPublish.ACTION_UPDATE;
import static org.wso2.carbon.identity.entitlement.common.EntitlementConstants.StatusTypes.ADD_POLICY;
import static org.wso2.carbon.identity.entitlement.common.EntitlementConstants.StatusTypes.DELETE_POLICY;
import static org.wso2.carbon.identity.entitlement.common.EntitlementConstants.StatusTypes.GET_POLICY;
import static org.wso2.carbon.identity.entitlement.common.EntitlementConstants.StatusTypes.PUBLISH_POLICY;
import static org.wso2.carbon.identity.entitlement.persistence.SubscriberPersistenceManagerTest.SUBSCRIBER_MODULE_NAME;

/**
 * This is the parent test class for the PAP Status Data Handler test classes.
 */
public abstract class PAPStatusDataHandlerTest {

    static final String SUBSCRIBER_ID_KEY = "subscriberId";
    static final String SUBSCRIBER_ID_DISPLAY_NAME = "Subscriber Id";

    protected static final String ABOUT_POLICY = "POLICY";
    protected static final String ABOUT_SUBSCRIBER = "SUBSCRIBER";
    protected static final String POLICY_KEY = "simple_policy";
    protected static final String SUBSCRIBER_KEY = "PDP_Subscriber_test";
    protected static final String PAP_POLICY_STORE_TARGET = "PAP POLICY STORE";
    protected static final String TARGET_ACTION_PERSIST = "PERSIST";
    protected static final String TARGET_ACTION_REMOVE = "REMOVE";
    protected static final String TARGET_ACTION_LOAD = "LOAD";
    protected static final String POLICY_VERSION_1 = "1";
    protected static final String SAMPLE_USER = "admin";

    StatusHolder statusHolderForAddPolicy;
    StatusHolder statusHolderForGetPolicy;
    StatusHolder statusHolderForRemovePolicy;

    StatusHolder statusHolderForAddPolicyInPDP;
    StatusHolder statusHolderForUpdatePolicyInPDP;
    StatusHolder statusHolderForRemovePolicyInPDP;

    List<StatusHolder> statusHolderListForAddPolicy;
    List<StatusHolder> statusHolderListForGetPolicy;
    List<StatusHolder> statusHolderListForRemovePolicy;

    List<StatusHolder> statusHolderListForAddPolicyInPDP;
    List<StatusHolder> statusHolderListForUpdatePolicyInPDP;
    List<StatusHolder> statusHolderListForRemovePolicyInPDP;
    PublisherDataHolder publisherDataHolder;

    PAPStatusDataHandler papStatusDataHandler;
    SubscriberPersistenceManager subscriberPersistenceManager;

    @BeforeClass
    public void init() {

        setupPolicyStatusData();
        setupSubscriberStatusData();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        Properties engineProperties = new Properties();
        engineProperties.put(PDPConstants.MAX_NO_OF_STATUS_RECORDS, "5");
        EntitlementConfigHolder.getInstance().setEngineProperties(engineProperties);

        Properties storeProps = new Properties();
        storeProps.put(PDPConstants.MAX_NO_OF_STATUS_RECORDS, "5");
        papStatusDataHandler = createPAPStatusDataHandler();
        papStatusDataHandler.init(storeProps);

        subscriberPersistenceManager = createSubscriberPersistenceManager();
        setSampleSubscriber();
    }

    @AfterMethod
    public void tearDown() throws Exception {

        papStatusDataHandler.handle(ABOUT_POLICY, POLICY_KEY, statusHolderListForRemovePolicy);
        papStatusDataHandler.handle(ABOUT_SUBSCRIBER, SUBSCRIBER_KEY, statusHolderListForRemovePolicyInPDP);
        subscriberPersistenceManager.removeSubscriber(SUBSCRIBER_KEY);
    }

    @DataProvider
    public Object[][] papStatusDataProvider() {

        return new Object[][]{
                {ABOUT_POLICY, POLICY_KEY, statusHolderListForAddPolicy, statusHolderListForGetPolicy},
                {ABOUT_SUBSCRIBER, SUBSCRIBER_KEY, statusHolderListForAddPolicyInPDP,
                        statusHolderListForUpdatePolicyInPDP}
        };
    }

    @DataProvider
    public Object[][] dataProviderForRemoveStatus() {

        return new Object[][]{
                {ABOUT_POLICY, POLICY_KEY, statusHolderListForAddPolicy, statusHolderListForRemovePolicy},
                {ABOUT_SUBSCRIBER, SUBSCRIBER_KEY, statusHolderListForAddPolicyInPDP,
                        statusHolderListForRemovePolicyInPDP}
        };
    }

    @Test(priority = 1, dataProvider = "papStatusDataProvider")
    public void testHandlePolicyStatus(String about, String key, List<StatusHolder> statusHoldersForAdd,
                                       List<StatusHolder> statusHoldersForModify) throws Exception {

        papStatusDataHandler.handle(about, key, statusHoldersForAdd);

        StatusHolder[] statusDataFromStorage =
                papStatusDataHandler.getStatusData(about, key, null, "*");
        assertEquals(statusDataFromStorage.length, 1);
        assertEquals(statusDataFromStorage[0].getType(), statusHoldersForAdd.get(0).getType());
        assertEquals(statusDataFromStorage[0].getKey(), statusHoldersForAdd.get(0).getKey());
        assertEquals(statusDataFromStorage[0].getTarget(), statusHoldersForAdd.get(0).getTarget());
        assertEquals(statusDataFromStorage[0].getTargetAction(), statusHoldersForAdd.get(0).getTargetAction());
        assertEquals(statusDataFromStorage[0].getUser(), statusHoldersForAdd.get(0).getUser());

        papStatusDataHandler.handle(about, key, statusHoldersForModify);

        StatusHolder[] allStatusDataFromStorage =
                papStatusDataHandler.getStatusData(about, key, null, "*");
        assertEquals(allStatusDataFromStorage.length, 2);
    }

    @Test(priority = 2, dataProvider = "papStatusDataProvider")
    public void testHandlePolicyWhenOnlyLastStatusUsed(String about, String key, List<StatusHolder> statusHoldersForAdd,
                                                       List<StatusHolder> statusHoldersForModify) throws Exception {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(() -> IdentityUtil.getProperty(PROP_USE_LAST_STATUS_ONLY)).thenReturn("true");

            papStatusDataHandler.handle(about, key, statusHoldersForAdd);
            StatusHolder[] statusDataFromStorage =
                    papStatusDataHandler.getStatusData(about, key, null, "*");
            assertEquals(statusDataFromStorage.length, 1);

            papStatusDataHandler.handle(about, key, statusHoldersForModify);
            statusDataFromStorage = papStatusDataHandler.getStatusData(about, key, null, "*");
            assertEquals(statusDataFromStorage.length, 1);

            assertEquals(statusDataFromStorage[0].getType(), statusHoldersForModify.get(0).getType());
            assertEquals(statusDataFromStorage[0].getKey(), statusHoldersForModify.get(0).getKey());
            assertEquals(statusDataFromStorage[0].getTarget(), statusHoldersForModify.get(0).getTarget());
            assertEquals(statusDataFromStorage[0].getTargetAction(), statusHoldersForModify.get(0).getTargetAction());
            assertEquals(statusDataFromStorage[0].getUser(), statusHoldersForModify.get(0).getUser());
        }
    }

    @Test(priority = 3, dataProvider = "dataProviderForRemoveStatus")
    public void testHandleRemovePolicyStatus(String about, String key, List<StatusHolder> statusHoldersForAdd,
                                             List<StatusHolder> statusHoldersForRemove) throws Exception {

        papStatusDataHandler.handle(about, key, statusHoldersForAdd);
        papStatusDataHandler.handle(about, key, statusHoldersForRemove);

        StatusHolder[] statusDataFromStorage = papStatusDataHandler.getStatusData(about, key, null, "*");
        assertEquals(statusDataFromStorage.length, 0);
    }

    @Test(priority = 4, dataProvider = "papStatusDataProvider")
    public void testHandlePolicyWhenMaxNoOfRecordsExceeds(String about, String key,
                                                          List<StatusHolder> statusHoldersForAdd,
                                                          List<StatusHolder> statusHoldersForModify) throws Exception {

        papStatusDataHandler.handle(about, key, statusHoldersForAdd);
        papStatusDataHandler.handle(about, key, statusHoldersForModify);
        papStatusDataHandler.handle(about, key, statusHoldersForModify);
        papStatusDataHandler.handle(about, key, statusHoldersForModify);
        papStatusDataHandler.handle(about, key, statusHoldersForModify);

        StatusHolder[] statusDataFromStorage = papStatusDataHandler.getStatusData(about, key, null, "*");
        assertEquals(statusDataFromStorage.length, 5);

        papStatusDataHandler.handle(about, key, statusHoldersForModify);
        StatusHolder[] statusDataAfterMaxNoOfRecords =
                papStatusDataHandler.getStatusData(about, key, null, "*");
        assertEquals(statusDataAfterMaxNoOfRecords.length, 5);
    }

    private void setupPolicyStatusData() {

        statusHolderForAddPolicy = new StatusHolder(ADD_POLICY, POLICY_KEY, POLICY_VERSION_1, PAP_POLICY_STORE_TARGET,
                TARGET_ACTION_PERSIST, true, null);
        statusHolderForAddPolicy.setUser(SAMPLE_USER);
        statusHolderListForAddPolicy = new ArrayList<>();
        statusHolderListForAddPolicy.add(statusHolderForAddPolicy);

        statusHolderForGetPolicy = new StatusHolder(GET_POLICY, POLICY_KEY, POLICY_VERSION_1, PAP_POLICY_STORE_TARGET,
                TARGET_ACTION_LOAD, true, null);
        statusHolderForGetPolicy.setUser(SAMPLE_USER);
        statusHolderListForGetPolicy = new ArrayList<>();
        statusHolderListForGetPolicy.add(statusHolderForGetPolicy);

        statusHolderForRemovePolicy =
                new StatusHolder(DELETE_POLICY, POLICY_KEY, POLICY_VERSION_1, PAP_POLICY_STORE_TARGET,
                        TARGET_ACTION_REMOVE, true, null);
        statusHolderForRemovePolicy.setUser(SAMPLE_USER);
        statusHolderListForRemovePolicy = new ArrayList<>();
        statusHolderListForRemovePolicy.add(statusHolderForRemovePolicy);
    }

    private void setupSubscriberStatusData() {

        statusHolderForAddPolicyInPDP = new StatusHolder(PUBLISH_POLICY, SUBSCRIBER_KEY, POLICY_VERSION_1, POLICY_KEY,
                ACTION_CREATE, true, null);
        statusHolderForAddPolicyInPDP.setUser(SAMPLE_USER);
        statusHolderListForAddPolicyInPDP = new ArrayList<>();
        statusHolderListForAddPolicyInPDP.add(statusHolderForAddPolicyInPDP);

        statusHolderForUpdatePolicyInPDP =
                new StatusHolder(PUBLISH_POLICY, SUBSCRIBER_KEY, POLICY_VERSION_1, POLICY_KEY,
                        ACTION_UPDATE, true, null);
        statusHolderForUpdatePolicyInPDP.setUser(SAMPLE_USER);
        statusHolderListForUpdatePolicyInPDP = new ArrayList<>();
        statusHolderListForUpdatePolicyInPDP.add(statusHolderForUpdatePolicyInPDP);

        statusHolderForRemovePolicyInPDP = new StatusHolder(DELETE_POLICY, SUBSCRIBER_KEY, POLICY_VERSION_1, POLICY_KEY,
                ACTION_DELETE, true, null);
        statusHolderForRemovePolicyInPDP.setUser(SAMPLE_USER);
        statusHolderListForRemovePolicyInPDP = new ArrayList<>();
        statusHolderListForRemovePolicyInPDP.add(statusHolderForRemovePolicyInPDP);
    }

    private void setSampleSubscriber() throws Exception {

        // Create a sample subscriber.
        PublisherPropertyDTO idProperty = new PublisherPropertyDTO();
        idProperty.setId(SUBSCRIBER_ID_KEY);
        idProperty.setValue(SUBSCRIBER_KEY);
        idProperty.setDisplayName(SUBSCRIBER_ID_DISPLAY_NAME);
        idProperty.setSecret(false);
        publisherDataHolder = new PublisherDataHolder();
        publisherDataHolder.setModuleName(SUBSCRIBER_MODULE_NAME);
        publisherDataHolder.setPropertyDTOs(new PublisherPropertyDTO[]{idProperty});
        subscriberPersistenceManager.addSubscriber(publisherDataHolder);
    }

    /**
     * Abstract method to create the PAP Status Data Handler.
     *
     * @return The PAP Status Data Handler.
     */
    protected abstract PAPStatusDataHandler createPAPStatusDataHandler();

    /**
     * Abstract method to create the subscriber persistence manager
     *
     * @return The subscriber persistence manager.
     */
    protected abstract SubscriberPersistenceManager createSubscriberPersistenceManager();
}
