/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.functions.library.mgt.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.functions.library.mgt.dao.FunctionLibraryDAO;
import org.wso2.carbon.identity.functions.library.mgt.dao.util.DAOUtils;
import org.wso2.carbon.identity.functions.library.mgt.exception.FunctionLibraryManagementException;
import org.wso2.carbon.identity.functions.library.mgt.model.FunctionLibrary;
import org.wso2.carbon.identity.testutil.powermock.PowerMockIdentityBaseTest;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@PrepareForTest({IdentityDatabaseUtil.class, IdentityTenantUtil.class})
@PowerMockIgnore("org.mockito.*")
public class FunctionLibraryDAOImplTest extends PowerMockIdentityBaseTest {

    private static final String SAMPLE_TENANT_DOMAIN = "carbon.super";
    private static final String SAMPLE_TENANT_DOMAIN2 = "abc.com";
    private static final String DB_NAME = "FUNCTIONLIB_DB";
    private static final Log log = LogFactory.getLog(FunctionLibraryDAOImplTest.class);

    @BeforeClass
    public void initTest() throws Exception {

        DAOUtils.initializeDataSource(DB_NAME, DAOUtils.getFilePath("function_lib.sql"));
    }

    @BeforeMethod
    public void setUp() {

        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);

        when(IdentityTenantUtil.getTenantId(SAMPLE_TENANT_DOMAIN)).thenReturn(-1234);
        when(IdentityTenantUtil.getTenantDomain(-1234)).thenReturn(SAMPLE_TENANT_DOMAIN);

        when(IdentityTenantUtil.getTenantId(SAMPLE_TENANT_DOMAIN2)).thenReturn(-123);
        when(IdentityTenantUtil.getTenantDomain(-123)).thenReturn(SAMPLE_TENANT_DOMAIN2);
    }

    @DataProvider(name = "createFunctionLibraryDataProvider")
    public Object[][] createFunctionLibraryData() {

        FunctionLibrary functionLibrary1 = new FunctionLibrary();
        functionLibrary1.setFunctionLibraryName("sample1");
        functionLibrary1.setDescription("sample1");
        functionLibrary1.setFunctionLibraryScript("samplefunction1");

        FunctionLibrary functionLibrary2 = new FunctionLibrary();
        functionLibrary2.setFunctionLibraryName("sample2");
        functionLibrary2.setDescription("sample2");
        functionLibrary2.setFunctionLibraryScript(null);

        FunctionLibrary functionLibrary3 = new FunctionLibrary();
        functionLibrary3.setFunctionLibraryName("sample3");
        functionLibrary3.setDescription("sample3");
        functionLibrary3.setFunctionLibraryScript("samplefunction3");

        return new Object[][]{
                {
                        functionLibrary1,
                        SAMPLE_TENANT_DOMAIN
                }
                ,
                {
                        functionLibrary2,
                        SAMPLE_TENANT_DOMAIN
                },
                {
                        functionLibrary3,
                        SAMPLE_TENANT_DOMAIN2
                }
        };
    }

    @Test(dataProvider = "createFunctionLibraryDataProvider")
    public void createFunctionLibrary(Object functionLibrary, String tenantDomain) {

        try (Connection connection1 = DAOUtils.getConnection(DB_NAME);
             Connection connection2 = DAOUtils.getConnection(DB_NAME);
             Connection connection3 = DAOUtils.getConnection(DB_NAME)) {

            FunctionLibraryDAO functionLibraryDAO = new FunctionLibraryDAOImpl();
            FunctionLibrary functionLibrary1 = new FunctionLibrary();
            functionLibrary1.setFunctionLibraryName("sample1");
            functionLibrary1.setDescription("sample1");
            functionLibrary1.setFunctionLibraryScript("samplefunction1");
            try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
                when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
                functionLibraryDAO.createFunctionLibrary(functionLibrary1, tenantDomain);
            } catch (FunctionLibraryManagementException e) {
                log.error("FunctionLibraryManagementException");
            } catch (SQLException e) {
                log.error("SQLException");
            }
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection1);
            functionLibraryDAO.createFunctionLibrary((FunctionLibrary) functionLibrary, tenantDomain);

            when(IdentityDatabaseUtil.getDBConnection(false)).thenReturn(connection2);
            assertEquals(((FunctionLibrary) functionLibrary).getFunctionLibraryName(),
                    functionLibraryDAO.getFunctionLibrary(((FunctionLibrary) functionLibrary).getFunctionLibraryName(),
                            tenantDomain).getFunctionLibraryName());

            // Clean after test
            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection3);
            functionLibraryDAO.deleteFunctionLibrary(((FunctionLibrary) functionLibrary).getFunctionLibraryName(),
                    tenantDomain);
        } catch (SQLException e) {
            log.error("SQLException");
        } catch (FunctionLibraryManagementException e) {
            assertEquals(e.getMessage(),
                    "Error while creating the script library: " +
                            ((FunctionLibrary) functionLibrary).getFunctionLibraryName() + ".");
        }
    }

    @DataProvider(name = "listFunctionLibraryDataProvider")
    public Object[][] listFunctionLibrariesData() {

        FunctionLibrary functionLibrary4 = new FunctionLibrary();
        functionLibrary4.setFunctionLibraryName("sample4");
        functionLibrary4.setDescription("sample4");
        functionLibrary4.setFunctionLibraryScript("samplefunction4");

        FunctionLibrary functionLibrary5 = new FunctionLibrary();
        functionLibrary5.setFunctionLibraryName("sample5");
        functionLibrary5.setDescription("sample5");
        functionLibrary5.setFunctionLibraryScript("samplefunction5");

        FunctionLibrary functionLibrary6 = new FunctionLibrary();
        functionLibrary6.setFunctionLibraryName("sample6");
        functionLibrary6.setDescription("sample6");
        functionLibrary6.setFunctionLibraryScript("samplefunction6");

        FunctionLibrary functionLibrary7 = new FunctionLibrary();
        functionLibrary7.setFunctionLibraryName("sample7");
        functionLibrary7.setDescription("sample7");
        functionLibrary7.setFunctionLibraryScript("samplefunction7");

        return new Object[][]{
                {
                        Arrays.asList(
                                functionLibrary4,
                                functionLibrary5
                                     ),
                        SAMPLE_TENANT_DOMAIN
                },
                {
                        Arrays.asList(
                                functionLibrary6,
                                functionLibrary7
                                     ),
                        SAMPLE_TENANT_DOMAIN2
                },
        };

    }

    @Test(dataProvider = "listFunctionLibraryDataProvider")
    public void listFunctionLibraries(List<Object> functionLibraries, String tenantDomain)
            throws FunctionLibraryManagementException {

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            FunctionLibraryDAO functionLibraryDAO = new FunctionLibraryDAOImpl();

            addFunctionLibraries(functionLibraryDAO, functionLibraries, tenantDomain);

            when(IdentityDatabaseUtil.getDBConnection(false)).thenReturn(connection);
            List<FunctionLibrary> functionLibrariesList = functionLibraryDAO.listFunctionLibraries(tenantDomain);
            assertTrue(functionLibrariesList != null && functionLibrariesList.size() != 0,
                    "Failed to retrieve script libraries.");

            // Clean after test
            deleteFunctionLibraries(functionLibraryDAO, functionLibraries, tenantDomain);

        } catch (SQLException e) {
            log.error("SQLException");
        }
    }

    @DataProvider(name = "getFunctionLibraryDataProvider")
    public Object[][] getFunctionLibraryData() {

        FunctionLibrary functionLibrary8 = new FunctionLibrary();
        functionLibrary8.setFunctionLibraryName("sample8");
        functionLibrary8.setDescription("sample8");
        functionLibrary8.setFunctionLibraryScript("samplefunction8");

        FunctionLibrary functionLibrary9 = new FunctionLibrary();
        functionLibrary9.setFunctionLibraryName("sample9");
        functionLibrary9.setDescription("sample9");
        functionLibrary9.setFunctionLibraryScript("samplefunction9");

        return new Object[][]{
                {
                        functionLibrary8,
                        SAMPLE_TENANT_DOMAIN
                },
                {
                        functionLibrary9,
                        SAMPLE_TENANT_DOMAIN2
                },

        };
    }

    @Test(dataProvider = "getFunctionLibraryDataProvider")
    public void getFunctionLibrary(Object functionLibrary, String tenantDomain)
            throws SQLException, FunctionLibraryManagementException {

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            FunctionLibraryDAO functionLibraryDAO = new FunctionLibraryDAOImpl();

            addFunctionLibraries(functionLibraryDAO, Collections.singletonList(functionLibrary), tenantDomain);

            when(IdentityDatabaseUtil.getDBConnection(false)).thenReturn(connection);
            assertTrue(functionLibraryDAO.getFunctionLibrary(
                    ((FunctionLibrary) functionLibrary).getFunctionLibraryName(), tenantDomain) != null,
                    "Failed to retrieve script library");
            // Clean after test
            deleteFunctionLibraries(functionLibraryDAO, Collections.singletonList(functionLibrary), tenantDomain);
        }
    }

    @DataProvider(name = "deleteFunctionLibraryData")
    public Object[][] deleteFunctionLibrary() {

        FunctionLibrary functionLibrary10 = new FunctionLibrary();
        functionLibrary10.setFunctionLibraryName("sample10");
        functionLibrary10.setDescription("sample10");
        functionLibrary10.setFunctionLibraryScript("samplefunction10");

        FunctionLibrary functionLibrary11 = new FunctionLibrary();
        functionLibrary11.setFunctionLibraryName("sample11");
        functionLibrary11.setDescription("sample11");
        functionLibrary11.setFunctionLibraryScript("samplefunction11");

        return new Object[][]{
                {
                        functionLibrary10,
                        SAMPLE_TENANT_DOMAIN
                },
                {
                        functionLibrary11,
                        SAMPLE_TENANT_DOMAIN2
                },
        };
    }

    @Test(dataProvider = "deleteFunctionLibraryData")
    public void deleteFunctionLibrary(Object functionLibrary, String tenantDomain)
            throws SQLException, FunctionLibraryManagementException {

        try (Connection connection1 = DAOUtils.getConnection(DB_NAME);
             Connection connection2 = DAOUtils.getConnection(DB_NAME)) {

            FunctionLibraryDAO functionLibraryDAO = new FunctionLibraryDAOImpl();

            addFunctionLibraries(functionLibraryDAO, Collections.singletonList(functionLibrary), tenantDomain);

            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection1);
            functionLibraryDAO.deleteFunctionLibrary(((FunctionLibrary) functionLibrary).getFunctionLibraryName(),
                    tenantDomain);

            when(IdentityDatabaseUtil.getDBConnection(false)).thenReturn(connection2);
            assertNull(functionLibraryDAO.getFunctionLibrary(((FunctionLibrary) functionLibrary)
                            .getFunctionLibraryName(), tenantDomain),
                    "Failed to delete the functionLibrary by name.");

        }
    }

    @Test(dataProvider = "deleteFunctionLibraryData", expectedExceptions = FunctionLibraryManagementException.class)
    public void deleteFunctionLibraryException(Object functionLibrary, String tenantDomain) throws SQLException,
            FunctionLibraryManagementException {

        FunctionLibraryDAO functionLibraryDAO = new FunctionLibraryDAOImpl();

        addFunctionLibraries(functionLibraryDAO, Collections.singletonList(functionLibrary), tenantDomain);

        functionLibraryDAO.deleteFunctionLibrary(((FunctionLibrary) functionLibrary).getFunctionLibraryName(),
                tenantDomain);
        Assert.fail("Expected: " + FunctionLibraryManagementException.class.getName());

    }

    @DataProvider(name = "updateFunctionLibraryDataProvider")
    public Object[][] updateFunctionLibraryData() {

        FunctionLibrary functionLibrary12 = new FunctionLibrary();
        functionLibrary12.setFunctionLibraryName("sample12");
        functionLibrary12.setDescription("sample12");
        functionLibrary12.setFunctionLibraryScript("samplefunction12");

        FunctionLibrary functionLibrary13 = new FunctionLibrary();
        functionLibrary13.setFunctionLibraryName("sample13");
        functionLibrary13.setDescription("sample13");
        functionLibrary13.setFunctionLibraryScript("samplefunction13");

        return new Object[][]{
                {
                        functionLibrary12,
                        SAMPLE_TENANT_DOMAIN
                },
                {
                        functionLibrary13,
                        SAMPLE_TENANT_DOMAIN2
                }
        };
    }

    @Test(dataProvider = "updateFunctionLibraryDataProvider")
    public void updateFunctionLibrary(Object functionLibrary, String tenantDomain)
            throws SQLException, FunctionLibraryManagementException {

        try (Connection connection1 = DAOUtils.getConnection(DB_NAME);
             Connection connection2 = DAOUtils.getConnection(DB_NAME)) {

            FunctionLibraryDAO functionLibraryDAO = new FunctionLibraryDAOImpl();

            addFunctionLibraries(functionLibraryDAO, Collections.singletonList(functionLibrary), tenantDomain);
            FunctionLibrary funLib = (FunctionLibrary) functionLibrary;
            String oldName = funLib.getFunctionLibraryName();
            funLib.setFunctionLibraryName("updatedName");

            when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection1);
            functionLibraryDAO.updateFunctionLibrary(oldName, funLib, tenantDomain);

            when(IdentityDatabaseUtil.getDBConnection(false)).thenReturn(connection2);
            assertNotNull(functionLibraryDAO.getFunctionLibrary(funLib.getFunctionLibraryName(), tenantDomain),
                    "Failed to update script library.");

            // Clean after test
            deleteFunctionLibraries(functionLibraryDAO, Collections.singletonList(functionLibrary), tenantDomain);
        }
    }

    @Test(dataProvider = "updateFunctionLibraryDataProvider",
            expectedExceptions = FunctionLibraryManagementException.class)
    public void updateFunctionLibraryException(Object functionLibrary, String tenantDomain)
            throws SQLException, FunctionLibraryManagementException {

        FunctionLibraryDAO functionLibraryDAO = new FunctionLibraryDAOImpl();

        addFunctionLibraries(functionLibraryDAO, Collections.singletonList(functionLibrary), tenantDomain);
        FunctionLibrary funLib = (FunctionLibrary) functionLibrary;
        String oldName = funLib.getFunctionLibraryName();
        funLib.setFunctionLibraryName("updatedName");
        functionLibraryDAO.updateFunctionLibrary(oldName, funLib, tenantDomain);
        Assert.fail("Expected: " + FunctionLibraryManagementException.class.getName());
        // Clean after test
        deleteFunctionLibraries(functionLibraryDAO, Collections.singletonList(functionLibrary), tenantDomain);
    }

    @DataProvider(name = "isFunctionLibraryExistsDataProvider")
    public Object[][] isFunctionLibraryExistsData() {

        FunctionLibrary functionLibrary14 = new FunctionLibrary();
        functionLibrary14.setFunctionLibraryName("sample14");
        functionLibrary14.setDescription("sample14");
        functionLibrary14.setFunctionLibraryScript("samplefunction14");

        FunctionLibrary functionLibrary15 = new FunctionLibrary();
        functionLibrary15.setFunctionLibraryName("sample15");
        functionLibrary15.setDescription("sample15");
        functionLibrary15.setFunctionLibraryScript("samplefunction15");

        return new Object[][]{
                {
                        functionLibrary14,
                        SAMPLE_TENANT_DOMAIN
                },
                {
                        functionLibrary15,
                        SAMPLE_TENANT_DOMAIN2
                },
        };
    }

    @Test(dataProvider = "isFunctionLibraryExistsDataProvider")
    public void isFunctionLibraryExists(Object functionLibrary, String tenantDomain)
            throws SQLException, FunctionLibraryManagementException {

        try (Connection connection1 = DAOUtils.getConnection(DB_NAME);
             Connection connection2 = DAOUtils.getConnection(DB_NAME)) {

            FunctionLibraryDAO functionLibraryDAO = new FunctionLibraryDAOImpl();

            addFunctionLibraries(functionLibraryDAO, Collections.singletonList(functionLibrary), tenantDomain);

            when(IdentityDatabaseUtil.getDBConnection(false)).thenReturn(connection1);
            assertTrue(functionLibraryDAO.isFunctionLibraryExists(
                    ((FunctionLibrary) functionLibrary).getFunctionLibraryName(), tenantDomain),
                    "Failed to check existence " +
                            "by script library name.");
            when(IdentityDatabaseUtil.getDBConnection(false)).thenReturn(connection2);
            assertFalse(functionLibraryDAO.isFunctionLibraryExists("InvalidName", tenantDomain),
                    "Failed to check existence " +
                            "by script library name.");

            // Clean after test
            deleteFunctionLibraries(functionLibraryDAO, Collections.singletonList(functionLibrary), tenantDomain);
        }
    }

    private void addFunctionLibraries(FunctionLibraryDAO functionLibraryDAO,
                                      List<Object> functionLibraries, String tenantDomain)
            throws SQLException, FunctionLibraryManagementException {

        for (Object functionLibrary : functionLibraries) {
            try (Connection connection1 = DAOUtils.getConnection(DB_NAME)) {
                when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection1);
                functionLibraryDAO.createFunctionLibrary((FunctionLibrary) functionLibrary, tenantDomain);
            }
        }
    }

    private void deleteFunctionLibraries(FunctionLibraryDAO functionLibraryDAO, List<Object> functionLibraries,
                                         String tenantDomain) throws SQLException, FunctionLibraryManagementException {

        for (Object functionLibrary : functionLibraries) {
            try (Connection connection1 = DAOUtils.getConnection(DB_NAME)) {
                when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection1);
                functionLibraryDAO.deleteFunctionLibrary(((FunctionLibrary) functionLibrary).getFunctionLibraryName(),
                        tenantDomain);
            }
        }
    }
}
