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

package org.wso2.carbon.identity.debug.framework.core.provider;

import org.wso2.carbon.identity.debug.framework.core.DebugContextProvider;
import org.wso2.carbon.identity.debug.framework.core.DebugExecutor;
import org.wso2.carbon.identity.debug.framework.core.DebugProcessor;
import org.wso2.carbon.identity.debug.framework.core.DebugProtocolProvider;

/**
 * Protocol provider for OIDC/OAuth2 debugging.
 */
public class OIDCDebugProtocolProvider implements DebugProtocolProvider {

    @Override
    public String getProtocolType() {
        return "OAUTH2_OIDC";
    }

    @Override
    public DebugContextProvider getContextProvider() {
        return null;
    }

    @Override
    public DebugExecutor getExecutor() {
        return null;
    }

    @Override
    public DebugProcessor getProcessor() {
        return null;
    }

    @Override
    public boolean supports(String protocolType) {
        return "OAUTH2_OIDC".equalsIgnoreCase(protocolType) ||
               "OIDC".equalsIgnoreCase(protocolType) ||
               "OAuth2".equalsIgnoreCase(protocolType) ||
               "OpenIDConnect".equalsIgnoreCase(protocolType);
    }
}
