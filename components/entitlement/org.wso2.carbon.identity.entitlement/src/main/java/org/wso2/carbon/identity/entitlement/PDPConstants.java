/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement;

/**
 * Entitlement and XACML related constant values
 */
public class PDPConstants {

    public static final String POLICY_TYPE = "policyType";

    public static final String POLICY_EDITOR_TYPE = "policyEditor";

    public static final String BASIC_POLICY_EDITOR_META_DATA = "basicPolicyEditorMetaData";

    public static final String BASIC_POLICY_EDITOR_META_DATA_AMOUNT = "NoOfBasicPolicyEditorMetaData";

    public static final String ACTIVE_POLICY = "isActive";

    public static final String PROMOTED_POLICY = "promoted";

    public static final String POLICY_VERSION = "version";

    public static final String LAST_MODIFIED_TIME = "lastModifiedTime";

    public static final String LAST_MODIFIED_USER = "lastModifiedUser";

    public static final String POLICY_LIFE_CYCLE = "policyLifeStatus";

    public static final String POLICY_ORDER = "policyOrder";

    public static final String MAX_POLICY_ORDER = "maxPolicyOrder";

    public static final String POLICY_ELEMENT = "Policy";

    public static final String POLICY_REFERENCE = "policyIdReferences";

    public static final String POLICY_SET_REFERENCE = "policySetIdReferences";

    public static final String APPLY_ELEMENT = "Apply";

    public static final String MATCH_ELEMENT = "Match";

    public static final String SUBJECT_ELEMENT = "Subject";

    public static final String ACTION_ELEMENT = "Action";

    public static final String RESOURCE_ELEMENT = "Resource";

    public static final String ENVIRONMENT_ELEMENT = "Environment";

    public static final String SUBJECT_CATEGORY_ID = "Subject";

    public static final String ACTION_CATEGORY_ID = "Action";

    public static final String RESOURCE_CATEGORY_ID = "Resource";

    public static final String ENVIRONMENT_CATEGORY_ID = "Environment";

    public static final String ANY_OF = "AnyOf";

    public static final String ALL_OF = "AllOf";

    public static final String RESOURCE_CATEGORY_URI = "urn:oasis:names:tc:xacml:3.0:" +
                                                       "attribute-category:resource";

    public static final String SUBJECT_CATEGORY_URI = "urn:oasis:names:tc:xacml:1.0:" +
                                                      "subject-category:access-subject";

    public static final String ACTION_CATEGORY_URI = "urn:oasis:names:tc:xacml:3.0:" +
                                                     "attribute-category:action";

    public static final String ENVIRONMENT_CATEGORY_URI = "urn:oasis:names:tc:xacml:3.0:" +
                                                          "attribute-category:environment";

    public static final String TARGET_ELEMENT = "Target";

    public static final String RULE_ELEMENT = "Rule";

    public static final String CONDITION_ELEMENT = "Condition";

    public static final String FUNCTION_ELEMENT = "Function";

    public static final String ATTRIBUTE_SELECTOR = "AttributeSelector";

    public static final String ATTRIBUTE_VALUE = "AttributeValue";

    public static final String FUNCTION = "Function";

    public static final String VARIABLE_REFERENCE = "VariableReference";

    public static final String ATTRIBUTE_DESIGNATOR = "AttributeDesignator";

    public static final String ATTRIBUTE_ID = "AttributeId";

    public static final String ATTRIBUTE = "Attribute";

    public static final String DATA_TYPE = "DataType";

    public static final String CATEGORY = "Category";

    public static final String REQUEST_CONTEXT_PATH = "RequestContextPath";

    public static final String SUBJECT_ID_DEFAULT = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";

    public static final String SUBJECT_CATEGORY_DEFAULT =
            "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";

    public static final String SUBJECT_ID_ROLE = "http://wso2.org/claims/roles";

    public static  final String SERVICE_PROVIDER_NAME = "http://wso2.org/identity/sp/sp-name";

    public static  final String SERVICE_PROVIDER_TENANT_DOMAIN = "http://wso2.org/identity/sp/sp-tenant-domain";

    public  static  final String SERVICE_PROVIDER = "http://wso2.org/identity/sp";

    public static final String RULE_EFFECT_PERMIT = "Permit";

    public static final String RULE_EFFECT_DENY = "Deny";

    public static final String RESPONSE_RESULT = "Result";

    public static final String RESPONSE_DECISION = "Decision";

    public static final String RESPONSE_RESOURCE_ID = "ResourceId";

    public static final String POLICY_META_DATA = "policyMetaData";

    public static final int POLICY_META_DATA_ARRAY_LENGTH = 4;

    public static final String AUTHORIZATION_PERMISSION = "/permission/admin/configure";

    public static final String ENTITLEMENT_CACHE_MANAGER = "ENTITLEMENT_CACHE_MANAGER";

    public static final String PIP_RESOURCE_CACHE = "PIP_RESOURCE_CACHE";

    public static final String PDP_DECISION_CACHE = "PDP_DECISION_CACHE";

    public static final String PDP_SIMPLE_DECISION_CACHE = "PDP_SIMPLE_DECISION_CACHE";

    public static final String PDP_DECISION_INVALIDATION_CACHE = "PDP_DECISION_INVALIDATION_CACHE";

    public static final String PIP_ABSTRACT_INVALIDATION_CACHE = "PIP_ABSTRACT_INVALIDATION_CACHE";

    public static final String POLICY_SEARCH_CACHE = "POLICY_SEARCH_CACHE";

    public static final String PIP_ABSTRACT_RESOURCE_CACHE = "PIP_ABSTRACT_RESOURCE_CACHE";

    public static final String PIP_ATTRIBUTE_CACHE = "PIP_ATTRIBUTE_CACHE";

    public static final String PIP_ABSTRACT_ATTRIBUTE_CACHE = "PIP_ABSTRACT_ATTRIBUTE_CACHE";

    public static final String ENTITLEMENT_POLICY_INVALIDATION_CACHE = "ENTITLEMENT_POLICY_INVALIDATION_CACHE";

    public static final int DEFAULT_ITEMS_PER_PAGE = 10;

    public static final String UNKNOWN = "UNKNOWN";

    public static final String REQUEST_ELEMENT = "Request";

    public static final String REQ_RES_CONTEXT = "urn:oasis:names:tc:xacml:2.0:context:schema:os";

    public static final String REQ_SCHEME = "http://www.w3.org/2001/XMLSchema-instance";

    public static final String STRING_DATA_TYPE = "http://www.w3.org/2001/XMLSchema#string";

    public static final String RESOURCE_ID_DEFAULT = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";

    public static final String ACTION_ID_DEFAULT = "urn:oasis:names:tc:xacml:1.0:action:action-id";

    public static final String ENVIRONMENT_ID_DEFAULT = "urn:oasis:names:tc:xacml:1.0:environment:environment-id";

    public static final String RESOURCE_SCOPE_ID = "urn:oasis:names:tc:xacml:1.0:resource:scope";

    public static final String RESOURCE_DESCENDANTS = "Descendants";

    public static final String RESOURCE_CHILDREN = "Children";

    public static final String ATTRIBUTE_SEPARATOR = ",";

    public static final String SEARCH_WARNING_MESSAGE1 = "Attribute values are not defined directly";

    public static final String SEARCH_WARNING_MESSAGE2 = "No Attributes are defined";

    public static final String SEARCH_WARNING_MESSAGE3 = "Attribute Selector Element is contained " +
                                                         "with Xpath expression";

    public static final String SEARCH_WARNING_MESSAGE4 = "Apply Element is not contained within Condition Element";

    public static final String SEARCH_ERROR = "Search_Error";

    public static final String SEARCH_ERROR_MESSAGE = "Therefore Advance Search can not be proceeded. " +
                                                      "Please de-active this policy, If policy is not" +
                                                      " relevant for the search";

    public static final String XACML_3_POLICY_XMLNS = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17";

    public static final String XACML_2_POLICY_XMLNS = "urn:oasis:names:tc:xacml:2.0:policy:schema:os";

    public static final String XACML_1_POLICY_XMLNS = "urn:oasis:names:tc:xacml:1.0:policy";

    public static final String XACML_3_POLICY_SCHEMA_FILE = "xacml3.xsd";

    public static final String XACML_2_POLICY_SCHEMA_FILE = "xacml2.xsd";

    public static final String XACML_1_POLICY_SCHEMA_FILE = "xacml1.xsd";

    public static final String ENTITLEMENT_POLICY_PUBLISHER = "/repository/identity/entitlement/publisher/";

    public static final String ENTITLEMENT_POLICY_PUBLISHER_VERIFICATION =
            "/repository/identity/entitlement/publisher/verification/";

    public static final String ENTITLEMENT_POLICY_VERSION =
            "/repository/identity/entitlement/policy/version/";

    public static final String ENTITLEMENT_POLICY_DATA =
            "/repository/identity/entitlement/policy/data/";

    public static final String ENTITLEMENT_POLICY_PAP = "/repository/identity/entitlement/policy/pap/";

    // entitlement.properties file configurations

    public static final String ON_DEMAND_POLICY_LOADING = "PDP.OnDemangPolicyLoading.Enable";

    public static final String ON_DEMAND_POLICY_MAX_POLICY_ENTRIES =
            "PDP.OnDemangPolicyLoading.MaxInMemoryPolicies";

    public static final String MAX_POLICY_REFERENCE_ENTRIES = "PDP.References.MaxPolicyEntries";

    public static final int MAX_NO_OF_IN_MEMORY_POLICIES = 10;

    public static final String DECISION_CACHING = "PDP.DecisionCaching.Enable";

    public static final String DECISION_CACHING_INTERVAL = "PDP.DecisionCaching.CachingInterval";

    public static final String ATTRIBUTE_CACHING = "PDP.AttributeCaching.Enable";

    public static final String ATTRIBUTE_CACHING_INTERVAL = "PDP.AttributeCaching.CachingInterval";

    public static final String RESOURCE_CACHING = "PDP.ResourceCaching.Enable";

    public static final String RESOURCE_CACHING_INTERVAL = "PDP.DecisionCaching.CachingInterval";

    public static final String PDP_ENABLE = "PDP.Enable";

    public static final String PAP_ENABLE = "PAP.Enable";

    public static final String BALANA_CONFIG_ENABLE = "PDP.Balana.Config.Enable";

    public static final String MULTIPLE_DECISION_PROFILE_ENABLE = "PDP.Multiple.Decision.Profile.Enable";

    public static final String FILESYSTEM_POLICY_PATH = "PAP.Policy.Add.Start.Policy.File.Path";

    public static final String START_UP_POLICY_ADDING = "PAP.Policy.Add.Start.Enable";

    public static final String POLICY_ID_REGEXP_PATTERN = "PAP.Policy.Id.Regexp.Pattern";

    public static final String ENTITLEMENT_ITEMS_PER_PAGE = "PAP.Items.Per.Page";

    public static final String PDP_GLOBAL_COMBINING_ALGORITHM = "PDP.Global.Policy.Combining.Algorithm";

    public static final String REGISTRY_MEDIA_TYPE = "application/xacml-policy+xml";

    public static final String ENTITLEMENT_ENGINE_CACHING_INTERVAL = "Entitlement.Engine.CachingInterval";

    public static final String PDP_REGISTRY_LEVEL_POLICY_CACHE_CLEAR = "PDP.Registry.Level.Policy.Cache.Clear";

    public static final String POLICY_CACHING_INTERVAL = "PDP.PolicyCaching.CachingInterval";

    public static final String XACML_JSON_SHORT_FORM_ENABLED = "JSON.Shorten.Form.Enabled";

    public static final String USER_CATEGORY = "http://wso2.org/identity/user";

    public static final String USER_TYPE_ID = USER_CATEGORY + "/user-type";
}
