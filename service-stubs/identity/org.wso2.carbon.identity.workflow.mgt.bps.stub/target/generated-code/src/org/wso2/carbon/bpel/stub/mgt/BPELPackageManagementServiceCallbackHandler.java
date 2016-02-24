
/**
 * BPELPackageManagementServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.bpel.stub.mgt;

    /**
     *  BPELPackageManagementServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class BPELPackageManagementServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public BPELPackageManagementServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public BPELPackageManagementServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for undeployBPELPackage method
            * override this method for handling normal response from undeployBPELPackage operation
            */
           public void receiveResultundeployBPELPackage(
                    org.wso2.carbon.bpel.stub.mgt.types.UndeployStatus_type0 result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from undeployBPELPackage operation
           */
            public void receiveErrorundeployBPELPackage(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for listDeployedPackagesPaginated method
            * override this method for handling normal response from listDeployedPackagesPaginated operation
            */
           public void receiveResultlistDeployedPackagesPaginated(
                    org.wso2.carbon.bpel.stub.mgt.types.DeployedPackagesPaginated result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from listDeployedPackagesPaginated operation
           */
            public void receiveErrorlistDeployedPackagesPaginated(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for listProcessesInPackage method
            * override this method for handling normal response from listProcessesInPackage operation
            */
           public void receiveResultlistProcessesInPackage(
                    org.wso2.carbon.bpel.stub.mgt.types.PackageType result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from listProcessesInPackage operation
           */
            public void receiveErrorlistProcessesInPackage(java.lang.Exception e) {
            }
                


    }
    