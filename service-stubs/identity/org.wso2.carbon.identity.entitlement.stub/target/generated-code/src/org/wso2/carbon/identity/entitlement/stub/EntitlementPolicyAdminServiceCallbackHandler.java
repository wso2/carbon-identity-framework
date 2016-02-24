
/**
 * EntitlementPolicyAdminServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.entitlement.stub;

    /**
     *  EntitlementPolicyAdminServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class EntitlementPolicyAdminServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public EntitlementPolicyAdminServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public EntitlementPolicyAdminServiceCallbackHandler(){
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
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getStatusData method
            * override this method for handling normal response from getStatusData operation
            */
           public void receiveResultgetStatusData(
                    org.wso2.carbon.identity.entitlement.stub.dto.PaginatedStatusHolder result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getStatusData operation
           */
            public void receiveErrorgetStatusData(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getSubscriber method
            * override this method for handling normal response from getSubscriber operation
            */
           public void receiveResultgetSubscriber(
                    org.wso2.carbon.identity.entitlement.stub.dto.PublisherDataHolder result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getSubscriber operation
           */
            public void receiveErrorgetSubscriber(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getPolicy method
            * override this method for handling normal response from getPolicy operation
            */
           public void receiveResultgetPolicy(
                    org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getPolicy operation
           */
            public void receiveErrorgetPolicy(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getAllPolicyIds method
            * override this method for handling normal response from getAllPolicyIds operation
            */
           public void receiveResultgetAllPolicyIds(
                    java.lang.String[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllPolicyIds operation
           */
            public void receiveErrorgetAllPolicyIds(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getEntitlementDataModules method
            * override this method for handling normal response from getEntitlementDataModules operation
            */
           public void receiveResultgetEntitlementDataModules(
                    org.wso2.carbon.identity.entitlement.stub.dto.EntitlementFinderDataHolder[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getEntitlementDataModules operation
           */
            public void receiveErrorgetEntitlementDataModules(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getSubscriberIds method
            * override this method for handling normal response from getSubscriberIds operation
            */
           public void receiveResultgetSubscriberIds(
                    java.lang.String[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getSubscriberIds operation
           */
            public void receiveErrorgetSubscriberIds(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getLightPolicy method
            * override this method for handling normal response from getLightPolicy operation
            */
           public void receiveResultgetLightPolicy(
                    org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getLightPolicy operation
           */
            public void receiveErrorgetLightPolicy(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getPublisherModuleData method
            * override this method for handling normal response from getPublisherModuleData operation
            */
           public void receiveResultgetPublisherModuleData(
                    org.wso2.carbon.identity.entitlement.stub.dto.PublisherDataHolder[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getPublisherModuleData operation
           */
            public void receiveErrorgetPublisherModuleData(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getPolicyVersions method
            * override this method for handling normal response from getPolicyVersions operation
            */
           public void receiveResultgetPolicyVersions(
                    java.lang.String[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getPolicyVersions operation
           */
            public void receiveErrorgetPolicyVersions(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getEntitlementData method
            * override this method for handling normal response from getEntitlementData operation
            */
           public void receiveResultgetEntitlementData(
                    org.wso2.carbon.identity.entitlement.stub.dto.EntitlementTreeNodeDTO result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getEntitlementData operation
           */
            public void receiveErrorgetEntitlementData(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getPolicyByVersion method
            * override this method for handling normal response from getPolicyByVersion operation
            */
           public void receiveResultgetPolicyByVersion(
                    org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getPolicyByVersion operation
           */
            public void receiveErrorgetPolicyByVersion(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getAllPolicies method
            * override this method for handling normal response from getAllPolicies operation
            */
           public void receiveResultgetAllPolicies(
                    org.wso2.carbon.identity.entitlement.stub.dto.PaginatedPolicySetDTO result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllPolicies operation
           */
            public void receiveErrorgetAllPolicies(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                


    }
    