package org.wso2.carbon.identity.functions.library.mgt.ui.client;


import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.identity.functions.library.mgt.model.xsd.FunctionLibrary;
import org.wso2.carbon.identity.functions.library.mgt.stub.FunctionLibraryManagementAdminServiceFunctionLibraryManagementException;
import org.wso2.carbon.identity.functions.library.mgt.stub.FunctionLibraryManagementAdminServiceStub;

import java.rmi.RemoteException;

/**
 * Function library management service client.
 */
public class FunctionLibraryManagementServiceClient {
    FunctionLibraryManagementAdminServiceStub stub;

    /**
     *
     * @param cookie
     * @param backendServerURL
     * @param configCtx
     * @throws AxisFault
     */
    public FunctionLibraryManagementServiceClient(String cookie, String backendServerURL,
                                                  ConfigurationContext configCtx) throws AxisFault {

        String serviceURL = backendServerURL + "FunctionLibraryManagementAdminService";
        stub = new FunctionLibraryManagementAdminServiceStub(configCtx, serviceURL);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     *
     * @param functionLibrary
     * @throws AxisFault
     */
    public void createFunctionLibrary (FunctionLibrary functionLibrary) throws AxisFault {

            try {
                stub.createFunctionLibrary(functionLibrary);

            } catch (RemoteException | FunctionLibraryManagementAdminServiceFunctionLibraryManagementException e) {
                handleException(e);
            }

    }

    /**
     *
     * @return
     * @throws AxisFault
     */
    public FunctionLibrary[] listFunctionLibraries () throws AxisFault {

        try {
            return stub.listFunctionLibraries();
        } catch (RemoteException | FunctionLibraryManagementAdminServiceFunctionLibraryManagementException e) {
            handleException(e);

            }

        return new
                FunctionLibrary[0];
    }

    /**
     *
     * @param functionLibraryName
     * @return
     * @throws AxisFault
     */
    public FunctionLibrary getFunctionLibrary (String functionLibraryName) throws AxisFault {


        try {
            return stub.getFunctionLibrary(functionLibraryName);
        } catch (RemoteException | FunctionLibraryManagementAdminServiceFunctionLibraryManagementException e) {
           handleException(e);

            }

        return null;
    }

    /**
     *
     * @param functionLibraryName
     * @throws AxisFault
     */
    public void deleteFunctionLibrary (String functionLibraryName) throws AxisFault {

        try {
            stub.deleteFunctionLibrary(functionLibraryName);

        } catch (RemoteException | FunctionLibraryManagementAdminServiceFunctionLibraryManagementException e) {
           handleException(e);
        }
    }

    /**
     *
     * @param functionLibrary
     * @param oldFunctionLibraryName
     * @throws AxisFault
     */
    public void updateFunctionLibrary (FunctionLibrary functionLibrary, String oldFunctionLibraryName)
            throws AxisFault {
        try {
            stub.updateFunctionLibrary(functionLibrary, oldFunctionLibraryName);

        } catch (RemoteException | FunctionLibraryManagementAdminServiceFunctionLibraryManagementException e) {
           handleException(e);

        }
    }

    /**
     *
     * @param e
     * @throws AxisFault
     */
    private void handleException(Exception e) throws AxisFault {
        String errorMessage = "Unknown error occurred.";

        if (e instanceof  FunctionLibraryManagementAdminServiceFunctionLibraryManagementException) {
            FunctionLibraryManagementAdminServiceFunctionLibraryManagementException  exception =
                    ( FunctionLibraryManagementAdminServiceFunctionLibraryManagementException ) e;
            if (exception.getFaultMessage().getFunctionLibraryManagementException() != null) {
                errorMessage = exception.getFaultMessage().getFunctionLibraryManagementException().getMessage();
            }
        } else {
            errorMessage = e.getMessage();

        }

        throw new AxisFault(errorMessage, e);

    }


}
