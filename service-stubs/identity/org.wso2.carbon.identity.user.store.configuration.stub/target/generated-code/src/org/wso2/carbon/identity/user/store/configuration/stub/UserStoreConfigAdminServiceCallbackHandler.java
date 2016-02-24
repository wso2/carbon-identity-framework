
/**
 * UserStoreConfigAdminServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.user.store.configuration.stub;

    /**
     *  UserStoreConfigAdminServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class UserStoreConfigAdminServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public UserStoreConfigAdminServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public UserStoreConfigAdminServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for getUserStoreManagerProperties method
            * override this method for handling normal response from getUserStoreManagerProperties operation
            */
           public void receiveResultgetUserStoreManagerProperties(
                    org.wso2.carbon.identity.user.store.configuration.stub.api.Properties result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserStoreManagerProperties operation
           */
            public void receiveErrorgetUserStoreManagerProperties(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for testRDBMSConnection method
            * override this method for handling normal response from testRDBMSConnection operation
            */
           public void receiveResulttestRDBMSConnection(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from testRDBMSConnection operation
           */
            public void receiveErrortestRDBMSConnection(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getSecondaryRealmConfigurations method
            * override this method for handling normal response from getSecondaryRealmConfigurations operation
            */
           public void receiveResultgetSecondaryRealmConfigurations(
                    org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getSecondaryRealmConfigurations operation
           */
            public void receiveErrorgetSecondaryRealmConfigurations(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAvailableUserStoreClasses method
            * override this method for handling normal response from getAvailableUserStoreClasses operation
            */
           public void receiveResultgetAvailableUserStoreClasses(
                    java.lang.String[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAvailableUserStoreClasses operation
           */
            public void receiveErrorgetAvailableUserStoreClasses(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                


    }
    