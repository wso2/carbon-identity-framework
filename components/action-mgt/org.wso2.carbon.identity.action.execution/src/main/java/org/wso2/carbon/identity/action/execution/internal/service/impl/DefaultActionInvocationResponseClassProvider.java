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

package org.wso2.carbon.identity.action.execution.internal.service.impl;

import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.service.ActionInvocationResponseClassProvider;

/**
 * Default implementation of the ActionInvocationResponseClassProvider. The downStream components need to extend
 * this class, when implementing the ActionInvocationResponseClassProvider.
 */
public class DefaultActionInvocationResponseClassProvider implements ActionInvocationResponseClassProvider {

    static DefaultActionInvocationResponseClassProvider instance = new DefaultActionInvocationResponseClassProvider();

    public static DefaultActionInvocationResponseClassProvider getInstance() {
        return instance;
    }

    @Override
    public ActionType getSupportedActionType() {

        throw new UnsupportedOperationException(
                "This method is not allowed for DefaultActionInvocationResponseClassProvider.");
    }
}
