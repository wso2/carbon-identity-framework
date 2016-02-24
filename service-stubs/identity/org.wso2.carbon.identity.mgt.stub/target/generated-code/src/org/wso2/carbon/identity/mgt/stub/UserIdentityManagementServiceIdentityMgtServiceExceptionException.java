
/**
 * UserIdentityManagementServiceIdentityMgtServiceExceptionException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.identity.mgt.stub;

public class UserIdentityManagementServiceIdentityMgtServiceExceptionException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290852487L;
    
    private org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceIdentityMgtServiceException faultMessage;

    
        public UserIdentityManagementServiceIdentityMgtServiceExceptionException() {
            super("UserIdentityManagementServiceIdentityMgtServiceExceptionException");
        }

        public UserIdentityManagementServiceIdentityMgtServiceExceptionException(java.lang.String s) {
           super(s);
        }

        public UserIdentityManagementServiceIdentityMgtServiceExceptionException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public UserIdentityManagementServiceIdentityMgtServiceExceptionException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceIdentityMgtServiceException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.identity.mgt.stub.UserIdentityManagementServiceIdentityMgtServiceException getFaultMessage(){
       return faultMessage;
    }
}
    