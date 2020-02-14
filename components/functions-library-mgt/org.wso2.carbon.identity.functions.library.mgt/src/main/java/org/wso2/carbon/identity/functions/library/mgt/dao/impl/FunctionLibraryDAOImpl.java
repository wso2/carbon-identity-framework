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

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.functions.library.mgt.FunctionLibMgtDBQueries;
import org.wso2.carbon.identity.functions.library.mgt.dao.FunctionLibraryDAO;
import org.wso2.carbon.identity.functions.library.mgt.exception.FunctionLibraryManagementException;
import org.wso2.carbon.identity.functions.library.mgt.model.FunctionLibrary;
import org.wso2.carbon.identity.functions.library.mgt.util.FunctionLibraryExceptionManagementUtil;
import org.wso2.carbon.identity.functions.library.mgt.util.FunctionLibraryManagementConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class access the IDN_FUNCTION_LIBRARY database table to store, update retrieve and delete function libraries.
 */
public class FunctionLibraryDAOImpl implements FunctionLibraryDAO {

    private static final Log log = LogFactory.getLog(FunctionLibraryDAOImpl.class);

    /**
     * Create a function library.
     *
     * @param functionLibrary Function library
     * @param tenantDomain    Tenant domain
     * @throws FunctionLibraryManagementException
     */
    public void createFunctionLibrary(FunctionLibrary functionLibrary, String tenantDomain)
            throws FunctionLibraryManagementException {

        // get logged-in users tenant identifier.
        int tenantID = MultitenantConstants.INVALID_TENANT_ID;

        if (tenantDomain != null) {
            tenantID = IdentityTenantUtil.getTenantId(tenantDomain);
        }

        if (tenantID != MultitenantConstants.INVALID_TENANT_ID) {
            try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
                try (PreparedStatement addFunctionLibStmt =
                             connection.prepareStatement(FunctionLibMgtDBQueries.STORE_FUNCTIONLIB_INFO)) {
                    addFunctionLibStmt.setString(1, functionLibrary.getFunctionLibraryName());
                    addFunctionLibStmt.setString(2, functionLibrary.getDescription());
                    addFunctionLibStmt.setString(3, "authentication");
                    addFunctionLibStmt.setInt(4, tenantID);
                    setBlobValue(functionLibrary.getFunctionLibraryScript(), addFunctionLibStmt, 5);
                    addFunctionLibStmt.executeUpdate();
                    IdentityDatabaseUtil.commitTransaction(connection);

                    if (log.isDebugEnabled()) {
                        log.debug("script Library stored successfully with script library name " +
                                functionLibrary.getFunctionLibraryName());
                    }
                } catch (SQLException e1) {
                    IdentityDatabaseUtil.rollbackTransaction(connection);
                    throw FunctionLibraryExceptionManagementUtil.handleServerException(
                            FunctionLibraryManagementConstants.ErrorMessage.ERROR_CODE_ADD_SCRIPT_LIBRARY,
                            functionLibrary.getFunctionLibraryName(), e1);
                }
            } catch (SQLException e) {
                throw FunctionLibraryExceptionManagementUtil.handleServerException(
                        FunctionLibraryManagementConstants.ErrorMessage.ERROR_CODE_ADD_SCRIPT_LIBRARY,
                        functionLibrary.getFunctionLibraryName(), e);
            } catch (IOException e) {
                throw FunctionLibraryExceptionManagementUtil.handleServerException(
                        FunctionLibraryManagementConstants.ErrorMessage.
                                ERROR_CODE_PROCESSING_CONTENT_STREAM_SCRIPT_LIBRARY,
                        functionLibrary.getFunctionLibraryName(), e);
            }
        } else {
            throw FunctionLibraryExceptionManagementUtil.handleServerException(
                    FunctionLibraryManagementConstants.ErrorMessage.ERROR_CODE_INVALID_TENANT);
        }
    }

    /**
     * Retrieve a function library from the given name.
     *
     * @param functionLibraryName Function library name
     * @param tenantDomain        Tenant domain
     * @return Function library
     * @throws FunctionLibraryManagementException
     */
    public FunctionLibrary getFunctionLibrary(String functionLibraryName, String tenantDomain)
            throws FunctionLibraryManagementException {

        // get logged-in users tenant identifier.
        int tenantID = MultitenantConstants.INVALID_TENANT_ID;

        if (tenantDomain != null) {
            tenantID = IdentityTenantUtil.getTenantId(tenantDomain);
        }

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement getFunctionLibStmt = connection
                    .prepareStatement(FunctionLibMgtDBQueries.LOAD_FUNCTIONLIB_FROM_TENANTID_AND_NAME)) {
                getFunctionLibStmt.setInt(1, tenantID);
                getFunctionLibStmt.setString(2, functionLibraryName);

                try (ResultSet resultSet = getFunctionLibStmt.executeQuery()) {
                    if (resultSet.next()) {
                        FunctionLibrary functionlib = new FunctionLibrary();
                        functionlib.setFunctionLibraryName(resultSet.getString("NAME"));
                        functionlib.setDescription(resultSet.getString("DESCRIPTION"));
                        functionlib.setFunctionLibraryScript(IOUtils.
                                toString(resultSet.getBinaryStream("DATA")));
                        return functionlib;
                    } else {
                        return null;
                    }
                }
            } catch (IOException e) {
                throw FunctionLibraryExceptionManagementUtil.handleServerException(
                        FunctionLibraryManagementConstants.ErrorMessage.ERROR_CODE_RETRIEVE_SCRIPT_LIBRARY,
                        functionLibraryName, e);
            }
        } catch (SQLException e) {
            throw FunctionLibraryExceptionManagementUtil.handleServerException(
                    FunctionLibraryManagementConstants.ErrorMessage.ERROR_CODE_RETRIEVE_SCRIPT_LIBRARY,
                    functionLibraryName, e);
        } catch (IdentityRuntimeException e) {
            throw FunctionLibraryExceptionManagementUtil.handleServerException(
                    FunctionLibraryManagementConstants.ErrorMessage.ERROR_CODE_DATABASE_CONNECTION, e);
        }
    }

    /**
     * Retrieve function library list in the tenant domain.
     *
     * @param tenantDomain Tenant domain
     * @return A list of function libraries
     * @throws FunctionLibraryManagementException
     */
    public List<FunctionLibrary> listFunctionLibraries(String tenantDomain)
            throws FunctionLibraryManagementException {

        int tenantID = MultitenantConstants.INVALID_TENANT_ID;

        if (tenantDomain != null) {
            tenantID = IdentityTenantUtil.getTenantId(tenantDomain);
        }

        List<FunctionLibrary> functionLibraries = new ArrayList<>();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement getFunctionLibrariesStmt = connection
                    .prepareStatement(FunctionLibMgtDBQueries.LOAD_FUNCTIONLIB_FROM_TENANTID)) {
                getFunctionLibrariesStmt.setInt(1, tenantID);

                try (ResultSet functionLibsResultSet = getFunctionLibrariesStmt.executeQuery()) {
                    while (functionLibsResultSet.next()) {
                        FunctionLibrary functionlib = new FunctionLibrary();
                        functionlib.setFunctionLibraryName(functionLibsResultSet.getString("NAME"));
                        functionlib.setDescription(functionLibsResultSet.getString("DESCRIPTION"));
                        functionLibraries.add(functionlib);
                    }
                }
            } catch (SQLException e1) {
                throw FunctionLibraryExceptionManagementUtil.handleServerException(
                        FunctionLibraryManagementConstants.ErrorMessage.ERROR_CODE_RETRIEVE_SCRIPT_LIBRARIES, e1);
            }
        } catch (SQLException e) {
            throw FunctionLibraryExceptionManagementUtil.handleServerException(
                    FunctionLibraryManagementConstants.ErrorMessage.ERROR_CODE_RETRIEVE_SCRIPT_LIBRARIES, e);
        } catch (IdentityRuntimeException e) {
            throw FunctionLibraryExceptionManagementUtil.handleServerException(
                    FunctionLibraryManagementConstants.ErrorMessage.ERROR_CODE_DATABASE_CONNECTION, e);
        }
        return functionLibraries;
    }

    /**
     * Update an existing function library.
     *
     * @param oldFunctionLibName Previous name of the function library
     * @param functionLibrary    Function library
     * @param tenantDomain       Tenant domain
     * @throws FunctionLibraryManagementException
     */
    public void updateFunctionLibrary(String oldFunctionLibName, FunctionLibrary functionLibrary, String tenantDomain)
            throws FunctionLibraryManagementException {

        // get logged-in users tenant identifier.
        int tenantID = MultitenantConstants.INVALID_TENANT_ID;

        if (tenantDomain != null) {
            tenantID = IdentityTenantUtil.getTenantId(tenantDomain);
        }

        if (tenantID != MultitenantConstants.INVALID_TENANT_ID) {
            try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
                try (PreparedStatement updateFunctionLibStmt =
                             connection.prepareStatement(FunctionLibMgtDBQueries.UPDATE_FUNCTIONLIB_INFO)) {
                    updateFunctionLibStmt.setString(1, functionLibrary.getFunctionLibraryName());
                    updateFunctionLibStmt.setString(2, functionLibrary.getDescription());
                    setBlobValue(functionLibrary.getFunctionLibraryScript(), updateFunctionLibStmt, 3);
                    updateFunctionLibStmt.setInt(4, tenantID);
                    updateFunctionLibStmt.setString(5, oldFunctionLibName);
                    updateFunctionLibStmt.executeUpdate();
                    IdentityDatabaseUtil.commitTransaction(connection);
                } catch (SQLException e1) {
                    IdentityDatabaseUtil.rollbackTransaction(connection);
                    throw FunctionLibraryExceptionManagementUtil.handleServerException(
                            FunctionLibraryManagementConstants.ErrorMessage.ERROR_CODE_UPDATE_SCRIPT_LIBRARY,
                            oldFunctionLibName, e1);
                }
            } catch (SQLException e) {
                throw FunctionLibraryExceptionManagementUtil.handleServerException(
                        FunctionLibraryManagementConstants.ErrorMessage.ERROR_CODE_UPDATE_SCRIPT_LIBRARY,
                        oldFunctionLibName, e);
            } catch (IOException e) {
                throw FunctionLibraryExceptionManagementUtil.handleServerException(
                        FunctionLibraryManagementConstants.ErrorMessage.
                                ERROR_CODE_PROCESSING_CONTENT_STREAM_SCRIPT_LIBRARY,
                        oldFunctionLibName, e);
            } catch (IdentityRuntimeException e) {
                throw FunctionLibraryExceptionManagementUtil.handleServerException(
                        FunctionLibraryManagementConstants.ErrorMessage.ERROR_CODE_DATABASE_CONNECTION, e);
            }
        } else {
            throw FunctionLibraryExceptionManagementUtil.handleServerException(
                    FunctionLibraryManagementConstants.ErrorMessage.ERROR_CODE_INVALID_TENANT);
        }
    }

    /**
     * Delete an existing function library.
     *
     * @param functionLibraryName Function library name
     * @param tenantDomain        Tenant domain
     * @throws FunctionLibraryManagementException
     */
    public void deleteFunctionLibrary(String functionLibraryName, String tenantDomain)
            throws FunctionLibraryManagementException {

        int tenantID = MultitenantConstants.INVALID_TENANT_ID;

        if (tenantDomain != null) {
            tenantID = IdentityTenantUtil.getTenantId(tenantDomain);
        }

        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
            try (PreparedStatement deleteFunctionLibStmt =
                         connection.prepareStatement(FunctionLibMgtDBQueries.REMOVE_FUNCTIONLIB)) {
                deleteFunctionLibStmt.setInt(1, tenantID);
                deleteFunctionLibStmt.setString(2, functionLibraryName);
                deleteFunctionLibStmt.executeUpdate();
                IdentityDatabaseUtil.commitTransaction(connection);
                if (log.isDebugEnabled()) {
                    log.debug("Removed the script library " + functionLibraryName + " successfully.");
                }
            } catch (SQLException e1) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw FunctionLibraryExceptionManagementUtil.handleServerException(
                        FunctionLibraryManagementConstants.ErrorMessage.
                                ERROR_CODE_PROCESSING_CONTENT_STREAM_SCRIPT_LIBRARY,
                        functionLibraryName, e1);
            }
        } catch (SQLException e) {
            throw FunctionLibraryExceptionManagementUtil.handleServerException(
                    FunctionLibraryManagementConstants.ErrorMessage.ERROR_CODE_PROCESSING_CONTENT_STREAM_SCRIPT_LIBRARY,
                    functionLibraryName, e);
        } catch (IdentityRuntimeException e) {
            throw FunctionLibraryExceptionManagementUtil.handleServerException(
                    FunctionLibraryManagementConstants.ErrorMessage.ERROR_CODE_DATABASE_CONNECTION, e);
        }
    }

    /**
     * Checks whether the function library already exists with the name.
     *
     * @param functionLibraryName Name of the function library
     * @param tenantDomain        Tenant domain
     * @return Existence of the function library
     * @throws FunctionLibraryManagementException
     */
    public boolean isFunctionLibraryExists(String functionLibraryName, String tenantDomain)
            throws FunctionLibraryManagementException {

        boolean isFunctionLibraryExists = false;
        int tenantID = MultitenantConstants.SUPER_TENANT_ID;
        if (tenantDomain != null) {
            tenantID = IdentityTenantUtil.getTenantId(tenantDomain);
        }

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement checkFunctionLibraryExistence = connection
                    .prepareStatement(FunctionLibMgtDBQueries.LOAD_FUNCTIONLIB_FROM_TENANTID_AND_NAME)) {
                checkFunctionLibraryExistence.setInt(1, tenantID);
                checkFunctionLibraryExistence.setString(2, functionLibraryName);

                try (ResultSet resultSet = checkFunctionLibraryExistence.executeQuery()) {
                    if (resultSet.next()) {
                        isFunctionLibraryExists = true;
                    }
                }
            } catch (SQLException e1) {
                throw FunctionLibraryExceptionManagementUtil.handleServerException(
                        FunctionLibraryManagementConstants.ErrorMessage.ERROR_CODE_FAILED_TO_CHECK_SCRIPT_LIBRARY,
                        functionLibraryName, e1);
            }
        } catch (SQLException e) {
            throw FunctionLibraryExceptionManagementUtil.handleServerException(
                    FunctionLibraryManagementConstants.ErrorMessage.ERROR_CODE_FAILED_TO_CHECK_SCRIPT_LIBRARY,
                    functionLibraryName, e);
        }
        return isFunctionLibraryExists;
    }

    /**
     * Set given string as Blob for the given index into the prepared-statement.
     *
     * @param value    string value to be converted to blob
     * @param prepStmt Prepared statement
     * @param index    Column index
     * @throws SQLException
     * @throws IOException
     */
    private void setBlobValue(String value, PreparedStatement prepStmt, int index) throws SQLException, IOException {

        if (value != null) {
            InputStream inputStream = new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
            prepStmt.setBinaryStream(index, inputStream, inputStream.available());
        } else {
            prepStmt.setBinaryStream(index, new ByteArrayInputStream(new byte[0]), 0);
        }
    }
}
