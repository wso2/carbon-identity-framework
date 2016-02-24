
/**
 * UserAdminUserAdminException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.user.mgt.stub;

public class UserAdminUserAdminException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290906179L;
    
    private org.wso2.carbon.user.mgt.stub.types.axis2.UserAdminUserAdminException faultMessage;

    
        public UserAdminUserAdminException() {
            super("UserAdminUserAdminException");
        }

        public UserAdminUserAdminException(java.lang.String s) {
           super(s);
        }

        public UserAdminUserAdminException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public UserAdminUserAdminException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.user.mgt.stub.types.axis2.UserAdminUserAdminException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.user.mgt.stub.types.axis2.UserAdminUserAdminException getFaultMessage(){
       return faultMessage;
    }
}
    