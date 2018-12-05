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

package org.wso2.carbon.identity.functions.library.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.functions.library.mgt.dao.impl.FunctionLibraryDAOImpl;
import org.wso2.carbon.identity.functions.library.mgt.exception.FunctionLibraryManagementException;
import org.wso2.carbon.identity.functions.library.mgt.model.FunctionLibrary;
import org.wso2.carbon.identity.testutil.powermock.PowerMockIdentityBaseTest;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.functions.library.mgt.FunctionLibraryMgtUtil.isRegexValidated;

@PrepareForTest({FunctionLibraryManagementServiceImpl.class})
public class FunctionLibraryManagementServiceTest extends PowerMockIdentityBaseTest {

    private static final String SAMPLE_TENANT_DOMAIN = "carbon.super";
    private static final String SAMPLE_TENANT_DOMAIN2 = "abc.com";
    private static final Log log = LogFactory.getLog(FunctionLibraryManagementServiceTest.class);

    @DataProvider(name = "createFunctionLibraryDataProvider")
    public Object[][] createFunctionLibraryData() {

        FunctionLibrary functionLibrary1 = new FunctionLibrary();
        functionLibrary1.setFunctionLibraryName("$#%%");
        functionLibrary1.setDescription("sample1");
        functionLibrary1.setFunctionLibraryScript("function samplefunction1(){}");

        FunctionLibrary functionLibrary2 = new FunctionLibrary();
        functionLibrary2.setFunctionLibraryName("sample2");
        functionLibrary2.setDescription("sample2");
        functionLibrary2.setFunctionLibraryScript(null);

        FunctionLibrary functionLibrary3 = new FunctionLibrary();
        functionLibrary3.setFunctionLibraryName("");
        functionLibrary3.setDescription("sample3");
        functionLibrary3.setFunctionLibraryScript("samplefunction3");

        FunctionLibrary functionLibrary4 = new FunctionLibrary();
        functionLibrary4.setFunctionLibraryName("sample4");
        functionLibrary4.setDescription("sample4");
        functionLibrary4.setFunctionLibraryScript("function samplefunction4(){}");

        FunctionLibrary functionLibrary5 = new FunctionLibrary();
        functionLibrary5.setFunctionLibraryName("sample5");
        functionLibrary5.setDescription("sample5");
        functionLibrary5.setFunctionLibraryScript("function samplefunction5(){}");

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
                },
                {
                        functionLibrary4,
                        SAMPLE_TENANT_DOMAIN2
                },
                {
                        functionLibrary5,
                        null
                }
        };
    }

    @Test(dataProvider = "createFunctionLibraryDataProvider")
    public void createFunctionLibrary(Object functionLibrary, String tenantDomain) {

        FunctionLibraryDAOImpl functionLibraryDAO = PowerMockito.mock(FunctionLibraryDAOImpl.class);
        try {

            PowerMockito.whenNew(FunctionLibraryDAOImpl.class).withNoArguments().thenReturn(functionLibraryDAO);
            if (((FunctionLibrary) functionLibrary).getFunctionLibraryName() == "sample4") {
                when(functionLibraryDAO.isFunctionLibraryExists("sample4", tenantDomain)).
                        thenReturn(true);
            } else {
                when(functionLibraryDAO.isFunctionLibraryExists(
                        ((FunctionLibrary) functionLibrary).getFunctionLibraryName(), tenantDomain)).thenReturn(false);
            }
            when(functionLibraryDAO.getFunctionLibrary(((FunctionLibrary) functionLibrary).getFunctionLibraryName(),
                    tenantDomain)).thenReturn((FunctionLibrary) functionLibrary);
            FunctionLibraryManagementService functionLibraryManagementService =
                    FunctionLibraryManagementServiceImpl.getInstance();

            functionLibraryManagementService.createFunctionLibrary((FunctionLibrary) functionLibrary, tenantDomain);

            assertEquals(functionLibraryManagementService.getFunctionLibrary(
                    ((FunctionLibrary) functionLibrary).getFunctionLibraryName(), tenantDomain).getFunctionLibraryName(),
                    ((FunctionLibrary) functionLibrary).getFunctionLibraryName());

            // Clean after test
            functionLibraryManagementService.deleteFunctionLibrary(
                    ((FunctionLibrary) functionLibrary).getFunctionLibraryName(), tenantDomain);
        } catch (FunctionLibraryManagementException e) {
            if (((FunctionLibrary) functionLibrary).getFunctionLibraryName() == null) {
                assertEquals(e.getMessage(), "Function Library Name is required");
            } else if (!isRegexValidated(((FunctionLibrary) functionLibrary).getFunctionLibraryName())) {
                assertEquals(e.getMessage(), "The function library name " + ((FunctionLibrary) functionLibrary).
                        getFunctionLibraryName() + " is not valid! It is not adhering " +
                        "to the regex " + FunctionLibraryMgtUtil.FUNCTION_LIBRARY_NAME_VALIDATING_REGEX + ".");
            } else {
                try {
                    if (functionLibraryDAO.isFunctionLibraryExists(
                            ((FunctionLibrary) functionLibrary).getFunctionLibraryName(), tenantDomain)) {
                        assertEquals(e.getMessage(),
                                "Already a function library available with the same name.");
                    }
                } catch (FunctionLibraryManagementException e1) {
                    log.error("Function Library Management Exception");
                }
            }
        } catch (Exception e) {
            log.error("Exception");
        }
    }

    @DataProvider(name = "getFunctionLibraryDataProvider")
    public Object[][] getFunctionLibraryData() {

        FunctionLibrary functionLibrary9 = new FunctionLibrary();
        functionLibrary9.setFunctionLibraryName("sample9");
        functionLibrary9.setDescription("sample9");
        functionLibrary9.setFunctionLibraryScript("function samplefunction9(){}");

        FunctionLibrary functionLibrary10 = new FunctionLibrary();
        functionLibrary10.setFunctionLibraryName("sample10");
        functionLibrary10.setDescription("sample10");
        functionLibrary10.setFunctionLibraryScript("function samplefunction10(){}");

        return new Object[][]{
                {
                        functionLibrary9,
                        SAMPLE_TENANT_DOMAIN
                },
                {
                        functionLibrary10,
                        null
                }
        };
    }

    @Test(dataProvider = "getFunctionLibraryDataProvider")
    public void getFunctionLibrary(Object functionLibrary, String tenantDomain) {

        try {
            FunctionLibraryManagementService functionLibraryManagementService =
                    FunctionLibraryManagementServiceImpl.getInstance();

            FunctionLibraryDAOImpl functionLibraryDAO = PowerMockito.mock(FunctionLibraryDAOImpl.class);
            PowerMockito.whenNew(FunctionLibraryDAOImpl.class).withNoArguments().thenReturn(functionLibraryDAO);
            when(functionLibraryDAO.isFunctionLibraryExists(
                    ((FunctionLibrary) functionLibrary).getFunctionLibraryName(), tenantDomain)).thenReturn(false);
            when(functionLibraryDAO.getFunctionLibrary(
                    ((FunctionLibrary) functionLibrary).getFunctionLibraryName(), tenantDomain)).
                    thenReturn((FunctionLibrary) functionLibrary);

            addFunctionLibraries(functionLibraryManagementService, Collections.singletonList(functionLibrary),
                    tenantDomain);

            assertTrue(functionLibraryManagementService.getFunctionLibrary(
                    ((FunctionLibrary) functionLibrary).getFunctionLibraryName(), tenantDomain) != null,
                    "Failed to retrieve function library");
            // Clean after test
            deleteFunctionLibraries(functionLibraryManagementService, Collections.singletonList(functionLibrary),
                    tenantDomain);
        } catch (Exception e) {
            log.error("Exception");
        }
    }

    @DataProvider(name = "listFunctionLibraryDataProvider")
    public Object[][] listFunctionLibrariesData() {

        FunctionLibrary functionLibrary6 = new FunctionLibrary();
        functionLibrary6.setFunctionLibraryName("sample6");
        functionLibrary6.setDescription("sample6");
        functionLibrary6.setFunctionLibraryScript("function samplefunction6(){}");

        FunctionLibrary functionLibrary7 = new FunctionLibrary();
        functionLibrary7.setFunctionLibraryName("sample7");
        functionLibrary7.setDescription("sample7");
        functionLibrary7.setFunctionLibraryScript("function samplefunction7(){}");

        FunctionLibrary functionLibrary8 = new FunctionLibrary();
        functionLibrary8.setFunctionLibraryName("sample8");
        functionLibrary8.setDescription("sample8");
        functionLibrary8.setFunctionLibraryScript("function samplefunction8(){}");

        return new Object[][]{
                {
                        Arrays.asList(
                                functionLibrary6,
                                functionLibrary7,
                                functionLibrary8
                        ),
                        SAMPLE_TENANT_DOMAIN
                }
        };
    }

    @Test(dataProvider = "listFunctionLibraryDataProvider")
    public void listFunctionLibraries(List<Object> functionLibraries, String tenantDomain) throws Exception {

        FunctionLibraryManagementService functionLibraryManagementService =
                FunctionLibraryManagementServiceImpl.getInstance();
        FunctionLibraryDAOImpl functionLibraryDAO = PowerMockito.mock(FunctionLibraryDAOImpl.class);
        PowerMockito.whenNew(FunctionLibraryDAOImpl.class).withNoArguments().thenReturn(functionLibraryDAO);
        for (Object functionLibrary : functionLibraries) {
            when(functionLibraryDAO.isFunctionLibraryExists(((FunctionLibrary) functionLibrary).getFunctionLibraryName(),
                    tenantDomain)).thenReturn(false);
        }
        List<FunctionLibrary> functionLibraries1 = Arrays.asList(new FunctionLibrary[3]);
        int i = 0;
        for (Object functionLibrary : functionLibraries) {
            functionLibraries1.set(i, (FunctionLibrary) functionLibrary);
            i += 1;
        }
        when(functionLibraryDAO.listFunctionLibraries(SAMPLE_TENANT_DOMAIN)).thenReturn(functionLibraries1);
        addFunctionLibraries(functionLibraryManagementService, functionLibraries, tenantDomain);

        List<FunctionLibrary> functionLibrariesList = functionLibraryManagementService.listFunctionLibraries(tenantDomain);
        assertTrue(functionLibrariesList != null && functionLibrariesList.size() != 0,
                "Failed to retrieve scopes.");

        // Clean after test
        deleteFunctionLibraries(functionLibraryManagementService, functionLibraries, tenantDomain);
    }

    @DataProvider(name = "updateFunctionLibraryDataProvider")
    public Object[][] updateFunctionLibraryData() {

        FunctionLibrary functionLibrary11 = new FunctionLibrary();
        functionLibrary11.setFunctionLibraryName("sample11");
        functionLibrary11.setDescription("sample11");
        functionLibrary11.setFunctionLibraryScript("function samplefunction11(){}");

        FunctionLibrary functionLibrary12 = new FunctionLibrary();
        functionLibrary12.setFunctionLibraryName("sample12");
        functionLibrary12.setDescription("sample12");
        functionLibrary12.setFunctionLibraryScript("function samplefunction12(){}");

        FunctionLibrary functionLibrary13 = new FunctionLibrary();
        functionLibrary13.setFunctionLibraryName("sample13");
        functionLibrary13.setDescription("sample13");
        functionLibrary13.setFunctionLibraryScript("function samplefunction13(){}");

        FunctionLibrary functionLibrary14 = new FunctionLibrary();
        functionLibrary14.setFunctionLibraryName("sample14");
        functionLibrary14.setDescription("sample14");
        functionLibrary14.setFunctionLibraryScript("function samplefunction14(){}");

        return new Object[][]{
                {
                        functionLibrary11,
                        SAMPLE_TENANT_DOMAIN
                },
                {
                        functionLibrary12,
                        SAMPLE_TENANT_DOMAIN2
                },
                {
                        functionLibrary13,
                        SAMPLE_TENANT_DOMAIN
                },
                {
                        functionLibrary14,
                        SAMPLE_TENANT_DOMAIN2
                }
        };
    }

    @Test(dataProvider = "updateFunctionLibraryDataProvider")
    public void updateFunctionLibrary(Object functionLibrary, String tenantDomain) throws Exception {

        FunctionLibraryManagementService functionLibraryManagementService =
                FunctionLibraryManagementServiceImpl.getInstance();
        FunctionLibraryDAOImpl functionLibraryDAO = PowerMockito.mock(FunctionLibraryDAOImpl.class);
        PowerMockito.whenNew(FunctionLibraryDAOImpl.class).withNoArguments().thenReturn(functionLibraryDAO);

        addFunctionLibraries(functionLibraryManagementService, Collections.singletonList(functionLibrary),
                tenantDomain);
        FunctionLibrary funLib = (FunctionLibrary) functionLibrary;
        String oldName = funLib.getFunctionLibraryName();
        if (oldName == "sample11") {
            funLib.setFunctionLibraryName("");
        } else if (oldName == "sample12") {
            funLib.setFunctionLibraryName("#$%^%^");
        } else if (oldName == "sample13") {
            funLib.setFunctionLibraryName("sample");
        } else {
            funLib.setFunctionLibraryName("updated");
        }
        try {
            when(functionLibraryDAO.isFunctionLibraryExists("sample", tenantDomain)).thenReturn(true);
            when(functionLibraryDAO.getFunctionLibrary(funLib.getFunctionLibraryName(), tenantDomain)).thenReturn(funLib);
            functionLibraryManagementService.updateFunctionLibrary(oldName, funLib, tenantDomain);

            assertNotNull(functionLibraryManagementService.getFunctionLibrary(funLib.getFunctionLibraryName(),
                    tenantDomain), "Failed to update function library.");

            // Clean after test
            deleteFunctionLibraries(functionLibraryManagementService, Collections.singletonList(functionLibrary),
                    tenantDomain);
        } catch (FunctionLibraryManagementException e) {
            if (!funLib.getFunctionLibraryName().equals(oldName) && functionLibraryDAO.isFunctionLibraryExists
                    (funLib.getFunctionLibraryName(), tenantDomain)) {
                assertEquals(e.getMessage(), "Already a function library available with the same name.");
            }
            if (!isRegexValidated(funLib.getFunctionLibraryName())) {
                assertEquals(e.getMessage(), "The function library name " +
                        funLib.getFunctionLibraryName() + " is not valid! It is not adhering " +
                        "to the regex " + FunctionLibraryMgtUtil.FUNCTION_LIBRARY_NAME_VALIDATING_REGEX + ".");
            }
        } catch (Exception e) {
            log.error("Exception");
        }
    }

    private void addFunctionLibraries(FunctionLibraryManagementService functionLibraryManagementService,
                                      List<Object> functionLibraries, String tenantDomain) {

        for (Object functionLibrary : functionLibraries) {
            try {
                functionLibraryManagementService.createFunctionLibrary((FunctionLibrary) functionLibrary, tenantDomain);
            } catch (FunctionLibraryManagementException e) {
                if (((FunctionLibrary) functionLibrary).getFunctionLibraryName() == null) {
                    assertEquals(e.getMessage(), "Function Library Name is required");
                }
            }
        }
    }

    private void deleteFunctionLibraries(FunctionLibraryManagementService functionLibraryManagementService,
                                         List<Object> functionLibraries, String tenantDomain)
            throws SQLException, FunctionLibraryManagementException {

        for (Object functionLibrary : functionLibraries) {
            functionLibraryManagementService.deleteFunctionLibrary(
                    ((FunctionLibrary) functionLibrary).getFunctionLibraryName(), tenantDomain);
        }
    }
}
