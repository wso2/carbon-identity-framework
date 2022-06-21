package org.wso2.carbon.identity.mgt.endpoint.util.client;

public class EnterpriseLoginRetrievalClientException extends Exception {

    /**
     * Client exception with message and a throwable.
     *
     * @param message   Error message.
     * @param throwable Throwable.
     */
    public EnterpriseLoginRetrievalClientException(String message, Throwable throwable) {

        super(message, throwable);
    }

    /**
     * Client Exception with error message.
     *
     * @param message Error message
     */
    public EnterpriseLoginRetrievalClientException(String message) {

        super(message);
    }

}
