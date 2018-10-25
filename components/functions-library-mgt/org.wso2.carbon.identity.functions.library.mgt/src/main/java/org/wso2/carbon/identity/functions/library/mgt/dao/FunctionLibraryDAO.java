package org.wso2.carbon.identity.functions.library.mgt.dao;

import org.wso2.carbon.identity.functions.library.mgt.exception.FunctionLibraryManagementException;
import org.wso2.carbon.identity.functions.library.mgt.model.FunctionLibrary;

/**
 * This interface access the data storage layer to store, update, retrieve and delete function libraries.
 */
public interface FunctionLibraryDAO {

    /**
     *
     * @param functionLibrary
     * @param tenantDomain
     * @throws FunctionLibraryManagementException
     */
    void createFunctionLibrary (FunctionLibrary functionLibrary, String tenantDomain)
            throws FunctionLibraryManagementException;

    /**
     *
     * @param functionLibraryName
     * @param tenantDomain
     * @return
     * @throws FunctionLibraryManagementException
     */
    FunctionLibrary getFunctionLibrary (String functionLibraryName, String tenantDomain)
            throws FunctionLibraryManagementException;

    /**
     *
     * @param tenantDomain
     * @return
     * @throws FunctionLibraryManagementException
     */
    FunctionLibrary[] listFunctionLibraries (String tenantDomain)
            throws FunctionLibraryManagementException;

    /**
     *
     * @param functionLibrary
     * @param tenantDomain
     * @param oldFunctionLibName
     * @throws FunctionLibraryManagementException
     */
    void updateFunctionLibrary (FunctionLibrary functionLibrary, String tenantDomain, String oldFunctionLibName)
            throws FunctionLibraryManagementException;

    /**
     *
     * @param functionLibraryName
     * @param tenantDomain
     * @throws FunctionLibraryManagementException
     */
    void deleteFunctionLibrary (String functionLibraryName, String tenantDomain)
            throws FunctionLibraryManagementException;

    /**
     * Checks whether the function library already exists with the name.
     *
     * @param functionLibraryName Name of the function library
     * @param tenantDomain          tenant domain
     * @return whether the function library exists or not
     */
    boolean isFunctionLibraryExists (String functionLibraryName, String tenantDomain)
            throws FunctionLibraryManagementException;


}
