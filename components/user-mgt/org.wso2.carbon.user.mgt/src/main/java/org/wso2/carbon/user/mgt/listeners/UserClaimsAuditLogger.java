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
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private static final String CONFIG_CHANGE_LOG_CLAIMS = "LoggableUserClaims.LoggableUserClaim";
    private String[] loggableClaimURIs;
    private static final String DEFAULT = "default";

    @Override
    public int getExecutionOrderId() {

        int result = super.getExecutionOrderId();
        return result <= 0 ? DEFAULT_EXECUTION_ORDER : result;
    }

    public void init() {

        Object configValue = IdentityConfigParser.getInstance().getConfiguration().get(CONFIG_CHANGE_LOG_CLAIMS);
        List<String> claimsFilters = null;
        if (configValue instanceof ArrayList) {
            claimsFilters = (ArrayList) configValue;
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
            logClaims(userName, "doPreSetUserClaimValues", userStoreManager);
        }
        return true;
    }

    @Override
    public boolean doPostSetUserClaimValues(String userName, Map<String, String> claims, String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        if (isEnable()) {
            logClaims(userName, "doPostSetUserClaimValues", userStoreManager);
        }
        return true;
    }


    private void logClaims(String userName, String action, UserStoreManager userStoreManager) {

        try {
            Map<String, String> loggableClaims = userStoreManager.getUserClaimValues(userName, loggableClaimURIs,
                    DEFAULT);
            if (MapUtils.isNotEmpty(loggableClaims)) {
                audit.info(String.format(AUDIT_MESSAGE, getUser(), action, userName, formatClaims(loggableClaims)));
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

    /**
     * Get the logged in user's username who is calling the operation
     *
     * @return username
     */
    private String getUser() {

        return UserCoreUtil.addTenantDomainToEntry(CarbonContext.getThreadLocalCarbonContext().getUsername(),
                CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
    }
}
