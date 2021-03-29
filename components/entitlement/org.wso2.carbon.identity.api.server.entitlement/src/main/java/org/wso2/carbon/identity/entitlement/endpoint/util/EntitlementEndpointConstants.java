/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.entitlement.endpoint.util;

public class EntitlementEndpointConstants {
    public static final String JSON = "json";
    public static final String XML = "xml";
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_XML = "application/xml";
    public static final String dateTimeFormat = "yyyy-MM-dd\'T\'HH:mm:ss";
    public static final String AUTH_TYPE_BASIC = "Basic";
    public static final String AUTH_TYPE_OAUTH = "Bearer";
    public static final String AUTH_PROPERTY_PRIMARY = "primary";
    public static final String AUTH_HEADER_USERNAME = "userName";
    public static final String AUTH_HEADER_PASSWORD = "password";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHENTICATION_TYPE_HEADER = "Auth_Type";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String ACCEPT_HEADER = "Accept";


    public static final String PROPERTY_NAME_PRIORITY = "Priority";
    public static final String PROPERTY_NAME_AUTH_SERVER = "AuthorizationServer";
    public static final String PROPERTY_NAME_USERNAME = "UserName";
    public static final String PROPERTY_NAME_PASSWORD = "Password";

    /**
     * Defines Constants related to XACML JSON representation
     */
    public static final String CATEGORY_DEFAULT = "Category";
    public static final String CATEGORY_RESOURCE = "Resource";
    public static final String CATEGORY_ACTION = "Action";
    public static final String CATEGORY_ENVIRONMENT = "Environment";
    public static final String CATEGORY_ACCESS_SUBJECT = "AccessSubject";
    public static final String CATEGORY_RECIPIENT_SUBJECT = "RecipientSubject";
    public static final String CATEGORY_INTERMEDIARY_SUBJECT = "IntermediarySubject";
    public static final String CATEGORY_CODEBASE = "Codebase";
    public static final String CATEGORY_REQUESTING_MACHINE = "RequestingMachine";

    public static final String CATEGORY_RESOURCE_URI = "urn:oasis:names:tc:xacml:3.0:attribute-category:resource";
    public static final String CATEGORY_ACTION_URI = "urn:oasis:names:tc:xacml:3.0:attribute-category:action";
    public static final String CATEGORY_ENVIRONMENT_URI = "urn:oasis:names:tc:xacml:3.0:attribute-category:environment";
    public static final String CATEGORY_ACCESS_SUBJECT_URI =
            "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";
    public static final String CATEGORY_RECIPIENT_SUBJECT_URI =
            "urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject";
    public static final String CATEGORY_INTERMEDIARY_SUBJECT_URI =
            "urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject";
    public static final String CATEGORY_CODEBASE_URI = "urn:oasis:names:tc:xacml:1.0:subject-category:codebase";
    public static final String CATEGORY_REQUESTING_MACHINE_URI =
            "urn:oasis:names:tc:xacml:1.0:subject-category:requesting-machine";

    // Attribute id uri
    public static final String ATTRIBUTE_RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
    public static final String ATTRIBUTE_ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
    public static final String ATTRIBUTE_ENVIRONMENT_ID = "urn:oasis:names:tc:xacml:1.0:environment:environment-id";
    public static final String ATTRIBUTE_SUBJECT_ID = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";
    public static final String ATTRIBUTE_RECIPIENT_SUBJECT_ID =
            "urn:oasis:names:tc:xacml:1.0:recipient-subject:recipient-subject-id";
    public static final String ATTRIBUTE_INTERMEDIARY_SUBJECT_ID =
            "urn:oasis:names:tc:xacml:1.0:intermediary-subject:intermediary-subject-id";
    public static final String ATTRBUTE_REQUESTING_MACHINE_ID =
            "urn:oasis:names:tc:xacml:1.0:requesting-machine:requesting-machine-id";
    public static final String ATTRIBUTE_CODEBASE_ID = "urn:oasis:names:tc:xacml:1.0:codebase:codebase-id";

    // Attribute id simple
    public static final String ATTRIBUTE_RESOURCE_ID_SHORTEN = "resource-id";
    public static final String ATTRIBUTE_ACTION_ID__SHORTEN = "action-id";
    public static final String ATTRIBUTE_ENVIRONMENT_ID_SHORTEN = "environment-id";
    public static final String ATTRIBUTE_SUBJECT_ID_SHORTEN = "subject-id";
    public static final String ATTRIBUTE_RECIPIENT_SUBJECT_ID_SHORTEN = "recipient-subject-id";
    public static final String ATTRIBUTE_INTERMEDIARY_SUBJECT_ID_SHORTEN = "intermediary-subject-id";
    public static final String ATTRBUTE_REQUESTING_MACHINE_ID_SHORTEN = "requesting-machine-id";
    public static final String ATTRIBUTE_CODEBASE_ID_SHORTEN = "codebase-id";

    public static final String CATEGORY_ID = "CategoryId";
    public static final String ID = "Id";
    public static final String CONTENT = "Content";

    public static final String ATTRIBUTE = "Attribute";
    public static final String ATTRIBUTE_ID = "AttributeId";
    public static final String ATTRIBUTE_VALUE = "Value";
    public static final String ATTRIBUTE_ISSUER = "Issuer";
    public static final String ATTRIBUTE_DATA_TYPE = "DataType";
    public static final String ATTRIBUTE_INCLUDE_IN_RESULT = "IncludeInResult";

    public static final String ATTRIBUTE_DATA_TYPE_STRING = "http://www.w3.org/2001/XMLSchema#string";
    public static final String ATTRIBUTE_DATA_TYPE_STRING_SHORT = "string";

    public static final String ATTRIBUTE_DATA_TYPE_BOOLEAN = "http://www.w3.org/2001/XMLSchema#boolean";
    public static final String ATTRIBUTE_DATA_TYPE_BOOLEAN_SHORT = "boolean";

    public static final String ATTRIBUTE_DATA_TYPE_INTEGER = "http://www.w3.org/2001/XMLSchema#integer";
    public static final String ATTRIBUTE_DATA_TYPE_INTEGER_SHORT = "integer";

    public static final String ATTRIBUTE_DATA_TYPE_DOUBLE = "http://www.w3.org/2001/XMLSchema#double";
    public static final String ATTRIBUTE_DATA_TYPE_DOUBLE_SHORT = "double";

    public static final String ATTRIBUTE_DATA_TYPE_TIME = "http://www.w3.org/2001/XMLSchema#time";
    public static final String ATTRIBUTE_DATA_TYPE_TIME_SHORT = "time";

    public static final String ATTRIBUTE_DATA_TYPE_DATE = "http://www.w3.org/2001/XMLSchema#date";
    public static final String ATTRIBUTE_DATA_TYPE_DATE_SHORT = "date";

    public static final String ATTRIBUTE_DATA_TYPE_DATE_TIME = "http://www.w3.org/2001/XMLSchema#dateTime";
    public static final String ATTRIBUTE_DATA_TYPE_DATE_TIME_SHORT = "dateTime";

    public static final String ATTRIBUTE_DATA_TYPE_DATE_TIME_DURATION = "http://www.w3.org/2001/XMLSchema#dayTimeDuration";
    public static final String ATTRIBUTE_DATA_TYPE_DATE_TIME_DURATION_SHORT = "dayTimeDuration";

    public static final String ATTRIBUTE_DATA_TYPE_YEAR_MONTH_DURATION = "http://www.w3.org/2001/XMLSchema#yearMonthDuration";
    public static final String ATTRIBUTE_DATA_TYPE_YEAR_MONTH_DURATION_SHORT = "yearMonthDuration";

    public static final String ATTRIBUTE_DATA_TYPE_ANY_URI = "http://www.w3.org/2001/XMLSchema#anyURI";
    public static final String ATTRIBUTE_DATA_TYPE_ANY_URI_SHORT = "anyURI";

    public static final String ATTRIBUTE_DATA_TYPE_HEX_BINARY = "http://www.w3.org/2001/XMLSchema#hexBinary";
    public static final String ATTRIBUTE_DATA_TYPE_HEX_BINARY_SHORT = "hexBinary";

    public static final String ATTRIBUTE_DATA_TYPE_BASE64_BINARY = "http://www.w3.org/2001/XMLSchema#base64Binary";
    public static final String ATTRIBUTE_DATA_TYPE_BASE64_BINARY_SHORT = "base64Binary";

    public static final String ATTRIBUTE_DATA_TYPE_RFC_822_NAME = "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name";
    public static final String ATTRIBUTE_DATA_TYPE_RFC_822_NAME_SHORT = "rfc822Name";

    public static final String ATTRIBUTE_DATA_TYPE_X_500_NAME = "urn:oasis:names:tc:xacml:1.0:data-type:x500Name";
    public static final String ATTRIBUTE_DATA_TYPE_X_500_NAME_SHORT = "x500Name";

    public static final String ATTRIBUTE_DATA_TYPE_IP_ADDRESS = "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress";
    public static final String ATTRIBUTE_DATA_TYPE_IP_ADDRESS_SHORT = "ipAddress";

    public static final String ATTRIBUTE_DATA_TYPE_DNS_NAME = "urn:oasis:names:tc:xacml:2.0:data-type:dnsName";
    public static final String ATTRIBUTE_DATA_TYPE_DNS_NAME_SHORT = "dnsName";

    public static final String ATTRIBUTE_DATA_TYPE_XPATH_EXPRESSION =
            "urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression";
    public static final String ATTRIBUTE_DATA_TYPE_XPATH_EXPRESSION_SHORT = "xpathExpression";


    public static final String XPATH_VERSION = "XPathVersion";
    public static final String MULTI_REQUESTS = "MultiRequests";
    public static final String REFERENCE_ID = "ReferenceId";


    public static final String RESPONSE = "Response";

    public static final String DECISION = "Decision";
    public static final String STATUS = "Status";
    public static final String OBLIGATIONS = "Obligations";
    public static final String ASSOCIATED_ADVICE = "AssociatedAdvice";

    public static final String STATUS_MESSAGE = "StatusMessage";
    public static final String STATUS_DETAIL = "StatusDetail";
    public static final String STATUS_CODE = "StatusCode";
    public static final String MISSING_ATTRIBUTE_DETAILS = "MissingAttributeDetail";

    public static final String STATUS_CODE_VALUE = "Value";

    public static final String OBLIGATION_OR_ADVICE_ID = "Id";
    public static final String ATTRIBUTE_ASSIGNMENTS = "AttributeAssignments";

    //Error codes and responses
    public static final int ERROR_UNAUTHORIZED_CODE = 40010;
    public static final String ERROR_UNAUTHORIZED_MESSAGE = "Authentication failed for this resource.";

    public static final int ERROR_REQUEST_PARSE_CODE = 40020;
    public static final String ERROR_REQUEST_PARSE_MESSAGE = "Request Parse Exception.";

    public static final int ERROR_RESPONSE_READ_CODE = 40030;
    public static final String ERROR_RESPONSE_READ_MESSAGE = "Error in Response.";

}
