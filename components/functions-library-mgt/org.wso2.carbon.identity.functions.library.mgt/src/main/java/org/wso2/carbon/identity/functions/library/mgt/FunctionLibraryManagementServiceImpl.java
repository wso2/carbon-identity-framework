package org.wso2.carbon.identity.functions.library.mgt;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.functions.library.mgt.dao.FunctionLibraryDAO;
import org.wso2.carbon.identity.functions.library.mgt.dao.impl.FunctionLibraryDAOImpl;
import org.wso2.carbon.identity.functions.library.mgt.exception.FunctionLibraryManagementException;
import org.wso2.carbon.identity.functions.library.mgt.model.FunctionLibrary;

import static org.wso2.carbon.identity.functions.library.mgt.FunctionLibraryMgtUtil.isRegexValidated;

/**
 * Function library management service implementation.
 */
public class FunctionLibraryManagementServiceImpl implements FunctionLibraryManagementService {

    private static final Log log = LogFactory.getLog(FunctionLibraryManagementServiceImpl.class);
    private static volatile FunctionLibraryManagementServiceImpl functionLibMgtService;

    private FunctionLibraryManagementServiceImpl() {
    }

    /**
     * Get FunctionLibraryManagementServiceImpl instance.
     *
     * @return  FunctionLibraryManagementServiceImpl instance
     */
    public static FunctionLibraryManagementServiceImpl getInstance() {

        if (functionLibMgtService == null) {
            synchronized (FunctionLibraryManagementServiceImpl.class) {
                if (functionLibMgtService == null) {
                    functionLibMgtService = new FunctionLibraryManagementServiceImpl();
                }
            }
        }
        return functionLibMgtService;
    }

    public void createFunctionLibrary(FunctionLibrary functionLibrary, String tenantDomain)
            throws FunctionLibraryManagementException {

        if (StringUtils.isBlank(functionLibrary.getFunctionLibraryName())) {
            // check for required attributes.
            throw new FunctionLibraryManagementException("Function Library Name is required");
        }

        FunctionLibraryDAO functionLibraryDAO = new FunctionLibraryDAOImpl();

        if (functionLibraryDAO.isFunctionLibraryExists(functionLibrary.getFunctionLibraryName(), tenantDomain)) {
            throw new FunctionLibraryManagementException("Already a function library available with the same name.");
        }

        String functionLibraryName = functionLibrary.getFunctionLibraryName();
        if (!isRegexValidated(functionLibraryName)) {
            throw new FunctionLibraryManagementException("The function library name " +
                    functionLibrary.getFunctionLibraryName() + " is not valid! It is not adhering " +
                    "to the regex " + FunctionLibraryMgtUtil.FUNCTION_LIBRARY_NAME_VALIDATING_REGEX);
        }

        try {
            functionLibraryDAO.createFunctionLibrary(functionLibrary, tenantDomain);
        } catch (FunctionLibraryManagementException e) {
            throw e;
        }

    }

    public FunctionLibrary[] listFunctionLibraries(String tenantDomain) throws FunctionLibraryManagementException {
        FunctionLibraryDAO functionLibraryDAO = new FunctionLibraryDAOImpl();
        return functionLibraryDAO.listFunctionLibraries(tenantDomain);

    }

    public FunctionLibrary  getFunctionLibrary(String functionLibraryName, String tenantDomain)
            throws FunctionLibraryManagementException {
        FunctionLibraryDAO functionLibraryDAO = new FunctionLibraryDAOImpl();
        return functionLibraryDAO.getFunctionLibrary(functionLibraryName, tenantDomain);
    }

    public void deleteFunctionLibrary(String functionLibraryName, String tenantDomain)
            throws FunctionLibraryManagementException {
        FunctionLibraryDAO functionLibraryDAO = new FunctionLibraryDAOImpl();
        functionLibraryDAO.deleteFunctionLibrary(functionLibraryName, tenantDomain);
    }

    public void updateFunctionLibrary(FunctionLibrary functionLibrary, String tenantDomain,
                                      String oldFunctionLibraryName)
            throws FunctionLibraryManagementException {

        if (StringUtils.isBlank(functionLibrary.getFunctionLibraryName())) {
            // check for required attributes.
            throw new FunctionLibraryManagementException("Function Library Name is required");
        }

        FunctionLibraryDAO functionLibraryDAO = new FunctionLibraryDAOImpl();

        if (!functionLibrary.getFunctionLibraryName().equals(oldFunctionLibraryName) &&
                functionLibraryDAO.isFunctionLibraryExists(functionLibrary.getFunctionLibraryName(), tenantDomain)) {
            throw new FunctionLibraryManagementException("Already a function library available with the same name.");
        }

        String functionLibraryName = functionLibrary.getFunctionLibraryName();
        if (!isRegexValidated(functionLibraryName)) {
            throw new FunctionLibraryManagementException("The function library name " +
                    functionLibrary.getFunctionLibraryName() + " is not valid! It is not adhering " +
                    "to the regex " + FunctionLibraryMgtUtil.FUNCTION_LIBRARY_NAME_VALIDATING_REGEX);
        }
      try {
          functionLibraryDAO.updateFunctionLibrary(functionLibrary, tenantDomain, oldFunctionLibraryName);
      } catch (FunctionLibraryManagementException e) {
          throw e;
      }

    }

}
