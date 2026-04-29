/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.script.engine.mode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.JsGraalGraphEngineModeRouter;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.ScriptEngineModeResolver;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

/**
 * Default implementation of {@link ScriptEngineModeResolver}.
 * <p>
 * Always resolves to the LOCAL engine.
 * This provides a safe default when no custom per-request routing logic is configured.
 * <p>
 * This implementation can be replaced by dropping a custom OSGi bundle into the
 * server's dropins folder with a higher service ranking.
 */
public class DefaultScriptEngineModeResolver implements ScriptEngineModeResolver {

    private static final Log log = LogFactory.getLog(DefaultScriptEngineModeResolver.class);

    @Override
    public JsGraalGraphEngineModeRouter.ExecutionMode resolve(AuthenticationContext authenticationContext) {

        if (authenticationContext == null) {
            if (log.isDebugEnabled()) {
                log.debug("[DefaultScriptEngineModeResolver] AuthenticationContext is null, falling back to LOCAL");
            }
            return JsGraalGraphEngineModeRouter.ExecutionMode.LOCAL;
        }

        String spName = authenticationContext.getServiceProviderName();
        String tenantDomain = authenticationContext.getTenantDomain();

        if (log.isDebugEnabled()) {
            log.debug("[DefaultScriptEngineModeResolver] Resolving engine mode for SP: " + spName +
                    ", tenant: " + tenantDomain);
        }

        return JsGraalGraphEngineModeRouter.ExecutionMode.LOCAL;
    }
}
