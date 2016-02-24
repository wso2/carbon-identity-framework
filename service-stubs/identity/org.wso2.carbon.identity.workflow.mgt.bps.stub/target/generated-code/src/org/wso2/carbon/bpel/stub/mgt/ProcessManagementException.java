
/**
 * ProcessManagementException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.bpel.stub.mgt;

public class ProcessManagementException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290934905L;
    
    private org.wso2.carbon.bpel.stub.mgt.types.ProcessManagementException faultMessage;

    
        public ProcessManagementException() {
            super("ProcessManagementException");
        }

        public ProcessManagementException(java.lang.String s) {
           super(s);
        }

        public ProcessManagementException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public ProcessManagementException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.bpel.stub.mgt.types.ProcessManagementException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.bpel.stub.mgt.types.ProcessManagementException getFaultMessage(){
       return faultMessage;
    }
}
    