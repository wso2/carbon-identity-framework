/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.claim.metadata.mgt.util;

import org.wso2.carbon.user.core.UserCoreConstants;

/**
 * Holds the claim metadata related constants.
 */
public class ClaimConstants {

    public static final String LOCAL_CLAIM_DIALECT_URI = UserCoreConstants.DEFAULT_CARBON_DIALECT;

    public static final String DIALECT_PROPERTY = "Dialect";
    public static final String DISPLAY_NAME_PROPERTY = "DisplayName";
    public static final String ATTRIBUTE_ID_PROPERTY = "AttributeID";
    public static final String DESCRIPTION_PROPERTY = "Description";
    public static final String REQUIRED_PROPERTY = "Required";
    public static final String DISPLAY_ORDER_PROPERTY = "DisplayOrder";
    public static final String SUPPORTED_BY_DEFAULT_PROPERTY = "SupportedByDefault";
    public static final String REGULAR_EXPRESSION_PROPERTY = "RegEx";
    public static final String READ_ONLY_PROPERTY = "ReadOnly";
    public static final String CLAIM_URI_PROPERTY = "ClaimURI";

    public static final String DEFAULT_ATTRIBUTE = "DefaultAttribute";
    public static final String MAPPED_LOCAL_CLAIM_PROPERTY = "MappedLocalClaim";

    /**
     * Enum for error messages.
     */
    public enum ErrorMessage {

        ERROR_CODE_EMPTY_CLAIM_DIALECT("100001",
                "Claim dialect cannot be empty"),
        ERROR_CODE_EMPTY_LOCAL_CLAIM_URI("100002",
                "Local claim URI cannot be empty"),
        ERROR_CODE_EMPTY_MAPPED_ATTRIBUTES_IN_LOCAL_CLAIM("100003",
                "Mapped attribute of the claim dialect URI : %s and Claim URI : %s cannot be empty"),
        ERROR_CODE_LOCAL_CLAIM_HAS_MAPPED_EXTERNAL_CLAIM("100004",
                "Cannot remove local claim %s while having associations with external claims."),
        ERROR_CODE_EMPTY_EXTERNAL_CLAIM_URI("100005",
                "External claim URI cannot be empty"),
        ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT("100006",
                "Invalid external claim dialect " + LOCAL_CLAIM_DIALECT_URI),
        ERROR_CODE_EMPTY_EXTERNAL_DIALECT_URI("100007",
                "External dialect URI cannot be empty"),
        ERROR_CODE_MAPPED_TO_EMPTY_LOCAL_CLAIM_URI("100008",
                "Mapped local claim URI cannot be empty"),
        ERROR_CODE_MAPPED_TO_INVALID_LOCAL_CLAIM_URI("100009",
                "Invalid Claim URI : %s for Claim Dialect : %s"),
        ERROR_CODE_EMPTY_TENANT_DOMAIN("60000", "Empty tenant domain in the request"),
        ERROR_CODE_DELETE_IDN_CLAIM_MAPPED_ATTRIBUTE("65001", "Error occurred while deleting claim " +
                "mapped attributes for domain : %s with tenant Id : %s from table : IDN_CLAIM_MAPPED_ATTRIBUTE"),
        ERROR_CODE_SERVER_ERROR_DELETING_CLAIM_MAPPINGS("65001", "Error occurred while deleting the " +
                "claim mapping for the tenant : %s with domain : %s");

        private final String code;
        private final String message;

        ErrorMessage(String code, String message) {

            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
