package org.wso2.carbon.identity.api.idpmgt.endpoint;

//APIException class is used when an exception occurred at client level
public class ApiException extends Exception {

    private int code;

    public ApiException(int code, String msg) {

        super(msg);
        this.code = code;
    }
}
