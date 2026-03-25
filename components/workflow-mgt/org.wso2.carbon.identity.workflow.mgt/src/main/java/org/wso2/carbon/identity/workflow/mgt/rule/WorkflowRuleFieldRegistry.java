/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.workflow.mgt.rule;

import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.Value;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry of static rule field definitions for the approval workflows.
 *
 */
public class WorkflowRuleFieldRegistry {

    public static final Map<String, FieldDefinition> FIELDS;

    private static final String ROLES_VALUES_LINK = "/scim2/v2/Roles?offset=0&count=10";
    private static final String ROLES_FILTER_LINK = "/scim2/v2/Roles?filter=&count=10";
    private static final String GROUPS_VALUES_LINK = "/scim2/Groups?offset=0&count=10";
    private static final String GROUPS_FILTER_LINK = "/scim2/Groups?filter=&count=10";
    private static final String USERSTORES_VALUES_LINK = "/userstores";
    private static final String APPLICATIONS_VALUES_LINK = "/applications?excludeSystemPortals=false&offset=0"
        + "&limit=10&attributes=associatedRoles.allowedAudience";
    private static final String APPLICATIONS_FILTER_LINK = "/applications?excludeSystemPortals=false&filter="
        + "&limit=10&attributes=associatedRoles.allowedAudience";

    static {
        Map<String, FieldDefinition> fields = new LinkedHashMap<>();

        fields.put("user.domain", new FieldDefinitionBuilder()
                .name("user.domain")
                .displayName("user store domain of user")
                .operators("equals", "notEquals", "contains", "notContains", "startsWith", "endsWith")
                .options()
                    .valueType(Value.ValueType.REFERENCE)
                    .referenceAttr("name")
                    .displayAttr("name")
                    .addLink(USERSTORES_VALUES_LINK, "GET", "values")
                .build());

        fields.put("user.groups", new FieldDefinitionBuilder()
                .name("user.groups")
                .displayName("assigned user groups")
                .operators("contains", "notContains")
                .options()
                    .valueType(Value.ValueType.REFERENCE)
                    .referenceAttr("id")
                    .displayAttr("displayName")
                    .addLink(GROUPS_VALUES_LINK, "GET", "values")
                    .addLink(GROUPS_FILTER_LINK, "GET", "filter")
                .build());

        fields.put("user.roles", new FieldDefinitionBuilder()
                .name("user.roles")
                .displayName("assigned user roles")
                .operators("contains", "notContains")
                .options()
                    .valueType(Value.ValueType.REFERENCE)
                    .referenceAttr("id")
                    .displayAttr("displayName")
                    .addLink(ROLES_VALUES_LINK, "GET", "values")
                    .addLink(ROLES_FILTER_LINK, "GET", "filter")
                .build());

        fields.put("initiator.domain", new FieldDefinitionBuilder()
                .name("initiator.domain")
                .displayName("user store domain of request initiator")
                .operators("equals", "notEquals", "contains", "notContains", "startsWith", "endsWith")
                .options()
                    .valueType(Value.ValueType.REFERENCE)
                    .referenceAttr("name")
                    .displayAttr("name")
                    .addLink(USERSTORES_VALUES_LINK, "GET", "values")
                .build());

        fields.put("initiator.groups", new FieldDefinitionBuilder()
                .name("initiator.groups")
                .displayName("assigned user groups of request initiator")
                .operators("contains", "notContains")
                .options()
                    .valueType(Value.ValueType.REFERENCE)
                    .referenceAttr("id")
                    .displayAttr("displayName")
                    .addLink(GROUPS_VALUES_LINK, "GET", "values")
                    .addLink(GROUPS_FILTER_LINK, "GET", "filter")
                .build());

        fields.put("initiator.roles", new FieldDefinitionBuilder()
                .name("initiator.roles")
                .displayName("assigned user roles of request initiator")
                .operators("contains", "notContains")
                .options()
                    .valueType(Value.ValueType.REFERENCE)
                    .referenceAttr("id")
                    .displayAttr("displayName")
                    .addLink(ROLES_VALUES_LINK, "GET", "values")
                    .addLink(ROLES_FILTER_LINK, "GET", "filter")
                .build());

        fields.put("role.id", new FieldDefinitionBuilder()
                .name("role.id")
                .displayName("role identifier")
                .operators("equals", "notEquals")
                .options()
                    .valueType(Value.ValueType.REFERENCE)
                    .referenceAttr("id")
                    .displayAttr("displayName")
                    .addLink(ROLES_VALUES_LINK, "GET", "values")
                    .addLink(ROLES_FILTER_LINK, "GET", "filter")
                .build());

        fields.put("role.audience", new FieldDefinitionBuilder()
                .name("role.audience")
                .displayName("audience of the role")
                .operators("equals", "notEquals")
                .options()
                    .valueType(Value.ValueType.REFERENCE)
                    .referenceAttr("id")
                    .displayAttr("name")
                    .addLink(APPLICATIONS_VALUES_LINK, "GET", "values")
                    .addLink(APPLICATIONS_FILTER_LINK, "GET", "filter")
                .build());

        fields.put("role.permissions", new FieldDefinitionBuilder()
                .name("role.permissions")
                .displayName("permissions of the role")
                .operators("contains", "notContains")
                .input()
                    .valueType(Value.ValueType.STRING)
                .build());
        
        fields.put("role.hasAssignedUsers", new FieldDefinitionBuilder()
                .name("role.hasAssignedUsers")
                .displayName("add users to the role")
                .operators("equals")
                .options()
                    .valueType(Value.ValueType.STRING)
                    .addFixedValue("true", "true")
                    .addFixedValue("false", "false")
                .build());

        fields.put("role.hasUnassignedUsers", new FieldDefinitionBuilder()
                .name("role.hasUnassignedUsers")
                .displayName("remove users from the role")
                .operators("equals")
                .options()
                    .valueType(Value.ValueType.STRING)
                    .addFixedValue("true", "true")
                    .addFixedValue("false", "false")
                .build());

        FIELDS = Collections.unmodifiableMap(fields);
    }

    private WorkflowRuleFieldRegistry() {

    }
}
