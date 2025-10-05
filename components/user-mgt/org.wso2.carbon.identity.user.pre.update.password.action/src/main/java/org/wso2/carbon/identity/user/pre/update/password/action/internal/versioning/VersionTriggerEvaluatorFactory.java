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

package org.wso2.carbon.identity.user.pre.update.password.action.internal.versioning;

import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.constant.PreUpdatePasswordActionConstants;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.versioning.v2.VersionTriggerEvaluatorV2;

/**
 * Factory class for getting the version trigger evaluator by Action version.
 */
public class VersionTriggerEvaluatorFactory {

    private static final VersionTriggerEvaluatorFactory instance = new VersionTriggerEvaluatorFactory();

    public static VersionTriggerEvaluatorFactory getInstance() {

        return instance;
    }

    public VersionTriggerEvaluator getVersionTriggerEvaluator(Action action) {

        switch (action.getVersion()) {
            case PreUpdatePasswordActionConstants.ACTION_VERSION_V2:
                return VersionTriggerEvaluatorV2.getInstance();
            default:
                return VersionTriggerEvaluator.getInstance();
        }
    }
}
