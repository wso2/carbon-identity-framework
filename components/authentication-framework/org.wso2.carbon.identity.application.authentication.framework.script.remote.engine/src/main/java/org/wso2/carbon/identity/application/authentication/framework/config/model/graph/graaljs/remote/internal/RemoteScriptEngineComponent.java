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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.RemoteJsGraphBuilderProvider;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.DefaultRemoteJsGraphBuilderProvider;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.ScriptEngineModeResolver;

/**
 * OSGi Declarative Service component for registering the
 * {@link RemoteJsGraphBuilderProvider} service.
 * <p>
 * Registers {@link DefaultRemoteJsGraphBuilderProvider} as the implementation
 * the authentication framework consumes when an adaptive script must execute
 * against the externalized GraalJS runtime. The framework's
 * {@code JsGraalGraphBuilderFactory} looks the service up via
 * {@code FrameworkServiceDataHolder} on each request.
 * <p>
 * If this bundle is absent at runtime, the framework continues to serve
 * LOCAL-mode requests; REMOTE / HYBRID modes that resolve to remote will fail
 * loudly at the factory rather than silently degrade.
 */
@Component(
        name = "org.wso2.carbon.identity.application.authentication.framework.script.remote.engine",
        immediate = true
)
public class RemoteScriptEngineComponent {

    private static final Log log = LogFactory.getLog(RemoteScriptEngineComponent.class);

    private ServiceRegistration<RemoteJsGraphBuilderProvider> registration;

    @Activate
    protected void activate(ComponentContext context) {

        try {
            DefaultRemoteJsGraphBuilderProvider provider = new DefaultRemoteJsGraphBuilderProvider();
            registration = context.getBundleContext().registerService(
                    RemoteJsGraphBuilderProvider.class, provider, null);
            log.info("DefaultRemoteJsGraphBuilderProvider registered as OSGi service.");
        } catch (Throwable e) {
            log.error("Error activating RemoteScriptEngineComponent.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (registration != null) {
            try {
                registration.unregister();
            } catch (IllegalStateException ignored) {
                // Service already unregistered by framework shutdown — safe to ignore.
            }
            registration = null;
        }
        if (log.isDebugEnabled()) {
            log.debug("RemoteScriptEngineComponent deactivated.");
        }
    }

    @Reference(
            name = "script.engine.mode.resolver",
            service = ScriptEngineModeResolver.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetScriptEngineModeResolver"
    )
    protected void setScriptEngineModeResolver(ScriptEngineModeResolver resolver) {

        RemoteScriptEngineDataHolder.getInstance().setScriptEngineModeResolver(resolver);
        log.info("ScriptEngineModeResolver set: " + resolver.getClass().getName());
    }

    protected void unsetScriptEngineModeResolver(ScriptEngineModeResolver resolver) {

        RemoteScriptEngineDataHolder.getInstance().setScriptEngineModeResolver(null);
        log.info("ScriptEngineModeResolver unset.");
    }
}
