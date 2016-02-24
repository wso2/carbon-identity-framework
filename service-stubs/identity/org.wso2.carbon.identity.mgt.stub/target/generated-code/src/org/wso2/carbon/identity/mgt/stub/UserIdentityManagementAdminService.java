

/**
 * UserIdentityManagementAdminService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.mgt.stub;

    /*
     *  UserIdentityManagementAdminService java interface
     */

    public interface UserIdentityManagementAdminService {
          

        /**
          * Auto generated method signature
          * 
                    * @param getChallengeQuestionsOfUser12
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException : 
         */

         
                     public org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO[] getChallengeQuestionsOfUser(

                        java.lang.String userName13)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getChallengeQuestionsOfUser12
            
          */
        public void startgetChallengeQuestionsOfUser(

            java.lang.String userName13,

            final org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAllPromotedUserChallenge16
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException : 
         */

         
                     public org.wso2.carbon.identity.mgt.stub.dto.UserChallengesSetDTO[] getAllPromotedUserChallenge(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllPromotedUserChallenge16
            
          */
        public void startgetAllPromotedUserChallenge(

            

            final org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAllChallengeQuestions19
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException : 
         */

         
                     public org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionDTO[] getAllChallengeQuestions(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllChallengeQuestions19
            
          */
        public void startgetAllChallengeQuestions(

            

            final org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException : 
         */
        public void  deleteUser(
         java.lang.String userName23

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException : 
         */
        public void  setChallengeQuestions(
         org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionDTO[] challengeQuestionDTOs25

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException : 
         */
        public void  resetUserPassword(
         java.lang.String userName27,java.lang.String newPassword28

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException : 
         */
        public void  unlockUserAccount(
         java.lang.String userName30,java.lang.String notificationType31

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param setChallengeQuestionsOfUser32
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException : 
         */

         
                     public void setChallengeQuestionsOfUser(

                        java.lang.String userName33,org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO[] challengesDTOs34)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param setChallengeQuestionsOfUser32
            
          */
        public void startsetChallengeQuestionsOfUser(

            java.lang.String userName33,org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO[] challengesDTOs34,

            final org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException : 
         */
        public void  lockUserAccount(
         java.lang.String userName37

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getAllUserIdentityClaims38
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException : 
         */

         
                     public org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO[] getAllUserIdentityClaims(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllUserIdentityClaims38
            
          */
        public void startgetAllUserIdentityClaims(

            

            final org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param isReadOnlyUserStore41
                
             * @throws org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException : 
         */

         
                     public boolean isReadOnlyUserStore(

                        java.lang.String userName42,java.lang.String tenantDomain43)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param isReadOnlyUserStore41
            
          */
        public void startisReadOnlyUserStore(

            java.lang.String userName42,java.lang.String tenantDomain43,

            final org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException : 
         */
        public void  changeUserPassword(
         java.lang.String newPassword47,java.lang.String oldPassword48

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException : 
         */
        public void  updateUserIdentityClaims(
         org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO[] userIdentityClaims50

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException;

        

        
       //
       }
    