/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public class SCIMProviderConstants {

    public static final String PROPERTY_NAME_PRIORITY = "Priority";
    public static final String PROPERTY_NAME_AUTH_SERVER = "AuthorizationServer";
    public static final String PROPERTY_NAME_USERNAME = "UserName";
    public static final String PROPERTY_NAME_PASSWORD = "Password";

    public static final String ELEMENT_NAME_SCIM = "SCIM";
    public static final String ELEMENT_NAME_SCIM_AUTHENTICATORS = "SCIMAuthenticators";
    public static final String ELEMENT_NAME_AUTHENTICATOR = "Authenticator";
    public static final String ELEMENT_NAME_PROPERTY = "Property";

    public static final String ATTRIBUTE_NAME_NAME = "name";
    public static final String ATTRIBUTE_NAME_CLASS = "class";

    public static final String DEFAULT_SCIM_DIALECT = "urn:scim:schemas:core:1.0";

    public static final String ID = "ID";
    public static final String INPUT_FORMAT = "INPUT_FORMAT";
    public static final String OUTPUT_FORMAT = "INPUT_FORMAT";
    public static final String AUTHORIZATION = "AUTHORIZATION";
    public static final String RESOURCE_STRING = "RESOURCE_STRING";
    public static final String HTTP_VERB = "HTTP_VERB";
    public static final String SEARCH_ATTRIBUTE = "SEARCH_ATTRIBUTE";
    public static final String FILTER = "FILTER";
    public static final String START_INDEX = "START_INDEX";
    public static final String COUNT = "COUNT";
    public static final String SORT_BY = "SORT_BY";
    public static final String SORT_ORDER = "SORT_ORDER";
    public static final String PATCH = "PATCH";

    private SCIMProviderConstants() {
    }

}
