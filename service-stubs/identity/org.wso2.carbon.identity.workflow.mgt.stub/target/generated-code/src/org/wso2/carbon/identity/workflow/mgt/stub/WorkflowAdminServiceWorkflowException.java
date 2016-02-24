
/**
 * WorkflowAdminServiceWorkflowException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.identity.workflow.mgt.stub;

public class WorkflowAdminServiceWorkflowException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290916677L;
    
    private org.wso2.carbon.identity.workflow.mgt.stub.mgt.WorkflowAdminServiceWorkflowException faultMessage;

    
        public WorkflowAdminServiceWorkflowException() {
            super("WorkflowAdminServiceWorkflowException");
        }

        public WorkflowAdminServiceWorkflowException(java.lang.String s) {
           super(s);
        }

        public WorkflowAdminServiceWorkflowException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public WorkflowAdminServiceWorkflowException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.identity.workflow.mgt.stub.mgt.WorkflowAdminServiceWorkflowException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.identity.workflow.mgt.stub.mgt.WorkflowAdminServiceWorkflowException getFaultMessage(){
       return faultMessage;
    }
}
    