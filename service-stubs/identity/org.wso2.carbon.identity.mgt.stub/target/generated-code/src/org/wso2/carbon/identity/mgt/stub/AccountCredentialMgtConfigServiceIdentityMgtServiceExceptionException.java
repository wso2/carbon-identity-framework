
/**
 * AccountCredentialMgtConfigServiceIdentityMgtServiceExceptionException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.identity.mgt.stub;

public class AccountCredentialMgtConfigServiceIdentityMgtServiceExceptionException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290859632L;
    
    private org.wso2.carbon.identity.mgt.stub.AccountCredentialMgtConfigServiceIdentityMgtServiceException faultMessage;

    
        public AccountCredentialMgtConfigServiceIdentityMgtServiceExceptionException() {
            super("AccountCredentialMgtConfigServiceIdentityMgtServiceExceptionException");
        }

        public AccountCredentialMgtConfigServiceIdentityMgtServiceExceptionException(java.lang.String s) {
           super(s);
        }

        public AccountCredentialMgtConfigServiceIdentityMgtServiceExceptionException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public AccountCredentialMgtConfigServiceIdentityMgtServiceExceptionException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.identity.mgt.stub.AccountCredentialMgtConfigServiceIdentityMgtServiceException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.identity.mgt.stub.AccountCredentialMgtConfigServiceIdentityMgtServiceException getFaultMessage(){
       return faultMessage;
    }
}
    