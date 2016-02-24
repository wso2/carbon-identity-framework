
/**
 * EntitlementAdminServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.entitlement.stub;

    /**
     *  EntitlementAdminServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class EntitlementAdminServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public EntitlementAdminServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public EntitlementAdminServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for doTestRequest method
            * override this method for handling normal response from doTestRequest operation
            */
           public void receiveResultdoTestRequest(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from doTestRequest operation
           */
            public void receiveErrordoTestRequest(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getPDPData method
            * override this method for handling normal response from getPDPData operation
            */
           public void receiveResultgetPDPData(
                    org.wso2.carbon.identity.entitlement.stub.dto.PDPDataHolder result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getPDPData operation
           */
            public void receiveErrorgetPDPData(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getPolicyFinderData method
            * override this method for handling normal response from getPolicyFinderData operation
            */
           public void receiveResultgetPolicyFinderData(
                    org.wso2.carbon.identity.entitlement.stub.dto.PolicyFinderDataHolder result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getPolicyFinderData operation
           */
            public void receiveErrorgetPolicyFinderData(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getPIPAttributeFinderData method
            * override this method for handling normal response from getPIPAttributeFinderData operation
            */
           public void receiveResultgetPIPAttributeFinderData(
                    org.wso2.carbon.identity.entitlement.stub.dto.PIPFinderDataHolder result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getPIPAttributeFinderData operation
           */
            public void receiveErrorgetPIPAttributeFinderData(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getGlobalPolicyAlgorithm method
            * override this method for handling normal response from getGlobalPolicyAlgorithm operation
            */
           public void receiveResultgetGlobalPolicyAlgorithm(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getGlobalPolicyAlgorithm operation
           */
            public void receiveErrorgetGlobalPolicyAlgorithm(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getPIPResourceFinderData method
            * override this method for handling normal response from getPIPResourceFinderData operation
            */
           public void receiveResultgetPIPResourceFinderData(
                    org.wso2.carbon.identity.entitlement.stub.dto.PIPFinderDataHolder result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getPIPResourceFinderData operation
           */
            public void receiveErrorgetPIPResourceFinderData(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for doTestRequestForGivenPolicies method
            * override this method for handling normal response from doTestRequestForGivenPolicies operation
            */
           public void receiveResultdoTestRequestForGivenPolicies(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from doTestRequestForGivenPolicies operation
           */
            public void receiveErrordoTestRequestForGivenPolicies(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                


    }
    