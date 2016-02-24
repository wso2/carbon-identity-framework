
/**
 * ClaimManagementServiceException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.claim.mgt.stub;

public class ClaimManagementServiceException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290817995L;
    
    private org.wso2.carbon.claim.mgt.stub.types.axis2.ClaimManagementServiceException faultMessage;

    
        public ClaimManagementServiceException() {
            super("ClaimManagementServiceException");
        }

        public ClaimManagementServiceException(java.lang.String s) {
           super(s);
        }

        public ClaimManagementServiceException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public ClaimManagementServiceException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.claim.mgt.stub.types.axis2.ClaimManagementServiceException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.claim.mgt.stub.types.axis2.ClaimManagementServiceException getFaultMessage(){
       return faultMessage;
    }
}
    