/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.workflow.mgt.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.workflow.mgt.bean.Property;
import org.wso2.carbon.identity.workflow.mgt.bean.RequestParameter;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.internal.WorkflowServiceDataHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.DISPLAY_NAME_PROPERTY;
import static org.wso2.carbon.identity.workflow.mgt.util.WFConstant.CLAIMS_PROPERTY_NAME;
import static org.wso2.carbon.identity.workflow.mgt.util.WFConstant.TENANT_DOMAIN_PARAM_NAME;

/**
 * Utility class for workflow management.
 */
public class Utils {

    private static final Log log = LogFactory.getLog(Utils.class);

    /**
     * Create PreparedStatement.
     *
     * @param connection           db connection
     * @param sqlQuery             SQL query
     * @param tenantId             Tenant ID
     * @param filterResolvedForSQL resolved filter for sql
     * @param offset               offset
     * @param limit                limit
     * @return PreparedStatement
     * @throws SQLException
     * @throws DataAccessException
     */
    public static PreparedStatement generatePrepStmt(Connection connection, String sqlQuery, int tenantId,
                                                     String filterResolvedForSQL, int offset, int limit)
            throws SQLException, DataAccessException {

        PreparedStatement prepStmt;
        if (JdbcUtils.isPostgreSQLDB()) {
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, filterResolvedForSQL);
            prepStmt.setInt(3, limit);
            prepStmt.setInt(4, offset);
        } else {
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, filterResolvedForSQL);
            prepStmt.setInt(3, offset);
            prepStmt.setInt(4, limit);
        }
        return prepStmt;
    }

    /**
     * Extracts the workflow request parameters and converts them to a list of properties.
     *
     * @param workflowRequest The workflow request containing the parameters.
     * @return A list of properties extracted from the workflow request parameters.
     */
    public static List<Property> getWorkflowRequestParameters(WorkflowRequest workflowRequest) {

        log.debug("Processing workflow request parameters to extract properties.");
        List<Property> workflowRequestProperties = new ArrayList<>();
        for (RequestParameter param : workflowRequest.getRequestParameters()) {
            if (StringUtils.equals(param.getName(), WFConstant.CREDENTIAL)) {
                continue;
            }
            Object value = param.getValue();
            if (Objects.nonNull(value)) {
                String valueString = value.toString().trim();
                String paramString = param.getName().trim();
                if (CLAIMS_PROPERTY_NAME.equals(paramString)) {
                    if (WorkflowDataType.STRING_STRING_MAP_TYPE.equals(param.getValueType()) &&
                            param.getValue() instanceof Map) {
                        try {
                            Map<String, String> claimsMap = (Map<String, String>) param.getValue();
                            List<LocalClaim> localClaims;
                            try {
                                localClaims =
                                        WorkflowServiceDataHolder.getInstance().getClaimMetadataManagementService()
                                                .getLocalClaims(
                                                        CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
                            } catch (ClaimMetadataException e) {
                                log.error("Error while retrieving local claims for tenant: " +
                                            CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), e);
                                continue;
                            }

                            for (Map.Entry<String, String> entry : claimsMap.entrySet()) {
                                String claimUri = entry.getKey();
                                String claimValue = entry.getValue();
                                if (StringUtils.isEmpty(claimUri) || StringUtils.isEmpty(claimValue)) {
                                    continue;
                                }
                                String displayName = localClaims.stream()
                                        .filter(localClaim -> localClaim.getClaimURI().equals(claimUri))
                                        .map(localClaim ->
                                                localClaim.getClaimProperty(DISPLAY_NAME_PROPERTY))
                                        .findFirst()
                                        .orElse(claimUri);
                                Property property = new Property();
                                property.setKey(displayName);
                                property.setValue(claimValue);
                                workflowRequestProperties.add(property);
                            }
                        } catch (ClassCastException e) {
                            if (log.isDebugEnabled()) {
                                log.debug("Invalid claims map format for parameter.", e);
                            }
                        }
                    }
                } else if (!TENANT_DOMAIN_PARAM_NAME.equals(paramString)) {
                    Property property = new Property();
                    property.setKey(paramString);
                    property.setValue(valueString);
                    workflowRequestProperties.add(property);
                }
            }
        }
        return workflowRequestProperties;
    }
}
