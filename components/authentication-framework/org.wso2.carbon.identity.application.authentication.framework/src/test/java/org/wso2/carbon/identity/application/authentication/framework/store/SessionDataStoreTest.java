/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.store;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;

import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Test class that includes unit tests of Session Data Store.
 */
@WithCarbonHome
public class SessionDataStoreTest extends DataStoreBaseTest {

    private static final String DB_NAME = "SESSION_DATA_STORE";

    @Mock
    FrameworkServiceDataHolder mockFrameworkServiceDataHolder;

    @BeforeClass
    public void setUp() throws Exception {

        initMocks(this);
        initH2DB(DB_NAME, getDatabaseScriptFilePath("session_data_store_h2.sql"));
    }

    @AfterClass
    public void tearDown() throws Exception {

        closeH2DB(DB_NAME);
    }

    @DataProvider
    public Object[][] getSessionData() {
        Object obj = mock(Object.class, withSettings().serializable());
        return new Object[][]{
                {"00000001", "sessionType", obj, 30000, 1},
        };
    }

    @Test(dataProvider = "getSessionData")
    public void testPersistSessionData(String key, String type, Object entry, long nanoTime, int tenantId) throws
            Exception {

        try (MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<IdPManagementUtil> idPManagementUtil = mockStatic(IdPManagementUtil.class);
             MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolder =
                     mockStatic(FrameworkServiceDataHolder.class);
             MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            Connection connection = getConnection(DB_NAME);
            mockIdentityDataBaseUtilConnection(connection, true, identityDatabaseUtil);
            mockCarbonContext(carbonContext);
            mockIdentityUtils(identityTenantUtil, idPManagementUtil, identityUtil);
            mockDataHolder(frameworkServiceDataHolder);
            SessionDataStore.getInstance().persistSessionData(key, type, entry, nanoTime, tenantId);
        }
    }

    @Test(dependsOnMethods = "testPersistSessionData")
    public void testRemoveExpiredSessionData() throws Exception {

        try (MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<IdPManagementUtil> idPManagementUtil = mockStatic(IdPManagementUtil.class);
             MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            Connection connection = getConnection(DB_NAME);
            mockIdentityDataBaseUtilConnection(connection, true, identityDatabaseUtil);
            mockCarbonContext(carbonContext);
            mockIdentityUtils(identityTenantUtil, idPManagementUtil, identityUtil);
            SessionDataStore.getInstance().removeExpiredSessionData();
        }
    }

    private void mockCarbonContext(MockedStatic<CarbonContext> carbonContext) {

        CarbonContext mockCarbonContext = mock(CarbonContext.class);
        carbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
        when(mockCarbonContext.getTenantDomain()).thenReturn("abc.com");
    }

    private void mockIdentityUtils(MockedStatic<IdentityTenantUtil> identityTenantUtil,
                                   MockedStatic<IdPManagementUtil> idPManagementUtil,
                                   MockedStatic<IdentityUtil> identityUtil) {

        idPManagementUtil.when(() -> IdPManagementUtil.getRememberMeTimeout(any(String.class))).thenReturn(11111111);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn("abc.com");
        identityUtil.when(() -> IdentityUtil.getCleanUpPeriod(any(String.class))).thenReturn(new Long(11111111));
    }

    private void mockDataHolder(MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolder) {

        frameworkServiceDataHolder.when(
                FrameworkServiceDataHolder::getInstance).thenReturn(mockFrameworkServiceDataHolder);
        when(mockFrameworkServiceDataHolder.getSessionSerializer()).thenReturn(new JavaSessionSerializer());
    }

    private void mockIdentityDataBaseUtilConnection(Connection connection, Boolean shouldApplyTransaction,
                                                    MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil)
            throws SQLException {

        Connection connection1 = spy(connection);
        doNothing().when(connection1).close();

        if (shouldApplyTransaction) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(connection1);
        } else {
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(shouldApplyTransaction))
                    .thenReturn(connection1);
        }
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getSessionDBConnection(shouldApplyTransaction))
                .thenReturn(connection1);
    }
}
