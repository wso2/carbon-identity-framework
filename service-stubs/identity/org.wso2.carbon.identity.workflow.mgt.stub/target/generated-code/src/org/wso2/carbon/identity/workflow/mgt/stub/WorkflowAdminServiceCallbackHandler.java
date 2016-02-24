
/**
 * WorkflowAdminServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.workflow.mgt.stub;

    /**
     *  WorkflowAdminServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class WorkflowAdminServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public WorkflowAdminServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public WorkflowAdminServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for getWorkflow method
            * override this method for handling normal response from getWorkflow operation
            */
           public void receiveResultgetWorkflow(
                    org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowWizard result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getWorkflow operation
           */
            public void receiveErrorgetWorkflow(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for listWorkflowEvents method
            * override this method for handling normal response from listWorkflowEvents operation
            */
           public void receiveResultlistWorkflowEvents(
                    org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowEvent[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from listWorkflowEvents operation
           */
            public void receiveErrorlistWorkflowEvents(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getTemplate method
            * override this method for handling normal response from getTemplate operation
            */
           public void receiveResultgetTemplate(
                    org.wso2.carbon.identity.workflow.mgt.stub.metadata.Template result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getTemplate operation
           */
            public void receiveErrorgetTemplate(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for listWorkflowImpls method
            * override this method for handling normal response from listWorkflowImpls operation
            */
           public void receiveResultlistWorkflowImpls(
                    org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowImpl[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from listWorkflowImpls operation
           */
            public void receiveErrorlistWorkflowImpls(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getWorkflowImpl method
            * override this method for handling normal response from getWorkflowImpl operation
            */
           public void receiveResultgetWorkflowImpl(
                    org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowImpl result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getWorkflowImpl operation
           */
            public void receiveErrorgetWorkflowImpl(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for listWorkflows method
            * override this method for handling normal response from listWorkflows operation
            */
           public void receiveResultlistWorkflows(
                    org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowWizard[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from listWorkflows operation
           */
            public void receiveErrorlistWorkflows(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for listAllAssociations method
            * override this method for handling normal response from listAllAssociations operation
            */
           public void receiveResultlistAllAssociations(
                    org.wso2.carbon.identity.workflow.mgt.stub.metadata.Association[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from listAllAssociations operation
           */
            public void receiveErrorlistAllAssociations(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getWorkflowsOfRequest method
            * override this method for handling normal response from getWorkflowsOfRequest operation
            */
           public void receiveResultgetWorkflowsOfRequest(
                    org.wso2.carbon.identity.workflow.mgt.stub.bean.WorkflowRequestAssociation[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getWorkflowsOfRequest operation
           */
            public void receiveErrorgetWorkflowsOfRequest(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for listAssociations method
            * override this method for handling normal response from listAssociations operation
            */
           public void receiveResultlistAssociations(
                    org.wso2.carbon.identity.workflow.mgt.stub.metadata.Association[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from listAssociations operation
           */
            public void receiveErrorlistAssociations(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for listTemplates method
            * override this method for handling normal response from listTemplates operation
            */
           public void receiveResultlistTemplates(
                    org.wso2.carbon.identity.workflow.mgt.stub.metadata.Template[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from listTemplates operation
           */
            public void receiveErrorlistTemplates(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getRequestsCreatedByUser method
            * override this method for handling normal response from getRequestsCreatedByUser operation
            */
           public void receiveResultgetRequestsCreatedByUser(
                    org.wso2.carbon.identity.workflow.mgt.stub.bean.WorkflowRequest[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getRequestsCreatedByUser operation
           */
            public void receiveErrorgetRequestsCreatedByUser(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getEvent method
            * override this method for handling normal response from getEvent operation
            */
           public void receiveResultgetEvent(
                    org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowEvent result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getEvent operation
           */
            public void receiveErrorgetEvent(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getRequestsInFilter method
            * override this method for handling normal response from getRequestsInFilter operation
            */
           public void receiveResultgetRequestsInFilter(
                    org.wso2.carbon.identity.workflow.mgt.stub.bean.WorkflowRequest[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getRequestsInFilter operation
           */
            public void receiveErrorgetRequestsInFilter(java.lang.Exception e) {
            }
                


    }
    