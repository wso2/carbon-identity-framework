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

package org.wso2.carbon.identity.application.authentication.framework.script.engine.mode.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.ScriptEngineModeResolver;
import org.wso2.carbon.identity.application.authentication.framework.script.engine.mode.DefaultScriptEngineModeResolver;

/**
 * OSGi Declarative Service component for registering the {@link ScriptEngineModeResolver} service.
 * <p>
 * Registers the {@link DefaultScriptEngineModeResolver} as the default implementation.
 * Custom implementations can override this by deploying an OSGi bundle with a higher
 * service ranking in the dropins folder.
 */
@Component(
        name = "org.wso2.carbon.identity.application.authentication.framework.script.engine.mode",
        immediate = true
)
public class ScriptEngineModeResolverComponent {

    private static final Log log = LogFactory.getLog(ScriptEngineModeResolverComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            DefaultScriptEngineModeResolver resolver = new DefaultScriptEngineModeResolver();
            context.getBundleContext().registerService(
                    ScriptEngineModeResolver.class.getName(), resolver, null);
            log.info("DefaultScriptEngineModeResolver registered as OSGi service.");
        } catch (Throwable e) {
            log.error("Error activating ScriptEngineModeResolver component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("ScriptEngineModeResolver component deactivated.");
        }
    }
}
