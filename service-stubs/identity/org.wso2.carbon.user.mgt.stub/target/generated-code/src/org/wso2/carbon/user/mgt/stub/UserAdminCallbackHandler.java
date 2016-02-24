
/**
 * UserAdminCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.user.mgt.stub;

    /**
     *  UserAdminCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class UserAdminCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public UserAdminCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public UserAdminCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for listUsers method
            * override this method for handling normal response from listUsers operation
            */
           public void receiveResultlistUsers(
                    java.lang.String[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from listUsers operation
           */
            public void receiveErrorlistUsers(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for listUserByClaimWithPermission method
            * override this method for handling normal response from listUserByClaimWithPermission operation
            */
           public void receiveResultlistUserByClaimWithPermission(
                    org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from listUserByClaimWithPermission operation
           */
            public void receiveErrorlistUserByClaimWithPermission(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for isSharedRolesEnabled method
            * override this method for handling normal response from isSharedRolesEnabled operation
            */
           public void receiveResultisSharedRolesEnabled(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from isSharedRolesEnabled operation
           */
            public void receiveErrorisSharedRolesEnabled(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for changePasswordByUser method
            * override this method for handling normal response from changePasswordByUser operation
            */
           public void receiveResultchangePasswordByUser(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from changePasswordByUser operation
           */
            public void receiveErrorchangePasswordByUser(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for listAllUsersWithPermission method
            * override this method for handling normal response from listAllUsersWithPermission operation
            */
           public void receiveResultlistAllUsersWithPermission(
                    org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from listAllUsersWithPermission operation
           */
            public void receiveErrorlistAllUsersWithPermission(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for addUser method
            * override this method for handling normal response from addUser operation
            */
           public void receiveResultaddUser(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addUser operation
           */
            public void receiveErroraddUser(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for hasMultipleUserStores method
            * override this method for handling normal response from hasMultipleUserStores operation
            */
           public void receiveResulthasMultipleUserStores(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from hasMultipleUserStores operation
           */
            public void receiveErrorhasMultipleUserStores(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getUserRealmInfo method
            * override this method for handling normal response from getUserRealmInfo operation
            */
           public void receiveResultgetUserRealmInfo(
                    org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserRealmInfo operation
           */
            public void receiveErrorgetUserRealmInfo(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getRolesOfUser method
            * override this method for handling normal response from getRolesOfUser operation
            */
           public void receiveResultgetRolesOfUser(
                    org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getRolesOfUser operation
           */
            public void receiveErrorgetRolesOfUser(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for listAllUsers method
            * override this method for handling normal response from listAllUsers operation
            */
           public void receiveResultlistAllUsers(
                    org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from listAllUsers operation
           */
            public void receiveErrorlistAllUsers(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getRolePermissions method
            * override this method for handling normal response from getRolePermissions operation
            */
           public void receiveResultgetRolePermissions(
                    org.wso2.carbon.user.mgt.stub.types.carbon.UIPermissionNode result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getRolePermissions operation
           */
            public void receiveErrorgetRolePermissions(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for deleteUser method
            * override this method for handling normal response from deleteUser operation
            */
           public void receiveResultdeleteUser(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from deleteUser operation
           */
            public void receiveErrordeleteUser(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getAllPermittedRoleNames method
            * override this method for handling normal response from getAllPermittedRoleNames operation
            */
           public void receiveResultgetAllPermittedRoleNames(
                    org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllPermittedRoleNames operation
           */
            public void receiveErrorgetAllPermittedRoleNames(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for updateUsersOfRole method
            * override this method for handling normal response from updateUsersOfRole operation
            */
           public void receiveResultupdateUsersOfRole(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from updateUsersOfRole operation
           */
            public void receiveErrorupdateUsersOfRole(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for changePassword method
            * override this method for handling normal response from changePassword operation
            */
           public void receiveResultchangePassword(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from changePassword operation
           */
            public void receiveErrorchangePassword(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getRolesOfCurrentUser method
            * override this method for handling normal response from getRolesOfCurrentUser operation
            */
           public void receiveResultgetRolesOfCurrentUser(
                    org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getRolesOfCurrentUser operation
           */
            public void receiveErrorgetRolesOfCurrentUser(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for listUserByClaim method
            * override this method for handling normal response from listUserByClaim operation
            */
           public void receiveResultlistUserByClaim(
                    org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from listUserByClaim operation
           */
            public void receiveErrorlistUserByClaim(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getUsersOfRole method
            * override this method for handling normal response from getUsersOfRole operation
            */
           public void receiveResultgetUsersOfRole(
                    org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUsersOfRole operation
           */
            public void receiveErrorgetUsersOfRole(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllUIPermissions method
            * override this method for handling normal response from getAllUIPermissions operation
            */
           public void receiveResultgetAllUIPermissions(
                    org.wso2.carbon.user.mgt.stub.types.carbon.UIPermissionNode result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllUIPermissions operation
           */
            public void receiveErrorgetAllUIPermissions(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllSharedRoleNames method
            * override this method for handling normal response from getAllSharedRoleNames operation
            */
           public void receiveResultgetAllSharedRoleNames(
                    org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllSharedRoleNames operation
           */
            public void receiveErrorgetAllSharedRoleNames(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllRolesNames method
            * override this method for handling normal response from getAllRolesNames operation
            */
           public void receiveResultgetAllRolesNames(
                    org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllRolesNames operation
           */
            public void receiveErrorgetAllRolesNames(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                


    }
    