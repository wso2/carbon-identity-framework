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

package org.wso2.carbon.identity.action.management.internal.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;

import java.util.Collections;
import java.util.List;

/**
 * Action Management Config util class.
 */
public class ActionManagementConfig {

    private static final Log LOG = LogFactory.getLog(ActionManagementConfig.class);

    /**
     * Retrieves the property values for a given property key from the identity configuration of the server.
     *
     * @param propertyKey The key of the property to retrieve.
     * @return A list of property values associated with the given key.
     */
    public List<String> getPropertyValues(String propertyKey) {

        Object propertyValue = IdentityConfigParser.getInstance().getConfiguration().get(propertyKey);

        if (propertyValue == null) {
            return Collections.emptyList();
        }

        if (propertyValue instanceof String) {
            return Collections.singletonList(propertyValue.toString());
        }

        if (propertyValue instanceof List) {
            return (List<String>) propertyValue;
        } else {
            LOG.warn("Invalid system configuration for: " + propertyKey +
                    " at /repository/conf/identity.xml. Expected a string list of values.");
            return Collections.emptyList();
        }
    }

    /**
     * Enum to hold the configuration properties for each action type.
     */
    public enum ActionTypeConfig {

        PRE_ISSUE_ACCESS_TOKEN(
                "Actions.Types.PreIssueAccessToken.ActionRequest.ExcludedParameters.Parameter",
                "Actions.Types.PreIssueAccessToken.ActionRequest.AllowedHeaders.Header"
        );

        private final String excludedHeadersProperty;
        private final String excludedParamsProperty;

        ActionTypeConfig(String excludedHeadersProperty, String excludedParamsProperty) {

            this.excludedHeadersProperty = excludedHeadersProperty;
            this.excludedParamsProperty = excludedParamsProperty;
        }

        public String getExcludedHeadersProperty() {

            return excludedHeadersProperty;
        }

        public String getExcludedParamsProperty() {

            return excludedParamsProperty;
        }
    }
}
