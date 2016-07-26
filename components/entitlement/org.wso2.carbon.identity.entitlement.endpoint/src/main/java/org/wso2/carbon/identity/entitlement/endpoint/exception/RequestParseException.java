package org.wso2.carbon.identity.entitlement.endpoint.exception;

/**
 * Created by manujith on 7/20/16.
 */
public class RequestParseException extends EntitlementException {
    public RequestParseException(){

    }
    public RequestParseException(String s){
        super(s);
    }
    public RequestParseException(String s,Exception e){
        super(s,e);
    }
}
