

/**
 * ClaimManagementService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.claim.mgt.stub;

    /*
     *  ClaimManagementService java interface
     */

    public interface ClaimManagementService {
          

        /**
          * Auto generated method signature
          * 
                    * @param getClaimMappingByDialect4
                
             * @throws org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceException : 
         */

         
                     public org.wso2.carbon.claim.mgt.stub.dto.ClaimDialectDTO getClaimMappingByDialect(

                        java.lang.String dialectUri5)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getClaimMappingByDialect4
            
          */
        public void startgetClaimMappingByDialect(

            java.lang.String dialectUri5,

            final org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceException : 
         */
        public void  addNewClaimMapping(
         org.wso2.carbon.claim.mgt.stub.dto.ClaimMappingDTO claimMappingDTO9

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceException : 
         */
        public void  addNewClaimDialect(
         org.wso2.carbon.claim.mgt.stub.dto.ClaimDialectDTO claimDialectDTO11

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceException : 
         */
        public void  removeClaimMapping(
         java.lang.String dialectUri13,java.lang.String claimUri14

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceException : 
         */
        public void  removeClaimDialect(
         java.lang.String dialectUri16

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceException : 
         */
        public void  upateClaimMapping(
         org.wso2.carbon.claim.mgt.stub.dto.ClaimMappingDTO claimMappingDTO18

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getClaimMappings19
                
             * @throws org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceException : 
         */

         
                     public org.wso2.carbon.claim.mgt.stub.dto.ClaimDialectDTO[] getClaimMappings(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getClaimMappings19
            
          */
        public void startgetClaimMappings(

            

            final org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    