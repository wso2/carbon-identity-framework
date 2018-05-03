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
package org.wso2.carbon.identity.provisioning.rules;

public class ProvisioningRuleConstanats {

    public static final String IDENTITY_ACTION_PROVISIONING = "provisioning";

    public static final String DATE_FORMAT = "yyyy/MM/dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";
    public static final String XACML_RESPONSE_RESULT_XPATH = "/Response/Result/Decision/text()";
    public static final String XACML_RESPONSE_DECISION_NODE = "Decision";

    public static final String XACML_ATTRIBUTE_CATAGORY = "urn:oasis:names:tc:xacml:3.0:attribute-category:";
    public static final String XACML_ATTRIBUTE_ID = "urn:oasis:names:tc:xacml:1.0:";

    public static final String XACML_CATAGORY_SERVICE_PROVIDER = "http://wso2.org/identity/sp";
    public static final String XACML_CATAGORY_USER = "http://wso2.org/identity/user";
    public static final String XACML_CATAGORY_IDENTITY_PROVIDER = "http://wso2.org/identity/idp";
    public static final String XACML_CATAGORY_IDENTITY_ACTION = "http://wso2.org/identity/identity-action";
    public static final String XACML_CATAGORY_PROVISIONING = "http://wso2.org/identity/provisioning";
    public static final String XACML_CATAGORY_ENVIRONMENT = "urn:oasis:names:tc:xacml:3.0:attribute-category:environment";

    public static final String XACML_ATTRIBUTE_TENAT_DOMAIN = "http://wso2.org/identity/user/user-tenant-domain";
    public static final String XACML_ATTRIBUTE_USER = "http://wso2.org/identity/user/username";
    public static final String XACML_ATTRIBUTE_SP_NAME = "http://wso2.org/identity/sp/sp-name";
    public static final String XACML_ATTRIBUTE_SP_TENANT_DOMAIN = "http://wso2.org/identity/auth/sp-tenant-domain";
    public static final String XACML_ATTRIBUTE_IDP_NAME = "http://wso2.org/identity/idp/idp-name";
    public static final String XACML_ATTRIBUTE_CONNECTOR_TYPE = "http://wso2.org/identity/idp/connector-type";
    public static final String XACML_ATTRIBUTE_IDENTITY_ACTION = "http://wso2.org/identity/identity-action/action-name";
    public static final String XACML_ATTRIBUTE_CLAIM_GROUPS = "http://wso2.org/identity/provisioning/claim-group";
    public static final String XACML_ATTRIBUTE_OPERATION = "http://wso2.org/identity/provisioning/provision-operation";
    public static final String XACML_ATTRIBUTE_DATE_TIME = "urn:oasis:names:tc:xacml:1.0:environment:current-dateTime";
    public static final String XACML_ATTRIBUTE_DATE = "urn:oasis:names:tc:xacml:1.0:environment:current-date";
    public static final String XACML_ATTRIBUTE_TIME = "urn:oasis:names:tc:xacml:1.0:environment:current-time";
    public static final String XACML_ATTRIBUTE_ENVIRONMENT = "urn:oasis:names:tc:xacml:1.0:environment:environment-id";


}
