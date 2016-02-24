
/**
 * IllegalOperationFault.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.humantask.stub.ui.task.client.api;

public class IllegalOperationFault extends java.lang.Exception{

    private static final long serialVersionUID = 1456290932170L;
    
    private org.wso2.carbon.humantask.stub.api.IllegalOperation faultMessage;

    
        public IllegalOperationFault() {
            super("IllegalOperationFault");
        }

        public IllegalOperationFault(java.lang.String s) {
           super(s);
        }

        public IllegalOperationFault(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public IllegalOperationFault(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.humantask.stub.api.IllegalOperation msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.humantask.stub.api.IllegalOperation getFaultMessage(){
       return faultMessage;
    }
}
    