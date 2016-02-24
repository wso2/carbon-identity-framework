
/**
 * UserManagementWorkflowServiceWorkflowExceptionException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.user.mgt.workflow.stub;

public class UserManagementWorkflowServiceWorkflowExceptionException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290913708L;
    
    private org.wso2.carbon.user.mgt.workflow.UserManagementWorkflowServiceWorkflowException faultMessage;

    
        public UserManagementWorkflowServiceWorkflowExceptionException() {
            super("UserManagementWorkflowServiceWorkflowExceptionException");
        }

        public UserManagementWorkflowServiceWorkflowExceptionException(java.lang.String s) {
           super(s);
        }

        public UserManagementWorkflowServiceWorkflowExceptionException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public UserManagementWorkflowServiceWorkflowExceptionException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.user.mgt.workflow.UserManagementWorkflowServiceWorkflowException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.user.mgt.workflow.UserManagementWorkflowServiceWorkflowException getFaultMessage(){
       return faultMessage;
    }
}
    