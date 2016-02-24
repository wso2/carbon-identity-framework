

/**
 * IdentityApplicationManagementService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.application.mgt.stub;

    /*
     *  IdentityApplicationManagementService java interface
     */

    public interface IdentityApplicationManagementService {
          

        /**
          * Auto generated method signature
          * 
                    * @param getAllRequestPathAuthenticators8
                
             * @throws org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException : 
         */

         
                     public org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig[] getAllRequestPathAuthenticators(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllRequestPathAuthenticators8
            
          */
        public void startgetAllRequestPathAuthenticators(

            

            final org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException : 
         */
        public void  createApplication(
         org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider serviceProvider12

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getApplication13
                
             * @throws org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException : 
         */

         
                     public org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider getApplication(

                        java.lang.String applicationName14)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getApplication13
            
          */
        public void startgetApplication(

            java.lang.String applicationName14,

            final org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAllApplicationBasicInfo17
                
             * @throws org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException : 
         */

         
                     public org.wso2.carbon.identity.application.common.model.xsd.ApplicationBasicInfo[] getAllApplicationBasicInfo(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllApplicationBasicInfo17
            
          */
        public void startgetAllApplicationBasicInfo(

            

            final org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getIdentityProvider20
                
             * @throws org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException : 
         */

         
                     public org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider getIdentityProvider(

                        java.lang.String federatedIdPName21)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getIdentityProvider20
            
          */
        public void startgetIdentityProvider(

            java.lang.String federatedIdPName21,

            final org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAllLocalAuthenticators24
                
             * @throws org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException : 
         */

         
                     public org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig[] getAllLocalAuthenticators(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllLocalAuthenticators24
            
          */
        public void startgetAllLocalAuthenticators(

            

            final org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException : 
         */
        public void  updateApplication(
         org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider serviceProvider28

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException : 
         */
        public void  deleteApplication(
         java.lang.String applicationName30

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getAllIdentityProviders31
                
             * @throws org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException : 
         */

         
                     public org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider[] getAllIdentityProviders(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllIdentityProviders31
            
          */
        public void startgetAllIdentityProviders(

            

            final org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAllLocalClaimUris34
                
             * @throws org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException : 
         */

         
                     public java.lang.String[] getAllLocalClaimUris(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllLocalClaimUris34
            
          */
        public void startgetAllLocalClaimUris(

            

            final org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    