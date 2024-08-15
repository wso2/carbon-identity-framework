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

package org.wso2.carbon.identity.action.execution.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.action.execution.model.ActionType;
import org.wso2.carbon.identity.core.util.IdentityUtil;

/**
 * This class holds the system configurations for the Action Executor Service.
 */
public class ActionExecutorConfig {

    private static final ActionExecutorConfig INSTANCE = new ActionExecutorConfig();

    private static final String PRE_ISSUE_ACCESS_TOKEN_ACTION_TYPE_ENABLE_PROPERTY =
            "Actions.Types.PreIssueAccessToken.Enable";

    private ActionExecutorConfig() {

    }

    public static ActionExecutorConfig getInstance() {

        return INSTANCE;
    }

    public boolean isExecutionForActionTypeEnabled(ActionType actionType) {

        switch (actionType) {
            case PRE_ISSUE_ACCESS_TOKEN:
                return isActionTypeEnabled(PRE_ISSUE_ACCESS_TOKEN_ACTION_TYPE_ENABLE_PROPERTY);
            default:
                return false;
        }
    }

    private boolean isActionTypeEnabled(String actionTypePropertyName) {

        boolean isActionTypeEnabled = false;
        String actionTypeEnabledPropertyValue = IdentityUtil.getProperty(actionTypePropertyName);
        if (StringUtils.isNotBlank(actionTypeEnabledPropertyValue)) {
            return Boolean.parseBoolean(actionTypeEnabledPropertyValue);
        }
        return isActionTypeEnabled;
    }

}
