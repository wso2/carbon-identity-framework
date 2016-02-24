
/**
 * EntitlementServiceIdentityException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.identity.entitlement.stub;

public class EntitlementServiceIdentityException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290830454L;
    
    private org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementServiceIdentityException faultMessage;

    
        public EntitlementServiceIdentityException() {
            super("EntitlementServiceIdentityException");
        }

        public EntitlementServiceIdentityException(java.lang.String s) {
           super(s);
        }

        public EntitlementServiceIdentityException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public EntitlementServiceIdentityException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementServiceIdentityException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementServiceIdentityException getFaultMessage(){
       return faultMessage;
    }
}
    