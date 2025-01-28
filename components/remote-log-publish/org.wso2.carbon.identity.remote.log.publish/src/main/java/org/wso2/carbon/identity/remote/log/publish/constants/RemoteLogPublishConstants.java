package org.wso2.carbon.identity.remote.log.publish.constants;

public class RemoteLogPublishConstants {

    public static final String SERVICE_PROPERTY_KEY_SERVICE_NAME = "service.name";
    public static final String SERVICE_PROPERTY_VAL_REMOTE_LOG_PUBLISH = "RemoteLogPublish";

    /**
     * Error messages.
     */
    public enum ErrorMessages {

        // Client errors.


        // Server errors.
        ERROR_WHILE_ADDING_CONFIG("65001", "Error while adding remote log publish config",
                "Error while persisting remote log publish config in the system."),
        ERROR_WHILE_RETRIEVING_CONFIG("65002",
                "Error while retrieving remote log publish config",
                "Error while retrieving remote log publish config from the system."),
        ERROR_WHILE_UPDATING_CONFIG("65003", "Error while updating remote log publish config",
                "Error while updating remote log publish config in the system."),
        ERROR_WHILE_DELETING_CONFIG("65004", "Error while deleting remote log publish config",
                "Error while deleting remote log publish config from the system.");
        private final String code;
        private final String message;
        private final String description;

        ErrorMessages(String code, String message, String description) {

            this.code = code;
            this.message = message;
            this.description = description;
        }

        public String getCode() {

            return code;
        }

        public String getMessage() {

            return message;
        }

        public String getDescription() {

            return description;
        }
    }
}
