
/**
 * IllegalAccessFault.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.humantask.stub.ui.task.client.api;

public class IllegalAccessFault extends java.lang.Exception{

    private static final long serialVersionUID = 1456290932192L;
    
    private org.wso2.carbon.humantask.stub.api.IllegalAccess faultMessage;

    
        public IllegalAccessFault() {
            super("IllegalAccessFault");
        }

        public IllegalAccessFault(java.lang.String s) {
           super(s);
        }

        public IllegalAccessFault(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public IllegalAccessFault(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.humantask.stub.api.IllegalAccess msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.humantask.stub.api.IllegalAccess getFaultMessage(){
       return faultMessage;
    }
}
    