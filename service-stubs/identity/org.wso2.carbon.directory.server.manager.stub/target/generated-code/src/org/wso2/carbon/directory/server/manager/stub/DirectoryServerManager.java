

/**
 * DirectoryServerManager.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.directory.server.manager.stub;

    /*
     *  DirectoryServerManager java interface
     */

    public interface DirectoryServerManager {
          

        /**
          * Auto generated method signature
          * 
                    * @param removeServer8
                
             * @throws org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerExceptionException : 
         */

         
                     public void removeServer(

                        java.lang.String serverName9)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param removeServer8
            
          */
        public void startremoveServer(

            java.lang.String serverName9,

            final org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param changePassword11
                
             * @throws org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerExceptionException : 
         */

         
                     public void changePassword(

                        java.lang.String serverPrinciple12,java.lang.String existingPassword13,java.lang.String newPassword14)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param changePassword11
            
          */
        public void startchangePassword(

            java.lang.String serverPrinciple12,java.lang.String existingPassword13,java.lang.String newPassword14,

            final org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getPasswordConformanceRegularExpression16
                
             * @throws org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerExceptionException : 
         */

         
                     public java.lang.String getPasswordConformanceRegularExpression(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getPasswordConformanceRegularExpression16
            
          */
        public void startgetPasswordConformanceRegularExpression(

            

            final org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param isKDCEnabled19
                
             * @throws org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerExceptionException : 
         */

         
                     public boolean isKDCEnabled(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param isKDCEnabled19
            
          */
        public void startisKDCEnabled(

            

            final org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param listServicePrinciples22
                
             * @throws org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerExceptionException : 
         */

         
                     public org.wso2.carbon.directory.common.stub.types.ServerPrinciple[] listServicePrinciples(

                        java.lang.String filter23)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param listServicePrinciples22
            
          */
        public void startlistServicePrinciples(

            java.lang.String filter23,

            final org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param isExistingServicePrinciple26
                
             * @throws org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerExceptionException : 
         */

         
                     public boolean isExistingServicePrinciple(

                        java.lang.String servicePrinciple27)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param isExistingServicePrinciple26
            
          */
        public void startisExistingServicePrinciple(

            java.lang.String servicePrinciple27,

            final org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param addServer30
                
             * @throws org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerExceptionException : 
         */

         
                     public void addServer(

                        java.lang.String serverName31,java.lang.String serverDescription32,java.lang.String serverPassword33)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param addServer30
            
          */
        public void startaddServer(

            java.lang.String serverName31,java.lang.String serverDescription32,java.lang.String serverPassword33,

            final org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getServiceNameConformanceRegularExpression35
                
             * @throws org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerExceptionException : 
         */

         
                     public java.lang.String getServiceNameConformanceRegularExpression(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getServiceNameConformanceRegularExpression35
            
          */
        public void startgetServiceNameConformanceRegularExpression(

            

            final org.wso2.carbon.directory.server.manager.stub.DirectoryServerManagerCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    