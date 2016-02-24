
/**
 * RelyingPartyAdminServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.provider.stub;

    /**
     *  RelyingPartyAdminServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class RelyingPartyAdminServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public RelyingPartyAdminServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public RelyingPartyAdminServiceCallbackHandler(){
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
            * auto generated Axis2 call back method for getAllUserTrustedRelyingParties method
            * override this method for handling normal response from getAllUserTrustedRelyingParties operation
            */
           public void receiveResultgetAllUserTrustedRelyingParties(
                    org.wso2.carbon.identity.provider.stub.rp.dto.UserTrustedRPDTO[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllUserTrustedRelyingParties operation
           */
            public void receiveErrorgetAllUserTrustedRelyingParties(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                


    }
    