

/**
 * WorkflowAdminService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.workflow.mgt.stub;

    /*
     *  WorkflowAdminService java interface
     */

    public interface WorkflowAdminService {
          

        /**
          * Auto generated method signature
          * 
                    * @param getWorkflow23
                
             * @throws org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException : 
         */

         
                     public org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowWizard getWorkflow(

                        java.lang.String workflowId24)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getWorkflow23
            
          */
        public void startgetWorkflow(

            java.lang.String workflowId24,

            final org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param listWorkflowEvents27
                
         */

         
                     public org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowEvent[] listWorkflowEvents(

                        )
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param listWorkflowEvents27
            
          */
        public void startlistWorkflowEvents(

            

            final org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getTemplate30
                
             * @throws org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException : 
         */

         
                     public org.wso2.carbon.identity.workflow.mgt.stub.metadata.Template getTemplate(

                        java.lang.String templateId31)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getTemplate30
            
          */
        public void startgetTemplate(

            java.lang.String templateId31,

            final org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException : 
         */
        public void  removeAssociation(
         java.lang.String associationId35

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException : 
         */
        public void  changeAssociationState(
         java.lang.String associationId37,boolean isEnable38

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException : 
         */
        public void  removeWorkflow(
         java.lang.String id40

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException;

        

        /**
          * Auto generated method signature
          * 
                    * @param listWorkflowImpls41
                
             * @throws org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException : 
         */

         
                     public org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowImpl[] listWorkflowImpls(

                        java.lang.String templateId42)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param listWorkflowImpls41
            
          */
        public void startlistWorkflowImpls(

            java.lang.String templateId42,

            final org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getWorkflowImpl45
                
             * @throws org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException : 
         */

         
                     public org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowImpl getWorkflowImpl(

                        java.lang.String templateId46,java.lang.String implementationId47)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getWorkflowImpl45
            
          */
        public void startgetWorkflowImpl(

            java.lang.String templateId46,java.lang.String implementationId47,

            final org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param listWorkflows50
                
             * @throws org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException : 
         */

         
                     public org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowWizard[] listWorkflows(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param listWorkflows50
            
          */
        public void startlistWorkflows(

            

            final org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param listAllAssociations53
                
             * @throws org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException : 
         */

         
                     public org.wso2.carbon.identity.workflow.mgt.stub.metadata.Association[] listAllAssociations(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param listAllAssociations53
            
          */
        public void startlistAllAssociations(

            

            final org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getWorkflowsOfRequest56
                
             * @throws org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException : 
         */

         
                     public org.wso2.carbon.identity.workflow.mgt.stub.bean.WorkflowRequestAssociation[] getWorkflowsOfRequest(

                        java.lang.String requestId57)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getWorkflowsOfRequest56
            
          */
        public void startgetWorkflowsOfRequest(

            java.lang.String requestId57,

            final org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException : 
         */
        public void  addAssociation(
         java.lang.String associationName61,java.lang.String workflowId62,java.lang.String eventId63,java.lang.String condition64

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException;

        

        /**
          * Auto generated method signature
          * 
                    * @param listAssociations65
                
             * @throws org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException : 
         */

         
                     public org.wso2.carbon.identity.workflow.mgt.stub.metadata.Association[] listAssociations(

                        java.lang.String workflowId66)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param listAssociations65
            
          */
        public void startlistAssociations(

            java.lang.String workflowId66,

            final org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException : 
         */
        public void  addWorkflow(
         org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowWizard workflow70

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException;

        

        /**
          * Auto generated method signature
          * 
                    * @param listTemplates71
                
             * @throws org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException : 
         */

         
                     public org.wso2.carbon.identity.workflow.mgt.stub.metadata.Template[] listTemplates(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param listTemplates71
            
          */
        public void startlistTemplates(

            

            final org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getRequestsCreatedByUser74
                
             * @throws org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException : 
         */

         
                     public org.wso2.carbon.identity.workflow.mgt.stub.bean.WorkflowRequest[] getRequestsCreatedByUser(

                        java.lang.String user75,java.lang.String beginDate76,java.lang.String endDate77,java.lang.String dateCategory78,java.lang.String status79)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getRequestsCreatedByUser74
            
          */
        public void startgetRequestsCreatedByUser(

            java.lang.String user75,java.lang.String beginDate76,java.lang.String endDate77,java.lang.String dateCategory78,java.lang.String status79,

            final org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getEvent82
                
         */

         
                     public org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowEvent getEvent(

                        java.lang.String eventId83)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getEvent82
            
          */
        public void startgetEvent(

            java.lang.String eventId83,

            final org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException : 
         */
        public void  deleteWorkflowRequest(
         java.lang.String requestId87

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getRequestsInFilter88
                
             * @throws org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException : 
         */

         
                     public org.wso2.carbon.identity.workflow.mgt.stub.bean.WorkflowRequest[] getRequestsInFilter(

                        java.lang.String beginDate89,java.lang.String endDate90,java.lang.String dateCategory91,java.lang.String status92)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getRequestsInFilter88
            
          */
        public void startgetRequestsInFilter(

            java.lang.String beginDate89,java.lang.String endDate90,java.lang.String dateCategory91,java.lang.String status92,

            final org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    