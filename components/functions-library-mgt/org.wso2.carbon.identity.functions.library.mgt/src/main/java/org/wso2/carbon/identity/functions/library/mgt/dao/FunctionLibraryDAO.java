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

package org.wso2.carbon.identity.functions.library.mgt.dao;

import org.wso2.carbon.identity.functions.library.mgt.exception.FunctionLibraryManagementException;
import org.wso2.carbon.identity.functions.library.mgt.model.FunctionLibrary;

import java.util.List;

/**
 * This interface access the data storage layer to store, update, retrieve and delete function libraries.
 */
public interface FunctionLibraryDAO {

    /**
     * Add a new function library.
     *
     * @param functionLibrary Function library
     * @param tenantDomain    Tenant domain
     * @throws FunctionLibraryManagementException
     */
    void createFunctionLibrary(FunctionLibrary functionLibrary, String tenantDomain)
            throws FunctionLibraryManagementException;

    /**
     * Retrieve a function library.
     *
     * @param functionLibraryName Name of the function library
     * @param tenantDomain        Tenant domain
     * @return Function library
     * @throws FunctionLibraryManagementException
     */
    FunctionLibrary getFunctionLibrary(String functionLibraryName, String tenantDomain)
            throws FunctionLibraryManagementException;

    /**
     * Retrieve a list of function libraries in the given tenant domain.
     *
     * @param tenantDomain Tenant domain
     * @return A list of function libraries
     * @throws FunctionLibraryManagementException
     */
    List<FunctionLibrary> listFunctionLibraries(String tenantDomain)
            throws FunctionLibraryManagementException;

    /**
     * Update an existing function library.
     *
     * @param oldFunctionLibName Previous name of the function library
     * @param functionLibrary    Function library
     * @param tenantDomain       Tenant domain
     * @throws FunctionLibraryManagementException
     */
    void updateFunctionLibrary(String oldFunctionLibName, FunctionLibrary functionLibrary, String tenantDomain)
            throws FunctionLibraryManagementException;

    /**
     * Delete an existing function library.
     *
     * @param functionLibraryName Name of the function library
     * @param tenantDomain        Tenant domain
     * @throws FunctionLibraryManagementException
     */
    void deleteFunctionLibrary(String functionLibraryName, String tenantDomain)
            throws FunctionLibraryManagementException;

    /**
     * Checks whether the function library already exists with the name.
     *
     * @param functionLibraryName Name of the function library
     * @param tenantDomain        Tenant domain
     * @return Whether the function library exists or not
     */
    boolean isFunctionLibraryExists(String functionLibraryName, String tenantDomain)
            throws FunctionLibraryManagementException;

}
