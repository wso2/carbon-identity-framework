

/**
 * EntitlementAdminService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.entitlement.stub;

    /*
     *  EntitlementAdminService java interface
     */

    public interface EntitlementAdminService {
          
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException : 
         */
        public void  clearAllAttributeCaches(
         

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
         */
        public void  clearResourceFinderCache(
         java.lang.String resourceFinder14

        ) throws java.rmi.RemoteException
        
        ;

        

        /**
          * Auto generated method signature
          * 
                    * @param doTestRequest15
                
             * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException : 
         */

         
                     public java.lang.String doTestRequest(

                        java.lang.String xacmlRequest16)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param doTestRequest15
            
          */
        public void startdoTestRequest(

            java.lang.String xacmlRequest16,

            final org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException : 
         */
        public void  clearCarbonAttributeCache(
         

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
         */
        public void  clearAttributeFinderCacheByAttributes(
         java.lang.String attributeFinder21,java.lang.String[] attributeIds22

        ) throws java.rmi.RemoteException
        
        ;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException : 
         */
        public void  setGlobalPolicyAlgorithm(
         java.lang.String policyCombiningAlgorithm24

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException : 
         */
        public void  clearAllResourceCaches(
         

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getPDPData26
                
         */

         
                     public org.wso2.carbon.identity.entitlement.stub.dto.PDPDataHolder getPDPData(

                        )
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getPDPData26
            
          */
        public void startgetPDPData(

            

            final org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException : 
         */
        public void  clearDecisionCache(
         

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException : 
         */
        public void  refreshResourceFinder(
         java.lang.String resourceFinder31

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException : 
         */
        public void  clearCarbonResourceCache(
         

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getPolicyFinderData33
                
         */

         
                     public org.wso2.carbon.identity.entitlement.stub.dto.PolicyFinderDataHolder getPolicyFinderData(

                        java.lang.String finder34)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getPolicyFinderData33
            
          */
        public void startgetPolicyFinderData(

            java.lang.String finder34,

            final org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getPIPAttributeFinderData37
                
         */

         
                     public org.wso2.carbon.identity.entitlement.stub.dto.PIPFinderDataHolder getPIPAttributeFinderData(

                        java.lang.String finder38)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getPIPAttributeFinderData37
            
          */
        public void startgetPIPAttributeFinderData(

            java.lang.String finder38,

            final org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getGlobalPolicyAlgorithm41
                
             * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException : 
         */

         
                     public java.lang.String getGlobalPolicyAlgorithm(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getGlobalPolicyAlgorithm41
            
          */
        public void startgetGlobalPolicyAlgorithm(

            

            final org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getPIPResourceFinderData44
                
         */

         
                     public org.wso2.carbon.identity.entitlement.stub.dto.PIPFinderDataHolder getPIPResourceFinderData(

                        java.lang.String finder45)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getPIPResourceFinderData44
            
          */
        public void startgetPIPResourceFinderData(

            java.lang.String finder45,

            final org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException : 
         */
        public void  refreshAttributeFinder(
         java.lang.String attributeFinder49

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException;

        

        /**
          * Auto generated method signature
          * 
                    * @param doTestRequestForGivenPolicies50
                
             * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException : 
         */

         
                     public java.lang.String doTestRequestForGivenPolicies(

                        java.lang.String xacmlRequest51,java.lang.String[] policies52)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param doTestRequestForGivenPolicies50
            
          */
        public void startdoTestRequestForGivenPolicies(

            java.lang.String xacmlRequest51,java.lang.String[] policies52,

            final org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
         */
        public void  clearAttributeFinderCache(
         java.lang.String attributeFinder56

        ) throws java.rmi.RemoteException
        
        ;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException : 
         */
        public void  refreshPolicyFinders(
         java.lang.String policyFinder58

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException;

        

        
       //
       }
    