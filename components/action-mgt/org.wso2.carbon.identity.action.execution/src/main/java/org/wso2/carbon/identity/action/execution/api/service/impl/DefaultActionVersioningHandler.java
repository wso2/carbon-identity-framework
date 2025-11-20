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

package org.wso2.carbon.identity.action.execution.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequestContext;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.service.ActionVersioningHandler;
import org.wso2.carbon.identity.action.execution.internal.util.ActionExecutorConfig;
import org.wso2.carbon.identity.action.management.api.model.Action;

/**
 * Default implementation for the ActionVersioningHandler interface.
 */
public class DefaultActionVersioningHandler implements ActionVersioningHandler {

    static DefaultActionVersioningHandler instance = new DefaultActionVersioningHandler();

    private static final String VERSION_PREFIX = "v";

    public static DefaultActionVersioningHandler getInstance() {
        return instance;
    }

    @Override
    public ActionType getSupportedActionType() {

        throw new UnsupportedOperationException("This method is not allowed for DefaultActionVersioningHandler.");
    }

    @Override
    public boolean canExecute(ActionExecutionRequestContext actionExecutionRequestContext, FlowContext flowContext)
            throws ActionExecutionException {

        return true;
    }

    @Override
    public boolean isRetiredActionVersion(ActionType actionType, Action action) {

        String retiredUpToVersion = ActionExecutorConfig.getInstance().getRetiredUpToVersion(actionType);
        if (StringUtils.isBlank(retiredUpToVersion)) {
            return false;
        }
        int retiredActionVersion = Integer.parseInt(retiredUpToVersion.replace(VERSION_PREFIX, StringUtils.EMPTY));
        int actionVersion = Integer.parseInt(action.getActionVersion().replace(VERSION_PREFIX, StringUtils.EMPTY));
        return (actionVersion <= retiredActionVersion);
    }
}
