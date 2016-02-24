
/**
 * MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.user.mgt.stub;

public class MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290908451L;
    
    private org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException faultMessage;

    
        public MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException() {
            super("MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException");
        }

        public MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException(java.lang.String s) {
           super(s);
        }

        public MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public MultipleCredentialsUserAdminMultipleCredentialsUserAdminExceptionException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.user.mgt.multiplecredentials.stub.types.carbon.MultipleCredentialsUserAdminMultipleCredentialsUserAdminException getFaultMessage(){
       return faultMessage;
    }
}
    