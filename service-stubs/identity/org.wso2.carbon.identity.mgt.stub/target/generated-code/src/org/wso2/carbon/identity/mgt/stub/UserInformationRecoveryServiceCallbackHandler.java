
/**
 * UserInformationRecoveryServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.mgt.stub;

    /**
     *  UserInformationRecoveryServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class UserInformationRecoveryServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public UserInformationRecoveryServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public UserInformationRecoveryServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for getAllChallengeQuestions method
            * override this method for handling normal response from getAllChallengeQuestions operation
            */
           public void receiveResultgetAllChallengeQuestions(
                    org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionDTO[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllChallengeQuestions operation
           */
            public void receiveErrorgetAllChallengeQuestions(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for registerUser method
            * override this method for handling normal response from registerUser operation
            */
           public void receiveResultregisterUser(
                    org.wso2.carbon.identity.mgt.stub.beans.VerificationBean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from registerUser operation
           */
            public void receiveErrorregisterUser(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for verifyAccount method
            * override this method for handling normal response from verifyAccount operation
            */
           public void receiveResultverifyAccount(
                    org.wso2.carbon.identity.mgt.stub.beans.VerificationBean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from verifyAccount operation
           */
            public void receiveErrorverifyAccount(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for verifyUser method
            * override this method for handling normal response from verifyUser operation
            */
           public void receiveResultverifyUser(
                    org.wso2.carbon.identity.mgt.stub.beans.VerificationBean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from verifyUser operation
           */
            public void receiveErrorverifyUser(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getUserIdentitySupportedClaims method
            * override this method for handling normal response from getUserIdentitySupportedClaims operation
            */
           public void receiveResultgetUserIdentitySupportedClaims(
                    org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserIdentitySupportedClaims operation
           */
            public void receiveErrorgetUserIdentitySupportedClaims(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for verifyUserChallengeAnswer method
            * override this method for handling normal response from verifyUserChallengeAnswer operation
            */
           public void receiveResultverifyUserChallengeAnswer(
                    org.wso2.carbon.identity.mgt.stub.beans.VerificationBean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from verifyUserChallengeAnswer operation
           */
            public void receiveErrorverifyUserChallengeAnswer(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for confirmUserSelfRegistration method
            * override this method for handling normal response from confirmUserSelfRegistration operation
            */
           public void receiveResultconfirmUserSelfRegistration(
                    org.wso2.carbon.identity.mgt.stub.beans.VerificationBean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from confirmUserSelfRegistration operation
           */
            public void receiveErrorconfirmUserSelfRegistration(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getUserChallengeQuestion method
            * override this method for handling normal response from getUserChallengeQuestion operation
            */
           public void receiveResultgetUserChallengeQuestion(
                    org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserChallengeQuestion operation
           */
            public void receiveErrorgetUserChallengeQuestion(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for updatePassword method
            * override this method for handling normal response from updatePassword operation
            */
           public void receiveResultupdatePassword(
                    org.wso2.carbon.identity.mgt.stub.beans.VerificationBean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from updatePassword operation
           */
            public void receiveErrorupdatePassword(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getUserChallengeQuestionIds method
            * override this method for handling normal response from getUserChallengeQuestionIds operation
            */
           public void receiveResultgetUserChallengeQuestionIds(
                    org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionIdsDTO result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserChallengeQuestionIds operation
           */
            public void receiveErrorgetUserChallengeQuestionIds(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for verifyConfirmationCode method
            * override this method for handling normal response from verifyConfirmationCode operation
            */
           public void receiveResultverifyConfirmationCode(
                    org.wso2.carbon.identity.mgt.stub.beans.VerificationBean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from verifyConfirmationCode operation
           */
            public void receiveErrorverifyConfirmationCode(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for sendRecoveryNotification method
            * override this method for handling normal response from sendRecoveryNotification operation
            */
           public void receiveResultsendRecoveryNotification(
                    org.wso2.carbon.identity.mgt.stub.beans.VerificationBean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from sendRecoveryNotification operation
           */
            public void receiveErrorsendRecoveryNotification(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getCaptcha method
            * override this method for handling normal response from getCaptcha operation
            */
           public void receiveResultgetCaptcha(
                    org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getCaptcha operation
           */
            public void receiveErrorgetCaptcha(java.lang.Exception e) {
            }
                


    }
    