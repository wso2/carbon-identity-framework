package org.wso2.carbon.identity.entitlement.endpoint.exception;

/**
 * Created by manujith on 7/20/16.
 */
public class ResponseWriteException extends AbstractEntitlementException {
    public ResponseWriteException(){
        super(40030,"Error in Response");
    }
    public ResponseWriteException(String s){
        super(40020,s);
    }
    public ResponseWriteException(int c,String s){ super(c,s); }
    public ResponseWriteException(String s, Exception e){
        super(s,e);
    }
}
