package org.wso2.carbon.identity.entitlement.endpoint.exception;

/**
 * Created by manujith on 7/20/16.
 */
public abstract class EntitlementException extends Exception{
    public EntitlementException(){

    }
    public EntitlementException(String s){
        super(s);
    }
    public EntitlementException(String s, Exception e){
        super(s,e);
    }
}
