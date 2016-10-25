package org.wso2.carbon.idp.mgt;

/**
 * Created by pasindutennage on 10/6/16.
 */
public class IdentityProviderSAMLException extends Exception {

    private static final long serialVersionUID = 3848393984629150057L;

    public IdentityProviderSAMLException(String message) {
        super(message);
    }

    public IdentityProviderSAMLException(String message, Throwable cause) {
        super(message, cause);
    }

}