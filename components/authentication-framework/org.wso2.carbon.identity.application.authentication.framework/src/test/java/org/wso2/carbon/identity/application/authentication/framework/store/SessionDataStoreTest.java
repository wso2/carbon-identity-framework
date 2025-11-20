/*
 * Copyright (c) 2021-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.store;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.SessionSerializerException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;

/**
 * Test class that includes unit tests of Session Data Store.
 */
@WithCarbonHome
public class SessionDataStoreTest extends DataStoreBaseTest {

    private static final String DB_NAME = "SESSION_DATA_STORE";
    private static final String OPERATION_STORE = "STORE";
    private static final String OPERATION_DELETE = "DELETE";

    @Mock
    FrameworkServiceDataHolder mockFrameworkServiceDataHolder;
    @Mock
    FrameworkServiceDataHolder mockFrameworkServiceDataHolder1;
    @Mock
    PreparedStatement mockPreparedStatement;
    @Mock
    ResultSet mockResultSet;
    @Mock
    SessionSerializer mockSessionSerializer;
    @Mock
    InputStream mockInputStream;
    @Mock
    DatabaseMetaData mockDatabaseMetaData;

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

        Object obj1 = mock(Object.class, withSettings().serializable());
        Object obj2 = mock(Object.class, withSettings().serializable());
        return new Object[][]{
                {"00000001", "sessionType", obj1, 30000, 1},
                {"00000002", "sessionType", obj2, 30001, 1},
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

    @Test(dependsOnMethods = "testRemoveSessionData")
    public void testGetSessionData() throws Exception {

        String key = "1";
        String type = "AuthCache";
        String operation = OPERATION_STORE;
        long nanoTime = 30000;
        int tenantId = 1;
        try (MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<IdPManagementUtil> idPManagementUtil = mockStatic(IdPManagementUtil.class);
             MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolder =
                     mockStatic(FrameworkServiceDataHolder.class);
             MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {

            Object obj = new Object();
            SessionContextDO sessionContextDO = new SessionContextDO(key, type, obj, nanoTime, tenantId);

            mockPreparedStatements(false, identityDatabaseUtil, frameworkServiceDataHolder,
                    sessionContextDO);
            mockCarbonContext(carbonContext);
            mockIdentityUtils(identityTenantUtil, idPManagementUtil, identityUtil);
            Object sessionData = SessionDataStore.getInstance().getSessionData(key, type, operation);
            assertEquals(sessionData, obj);
        }
    }

    @DataProvider
    public Object[][] getRemoveSessionData() {

        return new Object[][]{
                {"00000002", "sessionType"},
        };
    }

    @Test(dataProvider = "getRemoveSessionData", dependsOnMethods = "testPersistSessionData")
    public void testRemoveSessionData(String key, String type) throws Exception {

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
            SessionDataStore.getInstance().removeSessionData(key, type, 30002);
        }
    }

    @DataProvider
    public Object[][] getValidateLastOperationOnSessionData() {

        return new Object[][]{
                {"00000001", "sessionType", OPERATION_STORE, true},
                {"00000001", "sessionType", OPERATION_DELETE, false},
                {"00000002", "sessionType", OPERATION_DELETE, true},
                {"00000002", "sessionType", OPERATION_STORE, false},
        };
    }

    @Test(dependsOnMethods = "testRemoveSessionData", dataProvider = "getValidateLastOperationOnSessionData")
    public void testValidateLastOperationOnSessionData(String key, String type, String operation, boolean isExist)
            throws Exception {

        try (MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<IdPManagementUtil> idPManagementUtil = mockStatic(IdPManagementUtil.class);
             MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolder =
                     mockStatic(FrameworkServiceDataHolder.class);
             MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {

            Connection connection = getConnection(DB_NAME);

            mockIdentityDataBaseUtilConnection(connection, false, identityDatabaseUtil);
            mockCarbonContext(carbonContext);
            mockIdentityUtils(identityTenantUtil, idPManagementUtil, identityUtil);
            mockDataHolder(frameworkServiceDataHolder);
            boolean isSessionDataExist = SessionDataStore.getInstance()
                    .validateLastOperationOnSessionData(key, type, operation);
            assertEquals(isSessionDataExist, isExist);
        }
    }

    @Test(dependsOnMethods = "testValidateLastOperationOnSessionData")
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

    private void mockPreparedStatements(Boolean shouldApplyTransaction,
                                        MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil,
                                        MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolder,
                                        SessionContextDO sessionContextDO
                                       )
            throws SQLException, SessionSerializerException {

        frameworkServiceDataHolder.when(
                FrameworkServiceDataHolder::getInstance).thenReturn(mockFrameworkServiceDataHolder1);
        when(mockFrameworkServiceDataHolder1.getSessionSerializer()).thenReturn(mockSessionSerializer);
        when(mockSessionSerializer.deSerializeSessionObject(any())).thenReturn(sessionContextDO.getEntry());
        Connection connection1 = mock(Connection.class);
        doNothing().when(connection1).close();

        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getSessionDBConnection(shouldApplyTransaction))
                .thenReturn(connection1);

        when(connection1.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong(anyInt())).thenReturn(1000L);
        when(mockResultSet.getBinaryStream(anyInt())).thenReturn(mockInputStream);
        when(connection1.getMetaData()).thenReturn(mockDatabaseMetaData);
        when(mockDatabaseMetaData.getDriverName()).thenReturn("H2");
    }
}
