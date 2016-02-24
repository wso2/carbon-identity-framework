
/**
 * IdentityApplicationManagementServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.application.mgt.stub;

    /**
     *  IdentityApplicationManagementServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class IdentityApplicationManagementServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public IdentityApplicationManagementServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public IdentityApplicationManagementServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for getAllRequestPathAuthenticators method
            * override this method for handling normal response from getAllRequestPathAuthenticators operation
            */
           public void receiveResultgetAllRequestPathAuthenticators(
                    org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllRequestPathAuthenticators operation
           */
            public void receiveErrorgetAllRequestPathAuthenticators(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getApplication method
            * override this method for handling normal response from getApplication operation
            */
           public void receiveResultgetApplication(
                    org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getApplication operation
           */
            public void receiveErrorgetApplication(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllApplicationBasicInfo method
            * override this method for handling normal response from getAllApplicationBasicInfo operation
            */
           public void receiveResultgetAllApplicationBasicInfo(
                    org.wso2.carbon.identity.application.common.model.xsd.ApplicationBasicInfo[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllApplicationBasicInfo operation
           */
            public void receiveErrorgetAllApplicationBasicInfo(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getIdentityProvider method
            * override this method for handling normal response from getIdentityProvider operation
            */
           public void receiveResultgetIdentityProvider(
                    org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getIdentityProvider operation
           */
            public void receiveErrorgetIdentityProvider(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllLocalAuthenticators method
            * override this method for handling normal response from getAllLocalAuthenticators operation
            */
           public void receiveResultgetAllLocalAuthenticators(
                    org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllLocalAuthenticators operation
           */
            public void receiveErrorgetAllLocalAuthenticators(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getAllIdentityProviders method
            * override this method for handling normal response from getAllIdentityProviders operation
            */
           public void receiveResultgetAllIdentityProviders(
                    org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllIdentityProviders operation
           */
            public void receiveErrorgetAllIdentityProviders(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllLocalClaimUris method
            * override this method for handling normal response from getAllLocalClaimUris operation
            */
           public void receiveResultgetAllLocalClaimUris(
                    java.lang.String[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllLocalClaimUris operation
           */
            public void receiveErrorgetAllLocalClaimUris(java.lang.Exception e) {
            }
                


    }
    