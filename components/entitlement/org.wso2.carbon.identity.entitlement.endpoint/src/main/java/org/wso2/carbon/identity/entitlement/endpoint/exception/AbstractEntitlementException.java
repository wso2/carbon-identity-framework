/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.entitlement.endpoint.exception;

import javax.xml.bind.annotation.*;

/**
 * Abstract class for custom exceptions thrown from Entitlement Endpoint
 * Concrete excpetions will be implemented from this class
 */
public abstract class AbstractEntitlementException extends Exception{
    //Custom exception detail
    protected String description;
    //Error code described under the Errors section in User Documentation
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
/**
 * Java Bean Class to be used as a response object for the service.
 * When an exception occurs, the ExceptionMapper will catch it
 * and an ExceptionBean object will be created according to the exception caught.
 */
class ExceptionBean{
    //Corresponds to error code in AbstractException
    @XmlElement
    private int code;
    //Corresponds to error message in AbstractException
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
