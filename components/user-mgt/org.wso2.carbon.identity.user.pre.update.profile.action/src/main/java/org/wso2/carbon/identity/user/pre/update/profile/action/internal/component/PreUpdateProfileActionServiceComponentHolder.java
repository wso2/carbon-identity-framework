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

package org.wso2.carbon.identity.user.pre.update.profile.action.internal.component;

import org.wso2.carbon.identity.action.execution.api.service.ActionExecutorService;

/**
 * OSGI Service component Holder for the Pre Update Profile Action Service.
 */
public class PreUpdateProfileActionServiceComponentHolder {

    private ActionExecutorService actionExecutorService;

    public static final PreUpdateProfileActionServiceComponentHolder INSTANCE =
            new PreUpdateProfileActionServiceComponentHolder();

    private PreUpdateProfileActionServiceComponentHolder() {

    }

    /**
     * Get the instance of PreUpdateProfileActionServiceComponentHolder.
     *
     * @return ActionMgtServiceComponentHolder instance.
     */
    public static PreUpdateProfileActionServiceComponentHolder getInstance() {

        return INSTANCE;
    }

    /**
     * Get the ActionExecutorService.
     *
     * @return ActionExecutorService instance.
     */
    public ActionExecutorService getActionExecutorService() {

        return actionExecutorService;
    }

    /**
     * Set the ActionExecutorService.
     *
     * @param actionExecutorService ActionExecutorService instance.
     */
    public void setActionExecutorService(ActionExecutorService actionExecutorService) {

        this.actionExecutorService = actionExecutorService;
    }
}
