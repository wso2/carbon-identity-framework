

/**
 * UserProfileMgtService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.user.profile.stub;

    /*
     *  UserProfileMgtService java interface
     */

    public interface UserProfileMgtService {
          

        /**
          * Auto generated method signature
          * 
                    * @param getUserProfiles16
                
             * @throws org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException : 
         */

         
                     public org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO[] getUserProfiles(

                        java.lang.String username17)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getUserProfiles16
            
          */
        public void startgetUserProfiles(

            java.lang.String username17,

            final org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getUserProfile20
                
             * @throws org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException : 
         */

         
                     public org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO getUserProfile(

                        java.lang.String username21,java.lang.String profileName22)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getUserProfile20
            
          */
        public void startgetUserProfile(

            java.lang.String username21,java.lang.String profileName22,

            final org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param isAddProfileEnabled25
                
             * @throws org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException : 
         */

         
                     public boolean isAddProfileEnabled(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param isAddProfileEnabled25
            
          */
        public void startisAddProfileEnabled(

            

            final org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAssociatedIDs28
                
             * @throws org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException : 
         */

         
                     public org.wso2.carbon.identity.user.profile.stub.types.AssociatedAccountDTO[] getAssociatedIDs(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAssociatedIDs28
            
          */
        public void startgetAssociatedIDs(

            

            final org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param isAddProfileEnabledForDomain31
                
             * @throws org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException : 
         */

         
                     public boolean isAddProfileEnabledForDomain(

                        java.lang.String domain32)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param isAddProfileEnabledForDomain31
            
          */
        public void startisAddProfileEnabledForDomain(

            java.lang.String domain32,

            final org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException : 
         */
        public void  removeAssociateID(
         java.lang.String idpID36,java.lang.String associatedID37

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getProfileFieldsForInternalStore38
                
             * @throws org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException : 
         */

         
                     public org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO getProfileFieldsForInternalStore(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getProfileFieldsForInternalStore38
            
          */
        public void startgetProfileFieldsForInternalStore(

            

            final org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param isReadOnlyUserStore41
                
             * @throws org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException : 
         */

         
                     public boolean isReadOnlyUserStore(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param isReadOnlyUserStore41
            
          */
        public void startisReadOnlyUserStore(

            

            final org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException : 
         */
        public void  deleteUserProfile(
         java.lang.String username45,java.lang.String profileName46

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException : 
         */
        public void  setUserProfile(
         java.lang.String username48,org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO profile49

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getNameAssociatedWith50
                
             * @throws org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException : 
         */

         
                     public java.lang.String getNameAssociatedWith(

                        java.lang.String idpID51,java.lang.String associatedID52)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getNameAssociatedWith50
            
          */
        public void startgetNameAssociatedWith(

            java.lang.String idpID51,java.lang.String associatedID52,

            final org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getInstance55
                
         */

         
                     public org.wso2.carbon.identity.user.profile.stub.types.UserProfileAdmin getInstance(

                        )
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getInstance55
            
          */
        public void startgetInstance(

            

            final org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException : 
         */
        public void  associateID(
         java.lang.String idpID59,java.lang.String associatedID60

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;

        

        
       //
       }
    