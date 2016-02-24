

/**
 * XMPPConfigurationService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.provider.stub;

    /*
     *  XMPPConfigurationService java interface
     */

    public interface XMPPConfigurationService {
          
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.provider.stub.IdentityProviderException : 
         */
        public void  editXmppSettings(
         org.wso2.carbon.identity.provider.stub.dto.XMPPSettingsDTO dto9

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.provider.stub.IdentityProviderException;

        

        /**
          * Auto generated method signature
          * 
                    * @param addUserXmppSettings10
                
             * @throws org.wso2.carbon.identity.provider.stub.IdentityProviderException : 
         */

         
                     public boolean addUserXmppSettings(

                        org.wso2.carbon.identity.provider.stub.dto.XMPPSettingsDTO dto11)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.provider.stub.IdentityProviderException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param addUserXmppSettings10
            
          */
        public void startaddUserXmppSettings(

            org.wso2.carbon.identity.provider.stub.dto.XMPPSettingsDTO dto11,

            final org.wso2.carbon.identity.provider.stub.XMPPConfigurationServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getUserIM14
                
             * @throws org.wso2.carbon.identity.provider.stub.Exception : 
         */

         
                     public java.lang.String getUserIM(

                        java.lang.String userId15)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.provider.stub.Exception;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getUserIM14
            
          */
        public void startgetUserIM(

            java.lang.String userId15,

            final org.wso2.carbon.identity.provider.stub.XMPPConfigurationServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getXmppSettings18
                
             * @throws org.wso2.carbon.identity.provider.stub.IdentityProviderException : 
         */

         
                     public org.wso2.carbon.identity.provider.stub.dto.XMPPSettingsDTO getXmppSettings(

                        java.lang.String userId19)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.provider.stub.IdentityProviderException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getXmppSettings18
            
          */
        public void startgetXmppSettings(

            java.lang.String userId19,

            final org.wso2.carbon.identity.provider.stub.XMPPConfigurationServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param isXMPPSettingsEnabled22
                
             * @throws org.wso2.carbon.identity.provider.stub.IdentityProviderException : 
         */

         
                     public boolean isXMPPSettingsEnabled(

                        java.lang.String userId23)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.provider.stub.IdentityProviderException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param isXMPPSettingsEnabled22
            
          */
        public void startisXMPPSettingsEnabled(

            java.lang.String userId23,

            final org.wso2.carbon.identity.provider.stub.XMPPConfigurationServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param hasXMPPSettings26
                
             * @throws org.wso2.carbon.identity.provider.stub.IdentityProviderException : 
         */

         
                     public boolean hasXMPPSettings(

                        java.lang.String userId27)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.provider.stub.IdentityProviderException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param hasXMPPSettings26
            
          */
        public void starthasXMPPSettings(

            java.lang.String userId27,

            final org.wso2.carbon.identity.provider.stub.XMPPConfigurationServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    