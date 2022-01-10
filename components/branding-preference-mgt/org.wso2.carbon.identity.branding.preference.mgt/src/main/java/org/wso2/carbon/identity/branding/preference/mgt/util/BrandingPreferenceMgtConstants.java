/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.branding.preference.mgt.util;

/**
 * Constants related to branding preference management.
 */
public class BrandingPreferenceMgtConstants {

    public static final String BRANDING_RESOURCE_TYPE = "BRANDING_PREFERENCES";
    public static final String ORGANIZATION_TYPE = "ORG";
    public static final String APPLICATION_TYPE = "APP";
    public static final String CUSTOM_TYPE = "CUSTOM";
    public static final String DEFAULT_LOCALE = "en-US";
    public static final String RESOURCE_NAME_SEPARATOR = "_";

    public static final String RESOURCE_NOT_EXISTS_ERROR_CODE = "CONFIGM_00017";
    public static final String RESOURCE_ALREADY_EXISTS_ERROR_CODE = "CONFIGM_00013";

    /**
     * Enums for error messages.
     */
    public enum ErrorMessages {

        ERROR_CODE_INVALID_BRANDING_PREFERENCE("BRANDINGM_00001",
                "Invalid Branding Preference configurations for tenant: %s."),
        ERROR_CODE_BRANDING_PREFERENCE_NOT_EXISTS("BRANDINGM_00002",
                "Branding preferences are not configured for tenant: %s."),
        ERROR_CODE_BRANDING_PREFERENCE_ALREADY_EXISTS("BRANDINGM_00003",
                "Branding preference already exists for tenant: %s."),
        ERROR_CODE_ERROR_GETTING_BRANDING_PREFERENCE("BRANDINGM_00004",
                "Error while getting branding preference configurations for tenant: %s."),
        ERROR_CODE_ERROR_ADDING_BRANDING_PREFERENCE("BRANDINGM_00005",
                "Unable to add branding preference configurations tenant: %s."),
        ERROR_CODE_ERROR_DELETING_BRANDING_PREFERENCE("BRANDINGM_00006",
                "Unable to delete branding preference configurations for tenant: %s."),
        ERROR_CODE_ERROR_UPDATING_BRANDING_PREFERENCE("BRANDINGM_00007",
                "Unable to update branding preference configurations."),
        ERROR_CODE_ERROR_BUILDING_BRANDING_PREFERENCE("BRANDINGM_00008",
                "Unable to build branding preference from branding preference configurations for tenant: %s."),
        ERROR_CODE_ERROR_CHECKING_BRANDING_PREFERENCE_EXISTS("BRANDINGM_00009",
                "Error while checking branding preference configurations existence."),
        ERROR_CODE_UNSUPPORTED_ENCODING_EXCEPTION("BRANDINGM_00010",
                "Unsupported Encoding in the branding preference configurations of the tenant: %s.");

        private final String code;
        private final String message;

        ErrorMessages(String code, String message) {

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

}
