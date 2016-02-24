
/**
 * UserRegistrationAdminServiceUserRegistrationException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.identity.user.registration.stub;

public class UserRegistrationAdminServiceUserRegistrationException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290889186L;
    
    private org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceUserRegistrationException faultMessage;

    
        public UserRegistrationAdminServiceUserRegistrationException() {
            super("UserRegistrationAdminServiceUserRegistrationException");
        }

        public UserRegistrationAdminServiceUserRegistrationException(java.lang.String s) {
           super(s);
        }

        public UserRegistrationAdminServiceUserRegistrationException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public UserRegistrationAdminServiceUserRegistrationException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceUserRegistrationException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceUserRegistrationException getFaultMessage(){
       return faultMessage;
    }
}
    