
/**
 * IdentityProviderAdminServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.provider.stub;

    /**
     *  IdentityProviderAdminServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class IdentityProviderAdminServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public IdentityProviderAdminServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public IdentityProviderAdminServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for extractPrimaryUserName method
            * override this method for handling normal response from extractPrimaryUserName operation
            */
           public void receiveResultextractPrimaryUserName(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from extractPrimaryUserName operation
           */
            public void receiveErrorextractPrimaryUserName(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getAllOpenIDs method
            * override this method for handling normal response from getAllOpenIDs operation
            */
           public void receiveResultgetAllOpenIDs(
                    java.lang.String[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllOpenIDs operation
           */
            public void receiveErrorgetAllOpenIDs(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getPrimaryOpenID method
            * override this method for handling normal response from getPrimaryOpenID operation
            */
           public void receiveResultgetPrimaryOpenID(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getPrimaryOpenID operation
           */
            public void receiveErrorgetPrimaryOpenID(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                


    }
    