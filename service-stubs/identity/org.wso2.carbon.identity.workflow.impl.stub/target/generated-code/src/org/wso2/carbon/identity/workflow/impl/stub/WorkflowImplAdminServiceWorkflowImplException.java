
/**
 * WorkflowImplAdminServiceWorkflowImplException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.identity.workflow.impl.stub;

public class WorkflowImplAdminServiceWorkflowImplException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290921617L;
    
    private org.wso2.carbon.identity.workflow.impl.xsd.WorkflowImplAdminServiceWorkflowImplException faultMessage;

    
        public WorkflowImplAdminServiceWorkflowImplException() {
            super("WorkflowImplAdminServiceWorkflowImplException");
        }

        public WorkflowImplAdminServiceWorkflowImplException(java.lang.String s) {
           super(s);
        }

        public WorkflowImplAdminServiceWorkflowImplException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public WorkflowImplAdminServiceWorkflowImplException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.identity.workflow.impl.xsd.WorkflowImplAdminServiceWorkflowImplException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.identity.workflow.impl.xsd.WorkflowImplAdminServiceWorkflowImplException getFaultMessage(){
       return faultMessage;
    }
}
    