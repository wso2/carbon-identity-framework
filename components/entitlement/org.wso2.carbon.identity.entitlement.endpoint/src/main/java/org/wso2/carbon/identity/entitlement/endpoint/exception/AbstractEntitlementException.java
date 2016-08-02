package org.wso2.carbon.identity.entitlement.endpoint.exception;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.*;

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

    public ExceptionBean getExceptioBean(){
        return new ExceptionBean(code,description);
    }
}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "code",
        "message"
})
@XmlRootElement(name = "Error")
class ExceptionBean{
    @XmlElement
    private int code;
    @XmlElement
    private String message;

    public ExceptionBean(){
        //No-arg default constructor needed for JAXB
    }

    public ExceptionBean(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
