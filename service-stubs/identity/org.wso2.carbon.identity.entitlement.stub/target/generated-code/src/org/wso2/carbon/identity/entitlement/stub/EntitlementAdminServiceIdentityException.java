
/**
 * EntitlementAdminServiceIdentityException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.identity.entitlement.stub;

public class EntitlementAdminServiceIdentityException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290832637L;
    
    private org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementAdminServiceIdentityException faultMessage;

    
        public EntitlementAdminServiceIdentityException() {
            super("EntitlementAdminServiceIdentityException");
        }

        public EntitlementAdminServiceIdentityException(java.lang.String s) {
           super(s);
        }

        public EntitlementAdminServiceIdentityException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public EntitlementAdminServiceIdentityException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementAdminServiceIdentityException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.identity.entitlement.stub.types.axis2.EntitlementAdminServiceIdentityException getFaultMessage(){
       return faultMessage;
    }
}
    