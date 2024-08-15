/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
package org.wso2.carbon.identity.entitlement.persistence;

import org.apache.commons.logging.Log;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.PAPStatusDataHandler;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.StatusHolder;
import org.wso2.carbon.identity.entitlement.persistence.dao.StatusDAO;

import java.util.List;
import java.util.Properties;

/**
 * This class handles the status data of the policies in the JDBC data store.
 */
public class JDBCSimplePAPStatusDataHandler implements PAPStatusDataHandler {

    private static final Log AUDIT_LOG = CarbonConstants.AUDIT_LOG;
    private static final String AUDIT_MESSAGE
            = "Initiator : %s | Action : %s | Target : %s | Data : { %s } | Result : %s ";
    private int maxRecords;
    private static final StatusDAO statusDAO = new StatusDAO();

    /**
     * init entitlement status data handler module.
     *
     * @param properties properties.
     */
    @Override
    public void init(Properties properties) {

        maxRecords = EntitlementUtil.getMaxNoOfStatusRecords();
    }

    /**
     * Handles the status data.
     *
     * @param about         whether the status is about a policy or publisher.
     * @param key           key value of the status.
     * @param statusHolders <code>StatusHolder</code>.
     * @throws EntitlementException throws, if fails to handle.
     */
    @Override
    public void handle(String about, String key, List<StatusHolder> statusHolders) throws EntitlementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        // If the action is DELETE_POLICY, delete the policy or the subscriber status
        for (StatusHolder holder : statusHolders) {
            if (EntitlementConstants.StatusTypes.DELETE_POLICY.equals(holder.getType())) {
                statusDAO.deleteStatusTrail(about, key, tenantId);
                return;
            }
        }
        amendStatusTrail(about, key, statusHolders, tenantId);
    }

    /**
     * Returns status data.
     *
     * @param about        indicates what is related with this admin status action.
     * @param key          key value of the status.
     * @param type         admin action type.
     * @param searchString search string for <code>StatusHolder</code>.
     * @return An array of <code>StatusHolder</code>.
     * @throws EntitlementException if fails.
     */
    @Override
    public StatusHolder[] getStatusData(String about, String key, String type, String searchString)
            throws EntitlementException {

        String statusAboutType = EntitlementConstants.Status.ABOUT_POLICY.equals(about)
                ? EntitlementConstants.Status.ABOUT_POLICY
                : EntitlementConstants.Status.ABOUT_SUBSCRIBER;

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<StatusHolder> holders = statusDAO.getStatus(key, statusAboutType, tenantId);
        return EntitlementUtil.filterStatus(holders, searchString, about, type);
    }

    private void amendStatusTrail(String about, String key, List<StatusHolder> statusHolders, int tenantId)
            throws EntitlementException {

        boolean useLastStatusOnly = Boolean.parseBoolean(
                IdentityUtil.getProperty(EntitlementConstants.PROP_USE_LAST_STATUS_ONLY));

        if (statusHolders != null && !statusHolders.isEmpty()) {

            if (useLastStatusOnly) {
                // Delete all the previous statuses
                statusDAO.deleteStatusTrail(about, key, tenantId);
                auditAction(statusHolders.toArray(new StatusHolder[0]));
            }

            // Add new status to the database
            statusDAO.insertStatus(about, key, statusHolders, tenantId);

            if (!useLastStatusOnly) {
                statusDAO.deleteExcessStatusData(about, key, tenantId, maxRecords);
            }
        }
    }

    private void auditAction(StatusHolder[] statusHolders) {

        if (statusHolders != null) {
            for (StatusHolder statusHolder : statusHolders) {
                if (statusHolder != null) {
                    String initiator = statusHolder.getUser();
                    if (LoggerUtils.isLogMaskingEnable) {
                        initiator = LoggerUtils.getMaskedContent(initiator);
                    }
                    String action = statusHolder.getType();
                    String key = statusHolder.getKey();
                    String target = statusHolder.getTarget();
                    String targetAction = statusHolder.getTargetAction();
                    String result = "FAILURE";
                    if (statusHolder.isSuccess()) {
                        result = "SUCCESS";
                    }
                    String auditData = String.format("\"Key\" : \"%s\" , \"Target Action\" : \"%s\"",
                            key, targetAction);

                    AUDIT_LOG.info(String.format(AUDIT_MESSAGE, initiator, action, target, auditData, result));
                }
            }
        }
    }
}
