

/**
 * HumanTaskClientAPIAdmin.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.humantask.stub.ui.task.client.api;

    /*
     *  HumanTaskClientAPIAdmin java interface
     */

    public interface HumanTaskClientAPIAdmin {
          

        /**
          * Auto generated method signature
          * 
                    * @param batchStop135
                
         */

         
                     public org.wso2.carbon.humantask.stub.api.TBatchResponse[] batchStop(

                        org.apache.axis2.databinding.types.URI[] identifier136)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param batchStop135
            
          */
        public void startbatchStop(

            org.apache.axis2.databinding.types.URI[] identifier136,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getMyTaskAbstracts139
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
         */

         
                     public org.wso2.carbon.humantask.stub.types.TTaskAbstract[] getMyTaskAbstracts(

                        java.lang.String taskType140,java.lang.String genericHumanRole141,java.lang.String workQueue142,org.wso2.carbon.humantask.stub.types.TStatus[] status143,java.lang.String whereClause144,java.lang.String orderByClause145,java.lang.String createdOnClause146,int maxTasks147,int taskIndexOffset148)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getMyTaskAbstracts139
            
          */
        public void startgetMyTaskAbstracts(

            java.lang.String taskType140,java.lang.String genericHumanRole141,java.lang.String workQueue142,org.wso2.carbon.humantask.stub.types.TStatus[] status143,java.lang.String whereClause144,java.lang.String orderByClause145,java.lang.String createdOnClause146,int maxTasks147,int taskIndexOffset148,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param stop151
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void stop(

                        org.apache.axis2.databinding.types.URI identifier152)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param stop151
            
          */
        public void startstop(

            org.apache.axis2.databinding.types.URI identifier152,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param batchComplete154
                
         */

         
                     public org.wso2.carbon.humantask.stub.api.TBatchResponse[] batchComplete(

                        org.apache.axis2.databinding.types.URI[] identifier155,java.lang.Object taskData156)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param batchComplete154
            
          */
        public void startbatchComplete(

            org.apache.axis2.databinding.types.URI[] identifier155,java.lang.Object taskData156,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param resume159
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void resume(

                        org.apache.axis2.databinding.types.URI identifier160)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param resume159
            
          */
        public void startresume(

            org.apache.axis2.databinding.types.URI identifier160,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getRenderingTypes162
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
         */

         
                     public javax.xml.namespace.QName[] getRenderingTypes(

                        org.apache.axis2.databinding.types.URI identifier163)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getRenderingTypes162
            
          */
        public void startgetRenderingTypes(

            org.apache.axis2.databinding.types.URI identifier163,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param setTaskCompletionDeadlineExpression166
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void setTaskCompletionDeadlineExpression(

                        org.apache.axis2.databinding.types.URI identifier167,org.apache.axis2.databinding.types.NCName deadlineName168,java.lang.String deadlineExpression169)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param setTaskCompletionDeadlineExpression166
            
          */
        public void startsetTaskCompletionDeadlineExpression(

            org.apache.axis2.databinding.types.URI identifier167,org.apache.axis2.databinding.types.NCName deadlineName168,java.lang.String deadlineExpression169,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param setOutput171
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void setOutput(

                        org.apache.axis2.databinding.types.URI identifier172,org.apache.axis2.databinding.types.NCName part173,java.lang.Object taskData174)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param setOutput171
            
          */
        public void startsetOutput(

            org.apache.axis2.databinding.types.URI identifier172,org.apache.axis2.databinding.types.NCName part173,java.lang.Object taskData174,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getTaskOperations176
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
         */

         
                     public org.wso2.carbon.humantask.stub.types.TTaskOperations getTaskOperations(

                        org.apache.axis2.databinding.types.URI identifier177)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getTaskOperations176
            
          */
        public void startgetTaskOperations(

            org.apache.axis2.databinding.types.URI identifier177,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param batchRelease180
                
         */

         
                     public org.wso2.carbon.humantask.stub.api.TBatchResponse[] batchRelease(

                        org.apache.axis2.databinding.types.URI[] identifier181)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param batchRelease180
            
          */
        public void startbatchRelease(

            org.apache.axis2.databinding.types.URI[] identifier181,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getTaskDetails184
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
         */

         
                     public org.wso2.carbon.humantask.stub.types.TTaskDetails getTaskDetails(

                        org.apache.axis2.databinding.types.URI identifier185)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getTaskDetails184
            
          */
        public void startgetTaskDetails(

            org.apache.axis2.databinding.types.URI identifier185,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param forward188
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void forward(

                        org.apache.axis2.databinding.types.URI identifier189,org.wso2.carbon.humantask.stub.types.TOrganizationalEntity organizationalEntity190)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param forward188
            
          */
        public void startforward(

            org.apache.axis2.databinding.types.URI identifier189,org.wso2.carbon.humantask.stub.types.TOrganizationalEntity organizationalEntity190,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param suspend192
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void suspend(

                        org.apache.axis2.databinding.types.URI identifier193)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param suspend192
            
          */
        public void startsuspend(

            org.apache.axis2.databinding.types.URI identifier193,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAssignableUserList195
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
         */

         
                     public org.wso2.carbon.humantask.stub.types.TUser[] getAssignableUserList(

                        org.apache.axis2.databinding.types.URI identifier196)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAssignableUserList195
            
          */
        public void startgetAssignableUserList(

            org.apache.axis2.databinding.types.URI identifier196,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param isSubtask199
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public boolean isSubtask(

                        org.apache.axis2.databinding.types.URI taskIdentifier200)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param isSubtask199
            
          */
        public void startisSubtask(

            org.apache.axis2.databinding.types.URI taskIdentifier200,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param updateComment203
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void updateComment(

                        org.apache.axis2.databinding.types.URI taskIdentifier204,org.apache.axis2.databinding.types.URI commentIdentifier205,java.lang.String text206)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param updateComment203
            
          */
        public void startupdateComment(

            org.apache.axis2.databinding.types.URI taskIdentifier204,org.apache.axis2.databinding.types.URI commentIdentifier205,java.lang.String text206,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getMyTaskDetails208
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
         */

         
                     public org.wso2.carbon.humantask.stub.types.TTaskDetails[] getMyTaskDetails(

                        java.lang.String taskType209,java.lang.String genericHumanRole210,java.lang.String workQueue211,org.wso2.carbon.humantask.stub.types.TStatus[] status212,java.lang.String whereClause213,java.lang.String orderByClause214,java.lang.String createdOnClause215,int maxTasks216,int taskIndexOffset217)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getMyTaskDetails208
            
          */
        public void startgetMyTaskDetails(

            java.lang.String taskType209,java.lang.String genericHumanRole210,java.lang.String workQueue211,org.wso2.carbon.humantask.stub.types.TStatus[] status212,java.lang.String whereClause213,java.lang.String orderByClause214,java.lang.String createdOnClause215,int maxTasks216,int taskIndexOffset217,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param batchNominate220
                
         */

         
                     public org.wso2.carbon.humantask.stub.api.TBatchResponse[] batchNominate(

                        org.apache.axis2.databinding.types.URI[] identifier221)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param batchNominate220
            
          */
        public void startbatchNominate(

            org.apache.axis2.databinding.types.URI[] identifier221,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param loadTask224
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public org.wso2.carbon.humantask.stub.types.TTaskAbstract loadTask(

                        org.apache.axis2.databinding.types.URI identifier225)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param loadTask224
            
          */
        public void startloadTask(

            org.apache.axis2.databinding.types.URI identifier225,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getSubtaskIdentifiers228
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public org.apache.axis2.databinding.types.URI[] getSubtaskIdentifiers(

                        org.apache.axis2.databinding.types.URI taskIdentifier229)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getSubtaskIdentifiers228
            
          */
        public void startgetSubtaskIdentifiers(

            org.apache.axis2.databinding.types.URI taskIdentifier229,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getOutcome232
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
         */

         
                     public java.lang.String getOutcome(

                        org.apache.axis2.databinding.types.URI identifier233)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getOutcome232
            
          */
        public void startgetOutcome(

            org.apache.axis2.databinding.types.URI identifier233,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getRendering236
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
         */

         
                     public java.lang.Object getRendering(

                        org.apache.axis2.databinding.types.URI identifier237,javax.xml.namespace.QName renderingType238)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getRendering236
            
          */
        public void startgetRendering(

            org.apache.axis2.databinding.types.URI identifier237,javax.xml.namespace.QName renderingType238,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param simpleQuery241
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
         */

         
                     public org.wso2.carbon.humantask.stub.types.TTaskSimpleQueryResultSet simpleQuery(

                        org.wso2.carbon.humantask.stub.types.TSimpleQueryInput simpleQueryInput242)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param simpleQuery241
            
          */
        public void startsimpleQuery(

            org.wso2.carbon.humantask.stub.types.TSimpleQueryInput simpleQueryInput242,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param skip245
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void skip(

                        org.apache.axis2.databinding.types.URI identifier246)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param skip245
            
          */
        public void startskip(

            org.apache.axis2.databinding.types.URI identifier246,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param batchFail248
                
         */

         
                     public org.wso2.carbon.humantask.stub.api.TBatchResponse[] batchFail(

                        org.apache.axis2.databinding.types.URI[] identifier249,org.wso2.carbon.humantask.stub.types.TFault fault250)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param batchFail248
            
          */
        public void startbatchFail(

            org.apache.axis2.databinding.types.URI[] identifier249,org.wso2.carbon.humantask.stub.types.TFault fault250,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param setTaskCompletionDurationExpression253
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void setTaskCompletionDurationExpression(

                        org.apache.axis2.databinding.types.URI identifier254,org.apache.axis2.databinding.types.NCName deadlineName255,java.lang.String durationExpression256)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param setTaskCompletionDurationExpression253
            
          */
        public void startsetTaskCompletionDurationExpression(

            org.apache.axis2.databinding.types.URI identifier254,org.apache.axis2.databinding.types.NCName deadlineName255,java.lang.String durationExpression256,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param start258
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void start(

                        org.apache.axis2.databinding.types.URI identifier259)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param start258
            
          */
        public void startstart(

            org.apache.axis2.databinding.types.URI identifier259,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param fail261
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void fail(

                        org.apache.axis2.databinding.types.URI identifier262,org.wso2.carbon.humantask.stub.types.TFault fault263)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param fail261
            
          */
        public void startfail(

            org.apache.axis2.databinding.types.URI identifier262,org.wso2.carbon.humantask.stub.types.TFault fault263,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param activate265
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void activate(

                        org.apache.axis2.databinding.types.URI identifier266)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param activate265
            
          */
        public void startactivate(

            org.apache.axis2.databinding.types.URI identifier266,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param loadAuthorisationParams268
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
         */

         
                     public org.wso2.carbon.humantask.stub.types.TTaskAuthorisationParams loadAuthorisationParams(

                        org.apache.axis2.databinding.types.URI identifier269)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param loadAuthorisationParams268
            
          */
        public void startloadAuthorisationParams(

            org.apache.axis2.databinding.types.URI identifier269,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param addComment272
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public org.apache.axis2.databinding.types.URI addComment(

                        org.apache.axis2.databinding.types.URI identifier273,java.lang.String text274)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param addComment272
            
          */
        public void startaddComment(

            org.apache.axis2.databinding.types.URI identifier273,java.lang.String text274,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param deleteComment277
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void deleteComment(

                        org.apache.axis2.databinding.types.URI taskIdentifier278,org.apache.axis2.databinding.types.URI commentIdentifier279)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param deleteComment277
            
          */
        public void startdeleteComment(

            org.apache.axis2.databinding.types.URI taskIdentifier278,org.apache.axis2.databinding.types.URI commentIdentifier279,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getTaskInstanceData281
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public org.wso2.carbon.humantask.stub.types.TTaskInstanceData getTaskInstanceData(

                        org.apache.axis2.databinding.types.URI identifier282,java.lang.String properties283,org.wso2.carbon.humantask.stub.types.TRenderingTypes[] renderingPreferences284)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getTaskInstanceData281
            
          */
        public void startgetTaskInstanceData(

            org.apache.axis2.databinding.types.URI identifier282,java.lang.String properties283,org.wso2.carbon.humantask.stub.types.TRenderingTypes[] renderingPreferences284,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param delegate287
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.RecipientNotAllowedException : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void delegate(

                        org.apache.axis2.databinding.types.URI identifier288,org.wso2.carbon.humantask.stub.types.TOrganizationalEntity organizationalEntity289)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.RecipientNotAllowedException
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param delegate287
            
          */
        public void startdelegate(

            org.apache.axis2.databinding.types.URI identifier288,org.wso2.carbon.humantask.stub.types.TOrganizationalEntity organizationalEntity289,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getComments291
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public org.wso2.carbon.humantask.stub.types.TComment[] getComments(

                        org.apache.axis2.databinding.types.URI identifier292)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getComments291
            
          */
        public void startgetComments(

            org.apache.axis2.databinding.types.URI identifier292,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getParentTask295
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public org.wso2.carbon.humantask.stub.types.TTaskDetails getParentTask(

                        org.apache.axis2.databinding.types.URI taskIdentifier296)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getParentTask295
            
          */
        public void startgetParentTask(

            org.apache.axis2.databinding.types.URI taskIdentifier296,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param addAttachment299
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public boolean addAttachment(

                        org.apache.axis2.databinding.types.URI taskIdentifier300,java.lang.String name301,java.lang.String accessType302,java.lang.String contentType303,java.lang.Object attachment304)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param addAttachment299
            
          */
        public void startaddAttachment(

            org.apache.axis2.databinding.types.URI taskIdentifier300,java.lang.String name301,java.lang.String accessType302,java.lang.String contentType303,java.lang.Object attachment304,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param batchResume307
                
         */

         
                     public org.wso2.carbon.humantask.stub.api.TBatchResponse[] batchResume(

                        org.apache.axis2.databinding.types.URI[] identifier308)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param batchResume307
            
          */
        public void startbatchResume(

            org.apache.axis2.databinding.types.URI[] identifier308,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param batchRemove311
                
         */

         
                     public org.wso2.carbon.humantask.stub.api.TBatchResponse[] batchRemove(

                        org.apache.axis2.databinding.types.URI[] identifier312)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param batchRemove311
            
          */
        public void startbatchRemove(

            org.apache.axis2.databinding.types.URI[] identifier312,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAttachment315
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public org.wso2.carbon.humantask.stub.types.TAttachment[] getAttachment(

                        org.apache.axis2.databinding.types.URI taskIdentifier316,org.apache.axis2.databinding.types.URI attachmentIdentifier317)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAttachment315
            
          */
        public void startgetAttachment(

            org.apache.axis2.databinding.types.URI taskIdentifier316,org.apache.axis2.databinding.types.URI attachmentIdentifier317,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAttachmentInfos320
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public org.wso2.carbon.humantask.stub.types.TAttachmentInfo[] getAttachmentInfos(

                        org.apache.axis2.databinding.types.URI identifier321)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAttachmentInfos320
            
          */
        public void startgetAttachmentInfos(

            org.apache.axis2.databinding.types.URI identifier321,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param remove324
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void remove(

                        org.apache.axis2.databinding.types.URI identifier325)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param remove324
            
          */
        public void startremove(

            org.apache.axis2.databinding.types.URI identifier325,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param batchStart327
                
         */

         
                     public org.wso2.carbon.humantask.stub.api.TBatchResponse[] batchStart(

                        org.apache.axis2.databinding.types.URI[] identifier328)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param batchStart327
            
          */
        public void startbatchStart(

            org.apache.axis2.databinding.types.URI[] identifier328,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param instantiateSubtask331
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public org.apache.axis2.databinding.types.URI instantiateSubtask(

                        org.apache.axis2.databinding.types.URI taskIdentifier332,java.lang.String name333)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param instantiateSubtask331
            
          */
        public void startinstantiateSubtask(

            org.apache.axis2.databinding.types.URI taskIdentifier332,java.lang.String name333,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getTaskHistory336
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public org.wso2.carbon.humantask.stub.types.TTaskEventType[] getTaskHistory(

                        org.apache.axis2.databinding.types.URI identifier337,org.wso2.carbon.humantask.stub.types.TTaskHistoryFilter filter338,int startIndex339,int maxTasks340,boolean includeData341)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getTaskHistory336
            
          */
        public void startgetTaskHistory(

            org.apache.axis2.databinding.types.URI identifier337,org.wso2.carbon.humantask.stub.types.TTaskHistoryFilter filter338,int startIndex339,int maxTasks340,boolean includeData341,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param setTaskStartDeadlineExpression344
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void setTaskStartDeadlineExpression(

                        org.apache.axis2.databinding.types.URI identifier345,org.apache.axis2.databinding.types.NCName deadlineName346,java.lang.String deadlineExpression347)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param setTaskStartDeadlineExpression344
            
          */
        public void startsetTaskStartDeadlineExpression(

            org.apache.axis2.databinding.types.URI identifier345,org.apache.axis2.databinding.types.NCName deadlineName346,java.lang.String deadlineExpression347,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param loadTaskEvents349
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
         */

         
                     public org.wso2.carbon.humantask.stub.types.TTaskEvents loadTaskEvents(

                        org.apache.axis2.databinding.types.URI identifier350)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param loadTaskEvents349
            
          */
        public void startloadTaskEvents(

            org.apache.axis2.databinding.types.URI identifier350,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param batchSkip353
                
         */

         
                     public org.wso2.carbon.humantask.stub.api.TBatchResponse[] batchSkip(

                        org.apache.axis2.databinding.types.URI[] identifier354)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param batchSkip353
            
          */
        public void startbatchSkip(

            org.apache.axis2.databinding.types.URI[] identifier354,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param complete357
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void complete(

                        org.apache.axis2.databinding.types.URI identifier358,java.lang.String taskData359)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param complete357
            
          */
        public void startcomplete(

            org.apache.axis2.databinding.types.URI identifier358,java.lang.String taskData359,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param batchDelegate361
                
         */

         
                     public org.wso2.carbon.humantask.stub.api.TBatchResponse[] batchDelegate(

                        org.apache.axis2.databinding.types.URI[] identifier362,org.wso2.carbon.humantask.stub.types.TOrganizationalEntity organizationalEntity363)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param batchDelegate361
            
          */
        public void startbatchDelegate(

            org.apache.axis2.databinding.types.URI[] identifier362,org.wso2.carbon.humantask.stub.types.TOrganizationalEntity organizationalEntity363,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param batchSetGenericHumanRole366
                
         */

         
                     public org.wso2.carbon.humantask.stub.api.TBatchResponse[] batchSetGenericHumanRole(

                        org.apache.axis2.databinding.types.URI[] identifier367,java.lang.String genericHumanRole368,org.wso2.carbon.humantask.stub.types.TOrganizationalEntity organizationalEntity369)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param batchSetGenericHumanRole366
            
          */
        public void startbatchSetGenericHumanRole(

            org.apache.axis2.databinding.types.URI[] identifier367,java.lang.String genericHumanRole368,org.wso2.carbon.humantask.stub.types.TOrganizationalEntity organizationalEntity369,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param setGenericHumanRole372
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void setGenericHumanRole(

                        org.apache.axis2.databinding.types.URI identifier373,java.lang.String genericHumanRole374,org.wso2.carbon.humantask.stub.types.TOrganizationalEntity organizationalEntity375)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param setGenericHumanRole372
            
          */
        public void startsetGenericHumanRole(

            org.apache.axis2.databinding.types.URI identifier373,java.lang.String genericHumanRole374,org.wso2.carbon.humantask.stub.types.TOrganizationalEntity organizationalEntity375,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getInput377
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public java.lang.Object getInput(

                        org.apache.axis2.databinding.types.URI identifier378,org.apache.axis2.databinding.types.NCName part379)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getInput377
            
          */
        public void startgetInput(

            org.apache.axis2.databinding.types.URI identifier378,org.apache.axis2.databinding.types.NCName part379,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param hasSubtasks382
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public boolean hasSubtasks(

                        org.apache.axis2.databinding.types.URI taskIdentifier383)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param hasSubtasks382
            
          */
        public void starthasSubtasks(

            org.apache.axis2.databinding.types.URI taskIdentifier383,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param batchActivate386
                
         */

         
                     public org.wso2.carbon.humantask.stub.api.TBatchResponse[] batchActivate(

                        org.apache.axis2.databinding.types.URI[] identifier387)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param batchActivate386
            
          */
        public void startbatchActivate(

            org.apache.axis2.databinding.types.URI[] identifier387,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param claim390
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void claim(

                        org.apache.axis2.databinding.types.URI identifier391)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param claim390
            
          */
        public void startclaim(

            org.apache.axis2.databinding.types.URI identifier391,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param query393
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
         */

         
                     public org.wso2.carbon.humantask.stub.types.TTaskQueryResultSet query(

                        java.lang.String selectClause394,java.lang.String whereClause395,java.lang.String orderByClause396,int maxTasks397,int taskIndexOffset398)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param query393
            
          */
        public void startquery(

            java.lang.String selectClause394,java.lang.String whereClause395,java.lang.String orderByClause396,int maxTasks397,int taskIndexOffset398,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param batchClaim401
                
         */

         
                     public org.wso2.carbon.humantask.stub.api.TBatchResponse[] batchClaim(

                        org.apache.axis2.databinding.types.URI[] identifier402)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param batchClaim401
            
          */
        public void startbatchClaim(

            org.apache.axis2.databinding.types.URI[] identifier402,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param batchSetPriority405
                
         */

         
                     public org.wso2.carbon.humantask.stub.api.TBatchResponse[] batchSetPriority(

                        org.apache.axis2.databinding.types.URI[] identifier406,org.wso2.carbon.humantask.stub.types.TPriority priority407)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param batchSetPriority405
            
          */
        public void startbatchSetPriority(

            org.apache.axis2.databinding.types.URI[] identifier406,org.wso2.carbon.humantask.stub.types.TPriority priority407,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param setFault410
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void setFault(

                        org.apache.axis2.databinding.types.URI identifier411,org.wso2.carbon.humantask.stub.types.TFault fault412)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param setFault410
            
          */
        public void startsetFault(

            org.apache.axis2.databinding.types.URI identifier411,org.wso2.carbon.humantask.stub.types.TFault fault412,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param suspendUntil414
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void suspendUntil(

                        org.apache.axis2.databinding.types.URI identifier415,org.wso2.carbon.humantask.stub.types.TTime time416)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param suspendUntil414
            
          */
        public void startsuspendUntil(

            org.apache.axis2.databinding.types.URI identifier415,org.wso2.carbon.humantask.stub.types.TTime time416,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param setTaskStartDurationExpression418
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void setTaskStartDurationExpression(

                        org.apache.axis2.databinding.types.URI identifier419,org.apache.axis2.databinding.types.NCName deadlineName420,java.lang.String durationExpression421)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param setTaskStartDurationExpression418
            
          */
        public void startsetTaskStartDurationExpression(

            org.apache.axis2.databinding.types.URI identifier419,org.apache.axis2.databinding.types.NCName deadlineName420,java.lang.String durationExpression421,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getTaskDescription423
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
         */

         
                     public java.lang.String getTaskDescription(

                        org.apache.axis2.databinding.types.URI identifier424,java.lang.String contentType425)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getTaskDescription423
            
          */
        public void startgetTaskDescription(

            org.apache.axis2.databinding.types.URI identifier424,java.lang.String contentType425,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param deleteAttachment428
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void deleteAttachment(

                        org.apache.axis2.databinding.types.URI taskIdentifier429,org.apache.axis2.databinding.types.URI attachmentIdentifier430)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param deleteAttachment428
            
          */
        public void startdeleteAttachment(

            org.apache.axis2.databinding.types.URI taskIdentifier429,org.apache.axis2.databinding.types.URI attachmentIdentifier430,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param nominate432
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void nominate(

                        org.apache.axis2.databinding.types.URI identifier433,org.wso2.carbon.humantask.stub.types.TOrganizationalEntity organizationalEntity434)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param nominate432
            
          */
        public void startnominate(

            org.apache.axis2.databinding.types.URI identifier433,org.wso2.carbon.humantask.stub.types.TOrganizationalEntity organizationalEntity434,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param deleteOutput436
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void deleteOutput(

                        org.apache.axis2.databinding.types.URI identifier437)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param deleteOutput436
            
          */
        public void startdeleteOutput(

            org.apache.axis2.databinding.types.URI identifier437,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param batchForward439
                
         */

         
                     public org.wso2.carbon.humantask.stub.api.TBatchResponse[] batchForward(

                        org.apache.axis2.databinding.types.URI[] identifier440,org.wso2.carbon.humantask.stub.types.TOrganizationalEntity organizationalEntity441)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param batchForward439
            
          */
        public void startbatchForward(

            org.apache.axis2.databinding.types.URI[] identifier440,org.wso2.carbon.humantask.stub.types.TOrganizationalEntity organizationalEntity441,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param batchSuspend444
                
         */

         
                     public org.wso2.carbon.humantask.stub.api.TBatchResponse[] batchSuspend(

                        org.apache.axis2.databinding.types.URI[] identifier445)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param batchSuspend444
            
          */
        public void startbatchSuspend(

            org.apache.axis2.databinding.types.URI[] identifier445,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getSubtasks448
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public org.wso2.carbon.humantask.stub.types.TTaskDetails[] getSubtasks(

                        org.apache.axis2.databinding.types.URI taskIdentifier449)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getSubtasks448
            
          */
        public void startgetSubtasks(

            org.apache.axis2.databinding.types.URI taskIdentifier449,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param deleteFault452
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void deleteFault(

                        org.apache.axis2.databinding.types.URI identifier453)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param deleteFault452
            
          */
        public void startdeleteFault(

            org.apache.axis2.databinding.types.URI identifier453,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getOutput455
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public java.lang.Object getOutput(

                        org.apache.axis2.databinding.types.URI identifier456,org.apache.axis2.databinding.types.NCName part457)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getOutput455
            
          */
        public void startgetOutput(

            org.apache.axis2.databinding.types.URI identifier456,org.apache.axis2.databinding.types.NCName part457,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param release460
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void release(

                        org.apache.axis2.databinding.types.URI identifier461)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param release460
            
          */
        public void startrelease(

            org.apache.axis2.databinding.types.URI identifier461,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getFault463
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public org.wso2.carbon.humantask.stub.types.TFault getFault(

                        org.apache.axis2.databinding.types.URI identifier464)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getFault463
            
          */
        public void startgetFault(

            org.apache.axis2.databinding.types.URI identifier464,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param setPriority467
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public void setPriority(

                        org.apache.axis2.databinding.types.URI identifier468,org.wso2.carbon.humantask.stub.types.TPriority priority469)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param setPriority467
            
          */
        public void startsetPriority(

            org.apache.axis2.databinding.types.URI identifier468,org.wso2.carbon.humantask.stub.types.TPriority priority469,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param batchSuspendUntil471
                
         */

         
                     public org.wso2.carbon.humantask.stub.api.TBatchResponse[] batchSuspendUntil(

                        org.apache.axis2.databinding.types.URI[] identifier472,org.wso2.carbon.humantask.stub.types.TTime time473)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param batchSuspendUntil471
            
          */
        public void startbatchSuspendUntil(

            org.apache.axis2.databinding.types.URI[] identifier472,org.wso2.carbon.humantask.stub.types.TTime time473,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getParentTaskIdentifier476
                
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault : 
             * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault : 
         */

         
                     public org.apache.axis2.databinding.types.URI getParentTaskIdentifier(

                        org.apache.axis2.databinding.types.URI taskIdentifier477)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
          ,org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getParentTaskIdentifier476
            
          */
        public void startgetParentTaskIdentifier(

            org.apache.axis2.databinding.types.URI taskIdentifier477,

            final org.wso2.carbon.humantask.stub.ui.task.client.api.HumanTaskClientAPIAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    