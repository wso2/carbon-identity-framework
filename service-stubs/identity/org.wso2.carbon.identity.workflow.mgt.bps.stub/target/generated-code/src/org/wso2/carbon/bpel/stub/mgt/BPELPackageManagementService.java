

/**
 * BPELPackageManagementService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.bpel.stub.mgt;

    /*
     *  BPELPackageManagementService java interface
     */

    public interface BPELPackageManagementService {
          

        /**
          * Auto generated method signature
          * 
                    * @param undeployBPELPackage4
                
             * @throws org.wso2.carbon.bpel.stub.mgt.PackageManagementException : 
         */

         
                     public org.wso2.carbon.bpel.stub.mgt.types.UndeployStatus_type0 undeployBPELPackage(

                        java.lang.String _package5)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.bpel.stub.mgt.PackageManagementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param undeployBPELPackage4
            
          */
        public void startundeployBPELPackage(

            java.lang.String _package5,

            final org.wso2.carbon.bpel.stub.mgt.BPELPackageManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param listDeployedPackagesPaginated8
                
             * @throws org.wso2.carbon.bpel.stub.mgt.PackageManagementException : 
         */

         
                     public org.wso2.carbon.bpel.stub.mgt.types.DeployedPackagesPaginated listDeployedPackagesPaginated(

                        int page9,java.lang.String packageSearchString10)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.bpel.stub.mgt.PackageManagementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param listDeployedPackagesPaginated8
            
          */
        public void startlistDeployedPackagesPaginated(

            int page9,java.lang.String packageSearchString10,

            final org.wso2.carbon.bpel.stub.mgt.BPELPackageManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param listProcessesInPackage14
                
             * @throws org.wso2.carbon.bpel.stub.mgt.PackageManagementException : 
         */

         
                     public org.wso2.carbon.bpel.stub.mgt.types.PackageType listProcessesInPackage(

                        java.lang.String _package15)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.bpel.stub.mgt.PackageManagementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param listProcessesInPackage14
            
          */
        public void startlistProcessesInPackage(

            java.lang.String _package15,

            final org.wso2.carbon.bpel.stub.mgt.BPELPackageManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    