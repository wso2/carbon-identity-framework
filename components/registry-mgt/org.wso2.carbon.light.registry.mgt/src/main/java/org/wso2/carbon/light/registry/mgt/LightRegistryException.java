package org.wso2.carbon.light.registry.mgt;

public class LightRegistryException extends Exception {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public LightRegistryException(String message) {

        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause of this exception.
     */
    public LightRegistryException(String message, Throwable cause) {

        super(message, cause);
    }
}
