
/**
 * UserRegistrationAdminServiceException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.identity.user.registration.stub;

public class UserRegistrationAdminServiceException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290889197L;
    
    private org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceException faultMessage;

    
        public UserRegistrationAdminServiceException() {
            super("UserRegistrationAdminServiceException");
        }

        public UserRegistrationAdminServiceException(java.lang.String s) {
           super(s);
        }

        public UserRegistrationAdminServiceException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public UserRegistrationAdminServiceException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.identity.user.registration.stub.types.axis2.UserRegistrationAdminServiceException getFaultMessage(){
       return faultMessage;
    }
}
    