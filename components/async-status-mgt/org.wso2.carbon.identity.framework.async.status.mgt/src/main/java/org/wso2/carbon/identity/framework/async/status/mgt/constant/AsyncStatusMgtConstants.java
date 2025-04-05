package org.wso2.carbon.identity.framework.async.status.mgt.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.OperationModelProperties.MODEL_CORRELATION_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.OperationModelProperties.MODEL_LAST_MODIFIED;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.OperationModelProperties.MODEL_OPERATION_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.OperationModelProperties.MODEL_INITIATED_ORG_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.OperationModelProperties.MODEL_INITIATED_USER_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.OperationModelProperties.MODEL_OPERATION_POLICY;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.OperationModelProperties.MODEL_OPERATION_STATUS;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.OperationModelProperties.MODEL_SUBJECT_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.OperationModelProperties.MODEL_SUBJECT_TYPE;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.OperationModelProperties.MODEL_OPERATION_TYPE;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.OperationPlaceholders.CORRELATION_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.OperationPlaceholders.LAST_MODIFIED;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.OperationPlaceholders.INITIATED_ORG_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.OperationPlaceholders.INITIATED_USER_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.OperationPlaceholders.OPERATION_POLICY;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.OperationPlaceholders.OPERATION_STATUS;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.OperationPlaceholders.OPERATION_SUBJECT_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.OperationPlaceholders.OPERATION_SUBJECT_TYPE;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.OperationPlaceholders.OPERATION_TYPE;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.UnitOperationModelProperties.MODEL_CREATED_AT;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.UnitOperationModelProperties.MODEL_STATUS_MESSAGE;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.UnitOperationModelProperties.MODEL_RESIDENT_RESOURCE_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.UnitOperationModelProperties.MODEL_TARGET_ORG_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.UnitOperationModelProperties.MODEL_UNIT_OPERATION_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.UnitOperationModelProperties.MODEL_UNIT_OPERATION_STATUS;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.UnitOperationPlaceholders.CREATED_AT;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.UnitOperationPlaceholders.OPERATION_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.UnitOperationPlaceholders.OPERATION_STATUS_MESSAGE;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.UnitOperationPlaceholders.RESIDENT_RESOURCE_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.UnitOperationPlaceholders.TARGET_ORG_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.UnitOperationPlaceholders.UNIT_OPERATION_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.UnitOperationPlaceholders.UNIT_OPERATION_STATUS;

/**
 * Asynchronous operation status management constants
 */
public class AsyncStatusMgtConstants {

    public static final String ERROR_PREFIX = "ASM-";
    public static final String DESC_SORT_ORDER = "DESC";
    public static final String ASC_SORT_ORDER = "ASC";
    public static final String FILTER_PLACEHOLDER_PREFIX = "FILTER_ID_";

    private static final Map<String, String> attributeColumnMap = new HashMap<>();
    public static final Map<String, String> ATTRIBURE_COLUMN_MAP = Collections.unmodifiableMap(attributeColumnMap);

    public static final String EQ = "eq";
    public static final String CO = "co";
    public static final String SW = "sw";
    public static final String EW = "ew";
    public static final String GE = "ge";
    public static final String LE = "le";
    public static final String GT = "gt";
    public static final String LT = "lt";
    public static final String AND = "and";
    public static final String OR = "or";
    public static final String NOT = "not";

    public static final String PAGINATION_AFTER = "after";
    public static final String PAGINATION_BEFORE = "before";

    static {
        attributeColumnMap.put(MODEL_UNIT_OPERATION_ID, UNIT_OPERATION_ID);
        attributeColumnMap.put(MODEL_OPERATION_ID, OPERATION_ID);
        attributeColumnMap.put(MODEL_RESIDENT_RESOURCE_ID, RESIDENT_RESOURCE_ID);
        attributeColumnMap.put(MODEL_TARGET_ORG_ID, TARGET_ORG_ID);
        attributeColumnMap.put(MODEL_UNIT_OPERATION_STATUS, UNIT_OPERATION_STATUS);
        attributeColumnMap.put(MODEL_STATUS_MESSAGE, OPERATION_STATUS_MESSAGE);
        attributeColumnMap.put(MODEL_CREATED_AT, CREATED_AT);

        attributeColumnMap.put(MODEL_CORRELATION_ID, CORRELATION_ID);
        attributeColumnMap.put(MODEL_OPERATION_TYPE, OPERATION_TYPE);
        attributeColumnMap.put(MODEL_SUBJECT_TYPE, OPERATION_SUBJECT_TYPE);
        attributeColumnMap.put(MODEL_SUBJECT_ID, OPERATION_SUBJECT_ID);
        attributeColumnMap.put(MODEL_INITIATED_ORG_ID, INITIATED_ORG_ID);
        attributeColumnMap.put(MODEL_INITIATED_USER_ID, INITIATED_USER_ID);
        attributeColumnMap.put(MODEL_OPERATION_STATUS, OPERATION_STATUS);
        attributeColumnMap.put(MODEL_LAST_MODIFIED, LAST_MODIFIED);
        attributeColumnMap.put(MODEL_OPERATION_POLICY, OPERATION_POLICY);

        attributeColumnMap.put(PAGINATION_AFTER, CREATED_AT);
        attributeColumnMap.put(PAGINATION_BEFORE, CREATED_AT);
    }

    /**
     * Enum for Error Message
     */
    public static enum ErrorMessages {
        ERROR_CODE_INVALID_REQUEST_BODY("xx001", "Invalid request.",
                "Provided request body content is not in the expected format.");

        private final String code;
        private final String message;
        private final String description;

        private ErrorMessages(String code, String message, String description) {

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
