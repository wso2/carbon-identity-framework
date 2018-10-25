package org.wso2.carbon.identity.functions.library.mgt.exception;

/**
 * Function library manager Exception.
 */
public class FunctionLibraryManagementException extends Exception {
    private String message;


    /**
     *
     * @param message
     */
    public  FunctionLibraryManagementException (String message) {
        super(message);
        this.message = message;
    }

    /**
     *
     * @param message
     * @param e
     */
    public  FunctionLibraryManagementException (String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
