
/**
 * IllegalStateFault.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.humantask.stub.ui.task.client.api;

public class IllegalStateFault extends java.lang.Exception{

    private static final long serialVersionUID = 1456290932160L;
    
    private org.wso2.carbon.humantask.stub.api.IllegalState faultMessage;

    
        public IllegalStateFault() {
            super("IllegalStateFault");
        }

        public IllegalStateFault(java.lang.String s) {
           super(s);
        }

        public IllegalStateFault(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public IllegalStateFault(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.humantask.stub.api.IllegalState msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.humantask.stub.api.IllegalState getFaultMessage(){
       return faultMessage;
    }
}
    