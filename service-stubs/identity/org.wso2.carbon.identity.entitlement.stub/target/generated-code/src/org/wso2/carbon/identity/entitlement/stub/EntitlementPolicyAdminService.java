

/**
 * EntitlementPolicyAdminService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.entitlement.stub;

    /*
     *  EntitlementPolicyAdminService java interface
     */

    public interface EntitlementPolicyAdminService {
          
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */
        public void  addPolicies(
         org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO[] policies35

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */
        public void  addSubscriber(
         org.wso2.carbon.identity.entitlement.stub.dto.PublisherDataHolder holder37

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */
        public void  publish(
         java.lang.String verificationCode39

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */
        public void  orderPolicy(
         java.lang.String policyId41,int newOrder42

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getStatusData43
                
             * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */

         
                     public org.wso2.carbon.identity.entitlement.stub.dto.PaginatedStatusHolder getStatusData(

                        java.lang.String about44,java.lang.String key45,java.lang.String type46,java.lang.String searchString47,int pageNumber48)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getStatusData43
            
          */
        public void startgetStatusData(

            java.lang.String about44,java.lang.String key45,java.lang.String type46,java.lang.String searchString47,int pageNumber48,

            final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */
        public void  addPolicy(
         org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO policyDTO52

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */
        public void  removePolicy(
         java.lang.String policyId54,boolean dePromote55

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getSubscriber56
                
             * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */

         
                     public org.wso2.carbon.identity.entitlement.stub.dto.PublisherDataHolder getSubscriber(

                        java.lang.String subscribeId57)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getSubscriber56
            
          */
        public void startgetSubscriber(

            java.lang.String subscribeId57,

            final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */
        public void  updateSubscriber(
         org.wso2.carbon.identity.entitlement.stub.dto.PublisherDataHolder holder61

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getPolicy62
                
             * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */

         
                     public org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO getPolicy(

                        java.lang.String policyId63,boolean isPDPPolicy64)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getPolicy62
            
          */
        public void startgetPolicy(

            java.lang.String policyId63,boolean isPDPPolicy64,

            final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */
        public void  publishPolicies(
         java.lang.String[] policyIds68,java.lang.String[] subscriberIds69,java.lang.String action70,java.lang.String version71,boolean enabled72,int order73

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getAllPolicyIds74
                
             * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */

         
                     public java.lang.String[] getAllPolicyIds(

                        java.lang.String searchString75)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllPolicyIds74
            
          */
        public void startgetAllPolicyIds(

            java.lang.String searchString75,

            final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getEntitlementDataModules78
                
         */

         
                     public org.wso2.carbon.identity.entitlement.stub.dto.EntitlementFinderDataHolder[] getEntitlementDataModules(

                        )
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getEntitlementDataModules78
            
          */
        public void startgetEntitlementDataModules(

            

            final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getSubscriberIds81
                
             * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */

         
                     public java.lang.String[] getSubscriberIds(

                        java.lang.String searchString82)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getSubscriberIds81
            
          */
        public void startgetSubscriberIds(

            java.lang.String searchString82,

            final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */
        public void  rollBackPolicy(
         java.lang.String policyId86,java.lang.String version87

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getLightPolicy88
                
             * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */

         
                     public org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO getLightPolicy(

                        java.lang.String policyId89)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getLightPolicy88
            
          */
        public void startgetLightPolicy(

            java.lang.String policyId89,

            final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */
        public void  importPolicyFromRegistry(
         java.lang.String policyRegistryPath93

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getPublisherModuleData94
                
         */

         
                     public org.wso2.carbon.identity.entitlement.stub.dto.PublisherDataHolder[] getPublisherModuleData(

                        )
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getPublisherModuleData94
            
          */
        public void startgetPublisherModuleData(

            

            final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getPolicyVersions97
                
             * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */

         
                     public java.lang.String[] getPolicyVersions(

                        java.lang.String policyId98)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getPolicyVersions97
            
          */
        public void startgetPolicyVersions(

            java.lang.String policyId98,

            final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */
        public void  removePolicies(
         java.lang.String[] policyIds102,boolean dePromote103

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */
        public void  dePromotePolicy(
         java.lang.String policyId105

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getEntitlementData106
                
         */

         
                     public org.wso2.carbon.identity.entitlement.stub.dto.EntitlementTreeNodeDTO getEntitlementData(

                        java.lang.String dataModule107,java.lang.String category108,java.lang.String regexp109,int dataLevel110,int limit111)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getEntitlementData106
            
          */
        public void startgetEntitlementData(

            java.lang.String dataModule107,java.lang.String category108,java.lang.String regexp109,int dataLevel110,int limit111,

            final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */
        public void  publishToPDP(
         java.lang.String[] policyIds115,java.lang.String action116,java.lang.String version117,boolean enabled118,int order119

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getPolicyByVersion120
                
             * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */

         
                     public org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO getPolicyByVersion(

                        java.lang.String policyId121,java.lang.String version122)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getPolicyByVersion120
            
          */
        public void startgetPolicyByVersion(

            java.lang.String policyId121,java.lang.String version122,

            final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */
        public void  enableDisablePolicy(
         java.lang.String policyId126,boolean enable127

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getAllPolicies128
                
             * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */

         
                     public org.wso2.carbon.identity.entitlement.stub.dto.PaginatedPolicySetDTO getAllPolicies(

                        java.lang.String policyTypeFilter129,java.lang.String policySearchString130,int pageNumber131,boolean isPDPPolicy132)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllPolicies128
            
          */
        public void startgetAllPolicies(

            java.lang.String policyTypeFilter129,java.lang.String policySearchString130,int pageNumber131,boolean isPDPPolicy132,

            final org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */
        public void  deleteSubscriber(
         java.lang.String subscriberId136

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException : 
         */
        public void  updatePolicy(
         org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO policyDTO138

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;

        

        
       //
       }
    