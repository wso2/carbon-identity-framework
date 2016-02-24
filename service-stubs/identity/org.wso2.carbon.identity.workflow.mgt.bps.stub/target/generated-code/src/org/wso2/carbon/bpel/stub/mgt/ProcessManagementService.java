

/**
 * ProcessManagementService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.bpel.stub.mgt;

    /*
     *  ProcessManagementService java interface
     */

    public interface ProcessManagementService {
          
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.bpel.stub.mgt.ProcessManagementException : 
         */
        public void  retireProcess(
         javax.xml.namespace.QName pid10

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.bpel.stub.mgt.ProcessManagementException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getAllProcesses11
                
             * @throws org.wso2.carbon.bpel.stub.mgt.ProcessManagementException : 
         */

         
                     public java.lang.String[] getAllProcesses(

                        java.lang.String getAllProcesses12)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.bpel.stub.mgt.ProcessManagementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllProcesses11
            
          */
        public void startgetAllProcesses(

            java.lang.String getAllProcesses12,

            final org.wso2.carbon.bpel.stub.mgt.ProcessManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getPaginatedProcessListInput15
                
             * @throws org.wso2.carbon.bpel.stub.mgt.ProcessManagementException : 
         */

         
                     public org.wso2.carbon.bpel.stub.mgt.types.PaginatedProcessInfoList getPaginatedProcessList(

                        java.lang.String filter16,java.lang.String orderbyKeys17,int page18)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.bpel.stub.mgt.ProcessManagementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getPaginatedProcessListInput15
            
          */
        public void startgetPaginatedProcessList(

            java.lang.String filter16,java.lang.String orderbyKeys17,int page18,

            final org.wso2.carbon.bpel.stub.mgt.ProcessManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getProcessDeployDetails22
                
             * @throws org.wso2.carbon.bpel.stub.mgt.ProcessManagementException : 
         */

         
                     public org.wso2.carbon.bpel.stub.mgt.types.ProcessDeployDetailsList_type0 getProcessDeploymentInfo(

                        javax.xml.namespace.QName getProcessDeployDetails23)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.bpel.stub.mgt.ProcessManagementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getProcessDeployDetails22
            
          */
        public void startgetProcessDeploymentInfo(

            javax.xml.namespace.QName getProcessDeployDetails23,

            final org.wso2.carbon.bpel.stub.mgt.ProcessManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getProcessInfoIn26
                
             * @throws org.wso2.carbon.bpel.stub.mgt.ProcessManagementException : 
         */

         
                     public org.wso2.carbon.bpel.stub.mgt.types.ProcessInfoType getProcessInfo(

                        javax.xml.namespace.QName pid27)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.bpel.stub.mgt.ProcessManagementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getProcessInfoIn26
            
          */
        public void startgetProcessInfo(

            javax.xml.namespace.QName pid27,

            final org.wso2.carbon.bpel.stub.mgt.ProcessManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.bpel.stub.mgt.ProcessManagementException : 
         */
        public void  updateDeployInfo(
         org.wso2.carbon.bpel.stub.mgt.types.ProcessDeployDetailsList_type0 processDeployDetailsList40

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.bpel.stub.mgt.ProcessManagementException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.bpel.stub.mgt.ProcessManagementException : 
         */
        public void  activateProcess(
         javax.xml.namespace.QName pid42

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.bpel.stub.mgt.ProcessManagementException;

        

        
       //
       }
    