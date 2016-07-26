package org.wso2.carbon.identity.entitlement.endpoint.exception;

/**
 * Created by manujith on 7/20/16.
 */
public abstract class AbstractEntitlementException extends Exception{
    protected String description;
    protected int code;
    public AbstractEntitlementException(){
        this.code = -1;
        this.description = null;
    }
    public  AbstractEntitlementException(int code){
        this.code = code;
        this.description = null;
    }
    public AbstractEntitlementException(int code,String s){
        super(s);
        this.code = code;
        this.description = s;
    }
    public AbstractEntitlementException(String s){
        super(s);
        this.code = -1;
        this.description = s;
    }
    public AbstractEntitlementException(int code, String s, Exception e){
        super(s,e);
        this.code = code;
        this.description = s;

    }
    public AbstractEntitlementException(String s, Exception e){
        super(s,e);
        this.code = -1;
        this.description = s;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
