
/**
 * UserProfileMgtServiceUserProfileExceptionException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.identity.user.profile.stub;

public class UserProfileMgtServiceUserProfileExceptionException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290884377L;
    
    private org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileException faultMessage;

    
        public UserProfileMgtServiceUserProfileExceptionException() {
            super("UserProfileMgtServiceUserProfileExceptionException");
        }

        public UserProfileMgtServiceUserProfileExceptionException(java.lang.String s) {
           super(s);
        }

        public UserProfileMgtServiceUserProfileExceptionException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public UserProfileMgtServiceUserProfileExceptionException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileException getFaultMessage(){
       return faultMessage;
    }
}
    