/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.user.mgt.listeners;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.mgt.listeners.utils.ListenerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.utils.CarbonUtils.isLegacyAuditLogsDisabled;

/**
 * User operation listener which takes the claim changes and logs into "Audit" log.
 * The claim values available with config found in "identity.xml" under key
 * LoggableUserClaims.LoggableUserClaim.
 */
public class UserClaimsAuditLogger extends AbstractIdentityUserOperationEventListener {

    private static Log log = LogFactory.getLog(UserClaimsAuditLogger.class);

    private static final Log audit = CarbonConstants.AUDIT_LOG;
    private static final int DEFAULT_EXECUTION_ORDER = 9;
    private static String AUDIT_MESSAGE = "Initiator : %s | Action : %s | Target : %s | Claims : { %s }";
    private static String AUDIT_MESSAGE_FOR_UPDATED_CLAIMS = "Initiator : %s | Action : %s | Target : %s | " +
            "Added Claims : { %s } | Updated Claims : { %s } | Removed Claims : { %s }";
    private static final String CONFIG_CHANGE_LOG_CLAIMS = "LoggableUserClaims.LoggableUserClaim";
    private static final String LOG_UPDATED_CLAIMS_ONLY_PROPERTY = "LogUpdatedClaimsOnly";
    private static final String EVENT_LISTENER_TYPE = "org.wso2.carbon.user.core.listener.UserOperationEventListener";
    private String[] loggableClaimURIs;
    private static final String DEFAULT = "default";
    private static final String ROLE_CLAIM_URI = "http://wso2.org/claims/role";

    @Override
    public boolean isEnable() {

        if (super.isEnable()) {
            return !isLegacyAuditLogsDisabled();
        }
        return false;
    }

    @Override
    public int getExecutionOrderId() {

        int result = super.getExecutionOrderId();
        return result <= 0 ? DEFAULT_EXECUTION_ORDER : result;
    }

    public void init() {

        Object configValue = IdentityConfigParser.getInstance().getConfiguration().get(CONFIG_CHANGE_LOG_CLAIMS);
        List<String> claimsFilters = new ArrayList<>();
        if (configValue instanceof ArrayList) {
            claimsFilters = (ArrayList) configValue;
        } else if (configValue instanceof String) {
            claimsFilters.add((String)configValue);
        }
        if (CollectionUtils.isEmpty(claimsFilters)) {
            if (log.isDebugEnabled()) {
                log.debug("No Claim filters configured under " + CONFIG_CHANGE_LOG_CLAIMS + ". User claim changes " +
                        "will not be logged.");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Some Claim filters configured under " + CONFIG_CHANGE_LOG_CLAIMS + ". User claim changes " +
                        "will be logged.");
            }
            // Split the claimsFilters and assign to loggableClaimURIs.
            loggableClaimURIs = StringUtils.stripAll(claimsFilters.toArray(new String[claimsFilters.size()]));
        }
    }


    @Override
    public boolean doPreSetUserClaimValue(String userName, String claimURI, String claimValue, String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        if (isEnable()) {
            logClaims(userName, "doPreSetUserClaimValue", userStoreManager);
        }
        return true;
    }

    @Override
    public boolean doPostSetUserClaimValue(String userName, UserStoreManager userStoreManager) throws UserStoreException {

        if (isEnable()) {
            logClaims(userName, "doPostSetUserClaimValue", userStoreManager);
        }
        return true;
    }

    @Override
    public boolean doPreSetUserClaimValues(String userName, Map<String, String> claims, String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        if (isEnable()) {
            if (isLogUpdatedClaimsOnlyPropertyEnabled()) {
                logUpdatedClaims(userName, claims, "doPreSetUserClaimValues", userStoreManager);
            } else {
                logClaims(userName, "doPreSetUserClaimValues", userStoreManager);
            }
        }
        return true;
    }

    @Override
    public boolean doPostSetUserClaimValues(String userName, Map<String, String> claims, String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        if (isEnable()) {
            if (isLogUpdatedClaimsOnlyPropertyEnabled()) {
                return true;
            } else {
                logClaims(userName, "doPostSetUserClaimValues", userStoreManager);
            }
        }
        return true;
    }

    /**
     * This method is to check whether the 'LogUpdatedClaimsOnly' property is enabled for the EventListener
     * UserOperationEventListener in the identity.xml file.
     *
     * @return Whether 'LogUpdatedClaimsOnly' property is enabled or not.
     */
    private boolean isLogUpdatedClaimsOnlyPropertyEnabled() {

        Object propertyValue = IdentityUtil.readEventListenerProperty(EVENT_LISTENER_TYPE, this.getClass().getName())
                .getProperties().get(LOG_UPDATED_CLAIMS_ONLY_PROPERTY);
        if (propertyValue != null) {
            return Boolean.parseBoolean(propertyValue.toString());
        }

        return false;
    }

    /**
     * This will log only the updated user claims rather than logging every claim that is added under the loggable
     * claims configuration.
     *
     * @param userName         Username.
     * @param claims           User claims.
     * @param action           Action to be logged in the audit log.
     * @param userStoreManager Userstore manager.
     */
    private void logUpdatedClaims(String userName, Map<String, String> claims, String action,
                                  UserStoreManager userStoreManager) {

        try {
            Map<String, String> addedClaims = new HashMap<>();
            Map<String, String> updatedClaims = new HashMap<>();
            Map<String, String> removedClaims = new HashMap<>();
            Map<String, String> loggableClaims = userStoreManager.getUserClaimValues(userName, loggableClaimURIs,
                    DEFAULT);
            resolveLoggableClaims(loggableClaims);

            for (Map.Entry<String, String> entry : loggableClaims.entrySet()) {
                String claimURI = entry.getKey();
                String claimValue = entry.getValue();
                String updatedClaimValue = claims.get(claimURI);
                // Get updated claims.
                if (StringUtils.isNotEmpty(updatedClaimValue) && !updatedClaimValue.equals(claimValue)) {
                    updatedClaims.put(claimURI, updatedClaimValue);
                }
                // Get claims when the claim value is removed.
                if (updatedClaimValue != null &&
                        StringUtils.isEmpty(updatedClaimValue) && StringUtils.isNotEmpty(claimValue)) {
                    removedClaims.put(claimURI, claimValue);
                }
            }

            // Get claims when the claim value is set.
            for (String loggableClaim : loggableClaimURIs) {
                String claimValue = loggableClaims.get(loggableClaim);
                String updatedClaimValue = claims.get(loggableClaim);
                if (StringUtils.isNotEmpty(updatedClaimValue) && StringUtils.isEmpty(claimValue)) {
                    addedClaims.put(loggableClaim, updatedClaimValue);
                }
            }

            if (MapUtils.isNotEmpty(addedClaims) || MapUtils.isNotEmpty(updatedClaims) ||
                    MapUtils.isNotEmpty(removedClaims)) {
                if (LoggerUtils.isLogMaskingEnable) {
                    audit.info(String.format(AUDIT_MESSAGE_FOR_UPDATED_CLAIMS, ListenerUtils.getInitiatorFromContext(),
                            action, LoggerUtils.getMaskedContent(userName), formatClaims(LoggerUtils.getMaskedClaimsMap(
                                    addedClaims)), formatClaims(LoggerUtils.getMaskedClaimsMap(updatedClaims)),
                            formatClaims(LoggerUtils.getMaskedClaimsMap(removedClaims))));
                } else {
                    audit.info(String.format(AUDIT_MESSAGE_FOR_UPDATED_CLAIMS, ListenerUtils.getInitiatorFromContext(),
                            action, userName, formatClaims(addedClaims), formatClaims(updatedClaims),
                            formatClaims(removedClaims)));
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Updated claims are not configured under the user: " + userName);
                }
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while logging updated user claim changes.", e);
        }
    }

    /**
     * Resolve the map of loggable claims to remove read-only claims, as those claims cannot be updated and cannot be
     * logged as an audit log.
     *
     * @param loggableClaims Claims configured under LoggableUserClaims.
     */
    private void resolveLoggableClaims(Map<String, String> loggableClaims) {

        loggableClaims.remove(ROLE_CLAIM_URI);
        if (log.isDebugEnabled()) {
            log.debug(ROLE_CLAIM_URI + " claim is removed from the loggable claims as it is a read-only claim.");
        }
    }

    private void logClaims(String userName, String action, UserStoreManager userStoreManager) {

        try {
            Map<String, String> loggableClaims = userStoreManager.getUserClaimValues(userName, loggableClaimURIs,
                    DEFAULT);
            if (MapUtils.isNotEmpty(loggableClaims)) {
                if (LoggerUtils.isLogMaskingEnable) {
                    audit.info(String.format(AUDIT_MESSAGE, ListenerUtils.getInitiatorFromContext(), action, LoggerUtils
                            .getMaskedContent(userName), formatClaims(LoggerUtils.getMaskedClaimsMap(loggableClaims))));
                } else {
                    audit.info(String.format(AUDIT_MESSAGE, ListenerUtils.getInitiatorFromContext(), action, userName,
                            formatClaims(loggableClaims)));
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No claims are configured under the user : " + userName);
                }
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while logging user claim changes.", e);
        }
    }

    private String formatClaims(Map<String, String> claims) {

        StringBuilder stringBuilder = new StringBuilder();

        boolean notEmpty = false;
        for (Map.Entry<String, String> claimEntry : claims.entrySet()) {
            if (notEmpty) {
                stringBuilder.append(" , ");
            }
            if (StringUtils.isNotEmpty(claimEntry.getValue())) {
                stringBuilder.append("\"").append(claimEntry.getKey()).append("\"");
                stringBuilder.append(" : ");
                stringBuilder.append("\"").append(claimEntry.getValue()).append("\"");
                notEmpty = true;
            }
        }
        return stringBuilder.toString();
    }
}
