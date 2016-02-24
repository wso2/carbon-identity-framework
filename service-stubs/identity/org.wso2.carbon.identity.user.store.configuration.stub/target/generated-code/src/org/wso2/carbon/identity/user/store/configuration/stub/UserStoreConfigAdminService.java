

/**
 * UserStoreConfigAdminService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.user.store.configuration.stub;

    /*
     *  UserStoreConfigAdminService java interface
     */

    public interface UserStoreConfigAdminService {
          

        /**
          * Auto generated method signature
          * 
                    * @param getUserStoreManagerProperties6
                
             * @throws org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceIdentityUserStoreMgtException : 
         */

         
                     public org.wso2.carbon.identity.user.store.configuration.stub.api.Properties getUserStoreManagerProperties(

                        java.lang.String className7)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceIdentityUserStoreMgtException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getUserStoreManagerProperties6
            
          */
        public void startgetUserStoreManagerProperties(

            java.lang.String className7,

            final org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param testRDBMSConnection10
                
             * @throws org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceIdentityUserStoreMgtException : 
         */

         
                     public boolean testRDBMSConnection(

                        java.lang.String domainName11,java.lang.String driverName12,java.lang.String connectionURL13,java.lang.String username14,java.lang.String connectionPassword15,java.lang.String messageID16)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceIdentityUserStoreMgtException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param testRDBMSConnection10
            
          */
        public void starttestRDBMSConnection(

            java.lang.String domainName11,java.lang.String driverName12,java.lang.String connectionURL13,java.lang.String username14,java.lang.String connectionPassword15,java.lang.String messageID16,

            final org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceIdentityUserStoreMgtException : 
         */
        public void  addUserStore(
         org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO userStoreDTO20

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceIdentityUserStoreMgtException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getSecondaryRealmConfigurations21
                
             * @throws org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceIdentityUserStoreMgtException : 
         */

         
                     public org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO[] getSecondaryRealmConfigurations(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceIdentityUserStoreMgtException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getSecondaryRealmConfigurations21
            
          */
        public void startgetSecondaryRealmConfigurations(

            

            final org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAvailableUserStoreClasses24
                
             * @throws org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceIdentityUserStoreMgtException : 
         */

         
                     public java.lang.String[] getAvailableUserStoreClasses(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceIdentityUserStoreMgtException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAvailableUserStoreClasses24
            
          */
        public void startgetAvailableUserStoreClasses(

            

            final org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceIdentityUserStoreMgtException : 
         */
        public void  editUserStore(
         org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO userStoreDTO28

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceIdentityUserStoreMgtException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceIdentityUserStoreMgtException : 
         */
        public void  deleteUserStore(
         java.lang.String domainName30

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceIdentityUserStoreMgtException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceIdentityUserStoreMgtException : 
         */
        public void  editUserStoreWithDomainName(
         java.lang.String previousDomainName32,org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO userStoreDTO33

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceIdentityUserStoreMgtException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceIdentityUserStoreMgtException : 
         */
        public void  changeUserStoreState(
         java.lang.String domain35,boolean isDisable36

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceIdentityUserStoreMgtException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceIdentityUserStoreMgtException : 
         */
        public void  deleteUserStoresSet(
         java.lang.String[] domains38

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.user.store.configuration.stub.UserStoreConfigAdminServiceIdentityUserStoreMgtException;

        

        
       //
       }
    