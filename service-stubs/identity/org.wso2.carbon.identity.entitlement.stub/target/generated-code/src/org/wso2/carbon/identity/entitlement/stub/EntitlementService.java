

/**
 * EntitlementService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.entitlement.stub;

    /*
     *  EntitlementService java interface
     */

    public interface EntitlementService {
          

        /**
          * Auto generated method signature
          * 
                    * @param getEntitledAttributes10
                
             * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementServiceIdentityException : 
         */

         
                     public org.wso2.carbon.identity.entitlement.stub.dto.EntitledResultSetDTO getEntitledAttributes(

                        java.lang.String subjectName11,java.lang.String resourceName12,java.lang.String subjectId13,java.lang.String action14,boolean enableChildSearch15)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.entitlement.stub.EntitlementServiceIdentityException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getEntitledAttributes10
            
          */
        public void startgetEntitledAttributes(

            java.lang.String subjectName11,java.lang.String resourceName12,java.lang.String subjectId13,java.lang.String action14,boolean enableChildSearch15,

            final org.wso2.carbon.identity.entitlement.stub.EntitlementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param xACMLAuthzDecisionQuery18
                
             * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementServiceException : 
         */

         
                     public java.lang.String xACMLAuthzDecisionQuery(

                        java.lang.String request19)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.entitlement.stub.EntitlementServiceException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param xACMLAuthzDecisionQuery18
            
          */
        public void startxACMLAuthzDecisionQuery(

            java.lang.String request19,

            final org.wso2.carbon.identity.entitlement.stub.EntitlementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAllEntitlements22
                
             * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementServiceIdentityException : 
         */

         
                     public org.wso2.carbon.identity.entitlement.stub.dto.EntitledResultSetDTO getAllEntitlements(

                        java.lang.String identifier23,org.wso2.carbon.identity.entitlement.stub.dto.AttributeDTO[] givenAttributes24)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.entitlement.stub.EntitlementServiceIdentityException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllEntitlements22
            
          */
        public void startgetAllEntitlements(

            java.lang.String identifier23,org.wso2.carbon.identity.entitlement.stub.dto.AttributeDTO[] givenAttributes24,

            final org.wso2.carbon.identity.entitlement.stub.EntitlementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getDecision27
                
             * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementServiceException : 
         */

         
                     public java.lang.String getDecision(

                        java.lang.String request28)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.entitlement.stub.EntitlementServiceException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getDecision27
            
          */
        public void startgetDecision(

            java.lang.String request28,

            final org.wso2.carbon.identity.entitlement.stub.EntitlementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getDecisionByAttributes31
                
             * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementServiceException : 
         */

         
                     public java.lang.String getDecisionByAttributes(

                        java.lang.String subject32,java.lang.String resource33,java.lang.String action34,java.lang.String[] environment35)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.entitlement.stub.EntitlementServiceException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getDecisionByAttributes31
            
          */
        public void startgetDecisionByAttributes(

            java.lang.String subject32,java.lang.String resource33,java.lang.String action34,java.lang.String[] environment35,

            final org.wso2.carbon.identity.entitlement.stub.EntitlementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getBooleanDecision38
                
             * @throws org.wso2.carbon.identity.entitlement.stub.EntitlementServiceException : 
         */

         
                     public boolean getBooleanDecision(

                        java.lang.String subject39,java.lang.String resource40,java.lang.String action41)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.entitlement.stub.EntitlementServiceException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getBooleanDecision38
            
          */
        public void startgetBooleanDecision(

            java.lang.String subject39,java.lang.String resource40,java.lang.String action41,

            final org.wso2.carbon.identity.entitlement.stub.EntitlementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    