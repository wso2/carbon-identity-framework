
/**
 * UserProfileMgtServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.user.profile.stub;

    /**
     *  UserProfileMgtServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class UserProfileMgtServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public UserProfileMgtServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public UserProfileMgtServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for getUserProfiles method
            * override this method for handling normal response from getUserProfiles operation
            */
           public void receiveResultgetUserProfiles(
                    org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserProfiles operation
           */
            public void receiveErrorgetUserProfiles(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getUserProfile method
            * override this method for handling normal response from getUserProfile operation
            */
           public void receiveResultgetUserProfile(
                    org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserProfile operation
           */
            public void receiveErrorgetUserProfile(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for isAddProfileEnabled method
            * override this method for handling normal response from isAddProfileEnabled operation
            */
           public void receiveResultisAddProfileEnabled(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from isAddProfileEnabled operation
           */
            public void receiveErrorisAddProfileEnabled(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAssociatedIDs method
            * override this method for handling normal response from getAssociatedIDs operation
            */
           public void receiveResultgetAssociatedIDs(
                    org.wso2.carbon.identity.user.profile.stub.types.AssociatedAccountDTO[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAssociatedIDs operation
           */
            public void receiveErrorgetAssociatedIDs(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for isAddProfileEnabledForDomain method
            * override this method for handling normal response from isAddProfileEnabledForDomain operation
            */
           public void receiveResultisAddProfileEnabledForDomain(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from isAddProfileEnabledForDomain operation
           */
            public void receiveErrorisAddProfileEnabledForDomain(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getProfileFieldsForInternalStore method
            * override this method for handling normal response from getProfileFieldsForInternalStore operation
            */
           public void receiveResultgetProfileFieldsForInternalStore(
                    org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getProfileFieldsForInternalStore operation
           */
            public void receiveErrorgetProfileFieldsForInternalStore(java.lang.Exception e) {
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
                
           /**
            * auto generated Axis2 call back method for getNameAssociatedWith method
            * override this method for handling normal response from getNameAssociatedWith operation
            */
           public void receiveResultgetNameAssociatedWith(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getNameAssociatedWith operation
           */
            public void receiveErrorgetNameAssociatedWith(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getInstance method
            * override this method for handling normal response from getInstance operation
            */
           public void receiveResultgetInstance(
                    org.wso2.carbon.identity.user.profile.stub.types.UserProfileAdmin result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getInstance operation
           */
            public void receiveErrorgetInstance(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                


    }
    