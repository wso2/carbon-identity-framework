package org.wso2.carbon.identity.entitlement.endpoint.exception;

/**
 * Created by manujith on 7/26/16.
 */
public class UnauthorizedException extends AbstractEntitlementException {
    public UnauthorizedException(){
        super(40010);
    }
    public UnauthorizedException(String message){
        super(401,message);
    }
}
