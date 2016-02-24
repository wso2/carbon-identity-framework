
/**
 * DirectoryServerManagerExceptionException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.directory.server.manager.stub;

public class DirectoryServerManagerExceptionException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290902165L;
    
    private org.wso2.carbon.directory.server.stub.types.carbon.DirectoryServerManagerExceptionE faultMessage;

    
        public DirectoryServerManagerExceptionException() {
            super("DirectoryServerManagerExceptionException");
        }

        public DirectoryServerManagerExceptionException(java.lang.String s) {
           super(s);
        }

        public DirectoryServerManagerExceptionException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public DirectoryServerManagerExceptionException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.directory.server.stub.types.carbon.DirectoryServerManagerExceptionE msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.directory.server.stub.types.carbon.DirectoryServerManagerExceptionE getFaultMessage(){
       return faultMessage;
    }
}
    