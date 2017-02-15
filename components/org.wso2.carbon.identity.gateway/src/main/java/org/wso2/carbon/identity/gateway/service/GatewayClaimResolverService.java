/*
 *
 *  * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.carbon.identity.gateway.service;

import org.wso2.carbon.identity.claim.exception.ClaimResolvingServiceException;
import org.wso2.carbon.identity.claim.exception.ProfileMgtServiceException;
import org.wso2.carbon.identity.claim.mapping.profile.ClaimConfigEntry;
import org.wso2.carbon.identity.claim.mapping.profile.ProfileEntry;
import org.wso2.carbon.identity.claim.service.ClaimResolvingService;
import org.wso2.carbon.identity.claim.service.ProfileMgtService;
import org.wso2.carbon.identity.gateway.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandlerException;
import org.wso2.carbon.identity.mgt.claim.Claim;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GatewayClaimResolverService {
    private static GatewayClaimResolverService gatewayClaimResolverService = new GatewayClaimResolverService();

    private GatewayClaimResolverService(){

    }
    public static GatewayClaimResolverService getInstance(){
        return GatewayClaimResolverService.gatewayClaimResolverService;
    }


    public Claim transformToRootDialect(Claim otherDialectClaim, Optional<String> profile){
        return null;
    }


    public Claim transformToOtherDialect(Claim rootDialectClaim, Optional<String> profile){
        return null;
    }

    public Set<Claim> transformToRootDialect(Set<Claim> otherDialectClaims, Optional<String> profile){

        return null ;
    }


    public Set<Claim> transformToOtherDialect(Set<Claim> rootDialectClaims, Optional<String> profile){
        return null ;
    }
}
