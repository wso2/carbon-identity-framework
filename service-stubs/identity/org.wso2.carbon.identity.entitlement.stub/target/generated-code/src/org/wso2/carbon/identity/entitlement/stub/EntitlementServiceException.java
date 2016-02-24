
/**
 * EntitlementServiceException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.identity.entitlement.stub;

public class EntitlementServiceException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290830444L;
    
    private org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementServiceException faultMessage;

    
        public EntitlementServiceException() {
            super("EntitlementServiceException");
        }

        public EntitlementServiceException(java.lang.String s) {
           super(s);
        }

        public EntitlementServiceException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public EntitlementServiceException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementServiceException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementServiceException getFaultMessage(){
       return faultMessage;
    }
}
    