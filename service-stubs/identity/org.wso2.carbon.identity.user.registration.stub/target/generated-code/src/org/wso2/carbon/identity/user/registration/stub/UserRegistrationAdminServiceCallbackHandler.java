
/**
 * UserRegistrationAdminServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.user.registration.stub;

    /**
     *  UserRegistrationAdminServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class UserRegistrationAdminServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public UserRegistrationAdminServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public UserRegistrationAdminServiceCallbackHandler(){
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
            * auto generated Axis2 call back method for isAddUserWithInfoCardEnabled method
            * override this method for handling normal response from isAddUserWithInfoCardEnabled operation
            */
           public void receiveResultisAddUserWithInfoCardEnabled(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from isAddUserWithInfoCardEnabled operation
           */
            public void receiveErrorisAddUserWithInfoCardEnabled(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getPasswordRegularExpressions method
            * override this method for handling normal response from getPasswordRegularExpressions operation
            */
           public void receiveResultgetPasswordRegularExpressions(
                    org.wso2.carbon.identity.user.registration.stub.dto.PasswordRegExDTO[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getPasswordRegularExpressions operation
           */
            public void receiveErrorgetPasswordRegularExpressions(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for isAddUserWithOpenIDEnabled method
            * override this method for handling normal response from isAddUserWithOpenIDEnabled operation
            */
           public void receiveResultisAddUserWithOpenIDEnabled(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from isAddUserWithOpenIDEnabled operation
           */
            public void receiveErrorisAddUserWithOpenIDEnabled(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for isAddUserEnabled method
            * override this method for handling normal response from isAddUserEnabled operation
            */
           public void receiveResultisAddUserEnabled(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from isAddUserEnabled operation
           */
            public void receiveErrorisAddUserEnabled(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for isUserExist method
            * override this method for handling normal response from isUserExist operation
            */
           public void receiveResultisUserExist(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from isUserExist operation
           */
            public void receiveErrorisUserExist(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for readUserFieldsForUserRegistration method
            * override this method for handling normal response from readUserFieldsForUserRegistration operation
            */
           public void receiveResultreadUserFieldsForUserRegistration(
                    org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from readUserFieldsForUserRegistration operation
           */
            public void receiveErrorreadUserFieldsForUserRegistration(java.lang.Exception e) {
            }
                


    }
    