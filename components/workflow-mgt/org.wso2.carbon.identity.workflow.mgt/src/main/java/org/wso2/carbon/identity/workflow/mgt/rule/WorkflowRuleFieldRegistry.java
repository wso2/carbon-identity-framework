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

    static {
        Map<String, FieldDefinition> fields = new LinkedHashMap<>();

        fields.put("user.domain", new FieldDefinitionBuilder()
                .name("user.domain")
                .displayName("user domain")
                .operators("equals", "notEquals")
                .options()
                    .valueType(Value.ValueType.REFERENCE)
                    .referenceAttr("name")
                    .displayAttr("name")
                    .addLink("/api/server/v1/userstores", "GET", "values")
                .build());

        fields.put("user.groups", new FieldDefinitionBuilder()
                .name("user.groups")
                .displayName("user groups")
                .operators("contains")
                .options()
                    .valueType(Value.ValueType.REFERENCE)
                    .referenceAttr("id")
                    .displayAttr("displayName")
                    .addLink("/scim2/Groups?offset=0&limit=10", "GET", "values")
                    .addLink("/scim2/Groups?filter=displayName co &limit=10", "GET", "filter")
                .build());

        fields.put("user.roles", new FieldDefinitionBuilder()
                .name("user.roles")
                .displayName("user roles")
                .operators("contains")
                .options()
                    .valueType(Value.ValueType.REFERENCE)
                    .referenceAttr("id")
                    .displayAttr("displayName")
                    .addLink("/scim2/v2/Roles?offset=0&limit=10", "GET", "values")
                    .addLink("/scim2/v2/Roles?filter=displayName co &limit=10", "GET", "filter")
                .build());

        fields.put("initiator.domain", new FieldDefinitionBuilder()
                .name("initiator.domain")
                .displayName("initiator domain")
                .operators("equals", "notEquals")
                .options()
                    .valueType(Value.ValueType.REFERENCE)
                    .referenceAttr("name")
                    .displayAttr("name")
                    .addLink("/api/server/v1/userstores", "GET", "values")
                .build());

        fields.put("initiator.groups", new FieldDefinitionBuilder()
                .name("initiator.groups")
                .displayName("initiator groups")
                .operators("contains")
                .options()
                    .valueType(Value.ValueType.REFERENCE)
                    .referenceAttr("id")
                    .displayAttr("displayName")
                    .addLink("/scim2/Groups?offset=0&limit=10", "GET", "values")
                    .addLink("/scim2/Groups?filter=displayName co &limit=10", "GET", "filter")
                .build());

        fields.put("initiator.roles", new FieldDefinitionBuilder()
                .name("initiator.roles")
                .displayName("initiator roles")
                .operators("contains")
                .options()
                    .valueType(Value.ValueType.REFERENCE)
                    .referenceAttr("id")
                    .displayAttr("displayName")
                    .addLink("/scim2/v2/Roles?offset=0&limit=10", "GET", "values")
                    .addLink("/scim2/v2/Roles?filter=displayName co &limit=10", "GET", "filter")
                .build());

        fields.put("role.id", new FieldDefinitionBuilder()
                .name("role.id")
                .displayName("role")
                .operators("equals", "notEquals")
                .options()
                    .valueType(Value.ValueType.REFERENCE)
                    .referenceAttr("id")
                    .displayAttr("displayName")
                    .addLink("/scim2/v2/Roles?offset=0&limit=10", "GET", "values")
                    .addLink("/scim2/v2/Roles?filter=displayName co &limit=10", "GET", "filter")
                .build());

        fields.put("role.audience", new FieldDefinitionBuilder()
                .name("role.audience")
                .displayName("role audience")
                .operators("equals", "notEquals")
                .options()
                    .valueType(Value.ValueType.REFERENCE)
                    .referenceAttr("id")
                    .displayAttr("name")
                    .addLink("/applications?excludeSystemPortals=true&offset=0&limit=10", "GET", "values")
                    .addLink("/applications?excludeSystemPortals=true&filter=&limit=10", "GET", "filter")
                .build());

        fields.put("role.permissions", new FieldDefinitionBuilder()
                .name("role.permissions")
                .displayName("role permissions")
                .operators("contains")
                .input()
                    .valueType(Value.ValueType.STRING)
                .build());
        
        fields.put("role.hasAssignedUsers", new FieldDefinitionBuilder()
                .name("role.hasAssignedUsers")
                .displayName("role has assigned users")
                .operators("equals")
                .options()
                    .valueType(Value.ValueType.STRING)
                    .addFixedValue("true", "true")
                    .addFixedValue("false", "false")
                .build());

        fields.put("role.hasUnassignedUsers", new FieldDefinitionBuilder()
                .name("role.hasUnassignedUsers")
                .displayName("role has unassigned users")
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
