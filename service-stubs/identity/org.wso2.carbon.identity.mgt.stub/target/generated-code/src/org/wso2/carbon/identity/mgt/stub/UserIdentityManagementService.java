

/**
 * UserIdentityManagementService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.mgt.stub;

    /*
     *  UserIdentityManagementService java interface
     */

    public interface UserIdentityManagementService {
          
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceIdentityMgtServiceExceptionException : 
         */
        public void  recoverUserIdentityWithSecurityQuestions(
         java.lang.String userName17,org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO[] secQuesAnsweres18

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceIdentityMgtServiceExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getPrimarySecurityQuestions19
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceIdentityMgtServiceExceptionException : 
         */

         
                     public java.lang.String[] getPrimarySecurityQuestions(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getPrimarySecurityQuestions19
            
          */
        public void startgetPrimarySecurityQuestions(

            

            final org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param processPasswordRecovery22
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceIdentityMgtServiceExceptionException : 
         */

         
                     public boolean processPasswordRecovery(

                        java.lang.String userId23,java.lang.String confirmationCode24,java.lang.String notificationType25)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param processPasswordRecovery22
            
          */
        public void startprocessPasswordRecovery(

            java.lang.String userId23,java.lang.String confirmationCode24,java.lang.String notificationType25,

            final org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param authenticateWithTemporaryCredentials28
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceIdentityMgtServiceExceptionException : 
         */

         
                     public org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO[] authenticateWithTemporaryCredentials(

                        java.lang.String userName29,java.lang.String tempCredential30)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param authenticateWithTemporaryCredentials28
            
          */
        public void startauthenticateWithTemporaryCredentials(

            java.lang.String userName29,java.lang.String tempCredential30,

            final org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param confirmUserAccount33
                
         */

         
                     public org.wso2.carbon.identity.mgt.stub.beans.VerificationBean confirmUserAccount(

                        java.lang.String confirmationKey34)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param confirmUserAccount33
            
          */
        public void startconfirmUserAccount(

            java.lang.String confirmationKey34,

            final org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param verifyChallengeQuestion37
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceIdentityMgtServiceExceptionException : 
         */

         
                     public org.wso2.carbon.identity.mgt.stub.beans.VerificationBean verifyChallengeQuestion(

                        java.lang.String userName38,java.lang.String confirmation39,org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO[] userChallengesDTOs40)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param verifyChallengeQuestion37
            
          */
        public void startverifyChallengeQuestion(

            java.lang.String userName38,java.lang.String confirmation39,org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO[] userChallengesDTOs40,

            final org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param confirmUserRegistration43
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceIdentityMgtServiceExceptionException : 
         */

         
                     public org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO[] confirmUserRegistration(

                        java.lang.String userName44,java.lang.String confirmationCode45)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param confirmUserRegistration43
            
          */
        public void startconfirmUserRegistration(

            java.lang.String userName44,java.lang.String confirmationCode45,

            final org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceIdentityMgtServiceExceptionException : 
         */
        public void  recoverUserIdentityWithEmail(
         java.lang.String userName49

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceIdentityMgtServiceExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getChallengeQuestionsForUser50
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceIdentityMgtServiceExceptionException : 
         */

         
                     public org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO[] getChallengeQuestionsForUser(

                        java.lang.String userName51,java.lang.String confirmation52)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getChallengeQuestionsForUser50
            
          */
        public void startgetChallengeQuestionsForUser(

            java.lang.String userName51,java.lang.String confirmation52,

            final org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param updateCredential55
                
         */

         
                     public org.wso2.carbon.identity.mgt.stub.beans.VerificationBean updateCredential(

                        java.lang.String userName56,java.lang.String confirmation57,java.lang.String password58,org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean captchaInfoBean59)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param updateCredential55
            
          */
        public void startupdateCredential(

            java.lang.String userName56,java.lang.String confirmation57,java.lang.String password58,org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean captchaInfoBean59,

            final org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    