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
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.functions.library.mgt.exception.FunctionLibraryManagementException;
import org.wso2.carbon.identity.functions.library.mgt.model.FunctionLibrary;

import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Function library management admin service.
 */
public class FunctionLibraryManagementAdminService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(FunctionLibraryManagementAdminService.class);
    private FunctionLibraryManagementService functionLibMgtService;

    /**
     * Create a function library with function library name, description and script.
     *
     * @param functionLibrary Function library
     * @throws FunctionLibraryManagementException
     */
    public void createFunctionLibrary(FunctionLibrary functionLibrary) throws FunctionLibraryManagementException {

        try {
            validateInputs(functionLibrary);
            evaluateScript(functionLibrary);
            functionLibMgtService = FunctionLibraryManagementServiceImpl.getInstance();
            functionLibMgtService.createFunctionLibrary(functionLibrary, getTenantDomain());
        } catch (FunctionLibraryManagementException e) {
            log.error("Error while creating function library " + functionLibrary.getFunctionLibraryName() +
                    " for tenant domain " + getTenantDomain() + ".", e);
            throw e;
        }
    }

    /**
     * Get all function libraries in a tenant.
     *
     * @return A list of function libraries
     * @throws FunctionLibraryManagementException
     */
    public List<FunctionLibrary> listFunctionLibraries() throws FunctionLibraryManagementException {

        try {
            functionLibMgtService = FunctionLibraryManagementServiceImpl.getInstance();
            List<FunctionLibrary> functionLibraries = functionLibMgtService.listFunctionLibraries(getTenantDomain());
            return functionLibraries;
        } catch (FunctionLibraryManagementException e) {
            log.error("Error while retrieving function libraries for tenant: " + getTenantDomain() + ".", e);
            throw e;
        }
    }

    /**
     * Get a function library using function library name.
     *
     * @param functionLibraryName Name of the function library
     * @return Function library
     * @throws FunctionLibraryManagementException
     */
    public FunctionLibrary getFunctionLibrary(String functionLibraryName) throws FunctionLibraryManagementException {

        try {
            functionLibMgtService = FunctionLibraryManagementServiceImpl.getInstance();
            FunctionLibrary functionLibrary = null;
            functionLibrary = functionLibMgtService.getFunctionLibrary(functionLibraryName, getTenantDomain());
            return functionLibrary;
        } catch (FunctionLibraryManagementException e) {
            log.error("Error while retrieving function library " + functionLibraryName +
                    " for tenant domain " + getTenantDomain() + ".", e);
            throw e;
        }
    }

    /**
     * Delete an existing function library using the function library name.
     *
     * @param functionLibraryName Name of the function library
     * @throws FunctionLibraryManagementException
     */
    public void deleteFunctionLibrary(String functionLibraryName) throws FunctionLibraryManagementException {

        try {
            functionLibMgtService = FunctionLibraryManagementServiceImpl.getInstance();
            functionLibMgtService.deleteFunctionLibrary(functionLibraryName, getTenantDomain());
        } catch (FunctionLibraryManagementException e) {
            log.error("Error while deleting function library " + functionLibraryName +
                    " for tenant domain " + getTenantDomain(), e);
            throw e;
        }
    }

    /**
     * Update the details of a function library.
     *
     * @param oldFunctionLibraryName Previous name of the function library
     * @param functionLibrary        Function library with new details
     * @throws FunctionLibraryManagementException
     */
    public void updateFunctionLibrary(String oldFunctionLibraryName, FunctionLibrary functionLibrary)
            throws FunctionLibraryManagementException {

        try {
            validateInputs(functionLibrary);
            evaluateScript(functionLibrary);
            functionLibMgtService = FunctionLibraryManagementServiceImpl.getInstance();
            functionLibMgtService.updateFunctionLibrary(oldFunctionLibraryName, functionLibrary, getTenantDomain());
        } catch (FunctionLibraryManagementException e) {
            log.error("Error while updating function library " + oldFunctionLibraryName +
                    "for tenant domain " + getTenantDomain(), e);
            throw e;
        }
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
