
/**
 * WorkflowImplAdminServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.workflow.impl.stub;

    /**
     *  WorkflowImplAdminServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class WorkflowImplAdminServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public WorkflowImplAdminServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public WorkflowImplAdminServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for removeBPSPackage method
            * override this method for handling normal response from removeBPSPackage operation
            */
           public void receiveResultremoveBPSPackage(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from removeBPSPackage operation
           */
            public void receiveErrorremoveBPSPackage(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for addBPSProfile method
            * override this method for handling normal response from addBPSProfile operation
            */
           public void receiveResultaddBPSProfile(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addBPSProfile operation
           */
            public void receiveErroraddBPSProfile(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for removeBPSProfile method
            * override this method for handling normal response from removeBPSProfile operation
            */
           public void receiveResultremoveBPSProfile(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from removeBPSProfile operation
           */
            public void receiveErrorremoveBPSProfile(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for updateBPSProfile method
            * override this method for handling normal response from updateBPSProfile operation
            */
           public void receiveResultupdateBPSProfile(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from updateBPSProfile operation
           */
            public void receiveErrorupdateBPSProfile(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for listBPSProfiles method
            * override this method for handling normal response from listBPSProfiles operation
            */
           public void receiveResultlistBPSProfiles(
                    org.wso2.carbon.identity.workflow.impl.stub.bean.BPSProfile[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from listBPSProfiles operation
           */
            public void receiveErrorlistBPSProfiles(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getBPSProfile method
            * override this method for handling normal response from getBPSProfile operation
            */
           public void receiveResultgetBPSProfile(
                    org.wso2.carbon.identity.workflow.impl.stub.bean.BPSProfile result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getBPSProfile operation
           */
            public void receiveErrorgetBPSProfile(java.lang.Exception e) {
            }
                


    }
    