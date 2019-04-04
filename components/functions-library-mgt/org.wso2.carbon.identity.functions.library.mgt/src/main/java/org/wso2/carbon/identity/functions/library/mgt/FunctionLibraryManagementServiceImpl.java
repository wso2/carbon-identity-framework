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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.functions.library.mgt.dao.FunctionLibraryDAO;
import org.wso2.carbon.identity.functions.library.mgt.dao.impl.FunctionLibraryDAOImpl;
import org.wso2.carbon.identity.functions.library.mgt.exception.FunctionLibraryManagementException;
import org.wso2.carbon.identity.functions.library.mgt.model.FunctionLibrary;

import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static org.wso2.carbon.identity.functions.library.mgt.FunctionLibraryMgtUtil.isRegexValidated;

/**
 * Function library management service implementation.
 */
public class FunctionLibraryManagementServiceImpl implements FunctionLibraryManagementService {

    private static final Log log = LogFactory.getLog(FunctionLibraryManagementServiceImpl.class);
    private static FunctionLibraryManagementServiceImpl functionLibMgtService =
            new FunctionLibraryManagementServiceImpl();

    /**
     * Private constructor which will not allow to create objects of this class from outside.
     */
    private FunctionLibraryManagementServiceImpl() {

    }

    /**
     * Get FunctionLibraryManagementServiceImpl instance.
     *
     * @return FunctionLibraryManagementServiceImpl instance
     */
    public static FunctionLibraryManagementServiceImpl getInstance() {

        return functionLibMgtService;
    }

    @Override
    public void createFunctionLibrary(FunctionLibrary functionLibrary, String tenantDomain)
            throws FunctionLibraryManagementException {

        validateInputs(functionLibrary);
        evaluateScript(functionLibrary);
        FunctionLibraryDAO functionLibraryDAO = new FunctionLibraryDAOImpl();

        if (functionLibraryDAO.isFunctionLibraryExists(functionLibrary.getFunctionLibraryName(), tenantDomain)) {
            throw new FunctionLibraryManagementException("Already a function library available with the same name.");
        }

        String functionLibraryName = functionLibrary.getFunctionLibraryName();
        if (!isRegexValidated(functionLibraryName)) {
            throw new FunctionLibraryManagementException("The function library name " +
                    functionLibrary.getFunctionLibraryName() + " is not valid! It is not adhering " +
                    "to the regex " + FunctionLibraryMgtUtil.FUNCTION_LIBRARY_NAME_VALIDATING_REGEX + ".");
        }

        functionLibraryDAO.createFunctionLibrary(functionLibrary, tenantDomain);
    }

    @Override
    public List<FunctionLibrary> listFunctionLibraries(String tenantDomain) throws FunctionLibraryManagementException {

        FunctionLibraryDAO functionLibraryDAO = new FunctionLibraryDAOImpl();
        return functionLibraryDAO.listFunctionLibraries(tenantDomain);
    }

    @Override
    public FunctionLibrary getFunctionLibrary(String functionLibraryName, String tenantDomain)
            throws FunctionLibraryManagementException {

        FunctionLibraryDAO functionLibraryDAO = new FunctionLibraryDAOImpl();
        return functionLibraryDAO.getFunctionLibrary(functionLibraryName, tenantDomain);
    }

    @Override
    public void deleteFunctionLibrary(String functionLibraryName, String tenantDomain)
            throws FunctionLibraryManagementException {

        FunctionLibraryDAO functionLibraryDAO = new FunctionLibraryDAOImpl();
        functionLibraryDAO.deleteFunctionLibrary(functionLibraryName, tenantDomain);
    }

    @Override
    public void updateFunctionLibrary(String oldFunctionLibraryName, FunctionLibrary functionLibrary,
                                      String tenantDomain)
            throws FunctionLibraryManagementException {

        validateInputs(functionLibrary);
        evaluateScript(functionLibrary);
        FunctionLibraryDAO functionLibraryDAO = new FunctionLibraryDAOImpl();

        if (!functionLibrary.getFunctionLibraryName().equals(oldFunctionLibraryName) &&
                functionLibraryDAO.isFunctionLibraryExists(functionLibrary.getFunctionLibraryName(), tenantDomain)) {
            throw new FunctionLibraryManagementException("Already a function library available with the same name.");
        }

        String functionLibraryName = functionLibrary.getFunctionLibraryName();
        if (!isRegexValidated(functionLibraryName)) {
            throw new FunctionLibraryManagementException("The function library name " +
                    functionLibrary.getFunctionLibraryName() + " is not valid! It is not adhering " +
                    "to the regex " + FunctionLibraryMgtUtil.FUNCTION_LIBRARY_NAME_VALIDATING_REGEX + ".");
        }
        functionLibraryDAO.updateFunctionLibrary(oldFunctionLibraryName, functionLibrary, tenantDomain);
    }

    @Override
    public boolean isFunctionLibraryExists(String functionLibraryName, String tenantDomain)
            throws FunctionLibraryManagementException {

        FunctionLibraryDAO functionLibraryDAO = new FunctionLibraryDAOImpl();
        return functionLibraryDAO.isFunctionLibraryExists(functionLibraryName, tenantDomain);
    }

    /**
     * Check for required attributes.
     *
     * @param functionLibrary Function library
     * @throws FunctionLibraryManagementException
     */
    private void validateInputs(FunctionLibrary functionLibrary) throws FunctionLibraryManagementException {

        if (StringUtils.isBlank(functionLibrary.getFunctionLibraryName())) {
            throw new FunctionLibraryManagementException("Function Library Name is required.");
        } else if (StringUtils.isBlank(functionLibrary.getFunctionLibraryScript())) {
            throw new FunctionLibraryManagementException("Function Library Script is required.");
        }
    }

    /**
     * Evaluate the function library script.
     *
     * @param functionLibrary Function Library
     * @throws FunctionLibraryManagementException
     */
    private void evaluateScript(FunctionLibrary functionLibrary) throws FunctionLibraryManagementException {

        try {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            String head = "var module = { exports:{} }; \n" +
                    "var exports = {}; \n" +
                    "function require(name){};";
            String code = functionLibrary.getFunctionLibraryScript();
            code = head + code;
            engine.eval(code);
        } catch (ScriptException e) {
            log.error("Function library script of " + functionLibrary.getFunctionLibraryName() +
                    " contains errors." + e);
            throw new FunctionLibraryManagementException("Function library script of " +
                    functionLibrary.getFunctionLibraryName() + " contains errors.", e);
        }
    }
}
