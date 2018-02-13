package org.wso2.carbon.identity.mgt.endpoint.client;

public class ConsentMgtClientException extends Exception{

    public ConsentMgtClientException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ConsentMgtClientException(String message) {
        super(message);
    }

}
