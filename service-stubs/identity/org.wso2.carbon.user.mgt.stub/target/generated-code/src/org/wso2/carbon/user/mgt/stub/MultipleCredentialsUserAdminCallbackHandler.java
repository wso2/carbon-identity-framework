
/**
 * MultipleCredentialsUserAdminCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.user.mgt.stub;

    /**
     *  MultipleCredentialsUserAdminCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class MultipleCredentialsUserAdminCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public MultipleCredentialsUserAdminCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public MultipleCredentialsUserAdminCallbackHandler(){
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
            * auto generated Axis2 call back method for getUserClaimValue method
            * override this method for handling normal response from getUserClaimValue operation
            */
           public void receiveResultgetUserClaimValue(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserClaimValue operation
           */
            public void receiveErrorgetUserClaimValue(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getUserClaimValues method
            * override this method for handling normal response from getUserClaimValues operation
            */
           public void receiveResultgetUserClaimValues(
                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.ClaimValue[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserClaimValues operation
           */
            public void receiveErrorgetUserClaimValues(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getCredentials method
            * override this method for handling normal response from getCredentials operation
            */
           public void receiveResultgetCredentials(
                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.Credential[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getCredentials operation
           */
            public void receiveErrorgetCredentials(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllUserClaimValues method
            * override this method for handling normal response from getAllUserClaimValues operation
            */
           public void receiveResultgetAllUserClaimValues(
                    org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.ClaimValue[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllUserClaimValues operation
           */
            public void receiveErrorgetAllUserClaimValues(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for authenticate method
            * override this method for handling normal response from authenticate operation
            */
           public void receiveResultauthenticate(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from authenticate operation
           */
            public void receiveErrorauthenticate(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getUserId method
            * override this method for handling normal response from getUserId operation
            */
           public void receiveResultgetUserId(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserId operation
           */
            public void receiveErrorgetUserId(java.lang.Exception e) {
            }
                


    }
    