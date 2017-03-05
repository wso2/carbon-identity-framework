/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.authenticator;

import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.AuthenticationResponse;
import org.wso2.carbon.identity.mgt.claim.Claim;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

public interface ApplicationAuthenticator {


    public boolean canHandle(AuthenticationContext authenticationContext);


    public AuthenticationResponse process(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException;

    public String getContextIdentifier(AuthenticationContext authenticationContext);

    public String getName();


    public String getFriendlyName();


    public String getClaimDialectURI();

    public List<Properties> getConfigurationProperties();

    public Set<Claim> getMappedRootClaims(Set<Claim> claims, Optional<String> profile, Optional<String> dialect)
            throws AuthenticationHandlerException;
}
