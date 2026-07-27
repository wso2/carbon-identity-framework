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
     * the given default value is returned.
     *
     * @param flowType     Flow type.
     * @param defaultValue Default value to be used if the configuration is missing or invalid.
     * @return The maximum number of expressions that can be combined with AND for the given flow type.
     */
    public int getMaxExpressionsCombinedWithAnd(FlowType flowType, int defaultValue) {

        int maxExpressionsCombinedWithAnd = defaultValue;
        String propertyKey = FlowTypeConfig.valueOf(flowType.name()).getMaxExpressionsCombinedWithAndProperty();
        String propertyValue = (String) IdentityConfigParser.getInstance().getConfiguration().get(propertyKey);
        if (StringUtils.isNotBlank(propertyValue)) {
            try {
                int parsedValue = Integer.parseInt(propertyValue);
                if (parsedValue > 0) {
                    maxExpressionsCombinedWithAnd = parsedValue;
                } else if (LOG.isDebugEnabled()) {
                    LOG.debug("Non-positive value " + parsedValue + " configured for " + propertyKey +
                            " in identity.xml. Expects a positive number. Using the default value: " + defaultValue);
                }
            } catch (NumberFormatException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Failed to read " + propertyKey + " property in identity.xml." +
                            " Expects a number. Using the default value: " + defaultValue, e);
                }
            }
        }
        return maxExpressionsCombinedWithAnd;
    }

    /**
     * Enum to hold the configuration properties for each flow type.
     */
    private enum FlowTypeConfig {

        PRE_ISSUE_ACCESS_TOKEN("Rules.PreIssueAccessToken.MaxExpressionsCombinedWithAnd"),
        PRE_UPDATE_PASSWORD("Rules.PreUpdatePassword.MaxExpressionsCombinedWithAnd"),
        PRE_UPDATE_PROFILE("Rules.PreUpdateProfile.MaxExpressionsCombinedWithAnd"),
        PRE_ISSUE_ID_TOKEN("Rules.PreIssueIdToken.MaxExpressionsCombinedWithAnd"),
        APPROVAL_WORKFLOW("Rules.ApprovalWorkflow.MaxExpressionsCombinedWithAnd"),
        DEVICE_POLICY("Rules.DevicePolicy.MaxExpressionsCombinedWithAnd");

        private final String maxExpressionsCombinedWithAndProperty;

        FlowTypeConfig(String maxExpressionsCombinedWithAndProperty) {

            this.maxExpressionsCombinedWithAndProperty = maxExpressionsCombinedWithAndProperty;
        }

        /**
         * Get the identity.xml property key that holds the maximum number of expressions
         * combined with AND for this flow type.
         *
         * @return The configuration property key.
         */
        public String getMaxExpressionsCombinedWithAndProperty() {

            return maxExpressionsCombinedWithAndProperty;
        }
    }
}
