/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.claim.metadata.mgt.listener;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.List;
import java.util.Map;

/**
 * Log audit events in claim metadata management.
 */
public class ClaimMetadataManagementAuditLogger extends AbstractEventHandler {

    private static final Log audit = CarbonConstants.AUDIT_LOG;
    private static final String AUDIT_MESSAGE = "Initiator : %s | Action : %s | Target : %s | Data : { %s } | Result : %s ";
    private static final String SUCCESS = "Success";

    private static final Log log = LogFactory.getLog(ClaimMetadataManagementAuditLogger.class);

    /**
     * This handles the claim related operations that are subscribed and publish audit logs for those operations.
     *
     * @param event Event.
     * @throws IdentityEventException
     */
    @Override
    public void handleEvent(Event event) throws IdentityEventException {

        int tenantId = (int) event.getEventProperties().get(IdentityEventConstants.EventProperty.TENANT_ID);
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        if (log.isDebugEnabled()) {
            log.debug(event.getEventName() + " event received to ClaimMetadataManagementAuditLogger for the " +
                    "tenant: " + tenantDomain);
        }
        String initiator = getInitiator(tenantDomain);
        if (IdentityEventConstants.Event.POST_ADD_CLAIM_DIALECT.equals(event.getEventName())) {
            String claimDialectUri =
                    (String) event.getEventProperties().get(IdentityEventConstants.EventProperty.CLAIM_DIALECT_URI);
            audit.info(String.format(AUDIT_MESSAGE, initiator, "Add-Claim-Dialect", claimDialectUri, StringUtils.EMPTY,
                    SUCCESS));
        } else if (IdentityEventConstants.Event.POST_UPDATE_CLAIM_DIALECT.equals(event.getEventName())) {
            String oldClaimDialectUri =
                    (String) event.getEventProperties().get(IdentityEventConstants.EventProperty.OLD_CLAIM_DIALECT_URI);
            String newClaimDialectUri =
                    (String) event.getEventProperties().get(IdentityEventConstants.EventProperty.NEW_CLAIM_DIALECT_URI);
            String data = "Original-State:" + oldClaimDialectUri + ", Changed-State:" + newClaimDialectUri;
            audit.info(String.format(AUDIT_MESSAGE, initiator, "Update-Claim-Dialect", oldClaimDialectUri, data, SUCCESS));
        } else if (IdentityEventConstants.Event.POST_DELETE_CLAIM_DIALECT.equals(event.getEventName())) {
            String claimDialectUri =
                    (String) event.getEventProperties().get(IdentityEventConstants.EventProperty.CLAIM_DIALECT_URI);
            audit.info(String.format(AUDIT_MESSAGE, initiator, "Delete-Claim-Dialect", claimDialectUri,
                    StringUtils.EMPTY, SUCCESS));
        } else if (IdentityEventConstants.Event.POST_ADD_LOCAL_CLAIM.equals(event.getEventName())) {
            String localClaimUri =
                    (String) event.getEventProperties().get(IdentityEventConstants.EventProperty.LOCAL_CLAIM_URI);
            String data = buildLocalClaimData(event);
            audit.info(String.format(AUDIT_MESSAGE, initiator, "Add-Local-Claim", localClaimUri, data, SUCCESS));
        } else if (IdentityEventConstants.Event.POST_UPDATE_LOCAL_CLAIM.equals(event.getEventName())) {
            String localClaimUri =
                    (String) event.getEventProperties().get(IdentityEventConstants.EventProperty.LOCAL_CLAIM_URI);
            String data = buildLocalClaimData(event);
            audit.info(String.format(AUDIT_MESSAGE, initiator, "Update-Local-Claim", localClaimUri, data, SUCCESS));
        } else if (IdentityEventConstants.Event.POST_DELETE_LOCAL_CLAIM.equals(event.getEventName())) {
            String claimDialectUri =
                    (String) event.getEventProperties().get(IdentityEventConstants.EventProperty.CLAIM_DIALECT_URI);
            String claimUri = (String) event.getEventProperties()
                    .get(IdentityEventConstants.EventProperty.LOCAL_CLAIM_URI);
            String data = "Claim Dialect URI:" + claimDialectUri + ", Claim URI:" + claimUri;
            audit.info(String.format(AUDIT_MESSAGE, initiator, "Delete-Local-Claim", claimUri, data, SUCCESS));
        } else if (IdentityEventConstants.Event.POST_ADD_EXTERNAL_CLAIM.equals(event.getEventName())) {
            String externalClaimUri =
                    (String) event.getEventProperties().get(IdentityEventConstants.EventProperty.EXTERNAL_CLAIM_URI);
            String data = buildExternalClaimData(event);
            audit.info(String.format(AUDIT_MESSAGE, initiator, "Add-External-Claim", externalClaimUri, data, SUCCESS));
        } else if (IdentityEventConstants.Event.POST_UPDATE_EXTERNAL_CLAIM.equals(event.getEventName())) {
            String externalClaimUri =
                    (String) event.getEventProperties().get(IdentityEventConstants.EventProperty.EXTERNAL_CLAIM_URI);
            String data = buildExternalClaimData(event);
            audit.info(String.format(AUDIT_MESSAGE, initiator, "Update-External-Claim", externalClaimUri, data, SUCCESS));
        } else if (IdentityEventConstants.Event.POST_DELETE_EXTERNAL_CLAIM.equals(event.getEventName())) {
            String claimDialectUri =
                    (String) event.getEventProperties().get(IdentityEventConstants.EventProperty.CLAIM_DIALECT_URI);
            String externalClaimUri = (String) event.getEventProperties()
                    .get(IdentityEventConstants.EventProperty.EXTERNAL_CLAIM_URI);
            String data = "Claim Dialect URI:" + claimDialectUri + ", Claim URI:" + externalClaimUri;
            audit.info(String.format(AUDIT_MESSAGE, initiator, "Delete-External-Claim", externalClaimUri, data, SUCCESS));
        }
    }

    @Override
    public String getName() {

        return "ClaimMetadataManagementAuditLogger";
    }

    private String getInitiatorUsername(String tenantDomain) {

        String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isNotBlank(user)) {
            // Append tenant domain to username build the full qualified username of initiator.
            user = UserCoreUtil.addTenantDomainToEntry(user, tenantDomain);
        } else {
            user = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }
        return user;
    }

    private String buildExternalClaimData(Event event) {

        String claimDialectUri =
                (String) event.getEventProperties().get(IdentityEventConstants.EventProperty.CLAIM_DIALECT_URI);
        String externalClaimUri =
                (String) event.getEventProperties().get(IdentityEventConstants.EventProperty.EXTERNAL_CLAIM_URI);
        String mappedLocalClaimUri =
                (String) event.getEventProperties().get(IdentityEventConstants.EventProperty.MAPPED_LOCAL_CLAIM_URI);
        Map<String, String> claimProperties =
                (Map<String, String>) event.getEventProperties()
                        .get(IdentityEventConstants.EventProperty.EXTERNAL_CLAIM_PROPERTIES);

        StringBuilder data = new StringBuilder();
        data.append("External Claim URI:").append(externalClaimUri).append(", ");
        data.append("Claim Dialect URI:").append(claimDialectUri).append(", ");
        data.append("Mapped Local Claim URI:").append(mappedLocalClaimUri);
        if (MapUtils.isNotEmpty(claimProperties)) {
            data.append(", Claim Properties: {");
            String joiner = "";
            for (String key : claimProperties.keySet()) {
                String value = claimProperties.get(key);
                data.append(joiner).append(key).append(":").append(value);
                joiner = ", ";
            }
            data.append("}");
        }
        return data.toString();
    }

    private String buildLocalClaimData(Event event) {

        String claimDialectUri =
                (String) event.getEventProperties().get(IdentityEventConstants.EventProperty.CLAIM_DIALECT_URI);
        String localClaimUri =
                (String) event.getEventProperties().get(IdentityEventConstants.EventProperty.LOCAL_CLAIM_URI);
        Map<String, String> claimProperties =
                (Map<String, String>) event.getEventProperties()
                        .get(IdentityEventConstants.EventProperty.LOCAL_CLAIM_PROPERTIES);
        StringBuilder data = new StringBuilder();
        data.append("Local Claim URI:").append(localClaimUri).append(", ");
        data.append("Claim Dialect URI:").append(claimDialectUri);
        if (MapUtils.isNotEmpty(claimProperties)) {
            data.append(", Claim Properties: {");
            String joiner = "";
            for (String key : claimProperties.keySet()) {
                String value = claimProperties.get(key);
                data.append(joiner).append(key).append(":").append(value);
                joiner = ", ";
            }
            data.append("}");
        }

        List<AttributeMapping> attributeMappings = (List<AttributeMapping>) event.getEventProperties()
                .get(IdentityEventConstants.EventProperty.MAPPED_ATTRIBUTES);
        if (CollectionUtils.isNotEmpty(attributeMappings)) {
            data.append(", Attribute Mappings:[");
            String joiner = "";
            for (AttributeMapping mapping : attributeMappings) {
                data.append(joiner);
                joiner = ", ";
                data.append("{Name:").append(mapping.getAttributeName()).append(", Userstore Domain:")
                        .append(mapping.getUserStoreDomain()).append("}");
            }
            data.append("]");
        }
        return data.toString();
    }

    /**
     * Get the initiator for audit logs.
     *
     * @param tenantDomain Tenant domain of the initiator.
     * @return initiator.
     */
    private String getInitiator(String tenantDomain) {

        if (LoggerUtils.isLogMaskingEnable) {
            String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(tenantDomain)) {
                return IdentityUtil.getInitiatorId(username, tenantDomain);
            }
            return LoggerUtils.getMaskedContent(getInitiatorUsername(tenantDomain));
        }
        return getInitiatorUsername(tenantDomain);
    }
}
