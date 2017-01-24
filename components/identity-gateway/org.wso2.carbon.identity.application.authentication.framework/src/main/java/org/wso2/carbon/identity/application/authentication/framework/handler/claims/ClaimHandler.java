/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.handler.claims;

import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;

import java.util.Map;

public interface ClaimHandler {

    /**
     * @param stepConfig
     * @param context
     * @param extAttrs
     * @param isFederatedClaims
     * @return
     * @throws FrameworkException
     */
    public Map<String, String> handleClaimMappings(StepConfig stepConfig,
                                                   AuthenticationContext context, Map<String, String> extAttrs,
                                                   boolean isFederatedClaims) throws FrameworkException;

}
