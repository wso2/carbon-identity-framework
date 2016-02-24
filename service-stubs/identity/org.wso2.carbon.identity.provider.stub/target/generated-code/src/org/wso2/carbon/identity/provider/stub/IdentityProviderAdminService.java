

/**
 * IdentityProviderAdminService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.provider.stub;

    /*
     *  IdentityProviderAdminService java interface
     */

    public interface IdentityProviderAdminService {
          

        /**
          * Auto generated method signature
          * 
                    * @param extractPrimaryUserName4
                
             * @throws org.wso2.carbon.identity.provider.stub.IdentityProviderAdminServiceException : 
         */

         
                     public java.lang.String extractPrimaryUserName(

                        java.lang.String ppid5)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.provider.stub.IdentityProviderAdminServiceException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param extractPrimaryUserName4
            
          */
        public void startextractPrimaryUserName(

            java.lang.String ppid5,

            final org.wso2.carbon.identity.provider.stub.IdentityProviderAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
         */
        public void  removeOpenID(
         java.lang.String openID9

        ) throws java.rmi.RemoteException
        
        ;

        

        /**
          * Auto generated method signature
          * 
                    * @param getAllOpenIDs10
                
             * @throws org.wso2.carbon.identity.provider.stub.IdentityProviderAdminServiceException : 
         */

         
                     public java.lang.String[] getAllOpenIDs(

                        java.lang.String userName11)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.provider.stub.IdentityProviderAdminServiceException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllOpenIDs10
            
          */
        public void startgetAllOpenIDs(

            java.lang.String userName11,

            final org.wso2.carbon.identity.provider.stub.IdentityProviderAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getPrimaryOpenID14
                
             * @throws org.wso2.carbon.identity.provider.stub.IdentityProviderAdminServiceException : 
         */

         
                     public java.lang.String getPrimaryOpenID(

                        java.lang.String userName15)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.provider.stub.IdentityProviderAdminServiceException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getPrimaryOpenID14
            
          */
        public void startgetPrimaryOpenID(

            java.lang.String userName15,

            final org.wso2.carbon.identity.provider.stub.IdentityProviderAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
         */
        public void  addOpenID(
         java.lang.String openID19

        ) throws java.rmi.RemoteException
        
        ;

        

        
       //
       }
    