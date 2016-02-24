
/**
 * IdentityApplicationManagementServiceIdentityApplicationManagementException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.identity.application.mgt.stub;

public class IdentityApplicationManagementServiceIdentityApplicationManagementException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290822169L;
    
    private org.wso2.carbon.identity.application.mgt.stub.types.axis2.IdentityApplicationManagementServiceIdentityApplicationManagementException faultMessage;

    
        public IdentityApplicationManagementServiceIdentityApplicationManagementException() {
            super("IdentityApplicationManagementServiceIdentityApplicationManagementException");
        }

        public IdentityApplicationManagementServiceIdentityApplicationManagementException(java.lang.String s) {
           super(s);
        }

        public IdentityApplicationManagementServiceIdentityApplicationManagementException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public IdentityApplicationManagementServiceIdentityApplicationManagementException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.identity.application.mgt.stub.types.axis2.IdentityApplicationManagementServiceIdentityApplicationManagementException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.identity.application.mgt.stub.types.axis2.IdentityApplicationManagementServiceIdentityApplicationManagementException getFaultMessage(){
       return faultMessage;
    }
}
    