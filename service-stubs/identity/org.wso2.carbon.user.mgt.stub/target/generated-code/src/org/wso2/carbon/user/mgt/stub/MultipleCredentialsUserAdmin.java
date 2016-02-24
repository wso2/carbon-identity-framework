

/**
 * MultipleCredentialsUserAdmin.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.user.mgt.stub;

    /*
     *  MultipleCredentialsUserAdmin java interface
     */

    public interface MultipleCredentialsUserAdmin {
          
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
         */
        public void  deleteUserClaimValues(
         java.lang.String identifer49,java.lang.String credentialType50,java.lang.String[] claims51,java.lang.String profileName52

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getUserClaimValue53
                
             * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
         */

         
                     public java.lang.String getUserClaimValue(

                        java.lang.String identifer54,java.lang.String credentialType55,java.lang.String claimUri56,java.lang.String profileName57)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getUserClaimValue53
            
          */
        public void startgetUserClaimValue(

            java.lang.String identifer54,java.lang.String credentialType55,java.lang.String claimUri56,java.lang.String profileName57,

            final org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getUserClaimValues60
                
             * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
         */

         
                     public org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.ClaimValue[] getUserClaimValues(

                        java.lang.String identifer61,java.lang.String credentialType62,java.lang.String[] claims63,java.lang.String profileName64)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getUserClaimValues60
            
          */
        public void startgetUserClaimValues(

            java.lang.String identifer61,java.lang.String credentialType62,java.lang.String[] claims63,java.lang.String profileName64,

            final org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
         */
        public void  addUser(
         org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential credential68,java.lang.String[] roleList69,org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.ClaimValue[] claims70,java.lang.String profileName71

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
         */
        public void  addCredential(
         java.lang.String anIdentifier73,java.lang.String credentialType74,org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential credential75

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
         */
        public void  addUserWithUserId(
         java.lang.String userId77,org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential credential78,java.lang.String[] roleList79,org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.ClaimValue[] claims80,java.lang.String profileName81

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getCredentials82
                
             * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
         */

         
                     public org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential[] getCredentials(

                        java.lang.String anIdentifier83,java.lang.String credentialType84)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getCredentials82
            
          */
        public void startgetCredentials(

            java.lang.String anIdentifier83,java.lang.String credentialType84,

            final org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAllUserClaimValues87
                
             * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
         */

         
                     public org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.ClaimValue[] getAllUserClaimValues(

                        java.lang.String identifer88,java.lang.String credentialType89,java.lang.String profileName90)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllUserClaimValues87
            
          */
        public void startgetAllUserClaimValues(

            java.lang.String identifer88,java.lang.String credentialType89,java.lang.String profileName90,

            final org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
         */
        public void  addUsers(
         org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential[] credential94,java.lang.String[] roleList95,org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.ClaimValue[] claims96,java.lang.String profileName97

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param authenticate98
                
             * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
         */

         
                     public boolean authenticate(

                        org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential credential99)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param authenticate98
            
          */
        public void startauthenticate(

            org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential credential99,

            final org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
         */
        public void  setUserClaimValues(
         java.lang.String identifer103,java.lang.String credentialType104,org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.ClaimValue[] claims105,java.lang.String profileName106

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
         */
        public void  updateCredential(
         java.lang.String identifier108,java.lang.String credentialType109,org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential credential110

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
         */
        public void  deleteUserClaimValue(
         java.lang.String identifer112,java.lang.String credentialType113,java.lang.String claimURI114,java.lang.String profileName115

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
         */
        public void  deleteCredential(
         java.lang.String identifier117,java.lang.String credentialType118

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
         */
        public void  setUserClaimValue(
         java.lang.String identifer120,java.lang.String credentialType121,java.lang.String claimURI122,java.lang.String claimValue123,java.lang.String profileName124

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
         */
        public void  deleteUser(
         java.lang.String identifier126,java.lang.String credentialType127

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getUserId128
                
             * @throws org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException : 
         */

         
                     public java.lang.String getUserId(

                        org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential credential129)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getUserId128
            
          */
        public void startgetUserId(

            org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential credential129,

            final org.wso2.carbon.user.mgt.stub.MultipleCredentialsUserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    