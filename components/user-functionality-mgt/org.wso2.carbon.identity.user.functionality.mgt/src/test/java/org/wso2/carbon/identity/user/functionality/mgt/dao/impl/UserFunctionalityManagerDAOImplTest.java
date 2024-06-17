/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.user.functionality.mgt.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.user.functionality.mgt.dao.UserFunctionalityManagerDAO;
import org.wso2.carbon.identity.user.functionality.mgt.exception.UserFunctionalityManagementServerException;
import org.wso2.carbon.identity.user.functionality.mgt.model.FunctionalityLockStatus;
import org.wso2.carbon.identity.user.functionality.mgt.util.TestUtils;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class UserFunctionalityManagerDAOImplTest {

    private static final Log log = LogFactory.getLog(UserFunctionalityManagerDAOImplTest.class);
    private UserFunctionalityManagerDAO userFunctionalityManagerDAO = new UserFunctionalityManagerDAOImpl();

    @BeforeMethod
    public void setUp() throws Exception {

        TestUtils.initiateH2Base();
    }

    @AfterMethod
    public void tearDown() throws Exception {

        TestUtils.closeH2Base();
    }

    @DataProvider(name = "TestFunctionalityData")
    public Object[][] testFunctionalityData() {

        return new Object[][]{
                // functionalityIdentifier
                // userId
                // tenantId
                // isFunctionalityLocked
                // functionalityUnlockTime
                // functionalityLockReasonCode
                // functionalityLockReason
                {"functionalityIdentifier1", "user1", 1, false, 0, null, null},
                {"functionalityIdentifier1", "user1", 2, false, 0, null, null},
                {"functionalityIdentifier2", "user2", 2, false, 0, null, null},
                {"functionalityIdentifier3", "user3", 3, true, 300000, "E001", "Lock reason"}
        };
    }

    @Test(dataProvider = "TestFunctionalityData")
    public void testAddFunctionality(String functionalityIdentifier, String userId, int tenantId,
                                     boolean isFunctionalityLocked,
                                     int functionalityUnlockTime, String functionalityLockReasonCode,
                                     String functionalityLockReason) {

        DataSource dataSource = mock(DataSource.class);
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
            try (Connection connection = TestUtils.getConnection()) {
                Connection spyConnection = TestUtils.spyConnection(connection);
                when(dataSource.getConnection()).thenReturn(spyConnection);
                FunctionalityLockStatus functionalityLockStatus =
                        new FunctionalityLockStatus(isFunctionalityLocked, functionalityUnlockTime,
                                functionalityLockReasonCode, functionalityLockReason);
                try {
                    userFunctionalityManagerDAO
                            .addFunctionalityLock(userId, tenantId, functionalityIdentifier, functionalityLockStatus);
                } catch (UserFunctionalityManagementServerException e) {
                    log.error(String.format("Error while adding functionality: %s", functionalityIdentifier), e);
                }
                Assert.assertEquals(
                        userFunctionalityManagerDAO.getFunctionalityLockStatus(userId, tenantId,
                                        functionalityIdentifier)
                                .getLockStatus(),
                        functionalityLockStatus.getLockStatus());
                Assert.assertEquals(
                        userFunctionalityManagerDAO.getFunctionalityLockStatus(userId, tenantId,
                                        functionalityIdentifier)
                                .getUnlockTime(),
                        functionalityLockStatus.getUnlockTime());
                Assert.assertEquals(
                        userFunctionalityManagerDAO.getFunctionalityLockStatus(userId, tenantId,
                                        functionalityIdentifier)
                                .getLockReason(),
                        functionalityLockStatus.getLockReason());
                Assert.assertEquals(
                        userFunctionalityManagerDAO.getFunctionalityLockStatus(userId, tenantId,
                                        functionalityIdentifier)
                                .getLockReasonCode(), functionalityLockStatus.getLockReasonCode());
            } catch (SQLException e) {
                //Mock behaviour. Hence ignored.
            } catch (UserFunctionalityManagementServerException e) {
                assertEquals(e.getMessage(),
                        String.format("Error occurred while adding the functionality: %s, for user: %s, for tenant" +
                                        " id: %d, having the parameters, functionality lock status: %b, functionality " +
                                        "unlock time: %d, functionality lock reason code: %s, functionality lock reason: " +
                                        "%s.", functionalityIdentifier, userId, tenantId, isFunctionalityLocked,
                                functionalityUnlockTime, functionalityLockReasonCode, functionalityLockReason));
            }
        }
    }

    @Test
    public void testUniqueKeyConstraint() {

        DataSource dataSource = mock(DataSource.class);
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
            try (Connection connection = TestUtils.getConnection()) {
                Connection spyConnection = TestUtils.spyConnection(connection);
                when(dataSource.getConnection()).thenReturn(spyConnection);
                FunctionalityLockStatus functionality = new FunctionalityLockStatus(false, 0, null, null);
                FunctionalityLockStatus functionalityCopy = new FunctionalityLockStatus(false, 0, null, null);
                try {
                    userFunctionalityManagerDAO.addFunctionalityLock("user1", 1, "functionalityIdentifier1",
                            functionality);
                    userFunctionalityManagerDAO
                            .addFunctionalityLock("user1", 1, "functionalityIdentifier1", functionalityCopy);
                } catch (UserFunctionalityManagementServerException e) {
                    assertEquals(e.getMessage(),
                            String.format(
                                    "Error occurred while adding the functionality: %s, for user: %s, for tenant " +
                                            "id: %d, having the parameters, functionality lock status: %b, functionality " +
                                            "unlock time: %d, functionality lock reason code: %s, functionality lock " +
                                            "reason: %s.", "functionalityIdentifier1", "user1", 1, false, 0, null,
                                    null));
                }
            } catch (SQLException e) {
                //Mock behaviour. Hence ignored.
            }
        }
    }

    @Test(dataProvider = "TestFunctionalityData")
    public void testGetFunctionalityLockStatus(String functionalityIdentifier, String userId, int tenantId,
                                               boolean isFunctionalityLocked,
                                               int functionalityUnlockTime, String functionalityLockReasonCode,
                                               String functionalityLockReason) {

        DataSource dataSource = mock(DataSource.class);
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
            try (Connection connection = TestUtils.getConnection()) {
                Connection spyConnection = TestUtils.spyConnection(connection);
                when(dataSource.getConnection()).thenReturn(spyConnection);
                FunctionalityLockStatus
                        functionalityLockStatus =
                        new FunctionalityLockStatus(isFunctionalityLocked, functionalityUnlockTime,
                                functionalityLockReasonCode, functionalityLockReason);
                try {
                    userFunctionalityManagerDAO
                            .addFunctionalityLock(userId, tenantId, functionalityIdentifier, functionalityLockStatus);
                } catch (UserFunctionalityManagementServerException e) {
                    log.error(String.format("Error while adding functionality: %s", functionalityIdentifier), e);
                }
                Assert.assertEquals(
                        userFunctionalityManagerDAO.getFunctionalityLockStatus(userId, tenantId,
                                        functionalityIdentifier)
                                .getLockStatus(),
                        isFunctionalityLocked);
                Assert.assertEquals(
                        userFunctionalityManagerDAO.getFunctionalityLockStatus(userId, tenantId,
                                        functionalityIdentifier)
                                .getUnlockTime(),
                        functionalityUnlockTime);
                Assert.assertEquals(
                        userFunctionalityManagerDAO.getFunctionalityLockStatus(userId, tenantId,
                                        functionalityIdentifier)
                                .getLockReasonCode(),
                        functionalityLockReasonCode);
                Assert.assertEquals(
                        userFunctionalityManagerDAO.getFunctionalityLockStatus(userId, tenantId,
                                        functionalityIdentifier)
                                .getLockReason(),
                        functionalityLockReason);
            } catch (SQLException e) {
                //Mock behaviour. Hence ignored.
            } catch (UserFunctionalityManagementServerException e) {
                assertEquals(e.getMessage(),
                        String.format("Error occurred while retrieving functionality lock status from DB for " +
                                        "functionality id: %s, user Id: %s and tenant Id: %d.", functionalityIdentifier,
                                userId, tenantId));
            }
        }
    }

    @DataProvider(name = "TestUpdateFunctionalityData")
    public Object[][] testUpdateFunctionalityData() {

        FunctionalityLockStatus functionalityStatus1 = new FunctionalityLockStatus(false, 0, null, null);
        FunctionalityLockStatus newFunctionalityStatus1 =
                new FunctionalityLockStatus(true, 300000, "E001", "Lock Reason");
        FunctionalityLockStatus functionalityStatus2 = new FunctionalityLockStatus(false, 0, null, null);
        FunctionalityLockStatus newFunctionalityStatus2 = new FunctionalityLockStatus(false, 0, null, null);
        FunctionalityLockStatus functionalityStatus3 = new FunctionalityLockStatus(true, 300000, "E001", "Lock Reason");
        FunctionalityLockStatus newFunctionalityStatus3 = new FunctionalityLockStatus(false, 0, null, null);
        FunctionalityLockStatus functionalityStatus4 = new FunctionalityLockStatus(true, 300000, "E001", "Lock Reason");
        FunctionalityLockStatus newFunctionalityStatus4 = new FunctionalityLockStatus(false, 0, null, null);

        return new Object[][]{
                // functionality type
                // user id
                // current functionality lock status
                // new functionality lock status
                // tenantId
                {"functionalityIdentifier1", "user1", functionalityStatus1, newFunctionalityStatus1, 1},
                {"functionalityIdentifier2", "user2", functionalityStatus2, newFunctionalityStatus2, 1},
                {"functionalityIdentifier3", "user3", functionalityStatus3, newFunctionalityStatus3, 1},
                {"functionalityIdentifier3", "user3", functionalityStatus4, newFunctionalityStatus4, 1}
        };
    }

    @Test(dataProvider = "TestUpdateFunctionalityData")
    public void testUpdateFunctionality(String functionalityIdentifier, String userId, Object functionalityStatusObj,
                                        Object updatedFunctionalityStatusObj, int tenantId) {

        FunctionalityLockStatus functionalityLockStatus = (FunctionalityLockStatus) functionalityStatusObj;
        FunctionalityLockStatus updatedFunctionalityLockStatus =
                (FunctionalityLockStatus) updatedFunctionalityStatusObj;
        DataSource dataSource = mock(DataSource.class);
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
            try (Connection connection = TestUtils.getConnection()) {
                Connection spyConnection = TestUtils.spyConnection(connection);
                when(dataSource.getConnection()).thenReturn(spyConnection);
                try {
                    userFunctionalityManagerDAO
                            .addFunctionalityLock(userId, tenantId, functionalityIdentifier, functionalityLockStatus);
                } catch (UserFunctionalityManagementServerException e) {
                    log.error(String.format("Error while adding functionality: %s", functionalityIdentifier), e);
                }
                Assert.assertEquals(
                        userFunctionalityManagerDAO.getFunctionalityLockStatus(userId, tenantId,
                                        functionalityIdentifier)
                                .getLockStatus(),
                        functionalityLockStatus.getLockStatus());
                Assert.assertEquals(
                        userFunctionalityManagerDAO.getFunctionalityLockStatus(userId, tenantId,
                                        functionalityIdentifier)
                                .getUnlockTime(),
                        functionalityLockStatus.getUnlockTime());
                Assert.assertEquals(
                        userFunctionalityManagerDAO.getFunctionalityLockStatus(userId, tenantId,
                                        functionalityIdentifier)
                                .getLockReason(),
                        functionalityLockStatus.getLockReason());
                Assert.assertEquals(
                        userFunctionalityManagerDAO.getFunctionalityLockStatus(userId, tenantId,
                                        functionalityIdentifier)
                                .getLockReasonCode(), functionalityLockStatus.getLockReasonCode());

                userFunctionalityManagerDAO
                        .updateLockStatusForUser(userId, tenantId, functionalityIdentifier,
                                updatedFunctionalityLockStatus);
                Assert.assertEquals(
                        userFunctionalityManagerDAO.getFunctionalityLockStatus(userId, tenantId,
                                        functionalityIdentifier)
                                .getLockStatus(),
                        updatedFunctionalityLockStatus.getLockStatus());
                Assert.assertEquals(
                        userFunctionalityManagerDAO.getFunctionalityLockStatus(userId, tenantId,
                                        functionalityIdentifier)
                                .getUnlockTime(),
                        updatedFunctionalityLockStatus.getUnlockTime());
                Assert.assertEquals(
                        userFunctionalityManagerDAO.getFunctionalityLockStatus(userId, tenantId,
                                        functionalityIdentifier)
                                .getLockReason(),
                        updatedFunctionalityLockStatus.getLockReason());
                Assert.assertEquals(
                        userFunctionalityManagerDAO.getFunctionalityLockStatus(userId, tenantId,
                                        functionalityIdentifier)
                                .getLockReasonCode(), updatedFunctionalityLockStatus.getLockReasonCode());
            } catch (SQLException e) {
                //Mock behaviour. Hence ignored.
            } catch (UserFunctionalityManagementServerException e) {
                assertEquals(e.getMessage(),
                        String.format("Error occurred while updating the functionality: %s for user Id: " +
                                "%s and tenant Id: %d.", functionalityIdentifier, userId, tenantId));
            }
        }
    }

    @Test
    public void testDeleteFunctionality() {

        DataSource dataSource = mock(DataSource.class);
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
            try (Connection connection = TestUtils.getConnection()) {
                Connection spyConnection = TestUtils.spyConnection(connection);
                when(dataSource.getConnection()).thenReturn(spyConnection);
                FunctionalityLockStatus functionalityLockStatus = new FunctionalityLockStatus(false, 0, null, null);
                try {
                    userFunctionalityManagerDAO
                            .addFunctionalityLock("userId", 1, "functionalityIdentifier1", functionalityLockStatus);
                    userFunctionalityManagerDAO.deleteMappingForUser("userId", 1, "functionalityIdentifier1");
                    userFunctionalityManagerDAO.deleteMappingForUser("userId", 2, "functionalityIdentifier2");
                    assertNull(userFunctionalityManagerDAO
                            .getFunctionalityLockStatus("userId", 1, "functionalityIdentifier1"));
                } catch (UserFunctionalityManagementServerException e) {
                    log.error("FunctionalityManagementServer Exception", e);
                }
            } catch (SQLException e) {
                //Mock behaviour. Hence ignored.
            }
        }
    }

    @Test
    public void testDeleteAllMappingsForTenant() {

        DataSource dataSource = mock(DataSource.class);
        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
            String[] functionalityIdentifiers = {"functionality1", "functionality2", "functionality3"};
            try (Connection connection = TestUtils.getConnection()) {
                Connection spyConnection = TestUtils.spyConnection(connection);
                when(dataSource.getConnection()).thenReturn(spyConnection);
                FunctionalityLockStatus functionalityLockStatus = new FunctionalityLockStatus(false, 0, null, null);
                try {
                    for (String functionalityIdentifier : functionalityIdentifiers) {
                        userFunctionalityManagerDAO
                                .addFunctionalityLock("user", 1, functionalityIdentifier, functionalityLockStatus);
                    }
                    userFunctionalityManagerDAO.deleteAllMappingsForTenant(1);

                    for (String functionalityIdentifier : functionalityIdentifiers) {
                        assertNull(
                                userFunctionalityManagerDAO.getFunctionalityLockStatus("user", 1,
                                        functionalityIdentifier));
                    }

                } catch (UserFunctionalityManagementServerException e) {
                    log.error("FunctionalityManagementServer Exception", e);
                }
            } catch (SQLException e) {
                //Mock behaviour. Hence ignored.
            }
        }
    }
}
