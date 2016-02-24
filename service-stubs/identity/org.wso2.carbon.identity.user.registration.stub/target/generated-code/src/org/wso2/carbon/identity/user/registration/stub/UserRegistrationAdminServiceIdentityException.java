
/**
 * UserRegistrationAdminServiceIdentityException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.identity.user.registration.stub;

public class UserRegistrationAdminServiceIdentityException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290889206L;
    
    private org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceIdentityException faultMessage;

    
        public UserRegistrationAdminServiceIdentityException() {
            super("UserRegistrationAdminServiceIdentityException");
        }

        public UserRegistrationAdminServiceIdentityException(java.lang.String s) {
           super(s);
        }

        public UserRegistrationAdminServiceIdentityException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public UserRegistrationAdminServiceIdentityException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceIdentityException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceIdentityException getFaultMessage(){
       return faultMessage;
    }
}
    