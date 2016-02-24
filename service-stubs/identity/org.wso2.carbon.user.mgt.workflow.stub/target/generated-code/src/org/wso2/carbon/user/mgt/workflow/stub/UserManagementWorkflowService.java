

/**
 * UserManagementWorkflowService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.user.mgt.workflow.stub;

    /*
     *  UserManagementWorkflowService java interface
     */

    public interface UserManagementWorkflowService {
          

        /**
          * Auto generated method signature
          * 
                    * @param listAllEntityNames0
                
             * @throws org.wso2.carbon.user.mgt.workflow.stub.UserManagementWorkflowServiceWorkflowExceptionException : 
         */

         
                     public java.lang.String[] listAllEntityNames(

                        java.lang.String wfOperationType1,java.lang.String wfStatus2,java.lang.String entityType3,java.lang.String entityIdFilter4)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.workflow.stub.UserManagementWorkflowServiceWorkflowExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param listAllEntityNames0
            
          */
        public void startlistAllEntityNames(

            java.lang.String wfOperationType1,java.lang.String wfStatus2,java.lang.String entityType3,java.lang.String entityIdFilter4,

            final org.wso2.carbon.user.mgt.workflow.stub.UserManagementWorkflowServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    