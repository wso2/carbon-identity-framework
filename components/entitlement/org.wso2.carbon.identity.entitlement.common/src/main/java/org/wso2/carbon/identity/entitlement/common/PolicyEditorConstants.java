/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.entitlement.common;

/**
 * Policy editor related constants
 */
public class PolicyEditorConstants {


    public static final String ATTRIBUTE_SEPARATOR = ",";

    public static final String TARGET_ELEMENT = "Target";

    public static final String ANY_OF_ELEMENT = "AnyOf";

    public static final String ALL_OF_ELEMENT = "AllOf";

    public static final String COMBINE_FUNCTION_AND = "AND";

    public static final String COMBINE_FUNCTION_OR = "OR";

    public static final String COMBINE_FUNCTION_END = "END";

    public static final String MATCH_ELEMENT = "Match";

    public static final String MATCH_ID = "MatchId";

    public static final String ATTRIBUTE_ID = "AttributeId";

    public static final String CATEGORY = "Category";

    public static final String DATA_TYPE = "DataType";

    public static final String ISSUER = "Issuer";

    public static final String SOA_CATEGORY_USER = "Subject";

    public static final String SOA_CATEGORY_SUBJECT = "Subject";

    public static final String SOA_CATEGORY_RESOURCE = "Resource";

    public static final String SOA_CATEGORY_ACTION = "Action";

    public static final String SOA_CATEGORY_ENVIRONMENT = "Environment";

    public static final String MUST_BE_PRESENT = "MustBePresent";

    public static final String ATTRIBUTE_DESIGNATOR = "AttributeDesignator";
    public static final String RULE_EFFECT_PERMIT = "Permit";
    public static final String RULE_EFFECT_DENY = "Deny";
    public static final String RULE_ALGORITHM_IDENTIFIER_1 = "urn:oasis:names:tc:xacml:1.0:" +
            "rule-combining-algorithm:";
    public static final String RULE_ALGORITHM_IDENTIFIER_3 = "urn:oasis:names:tc:xacml:3.0:" +
            "rule-combining-algorithm:";
    public static final String POLICY_ALGORITHM_IDENTIFIER_1 = "urn:oasis:names:tc:xacml:1.0:" +
            "policy-combining-algorithm:";
    public static final String POLICY_ALGORITHM_IDENTIFIER_3 = "urn:oasis:names:tc:xacml:3.0:" +
            "policy-combining-algorithm:";
    public static final String POLICY_EDITOR_SEPARATOR = "|";
    public static final int POLICY_EDITOR_ROW_DATA = 7;
    public static final String DYNAMIC_SELECTOR_CATEGORY = "Category";
    public static final String DYNAMIC_SELECTOR_FUNCTION = "Function";
    public static final String SUBJECT_ID_DEFAULT = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";
    public static final String SUBJECT_ID_ROLE = "http://wso2.org/claims/roles";
    public static final String RESOURCE_ID_DEFAULT = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
    public static final String ACTION_ID_DEFAULT = "urn:oasis:names:tc:xacml:1.0:action:action-id";
    public static final String ENVIRONMENT_ID_DEFAULT = "urn:oasis:names:tc:xacml:1.0:environment:environment-id";
    public static final String RESOURCE_CATEGORY_URI = "urn:oasis:names:tc:xacml:3.0:" +
            "attribute-category:resource";
    public static final String SUBJECT_CATEGORY_URI = "urn:oasis:names:tc:xacml:1.0:" +
            "subject-category:access-subject";
    public static final String ACTION_CATEGORY_URI = "urn:oasis:names:tc:xacml:3.0:" +
            "attribute-category:action";
    public static final String ENVIRONMENT_CATEGORY_URI = "urn:oasis:names:tc:xacml:3.0:" +
            "attribute-category:environment";
    public static final String ENVIRONMENT_CURRENT_DATE = "urn:oasis:names:tc:xacml:1.0:environment:current-date";
    public static final String ENVIRONMENT_CURRENT_TIME = "urn:oasis:names:tc:xacml:1.0:environment:current-time";
    public static final String ENVIRONMENT_CURRENT_DATETIME = "urn:oasis:names:tc:xacml:1.0:environment:current-dateTime";
    public static final String SOA_POLICY_EDITOR = "SOA";

    public static final class PreFunctions {

        public static final String PRE_FUNCTION_IS = "is";

        public static final String PRE_FUNCTION_IS_NOT = "is-not";

        public static final String PRE_FUNCTION_ARE = "are";

        public static final String PRE_FUNCTION_ARE_NOT = "are-not";

        public static final String CAN_DO = "can";

        public static final String CAN_NOT_DO = "can not";
    }

    public static final class TargetPreFunctions {

        public static final String PRE_FUNCTION_IS = "is";

    }

    public static final class TargetFunctions {

        public static final String FUNCTION_EQUAL = "equal";

    }

    public static final class DataType {

        public static final String DAY_TIME_DURATION = "http://www.w3.org/2001/XMLSchema#dayTimeDuration";

        public static final String YEAR_MONTH_DURATION = "http://www.w3.org/2001/XMLSchema#yearMonthDuration";

        public static final String STRING = "http://www.w3.org/2001/XMLSchema#string";

        public static final String TIME = "http://www.w3.org/2001/XMLSchema#time";

        public static final String IP_ADDRESS = "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress";

        public static final String DATE_TIME = "http://www.w3.org/2001/XMLSchema#dateTime";

        public static final String DATE = "http://www.w3.org/2001/XMLSchema#date";

        public static final String DOUBLE = "http://www.w3.org/2001/XMLSchema#double";

        public static final String INT = "http://www.w3.org/2001/XMLSchema#integer";

    }

    public static final class CombiningAlog {

        public static final String DENY_OVERRIDE_ID = "deny-overrides";

        public static final String PERMIT_OVERRIDE_ID = "permit-overrides";

        public static final String FIRST_APPLICABLE_ID = "first-applicable";

        public static final String ORDER_PERMIT_OVERRIDE_ID = "ordered-permit-overrides";

        public static final String ORDER_DENY_OVERRIDE_ID = "ordered-deny-overrides";

        public static final String DENY_UNLESS_PERMIT_ID = "deny-unless-permit";

        public static final String PERMIT_UNLESS_DENY_ID = "permit-unless-deny";

        public static final String ONLY_ONE_APPLICABLE_ID = "only-one-applicable";

    }

    public static class FunctionIdentifier {

        public static final String ANY = "*";

        public static final String EQUAL_RANGE = "[";

        public static final String EQUAL_RANGE_CLOSE = "]";

        public static final String RANGE = "(";

        public static final String RANGE_CLOSE = ")";

        public static final String GREATER = ">";

        public static final String GREATER_EQUAL = ">=";

        public static final String LESS = "<";

        public static final String LESS_EQUAL = "<=";

        public static final String REGEX = "{";

        public static final String AND = "&";

        public static final String OR = "|";

    }

    public static final class AttributeId {

        public static final String ENV_DOMAIN = "Domain";

        public static final String ENV_DATE = "Date";

        public static final String ENV_DATE_TIME = "DateTime";

        public static final String ENV_IP = "IP";

        public static final String ENV_TIME = "Time";

        public static final String USER_AGE = "Age";
    }
}
