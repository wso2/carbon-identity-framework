/*
*  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.user.store.configuration.utils;

import org.wso2.carbon.utils.CarbonUtils;

/**
 * Constant class to hold IdentityUserStoreMgtConstants
 */
public class UserStoreConfigurationConstant {

    public static final String ENCRYPTED_PROPERTY_MASK = "ENCRYPTED PROPERTY";
    public static final String UNIQUE_ID_CONSTANT = "UniqueID";
    public static final String RANDOM_PHRASE_PREFIX = "random-password-generated!@#$%^&*(0)+_";
    public static final String ENCRYPT_TEXT = "#encrypt";
    //name constant of the cache manager
    public static final String SECONDARY_STORAGE_CACHE_MANAGER = "secondaryStorageCacheManager";
    //random password container cache
    public static final String RANDOM_PASSWORD_CONTAINER_CACHE = "randomPasswordContainerCache";
    public static final String UNDERSCORE = "_";
    public static final String DESCRIPTION = "Description";
    public static final String FEDERATED = "FEDERATED";
    public static final String USERSTORES = "userstores";
    public static final String USERSTORE = "USERSTORE";
    public static final String XML = "XML";
    public static final String DEPLOYMENT_DIRECTORY = CarbonUtils.getCarbonRepository() + USERSTORES;
    public static final String PERIOD = ".";
    public static final String DISABLED = "Disabled";
    public static final String FILE_EXTENSION_XML = ".xml";
    public static final String ALLOWED_USERSTORES = "AllowedUserstores";
    public static final String ALLOWED_USERSTORE = "AllowedUserstore";
    public static final String H2_INIT_REGEX = "\\s*;\\s*init\\s*=\\s*";

    // Attribute mappings constants
    public static final String CLAIM_CONFIG = "claim-config.xml";
    public static final String DIALECTS = "Dialects";
    public static final String DIALECT = "Dialect";
    public static final String LOCAL_DIALECT_URL = "http://wso2.org/claims";
    public static final String USERSTORE_TYPE = "userStoreType";
    public static final String DISPLAY_NAME = "DisplayName";
    public static final String ATTRIBUTE_ID = "AttributeID";
    public static final String CLAIM_URI = "ClaimURI";
    public static final String OPERATION = "Operation";
    public static final String ATTRIBUTES_DIR = "attributes";
    public static final String USERSTORE_DIR = "userstore";

    private UserStoreConfigurationConstant() {

    }

    /**
     * Error message enums.
     */
    public enum ErrorMessage {

        ERROR_CODE_XML_FILE_NOT_FOUND("SUS-60001", "Cannot find a configuration file with the " +
                "provided domain identifier."),
        ERROR_CODE_XML_FILE_ALREADY_EXISTS("SUS-60002", "There is a user store configuration file " +
                "already exists with same domain name."),
        ERROR_CODE_USER_STORE_DOMAIN_ALREADY_EXISTS("SUS-60002",
                "User store domain already exists with same domain name."),
        ERROR_CODE_USER_STORE_DOMAIN_NOT_FOUND("SUS-60001",
                "Unable to find any user store's domain id with the provided identifier."),
        ERROR_CODE_EMPTY_USERSTORE_DOMAIN_NAME("SUS-60008", "Userstore domain name cannot be emtpy.");

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

        @Override
        public String toString() {

            return code + ":" + message;
        }
    }

    /**
     * User Store Operation enums.
     */
    public enum UserStoreOperation {

        UPDATE,
        DELETE;

        @Override
        public String toString() {

            return this.name().toLowerCase();
        }
    }
}
