package org.wso2.carbon.identity.entitlement.endpoint.exception;

/**
 * Created by manujith on 7/20/16.
 */
public class RequestParseExceptionAbstract extends AbstractEntitlementException {
    public RequestParseExceptionAbstract(){

    }
    public RequestParseExceptionAbstract(String s){
        super(s);
    }
    public RequestParseExceptionAbstract(String s, Exception e){
        super(s,e);
    }
}
