/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.rule.metadata.internal.util;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataClientException;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataServerException;

/**
 * Utility class for building RuleMetadata exceptions.
 */
public class RuleMetadataExceptionBuilder {

    private RuleMetadataExceptionBuilder() {

    }

    public static RuleMetadataClientException buildClientException(RuleMetadataError error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }

        return new RuleMetadataClientException(error.getCode(), error.getMessage(), description);
    }

    public static RuleMetadataServerException buildServerException(RuleMetadataError error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }

        return new RuleMetadataServerException(error.getCode(), error.getMessage(), description);
    }

    public static RuleMetadataServerException buildServerException(RuleMetadataError error, Throwable e,
                                                                   String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }

        return new RuleMetadataServerException(error.getCode(), error.getMessage(), description, e);
    }

    /**
     * Enum class to represent the rule metadata errors.
     */
    public enum RuleMetadataError {

        // Client Errors.
        ERROR_INVALID_FLOW_TYPE("60001", "Invalid flow.", "Provided flow type is invalid or not implemented."),

        // Server Errors.
        ERROR_WHILE_LOADING_STATIC_RULE_METADATA("65001", "Error while loading static rule metadata.",
                "Server encountered an error while loading rule metadata from server configs."),
        ERROR_DUPLICATE_FIELD("65002", "Duplicate field found.",
                "Field: %s from metadata provider: %s already exists in the rule metadata."),
        ;

        private final String code;
        private final String message;
        private final String description;

        RuleMetadataError(String code, String message, String description) {

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
