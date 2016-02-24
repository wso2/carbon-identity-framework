
/**
 * UserIdentityManagementAdminServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.mgt.stub;

    /**
     *  UserIdentityManagementAdminServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class UserIdentityManagementAdminServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public UserIdentityManagementAdminServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public UserIdentityManagementAdminServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for getChallengeQuestionsOfUser method
            * override this method for handling normal response from getChallengeQuestionsOfUser operation
            */
           public void receiveResultgetChallengeQuestionsOfUser(
                    org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getChallengeQuestionsOfUser operation
           */
            public void receiveErrorgetChallengeQuestionsOfUser(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllPromotedUserChallenge method
            * override this method for handling normal response from getAllPromotedUserChallenge operation
            */
           public void receiveResultgetAllPromotedUserChallenge(
                    org.wso2.carbon.identity.mgt.stub.dto.UserChallengesSetDTO[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllPromotedUserChallenge operation
           */
            public void receiveErrorgetAllPromotedUserChallenge(java.lang.Exception e) {
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
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for setChallengeQuestionsOfUser method
            * override this method for handling normal response from setChallengeQuestionsOfUser operation
            */
           public void receiveResultsetChallengeQuestionsOfUser(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from setChallengeQuestionsOfUser operation
           */
            public void receiveErrorsetChallengeQuestionsOfUser(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getAllUserIdentityClaims method
            * override this method for handling normal response from getAllUserIdentityClaims operation
            */
           public void receiveResultgetAllUserIdentityClaims(
                    org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllUserIdentityClaims operation
           */
            public void receiveErrorgetAllUserIdentityClaims(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for isReadOnlyUserStore method
            * override this method for handling normal response from isReadOnlyUserStore operation
            */
           public void receiveResultisReadOnlyUserStore(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from isReadOnlyUserStore operation
           */
            public void receiveErrorisReadOnlyUserStore(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                


    }
    