
/**
 * ProcessManagementServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.bpel.stub.mgt;

    /**
     *  ProcessManagementServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class ProcessManagementServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public ProcessManagementServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public ProcessManagementServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getAllProcesses method
            * override this method for handling normal response from getAllProcesses operation
            */
           public void receiveResultgetAllProcesses(
                    java.lang.String[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllProcesses operation
           */
            public void receiveErrorgetAllProcesses(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getPaginatedProcessList method
            * override this method for handling normal response from getPaginatedProcessList operation
            */
           public void receiveResultgetPaginatedProcessList(
                    org.wso2.carbon.bpel.stub.mgt.types.PaginatedProcessInfoList result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getPaginatedProcessList operation
           */
            public void receiveErrorgetPaginatedProcessList(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getProcessDeploymentInfo method
            * override this method for handling normal response from getProcessDeploymentInfo operation
            */
           public void receiveResultgetProcessDeploymentInfo(
                    org.wso2.carbon.bpel.stub.mgt.types.ProcessDeployDetailsList_type0 result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getProcessDeploymentInfo operation
           */
            public void receiveErrorgetProcessDeploymentInfo(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getProcessInfo method
            * override this method for handling normal response from getProcessInfo operation
            */
           public void receiveResultgetProcessInfo(
                    org.wso2.carbon.bpel.stub.mgt.types.ProcessInfoType result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getProcessInfo operation
           */
            public void receiveErrorgetProcessInfo(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                


    }
    