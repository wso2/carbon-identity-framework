
/**
 * PackageManagementException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.bpel.stub.mgt;

public class PackageManagementException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290937412L;
    
    private org.wso2.carbon.bpel.stub.mgt.types.PackageManagementException faultMessage;

    
        public PackageManagementException() {
            super("PackageManagementException");
        }

        public PackageManagementException(java.lang.String s) {
           super(s);
        }

        public PackageManagementException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public PackageManagementException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.bpel.stub.mgt.types.PackageManagementException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.bpel.stub.mgt.types.PackageManagementException getFaultMessage(){
       return faultMessage;
    }
}
    