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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test class that includes unit tests of Session Data Store.
 */
@WithCarbonHome
@PrepareForTest({IdentityDatabaseUtil.class, IdPManagementUtil.class, IdentityUtil.class, IdentityTenantUtil.class,
        CarbonContext.class, CarbonContext.class, PrivilegedCarbonContext.class,
        ServerConfiguration.class, SessionCleanUpService.class, FrameworkServiceDataHolder.class})
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

        Connection connection = getConnection(DB_NAME);
        mockIdentityDataBaseUtilConnection(connection, true);
        mockCarbonContext();
        mockIdentityUtils();
        mockDataHolder();
        SessionDataStore.getInstance().persistSessionData(key, type, entry, nanoTime, tenantId);
    }

    @Test(dependsOnMethods = "testPersistSessionData")
    public void testRemoveExpiredSessionData() throws Exception {

        Connection connection = getConnection(DB_NAME);
        mockIdentityDataBaseUtilConnection(connection, true);
        mockCarbonContext();
        mockIdentityUtils();
        SessionDataStore.getInstance().removeExpiredSessionData();
    }

    private void mockCarbonContext() {

        mockStatic(CarbonContext.class);
        CarbonContext carbonContext = mock(CarbonContext.class);
        when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        when(CarbonContext.getThreadLocalCarbonContext().getTenantDomain()).thenReturn("abc.com");
    }

    private void mockIdentityUtils() {

        mockStatic(IdentityTenantUtil.class);
        mockStatic(IdPManagementUtil.class);
        when(IdPManagementUtil.getRememberMeTimeout(any(String.class))).thenReturn(11111111);
        when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn("abc.com");
        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getCleanUpPeriod(any(String.class))).thenReturn(new Long(11111111));
    }

    private void mockDataHolder() {

        mockStatic(FrameworkServiceDataHolder.class);
        when(FrameworkServiceDataHolder.getInstance()).thenReturn(mockFrameworkServiceDataHolder);
        when(mockFrameworkServiceDataHolder.getSessionSerializer()).thenReturn(new JavaSessionSerializer());
    }

    private void mockIdentityDataBaseUtilConnection(Connection connection, Boolean shouldApplyTransaction) throws
            SQLException {

        Connection connection1 = spy(connection);
        doNothing().when(connection1).close();
        mockStatic(IdentityDatabaseUtil.class);
        if (shouldApplyTransaction) {
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection1);
        } else {
            when(IdentityDatabaseUtil.getDBConnection(shouldApplyTransaction)).thenReturn(connection1);
        }
        when(IdentityDatabaseUtil.getSessionDBConnection(shouldApplyTransaction)).thenReturn(connection1);
    }
}
