

/**
 * RelyingPartyAdminService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.identity.provider.stub;

    /*
     *  RelyingPartyAdminService java interface
     */

    public interface RelyingPartyAdminService {
          
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.provider.stub.Exception : 
         */
        public void  removeUserTrustedRelyingParty(
         org.wso2.carbon.identity.provider.stub.rp.dto.UserTrustedRPDTO userrp2

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.provider.stub.Exception;

        

        /**
          * Auto generated method signature
          * 
                    * @param getAllUserTrustedRelyingParties3
                
             * @throws org.wso2.carbon.identity.provider.stub.Exception : 
         */

         
                     public org.wso2.carbon.identity.provider.stub.rp.dto.UserTrustedRPDTO[] getAllUserTrustedRelyingParties(

                        java.lang.String userId4)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.identity.provider.stub.Exception;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllUserTrustedRelyingParties3
            
          */
        public void startgetAllUserTrustedRelyingParties(

            java.lang.String userId4,

            final org.wso2.carbon.identity.provider.stub.RelyingPartyAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.identity.provider.stub.Exception : 
         */
        public void  createUserTrustedRP(
         org.wso2.carbon.identity.provider.stub.rp.dto.UserTrustedRPDTO userrp8

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.identity.provider.stub.Exception;

        

        
       //
       }
    