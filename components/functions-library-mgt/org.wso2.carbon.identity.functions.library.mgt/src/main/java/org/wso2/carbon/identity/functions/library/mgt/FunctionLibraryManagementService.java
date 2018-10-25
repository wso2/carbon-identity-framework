package org.wso2.carbon.identity.functions.library.mgt;

import org.wso2.carbon.identity.functions.library.mgt.exception.FunctionLibraryManagementException;
import org.wso2.carbon.identity.functions.library.mgt.model.FunctionLibrary;

/**
 * Function library management service interface.
 */
public interface FunctionLibraryManagementService {


    /**
     * Create a function library.
     * @param functionLibrary
     * @param tenantDomain
     * @throws FunctionLibraryManagementException
     */
    void createFunctionLibrary(FunctionLibrary functionLibrary, String tenantDomain)
            throws FunctionLibraryManagementException;

    /**
     * Retrieve function library list in the tenant domain.
     * @param tenantDomain
     * @return
     * @throws FunctionLibraryManagementException
     */
    FunctionLibrary[] listFunctionLibraries (String tenantDomain)
            throws FunctionLibraryManagementException;

    /**
     * Retrieve a function library.
     * @param functionLibraryName
     * @param tenantDomain
     * @return
     * @throws FunctionLibraryManagementException
     */
    FunctionLibrary getFunctionLibrary (String functionLibraryName, String tenantDomain)
            throws FunctionLibraryManagementException;

    /**
     * Delete a function library.
     * @param functionLibraryName
     * @param tenantDomain
     * @throws FunctionLibraryManagementException
     */
    void deleteFunctionLibrary (String functionLibraryName, String tenantDomain)
            throws FunctionLibraryManagementException;

    /**
     * Update a function library.
     * @param functionLibrary
     * @param tenatDomain
     * @param oldFunctionLibraryName
     * @throws FunctionLibraryManagementException
     */
    void updateFunctionLibrary (FunctionLibrary functionLibrary, String tenatDomain, String oldFunctionLibraryName)
            throws FunctionLibraryManagementException;

}
