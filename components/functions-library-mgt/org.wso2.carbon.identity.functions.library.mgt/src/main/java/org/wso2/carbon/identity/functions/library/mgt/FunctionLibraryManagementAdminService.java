package org.wso2.carbon.identity.functions.library.mgt;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.functions.library.mgt.exception.FunctionLibraryManagementException;
import org.wso2.carbon.identity.functions.library.mgt.model.FunctionLibrary;


/**
 * Function library management admin service.
 */
public class FunctionLibraryManagementAdminService extends AbstractAdmin {

    private static Log log = LogFactory.getLog(FunctionLibraryManagementAdminService.class);
    private FunctionLibraryManagementService functionLibMgtService;

    /**
     *
     * @param functionLibrary
     * @throws FunctionLibraryManagementException
     */
    public void createFunctionLibrary (FunctionLibrary functionLibrary) throws FunctionLibraryManagementException {
        try {
            functionLibMgtService = FunctionLibraryManagementServiceImpl.getInstance();
            functionLibMgtService.createFunctionLibrary(functionLibrary, getTenantDomain());
        } catch (FunctionLibraryManagementException flException) {
            log.error("Error while creating function library " + functionLibrary.getFunctionLibraryName() +
                    " for tenant domain " + getTenantDomain(), flException);
            throw flException;
        }

    }

    /**
     *
     * @return
     * @throws FunctionLibraryManagementException
     */
    public FunctionLibrary[] listFunctionLibraries() throws FunctionLibraryManagementException {
        try {
            functionLibMgtService = FunctionLibraryManagementServiceImpl.getInstance();
            FunctionLibrary[] functionLibraries = functionLibMgtService.listFunctionLibraries(getTenantDomain());
            return functionLibraries;
        } catch (FunctionLibraryManagementException flException) {
            log.error("Error while retrieving function libraris for tenant: " + getTenantDomain(), flException);
            throw flException;
        }
    }

    /**
     *
     * @param functionLibraryName
     * @return
     * @throws FunctionLibraryManagementException
     */
    public FunctionLibrary getFunctionLibrary(String functionLibraryName) throws FunctionLibraryManagementException {
        try {
            functionLibMgtService = FunctionLibraryManagementServiceImpl.getInstance();
            FunctionLibrary functionLibrary = null;
            functionLibrary = functionLibMgtService.getFunctionLibrary(functionLibraryName, getTenantDomain());
            return functionLibrary;
        } catch (FunctionLibraryManagementException flException) {
            log.error("Error while retrieving function library " + functionLibraryName +
                    " for tenant domain " + getTenantDomain(), flException);
            throw flException;
        }

    }

    /**
     *
     * @param functionLibraryName
     * @throws FunctionLibraryManagementException
     */
    public void deleteFunctionLibrary(String functionLibraryName) throws FunctionLibraryManagementException {
        try {
        functionLibMgtService = FunctionLibraryManagementServiceImpl.getInstance();
        functionLibMgtService.deleteFunctionLibrary(functionLibraryName, getTenantDomain());
        } catch (FunctionLibraryManagementException flException) {
            log.error("Error while deleting function library " + functionLibraryName +
                    " for tenant domain " + getTenantDomain(), flException);
            throw flException;
        }
    }

    /**
     *
     * @param functionLibrary
     * @param oldFunctionLibraryName
     * @throws FunctionLibraryManagementException
     */
    public void updateFunctionLibrary(FunctionLibrary functionLibrary, String oldFunctionLibraryName)
            throws FunctionLibraryManagementException {

        try {
            functionLibMgtService = FunctionLibraryManagementServiceImpl.getInstance();
            functionLibMgtService.updateFunctionLibrary(functionLibrary, getTenantDomain(), oldFunctionLibraryName);
        } catch (FunctionLibraryManagementException flException) {
            log.error("Error while updating function library " + oldFunctionLibraryName +
                    "for tenant domain " + getTenantDomain(), flException);
            throw flException;
        }
    }

}
