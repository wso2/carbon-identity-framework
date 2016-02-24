

/**
 * IdentityProviderMgtService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.idp.mgt.stub;

    /*
     *  IdentityProviderMgtService java interface
     */

    public interface IdentityProviderMgtService {
          

        /**
          * Auto generated method signature
          * 
                    * @param getIdPByName9
                
             * @throws org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException : 
         */

         
                     public org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider getIdPByName(

                        java.lang.String idPName10)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getIdPByName9
            
          */
        public void startgetIdPByName(

            java.lang.String idPName10,

            final org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAllIdPs13
                
             * @throws org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException : 
         */

         
                     public org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider[] getAllIdPs(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllIdPs13
            
          */
        public void startgetAllIdPs(

            

            final org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAllProvisioningConnectors16
                
             * @throws org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException : 
         */

         
                     public org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig[] getAllProvisioningConnectors(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllProvisioningConnectors16
            
          */
        public void startgetAllProvisioningConnectors(

            

            final org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException : 
         */
        public void  updateIdP(
         java.lang.String oldIdPName20,org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider identityProvider21

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getResidentIdP22
                
             * @throws org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException : 
         */

         
                     public org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider getResidentIdP(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getResidentIdP22
            
          */
        public void startgetResidentIdP(

            

            final org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException : 
         */
        public void  addIdP(
         org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider identityProvider26

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getAllLocalClaimUris27
                
             * @throws org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException : 
         */

         
                     public java.lang.String[] getAllLocalClaimUris(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllLocalClaimUris27
            
          */
        public void startgetAllLocalClaimUris(

            

            final org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getEnabledAllIdPs30
                
             * @throws org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException : 
         */

         
                     public org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider[] getEnabledAllIdPs(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getEnabledAllIdPs30
            
          */
        public void startgetEnabledAllIdPs(

            

            final org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException : 
         */
        public void  updateResidentIdP(
         org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider identityProvider34

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException : 
         */
        public void  deleteIdP(
         java.lang.String idPName36

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getAllFederatedAuthenticators37
                
             * @throws org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException : 
         */

         
                     public org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig[] getAllFederatedAuthenticators(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllFederatedAuthenticators37
            
          */
        public void startgetAllFederatedAuthenticators(

            

            final org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    