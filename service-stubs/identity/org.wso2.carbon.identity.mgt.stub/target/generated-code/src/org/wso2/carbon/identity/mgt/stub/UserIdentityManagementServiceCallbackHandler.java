
/**
 * UserIdentityManagementServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.mgt.stub;

    /**
     *  UserIdentityManagementServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class UserIdentityManagementServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public UserIdentityManagementServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public UserIdentityManagementServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getPrimarySecurityQuestions method
            * override this method for handling normal response from getPrimarySecurityQuestions operation
            */
           public void receiveResultgetPrimarySecurityQuestions(
                    java.lang.String[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getPrimarySecurityQuestions operation
           */
            public void receiveErrorgetPrimarySecurityQuestions(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for processPasswordRecovery method
            * override this method for handling normal response from processPasswordRecovery operation
            */
           public void receiveResultprocessPasswordRecovery(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from processPasswordRecovery operation
           */
            public void receiveErrorprocessPasswordRecovery(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for authenticateWithTemporaryCredentials method
            * override this method for handling normal response from authenticateWithTemporaryCredentials operation
            */
           public void receiveResultauthenticateWithTemporaryCredentials(
                    org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from authenticateWithTemporaryCredentials operation
           */
            public void receiveErrorauthenticateWithTemporaryCredentials(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for confirmUserAccount method
            * override this method for handling normal response from confirmUserAccount operation
            */
           public void receiveResultconfirmUserAccount(
                    org.wso2.carbon.identity.mgt.stub.beans.VerificationBean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from confirmUserAccount operation
           */
            public void receiveErrorconfirmUserAccount(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for verifyChallengeQuestion method
            * override this method for handling normal response from verifyChallengeQuestion operation
            */
           public void receiveResultverifyChallengeQuestion(
                    org.wso2.carbon.identity.mgt.stub.beans.VerificationBean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from verifyChallengeQuestion operation
           */
            public void receiveErrorverifyChallengeQuestion(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for confirmUserRegistration method
            * override this method for handling normal response from confirmUserRegistration operation
            */
           public void receiveResultconfirmUserRegistration(
                    org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from confirmUserRegistration operation
           */
            public void receiveErrorconfirmUserRegistration(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getChallengeQuestionsForUser method
            * override this method for handling normal response from getChallengeQuestionsForUser operation
            */
           public void receiveResultgetChallengeQuestionsForUser(
                    org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getChallengeQuestionsForUser operation
           */
            public void receiveErrorgetChallengeQuestionsForUser(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for updateCredential method
            * override this method for handling normal response from updateCredential operation
            */
           public void receiveResultupdateCredential(
                    org.wso2.carbon.identity.mgt.stub.beans.VerificationBean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from updateCredential operation
           */
            public void receiveErrorupdateCredential(java.lang.Exception e) {
            }
                


    }
    