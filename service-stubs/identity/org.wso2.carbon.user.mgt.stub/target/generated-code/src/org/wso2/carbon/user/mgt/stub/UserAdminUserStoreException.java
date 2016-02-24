
/**
 * UserAdminUserStoreException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.user.mgt.stub;

public class UserAdminUserStoreException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290906169L;
    
    private org.wso2.carbon.user.mgt.stub.types.axis2.UserAdminUserStoreException faultMessage;

    
        public UserAdminUserStoreException() {
            super("UserAdminUserStoreException");
        }

        public UserAdminUserStoreException(java.lang.String s) {
           super(s);
        }

        public UserAdminUserStoreException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public UserAdminUserStoreException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.user.mgt.stub.types.axis2.UserAdminUserStoreException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.user.mgt.stub.types.axis2.UserAdminUserStoreException getFaultMessage(){
       return faultMessage;
    }
}
    