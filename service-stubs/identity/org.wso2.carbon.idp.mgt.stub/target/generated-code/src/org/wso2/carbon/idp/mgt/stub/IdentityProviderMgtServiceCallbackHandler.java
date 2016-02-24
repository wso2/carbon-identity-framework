
/**
 * IdentityProviderMgtServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.idp.mgt.stub;

    /**
     *  IdentityProviderMgtServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class IdentityProviderMgtServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public IdentityProviderMgtServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public IdentityProviderMgtServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for getIdPByName method
            * override this method for handling normal response from getIdPByName operation
            */
           public void receiveResultgetIdPByName(
                    org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getIdPByName operation
           */
            public void receiveErrorgetIdPByName(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllIdPs method
            * override this method for handling normal response from getAllIdPs operation
            */
           public void receiveResultgetAllIdPs(
                    org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllIdPs operation
           */
            public void receiveErrorgetAllIdPs(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllProvisioningConnectors method
            * override this method for handling normal response from getAllProvisioningConnectors operation
            */
           public void receiveResultgetAllProvisioningConnectors(
                    org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllProvisioningConnectors operation
           */
            public void receiveErrorgetAllProvisioningConnectors(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getResidentIdP method
            * override this method for handling normal response from getResidentIdP operation
            */
           public void receiveResultgetResidentIdP(
                    org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getResidentIdP operation
           */
            public void receiveErrorgetResidentIdP(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
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
                
           /**
            * auto generated Axis2 call back method for getEnabledAllIdPs method
            * override this method for handling normal response from getEnabledAllIdPs operation
            */
           public void receiveResultgetEnabledAllIdPs(
                    org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getEnabledAllIdPs operation
           */
            public void receiveErrorgetEnabledAllIdPs(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getAllFederatedAuthenticators method
            * override this method for handling normal response from getAllFederatedAuthenticators operation
            */
           public void receiveResultgetAllFederatedAuthenticators(
                    org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllFederatedAuthenticators operation
           */
            public void receiveErrorgetAllFederatedAuthenticators(java.lang.Exception e) {
            }
                


    }
    