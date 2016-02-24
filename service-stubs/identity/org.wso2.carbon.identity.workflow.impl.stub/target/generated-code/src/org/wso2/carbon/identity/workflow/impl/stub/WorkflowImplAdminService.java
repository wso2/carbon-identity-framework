

/**
 * WorkflowImplAdminService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.workflow.impl.stub;

    /*
     *  WorkflowImplAdminService java interface
     */

    public interface WorkflowImplAdminService {
          

        /**
          * Auto generated method signature
          * 
                    * @param removeBPSPackage6
                
             * @throws org.wso2.carbon.identity.workflow.impl.stub.WorkflowImplAdminServiceWorkflowImplException : 
         */

         
                     public void removeBPSPackage(

                        org.wso2.carbon.identity.workflow.mgt.bean.xsd.Workflow workflow7)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.workflow.impl.stub.WorkflowImplAdminServiceWorkflowImplException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param removeBPSPackage6
            
          */
        public void startremoveBPSPackage(

            org.wso2.carbon.identity.workflow.mgt.bean.xsd.Workflow workflow7,

            final org.wso2.carbon.identity.workflow.impl.stub.WorkflowImplAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param addBPSProfile9
                
             * @throws org.wso2.carbon.identity.workflow.impl.stub.WorkflowImplAdminServiceWorkflowImplException : 
         */

         
                     public void addBPSProfile(

                        org.wso2.carbon.identity.workflow.impl.stub.bean.BPSProfile bpsProfileDTO10)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.workflow.impl.stub.WorkflowImplAdminServiceWorkflowImplException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param addBPSProfile9
            
          */
        public void startaddBPSProfile(

            org.wso2.carbon.identity.workflow.impl.stub.bean.BPSProfile bpsProfileDTO10,

            final org.wso2.carbon.identity.workflow.impl.stub.WorkflowImplAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param removeBPSProfile12
                
             * @throws org.wso2.carbon.identity.workflow.impl.stub.WorkflowImplAdminServiceWorkflowImplException : 
         */

         
                     public void removeBPSProfile(

                        java.lang.String profileName13)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.workflow.impl.stub.WorkflowImplAdminServiceWorkflowImplException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param removeBPSProfile12
            
          */
        public void startremoveBPSProfile(

            java.lang.String profileName13,

            final org.wso2.carbon.identity.workflow.impl.stub.WorkflowImplAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param updateBPSProfile15
                
             * @throws org.wso2.carbon.identity.workflow.impl.stub.WorkflowImplAdminServiceWorkflowImplException : 
         */

         
                     public void updateBPSProfile(

                        org.wso2.carbon.identity.workflow.impl.stub.bean.BPSProfile bpsProfileDTO16)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.workflow.impl.stub.WorkflowImplAdminServiceWorkflowImplException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param updateBPSProfile15
            
          */
        public void startupdateBPSProfile(

            org.wso2.carbon.identity.workflow.impl.stub.bean.BPSProfile bpsProfileDTO16,

            final org.wso2.carbon.identity.workflow.impl.stub.WorkflowImplAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param listBPSProfiles18
                
             * @throws org.wso2.carbon.identity.workflow.impl.stub.WorkflowImplAdminServiceWorkflowImplException : 
         */

         
                     public org.wso2.carbon.identity.workflow.impl.stub.bean.BPSProfile[] listBPSProfiles(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.workflow.impl.stub.WorkflowImplAdminServiceWorkflowImplException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param listBPSProfiles18
            
          */
        public void startlistBPSProfiles(

            

            final org.wso2.carbon.identity.workflow.impl.stub.WorkflowImplAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getBPSProfile21
                
             * @throws org.wso2.carbon.identity.workflow.impl.stub.WorkflowImplAdminServiceWorkflowImplException : 
         */

         
                     public org.wso2.carbon.identity.workflow.impl.stub.bean.BPSProfile getBPSProfile(

                        java.lang.String bpsProfileName22)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.workflow.impl.stub.WorkflowImplAdminServiceWorkflowImplException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getBPSProfile21
            
          */
        public void startgetBPSProfile(

            java.lang.String bpsProfileName22,

            final org.wso2.carbon.identity.workflow.impl.stub.WorkflowImplAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    