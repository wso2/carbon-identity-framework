
/**
 * IdentityProviderException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.identity.provider.stub;

public class IdentityProviderException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290877832L;
    
    private org.wso2.carbon.identity.provider.stub.types.axis2.IdentityProviderExceptionE faultMessage;

    
        public IdentityProviderException() {
            super("IdentityProviderException");
        }

        public IdentityProviderException(java.lang.String s) {
           super(s);
        }

        public IdentityProviderException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public IdentityProviderException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.identity.provider.stub.types.axis2.IdentityProviderExceptionE msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.identity.provider.stub.types.axis2.IdentityProviderExceptionE getFaultMessage(){
       return faultMessage;
    }
}
    