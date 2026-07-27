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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.policy.management.internal.util;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.utils.AuditLog;

import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.jsonObjectToMap;
import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.triggerAuditLogEvent;

/**
 * Policy management audit logger class.
 * This class is responsible for logging policy management mutations, i.e. adding, updating and
 * deleting policies. Reads are intentionally not audited. Only the policy id, name, tenant domain
 * and resource targets/types are logged; rule conditions or other resource content are never included.
 */
public class PolicyManagementAuditLogger {

    /**
     * Print policy audit log related to the operation.
     *
     * @param operation Operation associated with the state change.
     * @param policy    Policy to be logged.
     */
    public void printAuditLog(Operation operation, Policy policy) {

        JSONObject data = createAuditLogEntry(policy);
        buildAuditLog(policy.getId(), operation, data);
    }

    /**
     * Build audit log using the provided data.
     *
     * @param targetId  ID of the policy the log entry is about.
     * @param operation Operation to be logged.
     * @param data      Data to be logged.
     */
    private void buildAuditLog(String targetId, Operation operation, JSONObject data) {

        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(getInitiatorId(),
                LoggerUtils.getInitiatorType(getInitiatorId()),
                targetId,
                LoggerUtils.Target.Policy.name(),
                operation.getLogAction()).
                data(jsonObjectToMap(data));
        triggerAuditLogEvent(auditLogBuilder);
    }

    /**
     * Create audit log data for the given policy.
     *
     * @param policy Policy to be logged.
     * @return Audit log data as a JSONObject.
     */
    private JSONObject createAuditLogEntry(Policy policy) {

        JSONObject data = new JSONObject();
        data.put(LogConstants.ID_FIELD, policy.getId() != null ? policy.getId() : JSONObject.NULL);
        data.put(LogConstants.NAME_FIELD, policy.getName() != null ? policy.getName() : JSONObject.NULL);
        data.put(LogConstants.TENANT_DOMAIN_FIELD,
                policy.getTenantDomain() != null ? policy.getTenantDomain() : JSONObject.NULL);
        data.put(LogConstants.RESOURCES_FIELD,
                policy.getResources() != null ? buildResourcesArray(policy) : JSONObject.NULL);
        return data;
    }

    /**
     * Build the resource summary array for a policy, containing only each resource's target and
     * type. Resource-specific content (e.g. rule conditions) is deliberately excluded.
     *
     * @param policy Policy whose resources are summarized. Must have a non-null resource list.
     * @return JSONArray of {@code {target, type}} entries.
     */
    private JSONArray buildResourcesArray(Policy policy) {

        JSONArray resources = new JSONArray();
        for (PolicyResource resource : policy.getResources()) {
            if (resource == null) {
                continue;
            }
            JSONObject resourceEntry = new JSONObject();
            resourceEntry.put(LogConstants.RESOURCE_TARGET_FIELD,
                    resource.getTarget() != null ? resource.getTarget() : JSONObject.NULL);
            resourceEntry.put(LogConstants.RESOURCE_TYPE_FIELD,
                    resource.getResourceType() != null ? resource.getResourceType().name() : JSONObject.NULL);
            resources.put(resourceEntry);
        }
        return resources;
    }

    /**
     * Get the initiator for audit logs.
     *
     * @return Initiator id despite masking.
     */
    private String getInitiatorId() {

        // Prefer IdentityContext → concrete actor userId if present
        IdentityContext idCtx = IdentityContext.getThreadLocalIdentityContext();
        if (idCtx.isUserActor() && StringUtils.isNotBlank(idCtx.getUserActor().getUserId())) {
            return idCtx.getUserActor().getUserId();
        }

        // Fallback to CarbonContext
        CarbonContext carbonCtx = CarbonContext.getThreadLocalCarbonContext();
        String username = carbonCtx.getUsername();
        String tenantDomain = carbonCtx.getTenantDomain();

        // If we still don't have a username, treat as system-initiated
        if (StringUtils.isBlank(username)) {
            return LoggerUtils.Initiator.System.name();
        }

        String initiator = null;
        if (StringUtils.isNotBlank(tenantDomain)) {
            initiator = IdentityUtil.getInitiatorId(username, tenantDomain);
        }

        // Final fallback: mask the username for privacy
        return StringUtils.isNotBlank(initiator) ? initiator : LoggerUtils.getMaskedContent(username);
    }

    /**
     * Operations to be logged.
     */
    public enum Operation {
        ADD("add-policy"),
        UPDATE("update-policy"),
        DELETE("delete-policy");

        private final String logAction;

        Operation(String logAction) {

            this.logAction = logAction;
        }

        /**
         * Get the log action associated with the operation.
         *
         * @return Log action string.
         */
        public String getLogAction() {

            return this.logAction;
        }
    }

    /**
     * Policy management related log constants.
     */
    private static class LogConstants {

        public static final String ID_FIELD = "Id";
        public static final String NAME_FIELD = "Name";
        public static final String TENANT_DOMAIN_FIELD = "TenantDomain";
        public static final String RESOURCES_FIELD = "Resources";
        public static final String RESOURCE_TARGET_FIELD = "target";
        public static final String RESOURCE_TYPE_FIELD = "type";
    }
}
