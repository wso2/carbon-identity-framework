
/**
 * IdentityProviderAdminServiceException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.identity.provider.stub;

public class IdentityProviderAdminServiceException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290873444L;
    
    private org.wso2.carbon.identity.provider.stub.types.axis2.IdentityProviderAdminServiceException faultMessage;

    
        public IdentityProviderAdminServiceException() {
            super("IdentityProviderAdminServiceException");
        }

        public IdentityProviderAdminServiceException(java.lang.String s) {
           super(s);
        }

        public IdentityProviderAdminServiceException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public IdentityProviderAdminServiceException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.identity.provider.stub.types.axis2.IdentityProviderAdminServiceException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.identity.provider.stub.types.axis2.IdentityProviderAdminServiceException getFaultMessage(){
       return faultMessage;
    }
}
    