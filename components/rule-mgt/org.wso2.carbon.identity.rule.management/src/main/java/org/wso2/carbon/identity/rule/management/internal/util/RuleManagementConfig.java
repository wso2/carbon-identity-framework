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

package org.wso2.carbon.identity.rule.management.internal.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.rule.management.api.model.FlowType;

/**
 * Rule Management Config util class.
 * Holds system configurations related to rule management that can be overridden per flow type in identity.xml.
 */
public class RuleManagementConfig {

    private static final RuleManagementConfig INSTANCE = new RuleManagementConfig();

    private static final Log LOG = LogFactory.getLog(RuleManagementConfig.class);

    private static final int DEFAULT_MAX_EXPRESSIONS_COMBINED_WITH_AND = 5;

    private RuleManagementConfig() {

    }

    /**
     * Returns the singleton instance of RuleManagementConfig.
     *
     * @return RuleManagementConfig instance.
     */
    public static RuleManagementConfig getInstance() {

        return INSTANCE;
    }

    /**
     * Returns the maximum number of expressions that can be combined with AND for the given flow type.
     * The value can be overridden per flow type in identity.xml. If the property is not configured or invalid,
     * the default value is returned.
     *
     * @param flowType Flow type.
     * @return The maximum number of expressions that can be combined with AND for the given flow type.
     */
    public int getMaxAndExpressionCount(FlowType flowType) {

        String propertyKey;
        switch (flowType) {
            case PRE_ISSUE_ACCESS_TOKEN:
                propertyKey = "Rules.PreIssueAccessToken.MaxExpressionsCombinedWithAnd";
                break;
            case PRE_UPDATE_PASSWORD:
                propertyKey = "Rules.PreUpdatePassword.MaxExpressionsCombinedWithAnd";
                break;
            case PRE_UPDATE_PROFILE:
                propertyKey = "Rules.PreUpdateProfile.MaxExpressionsCombinedWithAnd";
                break;
            case PRE_ISSUE_ID_TOKEN:
                propertyKey = "Rules.PreIssueIdToken.MaxExpressionsCombinedWithAnd";
                break;
            case APPROVAL_WORKFLOW:
                propertyKey = "Rules.ApprovalWorkflow.MaxExpressionsCombinedWithAnd";
                break;
            case DEVICE_POLICY:
                propertyKey = "Rules.DevicePolicy.MaxExpressionsCombinedWithAnd";
                break;
            default:
                return DEFAULT_MAX_EXPRESSIONS_COMBINED_WITH_AND;
        }

        try {
            String propertyValue = (String) IdentityConfigParser.getInstance().getConfiguration().get(propertyKey);
            if (StringUtils.isNotBlank(propertyValue)) {
                int parsedValue = Integer.parseInt(propertyValue);
                if (parsedValue > 0) {
                    return parsedValue;
                }
            }
        } catch (NullPointerException | NumberFormatException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to resolve " + propertyKey + " from identity.xml. Using the default value: " +
                        DEFAULT_MAX_EXPRESSIONS_COMBINED_WITH_AND, e);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Positive value not configured for " + propertyKey +
                    ". Using the default value: " + DEFAULT_MAX_EXPRESSIONS_COMBINED_WITH_AND);
        }
        return DEFAULT_MAX_EXPRESSIONS_COMBINED_WITH_AND;
    }
}
