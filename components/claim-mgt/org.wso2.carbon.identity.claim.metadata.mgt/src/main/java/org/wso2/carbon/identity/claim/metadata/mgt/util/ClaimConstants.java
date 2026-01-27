/*
 * Copyright (c) 2016-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.claim.metadata.mgt.util;

import org.wso2.carbon.user.core.UserCoreConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    public static final String CANONICAL_VALUES_PROPERTY = "canonicalValues";
    public static final String INPUT_FORMAT_PROPERTY = "inputFormat";
    public static final String REGULAR_EXPRESSION_PROPERTY = "RegEx";
    public static final String READ_ONLY_PROPERTY = "ReadOnly";
    public static final String CLAIM_URI_PROPERTY = "ClaimURI";
    public static final String MASKING_REGULAR_EXPRESSION_PROPERTY = "MaskingRegEx";
    public static final String CLAIM_UNIQUENESS_SCOPE_PROPERTY = "UniquenessScope";
    public static final String MANAGED_IN_USER_STORE_PROPERTY = "ManagedInUserStore";
    public static final String IS_UNIQUE_CLAIM_PROPERTY = "isUnique";
    public static final String PROFILES_CLAIM_PROPERTY_PREFIX = "Profiles.";
    public static final String UNIQUENESS_VALIDATION_SCOPE = "UserClaimUpdate.UniquenessValidation.ScopeWithinUserstore";
    public static final String ALLOWED_ATTRIBUTE_PROFILE_CONFIG = "UserClaimUpdate.AllowedAttributeProfiles";
    public static final String CLAIM_PROFILE_PROPERTY_DELIMITER = ".";

    public static final String DEFAULT_ATTRIBUTE = "DefaultAttribute";
    public static final String MAPPED_LOCAL_CLAIM_PROPERTY = "MappedLocalClaim";
    public static final String EXCLUDED_USER_STORES_PROPERTY = "ExcludedUserStores";
    public static final String COMMA_SEPARATOR = ",";
    public static final String MIN_LENGTH = "minLength";
    public static final String MAX_LENGTH = "maxLength";
    public static final String IS_SYSTEM_CLAIM = "isSystemClaim";
    public static final String SHARED_PROFILE_VALUE_RESOLVING_METHOD = "SharedProfileValueResolvingMethod";
    public static final String FLOW_INITIATOR = "FlowInitiator";
    public static final String EXTERNAL_CLAIM_ADDITION_NOT_ALLOWED_FOR_DIALECT =
            "ExternalClaimAdditionNotAllowedForDialect";
    public static final String DATA_TYPE_PROPERTY = "dataType";
    public static final String MULTI_VALUED_PROPERTY = "multiValued";
    public static final String SUB_ATTRIBUTES_PROPERTY = "subAttributes";
    public static final String SUB_ATTRIBUTE_PREFIX = "subAttribute.";
    public static final String CANONICAL_VALUE_PREFIX = "canonicalValue.";

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
        ERROR_CODE_EXISTING_EXTERNAL_CLAIM_URI("100010",
                "Claim URI : %s already exists for claim dialect : %s"),
        ERROR_CODE_EXISTING_LOCAL_CLAIM_URI("100011",
                "Local claim URI : %s already exists."),
        ERROR_CODE_NON_EXISTING_LOCAL_CLAIM_URI("100012",
                "Local claim URI : %s does not exist."),
        ERROR_CODE_LOCAL_CLAIM_REFERRED_BY_APPLICATION("10013",
                "Unable to delete claim as it is referred by an application"),
        ERROR_CODE_LOCAL_CLAIM_REFERRED_BY_AN_IDP("10014",
                "Unable to delete claim as it is referred by an IDP"),
        ERROR_CODE_INVALID_EXTERNAL_CLAIM_URI("10015",
                "External claim URI contains invalid characters"),

        // Client errors.
        ERROR_CODE_EMPTY_TENANT_DOMAIN("60000", "Empty tenant domain in the request"),
        ERROR_CODE_INVALID_TENANT_DOMAIN("CMT-60001", "Invalid tenant domain: %s"),
        ERROR_CODE_EXISTING_CLAIM_DIALECT("CMT-60002", "Claim dialect: %s already exists"),
        ERROR_CODE_CLAIM_PROPERTY_CHAR_LIMIT_EXCEED("CMT-60003", "Claim property: %s has " +
                "exceeded maximum character limit of: %s"),
        ERROR_CODE_EXISTING_LOCAL_CLAIM_MAPPING("CMT-60004", "Local claim URI : %s is already mapped in claim " +
                "dialect: %s"),
        ERROR_CODE_CLAIM_LENGTH_LIMIT("CMT-60005", "Claim property: %s should be between %s and %s"),
        ERROR_CODE_NO_DELETE_SYSTEM_CLAIM("CMT-60006", "Cannot delete claim %s as it is a system claim"),
        ERROR_CODE_NO_RENAME_SYSTEM_DIALECT("CMT-60007", "Cannot rename dialect %s as it is a system dialect"),
        ERROR_CODE_NO_DELETE_SYSTEM_DIALECT("CMT-60008", "Cannot delete dialect %s as it is a system dialect"),
        ERROR_CODE_INVALID_EXTERNAL_CLAIM_DIALECT_URI("CMT-60009", "Invalid external claim dialect URI: %s"),
        ERROR_CODE_NON_EXISTING_EXTERNAL_CLAIM_URI("CMT-60010", "External claim URI: %s in dialect: %s does not exist."),
        ERROR_CODE_NON_EXISTING_LOCAL_CLAIM("CMT-60011", "Local claim URI: %s  does not exist."),
        ERROR_CODE_EXISTING_EXTERNAL_CLAIM("CMT-60012", "External claim URI: %s in dialect: %s already exists."),
        ERROR_CODE_NO_SHARED_PROFILE_VALUE_RESOLVING_METHOD_CHANGE_FOR_SYSTEM_CLAIM("CMT-60013",
                "Cannot change the shared profile value resolving method of the system claim: %s"),
        ERROR_CODE_INVALID_SHARED_PROFILE_VALUE_RESOLVING_METHOD("CMT-60014",
                "Invalid shared profile value resolving method: %s"),
        ERROR_CODE_INVALID_ATTRIBUTE_PROFILE("CMT-600015", "Invalid attribute profile name."),
        ERROR_CODE_CANNOT_ADD_TO_EXTERNAL_DIALECT("CMT-60016",
                "Adding claims to dialect %s is not allowed"),
        ERROR_CODE_CANNOT_MODIFY_FLOW_INITIATOR_CLAIM_PROPERTY("CMT-60017",
                "Cannot change flow initiator property of the system claim: %s"),
        ERROR_CODE_CANNOT_EXCLUDE_USER_STORE("CMT-60018",
                "User store '%s' cannot be excluded because it is configured to manage claims."),
        ERROR_CODE_CLAIM_MUST_BE_MANAGED_IN_USER_STORE("CMT-60019",
                "Claim '%s' must be managed in user store."),

        // Server Errors
        ERROR_CODE_DELETE_IDN_CLAIM_MAPPED_ATTRIBUTE("65001", "Error occurred while deleting claim " +
                "mapped attributes for domain : %s with tenant Id : %s from table : IDN_CLAIM_MAPPED_ATTRIBUTE"),
        ERROR_CODE_SERVER_ERROR_DELETING_CLAIM_MAPPINGS("65001", "Error occurred while deleting the " +
                "claim mapping for the tenant : %s with domain : %s"),
        ERROR_CODE_FAILED_TO_RESOLVE_ORGANIZATION_ID("65003", "Error occurred while resolving the " +
                "organization id of tenant: %s with domain : %s"),
        ERROR_CODE_FAILED_TO_RESOLVE_TENANT_ID_DURING_HIERARCHICAL_AGGREGATION("65004", "Error " +
                "occurred while resolving the tenant id for organization: %s during hierarchical aggregation"),
        ERROR_CODE_FAILURE_IN_CHECKING_IS_TENANT_AN_ORGANIZATION("65005", "Error occurred " +
                "while checking whether the tenant: %s is an organization"),
        ERROR_CODE_FAILURE_IN_TRAVERSING_HIERARCHY("65006", "Error occurred while traversing the " +
                "organization hierarchy of tenant: %s with domain: %s"),
        ERROR_CODE_SERVER_ERROR_GETTING_USER_STORE_MANAGER("65007", "Server error occurred while " +
                "getting user store manager for tenant: %s .");

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

    /**
     * Enum for data types of claims.
     */
    public enum ClaimDataType {
        STRING,
        INTEGER,
        DECIMAL,
        BOOLEAN,
        DATE_TIME,
        COMPLEX
    }

    /**
     * Enum for claim uniqueness validation scopes.
     */
    public enum ClaimUniquenessScope {
        NONE,
        WITHIN_USERSTORE,
        ACROSS_USERSTORES
    }

    /**
     * Enum for shared profile value resolving methods.
     */
    public enum SharedProfileValueResolvingMethod {
        FROM_SHARED_PROFILE("FromSharedProfile"),
        FROM_ORIGIN("FromOrigin"),
        FROM_FIRST_FOUND_IN_HIERARCHY("FromFirstFoundInHierarchy");

        private final String name;

        SharedProfileValueResolvingMethod(String name) {

            this.name = name;
        }

        public String getName() {

            return name;
        }

        /**
         * Get the SharedProfileValueResolvingMethod from the name.
         *
         * @param name Name of the SharedProfileValueResolvingMethod.
         * @return SharedProfileValueResolvingMethod name.
         */
        public static SharedProfileValueResolvingMethod fromName(String name) {

            for (SharedProfileValueResolvingMethod method : SharedProfileValueResolvingMethod.values()) {
                if (method.getName().equals(name)) {
                    return method;
                }
            }
            throw new IllegalArgumentException("Invalid value: " + name);
        }
    }

    /**
     * Enum for default allowed claim profiles.
     */
    public enum DefaultAllowedClaimProfile {

        CONSOLE("console"),
        END_USER("endUser"),
        SELF_REGISTRATION("selfRegistration");

        private final String profileName;

        DefaultAllowedClaimProfile(String profileName) {
            this.profileName = profileName;
        }

        public String getProfileName() {
            return profileName;
        }
    }

    public static final List<String> ALLOWED_PROFILE_PROPERTY_KEYS = Collections.unmodifiableList(Arrays.asList(
            ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY,
            ClaimConstants.REQUIRED_PROPERTY,
            ClaimConstants.READ_ONLY_PROPERTY));
}
