

/**
 * UserAdmin.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.user.mgt.stub;

    /*
     *  UserAdmin java interface
     */

    public interface UserAdmin {
          

        /**
          * Auto generated method signature
          * 
                    * @param listUsers58
                
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */

         
                     public java.lang.String[] listUsers(

                        java.lang.String filter59,int limit60)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param listUsers58
            
          */
        public void startlistUsers(

            java.lang.String filter59,int limit60,

            final org.wso2.carbon.user.mgt.stub.UserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */
        public void  setRoleUIPermission(
         java.lang.String roleName64,java.lang.String[] rawResources65

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */
        public void  addRemoveUsersOfRole(
         java.lang.String roleName67,java.lang.String[] newUsers68,java.lang.String[] deletedUsers69

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        

        /**
          * Auto generated method signature
          * 
                    * @param listUserByClaimWithPermission70
                
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserStoreException : 
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */

         
                     public org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] listUserByClaimWithPermission(

                        org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue claimValue71,java.lang.String filter72,java.lang.String permission73,int maxLimit74)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserStoreException
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param listUserByClaimWithPermission70
            
          */
        public void startlistUserByClaimWithPermission(

            org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue claimValue71,java.lang.String filter72,java.lang.String permission73,int maxLimit74,

            final org.wso2.carbon.user.mgt.stub.UserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param isSharedRolesEnabled77
                
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */

         
                     public boolean isSharedRolesEnabled(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param isSharedRolesEnabled77
            
          */
        public void startisSharedRolesEnabled(

            

            final org.wso2.carbon.user.mgt.stub.UserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param changePasswordByUser80
                
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */

         
                     public void changePasswordByUser(

                        java.lang.String userName81,java.lang.String oldPassword82,java.lang.String newPassword83)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param changePasswordByUser80
            
          */
        public void startchangePasswordByUser(

            java.lang.String userName81,java.lang.String oldPassword82,java.lang.String newPassword83,

            final org.wso2.carbon.user.mgt.stub.UserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param listAllUsersWithPermission85
                
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */

         
                     public org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] listAllUsersWithPermission(

                        java.lang.String filter86,java.lang.String permission87,int limit88)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param listAllUsersWithPermission85
            
          */
        public void startlistAllUsersWithPermission(

            java.lang.String filter86,java.lang.String permission87,int limit88,

            final org.wso2.carbon.user.mgt.stub.UserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param addUser91
                
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */

         
                     public void addUser(

                        java.lang.String userName92,java.lang.String password93,java.lang.String[] roles94,org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue[] claims95,java.lang.String profileName96)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param addUser91
            
          */
        public void startaddUser(

            java.lang.String userName92,java.lang.String password93,java.lang.String[] roles94,org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue[] claims95,java.lang.String profileName96,

            final org.wso2.carbon.user.mgt.stub.UserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param hasMultipleUserStores98
                
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */

         
                     public boolean hasMultipleUserStores(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param hasMultipleUserStores98
            
          */
        public void starthasMultipleUserStores(

            

            final org.wso2.carbon.user.mgt.stub.UserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getUserRealmInfo101
                
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */

         
                     public org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo getUserRealmInfo(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getUserRealmInfo101
            
          */
        public void startgetUserRealmInfo(

            

            final org.wso2.carbon.user.mgt.stub.UserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */
        public void  updateRoleName(
         java.lang.String roleName105,java.lang.String newRoleName106

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getRolesOfUser107
                
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */

         
                     public org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] getRolesOfUser(

                        java.lang.String userName108,java.lang.String filter109,int limit110)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getRolesOfUser107
            
          */
        public void startgetRolesOfUser(

            java.lang.String userName108,java.lang.String filter109,int limit110,

            final org.wso2.carbon.user.mgt.stub.UserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param listAllUsers113
                
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */

         
                     public org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] listAllUsers(

                        java.lang.String filter114,int limit115)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param listAllUsers113
            
          */
        public void startlistAllUsers(

            java.lang.String filter114,int limit115,

            final org.wso2.carbon.user.mgt.stub.UserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getRolePermissions118
                
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */

         
                     public org.wso2.carbon.user.mgt.stub.types.carbon.UIPermissionNode getRolePermissions(

                        java.lang.String roleName119)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getRolePermissions118
            
          */
        public void startgetRolePermissions(

            java.lang.String roleName119,

            final org.wso2.carbon.user.mgt.stub.UserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */
        public void  deleteRole(
         java.lang.String roleName123

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        

        /**
          * Auto generated method signature
          * 
                    * @param deleteUser124
                
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */

         
                     public void deleteUser(

                        java.lang.String userName125)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param deleteUser124
            
          */
        public void startdeleteUser(

            java.lang.String userName125,

            final org.wso2.carbon.user.mgt.stub.UserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */
        public void  updateRolesOfUser(
         java.lang.String userName128,java.lang.String[] newRoleList129

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getAllPermittedRoleNames130
                
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */

         
                     public org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] getAllPermittedRoleNames(

                        java.lang.String filter131,java.lang.String permission132,int limit133)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllPermittedRoleNames130
            
          */
        public void startgetAllPermittedRoleNames(

            java.lang.String filter131,java.lang.String permission132,int limit133,

            final org.wso2.carbon.user.mgt.stub.UserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param updateUsersOfRole136
                
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */

         
                     public void updateUsersOfRole(

                        java.lang.String roleName137,org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] userList138)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param updateUsersOfRole136
            
          */
        public void startupdateUsersOfRole(

            java.lang.String roleName137,org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] userList138,

            final org.wso2.carbon.user.mgt.stub.UserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param changePassword140
                
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */

         
                     public void changePassword(

                        java.lang.String userName141,java.lang.String newPassword142)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param changePassword140
            
          */
        public void startchangePassword(

            java.lang.String userName141,java.lang.String newPassword142,

            final org.wso2.carbon.user.mgt.stub.UserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */
        public void  addInternalRole(
         java.lang.String roleName145,java.lang.String[] userList146,java.lang.String[] permissions147

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */
        public void  addRole(
         java.lang.String roleName149,java.lang.String[] userList150,java.lang.String[] permissions151,boolean isSharedRole152

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getRolesOfCurrentUser153
                
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */

         
                     public org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] getRolesOfCurrentUser(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getRolesOfCurrentUser153
            
          */
        public void startgetRolesOfCurrentUser(

            

            final org.wso2.carbon.user.mgt.stub.UserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param listUserByClaim156
                
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */

         
                     public org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] listUserByClaim(

                        org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue claimValue157,java.lang.String filter158,int maxLimit159)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param listUserByClaim156
            
          */
        public void startlistUserByClaim(

            org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue claimValue157,java.lang.String filter158,int maxLimit159,

            final org.wso2.carbon.user.mgt.stub.UserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */
        public void  addRemoveRolesOfUser(
         java.lang.String userName163,java.lang.String[] newRoles164,java.lang.String[] deletedRoles165

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getUsersOfRole166
                
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */

         
                     public org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] getUsersOfRole(

                        java.lang.String roleName167,java.lang.String filter168,int limit169)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getUsersOfRole166
            
          */
        public void startgetUsersOfRole(

            java.lang.String roleName167,java.lang.String filter168,int limit169,

            final org.wso2.carbon.user.mgt.stub.UserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAllUIPermissions172
                
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */

         
                     public org.wso2.carbon.user.mgt.stub.types.carbon.UIPermissionNode getAllUIPermissions(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllUIPermissions172
            
          */
        public void startgetAllUIPermissions(

            

            final org.wso2.carbon.user.mgt.stub.UserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAllSharedRoleNames175
                
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */

         
                     public org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] getAllSharedRoleNames(

                        java.lang.String filter176,int limit177)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllSharedRoleNames175
            
          */
        public void startgetAllSharedRoleNames(

            java.lang.String filter176,int limit177,

            final org.wso2.carbon.user.mgt.stub.UserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAllRolesNames180
                
             * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */

         
                     public org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName[] getAllRolesNames(

                        java.lang.String filter181,int limit182)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllRolesNames180
            
          */
        public void startgetAllRolesNames(

            java.lang.String filter181,int limit182,

            final org.wso2.carbon.user.mgt.stub.UserAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException : 
         */
        public void  bulkImportUsers(
         java.lang.String userStoreDomain186,java.lang.String fileName187,javax.activation.DataHandler handler188,java.lang.String defaultPassword189

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

        

        
       //
       }
    