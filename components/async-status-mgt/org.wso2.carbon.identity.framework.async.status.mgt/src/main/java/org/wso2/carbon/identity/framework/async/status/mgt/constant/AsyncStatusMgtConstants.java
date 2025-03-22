package org.wso2.carbon.identity.framework.async.status.mgt.constant;

/**
 * Asynchronous operation status management constants
 */
public class AsyncStatusMgtConstants {

    public static final String ERROR_PREFIX = "ASM-";

    /**
     * Enum for Error Message
     */
    public enum ErrorMessage {
        ;

        private final String code;
        private final String message;
        private final String description;

        ErrorMessage(String code, String message, String description) {

            this.code = code;
            this.message = message;
            this.description = description;
        }

        public String getCode() {

            return ERROR_PREFIX + code;
        }

        public String getMessage() {

            return message;
        }

        public String getDescription() {

            return description;
        }

        @Override
        public String toString() {

            return code + " | " + message;
        }
    }

}
