
/**
 * EntitlementServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.entitlement.stub;

    /**
     *  EntitlementServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class EntitlementServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public EntitlementServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public EntitlementServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for getEntitledAttributes method
            * override this method for handling normal response from getEntitledAttributes operation
            */
           public void receiveResultgetEntitledAttributes(
                    org.wso2.carbon.identity.entitlement.stub.dto.EntitledResultSetDTO result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getEntitledAttributes operation
           */
            public void receiveErrorgetEntitledAttributes(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for xACMLAuthzDecisionQuery method
            * override this method for handling normal response from xACMLAuthzDecisionQuery operation
            */
           public void receiveResultxACMLAuthzDecisionQuery(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from xACMLAuthzDecisionQuery operation
           */
            public void receiveErrorxACMLAuthzDecisionQuery(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllEntitlements method
            * override this method for handling normal response from getAllEntitlements operation
            */
           public void receiveResultgetAllEntitlements(
                    org.wso2.carbon.identity.entitlement.stub.dto.EntitledResultSetDTO result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllEntitlements operation
           */
            public void receiveErrorgetAllEntitlements(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getDecision method
            * override this method for handling normal response from getDecision operation
            */
           public void receiveResultgetDecision(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getDecision operation
           */
            public void receiveErrorgetDecision(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getDecisionByAttributes method
            * override this method for handling normal response from getDecisionByAttributes operation
            */
           public void receiveResultgetDecisionByAttributes(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getDecisionByAttributes operation
           */
            public void receiveErrorgetDecisionByAttributes(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getBooleanDecision method
            * override this method for handling normal response from getBooleanDecision operation
            */
           public void receiveResultgetBooleanDecision(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getBooleanDecision operation
           */
            public void receiveErrorgetBooleanDecision(java.lang.Exception e) {
            }
                


    }
    