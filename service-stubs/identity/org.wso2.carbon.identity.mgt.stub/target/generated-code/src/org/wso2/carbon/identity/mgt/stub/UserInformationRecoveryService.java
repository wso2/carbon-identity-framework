

/**
 * UserInformationRecoveryService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.mgt.stub;

    /*
     *  UserInformationRecoveryService java interface
     */

    public interface UserInformationRecoveryService {
          

        /**
          * Auto generated method signature
          * 
                    * @param getAllChallengeQuestions29
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException : 
         */

         
                     public org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionDTO[] getAllChallengeQuestions(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllChallengeQuestions29
            
          */
        public void startgetAllChallengeQuestions(

            

            final org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param registerUser32
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException : 
         */

         
                     public org.wso2.carbon.identity.mgt.stub.beans.VerificationBean registerUser(

                        java.lang.String userName33,java.lang.String password34,org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO[] claims35,java.lang.String profileName36,java.lang.String tenantDomain37)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param registerUser32
            
          */
        public void startregisterUser(

            java.lang.String userName33,java.lang.String password34,org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO[] claims35,java.lang.String profileName36,java.lang.String tenantDomain37,

            final org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param verifyAccount40
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException : 
         */

         
                     public org.wso2.carbon.identity.mgt.stub.beans.VerificationBean verifyAccount(

                        org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO[] claims41,org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean captcha42,java.lang.String tenantDomain43)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param verifyAccount40
            
          */
        public void startverifyAccount(

            org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO[] claims41,org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean captcha42,java.lang.String tenantDomain43,

            final org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param verifyUser46
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException : 
         */

         
                     public org.wso2.carbon.identity.mgt.stub.beans.VerificationBean verifyUser(

                        java.lang.String username47,org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean captcha48)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param verifyUser46
            
          */
        public void startverifyUser(

            java.lang.String username47,org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean captcha48,

            final org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getUserIdentitySupportedClaims51
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityExceptionException : 
         */

         
                     public org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO[] getUserIdentitySupportedClaims(

                        java.lang.String dialect52)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getUserIdentitySupportedClaims51
            
          */
        public void startgetUserIdentitySupportedClaims(

            java.lang.String dialect52,

            final org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param verifyUserChallengeAnswer55
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException : 
         */

         
                     public org.wso2.carbon.identity.mgt.stub.beans.VerificationBean verifyUserChallengeAnswer(

                        java.lang.String userName56,java.lang.String confirmation57,java.lang.String questionId58,java.lang.String answer59)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param verifyUserChallengeAnswer55
            
          */
        public void startverifyUserChallengeAnswer(

            java.lang.String userName56,java.lang.String confirmation57,java.lang.String questionId58,java.lang.String answer59,

            final org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param confirmUserSelfRegistration62
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException : 
         */

         
                     public org.wso2.carbon.identity.mgt.stub.beans.VerificationBean confirmUserSelfRegistration(

                        java.lang.String username63,java.lang.String code64,org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean captcha65,java.lang.String tenantDomain66)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param confirmUserSelfRegistration62
            
          */
        public void startconfirmUserSelfRegistration(

            java.lang.String username63,java.lang.String code64,org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean captcha65,java.lang.String tenantDomain66,

            final org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getUserChallengeQuestion69
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException : 
         */

         
                     public org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO getUserChallengeQuestion(

                        java.lang.String userName70,java.lang.String confirmation71,java.lang.String questionId72)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getUserChallengeQuestion69
            
          */
        public void startgetUserChallengeQuestion(

            java.lang.String userName70,java.lang.String confirmation71,java.lang.String questionId72,

            final org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param updatePassword75
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException : 
         */

         
                     public org.wso2.carbon.identity.mgt.stub.beans.VerificationBean updatePassword(

                        java.lang.String username76,java.lang.String confirmationCode77,java.lang.String newPassword78)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param updatePassword75
            
          */
        public void startupdatePassword(

            java.lang.String username76,java.lang.String confirmationCode77,java.lang.String newPassword78,

            final org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getUserChallengeQuestionIds81
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException : 
         */

         
                     public org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionIdsDTO getUserChallengeQuestionIds(

                        java.lang.String username82,java.lang.String confirmation83)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getUserChallengeQuestionIds81
            
          */
        public void startgetUserChallengeQuestionIds(

            java.lang.String username82,java.lang.String confirmation83,

            final org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param verifyConfirmationCode86
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException : 
         */

         
                     public org.wso2.carbon.identity.mgt.stub.beans.VerificationBean verifyConfirmationCode(

                        java.lang.String username87,java.lang.String code88,org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean captcha89)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param verifyConfirmationCode86
            
          */
        public void startverifyConfirmationCode(

            java.lang.String username87,java.lang.String code88,org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean captcha89,

            final org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param sendRecoveryNotification92
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException : 
         */

         
                     public org.wso2.carbon.identity.mgt.stub.beans.VerificationBean sendRecoveryNotification(

                        java.lang.String username93,java.lang.String key94,java.lang.String notificationType95)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param sendRecoveryNotification92
            
          */
        public void startsendRecoveryNotification(

            java.lang.String username93,java.lang.String key94,java.lang.String notificationType95,

            final org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getCaptcha98
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException : 
         */

         
                     public org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean getCaptcha(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getCaptcha98
            
          */
        public void startgetCaptcha(

            

            final org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    