
/**
 * Exception.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.identity.provider.stub;

public class Exception extends java.lang.Exception{

    private static final long serialVersionUID = 1456290875744L;
    
    private org.wso2.carbon.identity.provider.stub.rp.types.axis2.ExceptionE faultMessage;

    
        public Exception() {
            super("Exception");
        }

        public Exception(java.lang.String s) {
           super(s);
        }

        public Exception(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public Exception(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.identity.provider.stub.rp.types.axis2.ExceptionE msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.identity.provider.stub.rp.types.axis2.ExceptionE getFaultMessage(){
       return faultMessage;
    }
}
    