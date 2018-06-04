/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;

import java.util.List;
import java.util.Map;

/**
 * Filtering out and selecting the SP claims.
 */
public interface ClaimFilter {

    /**
     * Priority of the Claim Filter. Claims filters will be sorted based on their priority value and by default only
     * the claim filter with the highest priority will be executed.
     *
     * @return priority of the filter.
     */
    int getPriority();

    /**
     * Filtering out and get the selected claim mappings.
     *
     * @param context Authentication context
     * @param appConfig Application Configuration
     * @return Modified Application Configuration
     */
    List<ClaimMapping> getFilteredClaims(AuthenticationContext context, ApplicationConfig appConfig);

    /**
     * Filtering and selecting the requested claim mappings.
     *
     * @param spClaimMappings SP configured claim mapping
     * @param requestedClaimsInRequest Claims in request
     * @return
     */
    List<ClaimMapping> filterRequestedClaims(List<ClaimMapping> spClaimMappings,
                                             List<ClaimMapping> requestedClaimsInRequest);
}
