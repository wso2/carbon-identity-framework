
/**
 * IdentityProviderMgtServiceIdentityProviderManagementExceptionException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.idp.mgt.stub;

public class IdentityProviderMgtServiceIdentityProviderManagementExceptionException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290897753L;
    
    private org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementException faultMessage;

    
        public IdentityProviderMgtServiceIdentityProviderManagementExceptionException() {
            super("IdentityProviderMgtServiceIdentityProviderManagementExceptionException");
        }

        public IdentityProviderMgtServiceIdentityProviderManagementExceptionException(java.lang.String s) {
           super(s);
        }

        public IdentityProviderMgtServiceIdentityProviderManagementExceptionException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public IdentityProviderMgtServiceIdentityProviderManagementExceptionException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.idp.mgt.stub.IdentityProviderMgtServiceIdentityProviderManagementException getFaultMessage(){
       return faultMessage;
    }
}
    