package org.wso2.carbon.identity.entitlement.endpoint.exception;

/**
 * Created by manujith on 7/20/16.
 */
public class RequestParseException extends AbstractEntitlementException {
    public RequestParseException(){
        super(40020,"Error in request");
    }
    public RequestParseException(String s){
        super(40020,s);
    }
    public RequestParseException(int c,String s){
        super(c,s);
    }
    public RequestParseException(String s, Exception e){
        super(s,e);
    }
}
