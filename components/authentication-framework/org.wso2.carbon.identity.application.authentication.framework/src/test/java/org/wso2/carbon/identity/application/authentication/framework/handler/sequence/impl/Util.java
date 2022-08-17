/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl;

import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.ClaimHandler;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

public class Util {

    public static Object[][] getSpRoleMappingData() {

        Map<String, String> spRoleMappings = new HashMap<>();
        spRoleMappings.put("LOCAL_ROLE1", "SP_ROLE1");
        spRoleMappings.put("LOCAL_ROLE2", "SP_ROLE2");

        List<String> localUserRoles = Arrays.asList(new String[]{"LOCAL_ROLE1", "ADMIN", "LOCAL_ROLE2"});
        String localRoles = "LOCAL_ROLE1,ADMIN,LOCAL_ROLE2";

        List<String> localUserRolesWithInternal = Arrays.asList(new String[] {
                "LOCAL_ROLE1", "ADMIN", "LOCAL_ROLE2", "Internal/everyone"
        });
        String localRolesWithInternal = "LOCAL_ROLE1,ADMIN,LOCAL_ROLE2,Internal/everyone";

        return new Object[][]{
                {spRoleMappings, localUserRoles, "##", "SP_ROLE1##SP_ROLE2##ADMIN"},
                {null, localUserRoles, ",", localRoles},
                {null, localUserRolesWithInternal, ",", localRolesWithInternal},
                {new HashMap<>(), localUserRoles, ",", localRoles},
                {spRoleMappings, new ArrayList<>(), ",", null},
                {spRoleMappings, null, ",", null},
                {null, null, ",", null}
        };
    }

    public static void mockMultiAttributeSeparator(String multiAttributeSeparator) {

        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getMultiAttributeSeparator()).thenReturn(multiAttributeSeparator);
    }

    public static SequenceConfig mockSequenceConfig(Map<String, String> spRoleMappings) {

        SequenceConfig sequenceConfig = spy(new SequenceConfig());
        ApplicationConfig applicationConfig = mock(ApplicationConfig.class);
        ServiceProvider serviceProvider = mock(ServiceProvider.class);
        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = mock
                (LocalAndOutboundAuthenticationConfig.class);
        when(applicationConfig.getApplicationName()).thenReturn("APP");
        when(sequenceConfig.getApplicationConfig()).thenReturn(applicationConfig);
        when(applicationConfig.getRoleMappings()).thenReturn(spRoleMappings);
        when(applicationConfig.getServiceProvider()).thenReturn(serviceProvider);
        when(serviceProvider.getLocalAndOutBoundAuthenticationConfig())
                .thenReturn(localAndOutboundAuthenticationConfig);
        when(localAndOutboundAuthenticationConfig.isUseUserstoreDomainInRoles()).thenReturn(false);
        return sequenceConfig;
    }

    public static ClaimHandler mockClaimHandler() throws FrameworkException {

        ClaimHandler claimHandler = mock(ClaimHandler.class);
        Map<String, String> claims = new HashMap<>();
        claims.put("claim1", "value1");

        when(claimHandler.handleClaimMappings(any(StepConfig.class), any(AuthenticationContext.class),
                any(Map.class), anyBoolean())).thenReturn(claims);
        return claimHandler;
    }

    public static void mockIdentityUtil() {

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getLocalGroupsClaimURI()).thenReturn(UserCoreConstants.ROLE_CLAIM);
    }
}
