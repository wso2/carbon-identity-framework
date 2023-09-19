/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.ui;

/**
 * Constants related with XACML policy such as per-defined Element Names and NameSpaces
 */

/**
 *  @deprecated  As this moved to org.wso2.carbon.identity.entitlement.common
 */
@Deprecated
public class EntitlementPolicyConstants {

    public static final int DEFAULT_ITEMS_PER_PAGE = 10;
    public static final String ENTITLEMENT_ADMIN_CLIENT = "EntitlementAdminClient";
    public static final String ENTITLEMENT_SUBSCRIBER_CLIENT = "EntitlementSubscriberClient";

    public static final String ENTITLEMENT_CURRENT_VERSION = "currentVersion";

    public static final String XACML3_POLICY_NAMESPACE = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17";

    public static final String ATTRIBUTE_NAMESPACE = "urn:oasis:names:tc:xacml:2.0:example:attribute:";

    public static final String POLICY_ELEMENT = "Policy";

    public static final String APPLY_ELEMENT = "Apply";

    public static final String MATCH_ELEMENT = "Match";

    public static final String SUBJECT_ELEMENT = "Subject";

    public static final String ACTION_ELEMENT = "Action";

    public static final String RESOURCE_ELEMENT = "Resource";

    public static final String ENVIRONMENT_ELEMENT = "Environment";

    public static final String POLICY_ID = "PolicyId";

    public static final String RULE_ALGORITHM = "RuleCombiningAlgId";

    public static final String POLICY_VERSION = "Version";

    public static final String DESCRIPTION_ELEMENT = "Description";

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

    public static final String CATEGORY = "Category";

    public static final String ATTRIBUTE = "Attribute";

    public static final String ATTRIBUTES = "Attributes";

    public static final String INCLUDE_RESULT = "IncludeInResult";

    public static final String DATA_TYPE = "DataType";

    public static final String ISSUER = "Issuer";

    public static final String MUST_BE_PRESENT = "MustBePresent";

    public static final String REQUEST_CONTEXT_PATH = "RequestContextPath";

    public static final String MATCH_ID = "MatchId";

    public static final String RULE_ID = "RuleId";

    public static final String RULE_EFFECT = "Effect";

    public static final String RULE_DESCRIPTION = "Description";

    public static final String FUNCTION_ID = "FunctionId";

    public static final String VARIABLE_ID = "VariableId";

    public static final String OBLIGATION_EXPRESSIONS = "ObligationExpressions";

    public static final String OBLIGATION_EXPRESSION = "ObligationExpression";

    public static final String OBLIGATION_ID = "ObligationId";

    public static final String OBLIGATION_EFFECT = "FulfillOn";

    public static final String ADVICE_EXPRESSIONS = "AdviceExpressions";

    public static final String ADVICE_EXPRESSION = "AdviceExpression";

    public static final String ADVICE_ID = "AdviceId";

    public static final String ADVICE_EFFECT = "AppliesTo";

    public static final String ATTRIBUTE_ASSIGNMENT = "AttributeAssignmentExpression";

    public static final String STRING_DATA_TYPE = "http://www.w3.org/2001/XMLSchema#string";

    public static final String INT_DATA_TYPE = "http://www.w3.org/2001/XMLSchema#integer";

    public static final String BOOLEAN_DATA_TYPE = "http://www.w3.org/2001/XMLSchema#boolean";

    public static final String DATE_DATA_TYPE = "http://www.w3.org/2001/XMLSchema#date";

    public static final String TIME_DATA_TYPE = "http://www.w3.org/2001/XMLSchema#time";

    public static final String DATE_TIME_DATA_TYPE = "http://www.w3.org/2001/XMLSchema#dateTime";

    public static final String FUNCTION_BAG = "urn:oasis:names:tc:xacml:1.0:function:string-bag";

    public static final String SUBJECT_ID_DEFAULT = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";

    public static final String SUBJECT_ID_ROLE = "http://wso2.org/claims/roles";

    public static final String RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";

    public static final String RESOURCE_ID_DEFAULT = "urn:oasis:names:tc:xacml:1.0:resource:resource";

//    public static final String FUNCTION_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:string-equal";
//
//    public static final String FUNCTION_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:string-one-and-only";
//
//    public static final String FUNCTION_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:string-is-in";
//
//    public static final String FUNCTION_REGEXP = "urn:oasis:names:tc:xacml:1.0:function:string-regexp-match";
//
//    public static final String FUNCTION_AT_LEAST = "urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of";
//
//    public static final String FUNCTION_UNION = "urn:oasis:names:tc:xacml:1.0:function:string-union";
//
//    public static final String FUNCTION_SUBSET = "urn:oasis:names:tc:xacml:1.0:function:string-subset";
//
//    public static final String FUNCTION_SET_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:string-set-equals";
//
//    public static final String FUNCTION_ANY_OF = "urn:oasis:names:tc:xacml:1.0:function:any-of";
//
//    public static final String FUNCTION_AND = "urn:oasis:names:tc:xacml:1.0:function:and";
//
//    public static final String EQUAL_TO = "equals to";
//
//    public static final String MATCH_TO = "matching-with";
//
//    public static final String IS_IN = "in";
//
//    public static final String REGEXP_MATCH = "matching reg-ex to";
//
//    public static final String AT_LEAST = "at-least-one-member-of";
//
//    public static final String AT_LEAST_ONE_MATCH = "at-least-one-matching-member-of";
//
//    public static final String AT_LEAST_ONE_MATCH_REGEXP = "at-least-one-matching-reg-ex-member-of";
//
//    public static final String SUBSET_OF = "a-sub-set-of";
//
//    public static final String SET_OF = "a-matching-set-of";
//
//    public static final String MATCH_REGEXP_SET_OF = "a matching reg-ex set of";

    public static final String RULE_EFFECT_PERMIT = "Permit";

    public static final String RULE_EFFECT_NOT_APPLICABLE = "Not Applicable";

    public static final String RULE_EFFECT_DENY = "Deny";

    public static final String ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";

    public static final String ENVIRONMENT_ID = "urn:oasis:names:tc:xacml:1.0:environment:environment-id";

    public static final String SUBJECT_TYPE_ROLES = "Roles";

    public static final String SUBJECT_TYPE_USERS = "Users";

    public static final String DEFAULT_CARBON_DIALECT = "http://wso2.org/claims";

    public static final String IMPORT_POLICY_REGISTRY = "Registry";

    public static final String IMPORT_POLICY_FILE_SYSTEM = "FileSystem";

    public static final String REQ_RES_CONTEXT_XACML2 = "urn:oasis:names:tc:xacml:2.0:context:schema:os";

    public static final String REQ_RES_CONTEXT_XACML3 = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17";

    public static final String REQ_SCHEME = "http://www.w3.org/2001/XMLSchema-instance";

    public static final String RETURN_POLICY_LIST = "ReturnPolicyIdList";

    public static final String COMBINED_DECISION = "CombinedDecision";

    public static final String REQUEST_ELEMENT = "Request";

    public static final String POLICY_SET_ID = "PolicySetId";

    public static final String POLICY_ALGORITHM = "PolicyCombiningAlgId";

    public static final String POLICY_SET_ELEMENT = "PolicySet";

    public static final String POLICY_REFERENCE = "PolicyIdReference";

    public static final String POLICY_SET_REFERENCE = "PolicySetIdReference";

    public static final String ATTRIBUTE_SEPARATOR = ",";

    public static final String COMBO_BOX_DEFAULT_VALUE = "---Select---";

    public static final String COMBO_BOX_ANY_VALUE = "Any";

    public static final String SEARCH_ERROR = "Search_Error";

    public static final String DEFAULT_META_DATA_MODULE_NAME = "Carbon Attribute Finder Module";

    public static final int BASIC_POLICY_EDITOR_RULE_DATA_AMOUNT = 23;

    public static final int BASIC_POLICY_EDITOR_TARGET_DATA_AMOUNT = 20;

    public static final String ENTITLEMENT_PUBLISHER_PROPERTY = "entitlementPublisherPropertyDTO";

    public static final String ENTITLEMENT_PUBLISHER_MODULE = "entitlementPublisherModuleHolders";

}
