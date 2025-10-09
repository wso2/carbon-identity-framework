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

import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionRequestBuilder;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.constant.PreUpdatePasswordActionConstants;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.versioning.v1.PreUpdatePasswordRequestBuilderV1;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.versioning.v2.PreUpdatePasswordRequestBuilderV2;

/**
 * Factory class for getting the PreUpdatePasswordRequestBuilder by Action version.
 */
public class PreUpdatePasswordRequestBuilderFactory {

    private static final PreUpdatePasswordRequestBuilderFactory instance = new PreUpdatePasswordRequestBuilderFactory();

    public static PreUpdatePasswordRequestBuilderFactory getInstance() {

        return instance;
    }

    /**
     * Returns an {@link ActionExecutionRequestBuilder} instance for the specified action version.
     *
     * @param actionVersion The version of the action for which the request builder is required.
     * @return An instance of {@link ActionExecutionRequestBuilder} corresponding to the given version.
     * @throws ActionExecutionRequestBuilderException If the action version is unsupported.
     */
    public ActionExecutionRequestBuilder getActionExecutionRequestBuilder(String actionVersion)
            throws ActionExecutionRequestBuilderException {

        switch (actionVersion) {
            case PreUpdatePasswordActionConstants.ACTION_VERSION_V1:
                return new PreUpdatePasswordRequestBuilderV1();
            case PreUpdatePasswordActionConstants.ACTION_VERSION_V2:
                return new PreUpdatePasswordRequestBuilderV2();
            default:
                throw new ActionExecutionRequestBuilderException(
                        "Unsupported pre-update-password action version: " + actionVersion);
        }
    }
}
