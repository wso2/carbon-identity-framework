
/**
 * UserInformationRecoveryServiceIdentityExceptionException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.identity.mgt.stub;

public class UserInformationRecoveryServiceIdentityExceptionException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290855405L;
    
    private org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityException faultMessage;

    
        public UserInformationRecoveryServiceIdentityExceptionException() {
            super("UserInformationRecoveryServiceIdentityExceptionException");
        }

        public UserInformationRecoveryServiceIdentityExceptionException(java.lang.String s) {
           super(s);
        }

        public UserInformationRecoveryServiceIdentityExceptionException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public UserInformationRecoveryServiceIdentityExceptionException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityException getFaultMessage(){
       return faultMessage;
    }
}
    