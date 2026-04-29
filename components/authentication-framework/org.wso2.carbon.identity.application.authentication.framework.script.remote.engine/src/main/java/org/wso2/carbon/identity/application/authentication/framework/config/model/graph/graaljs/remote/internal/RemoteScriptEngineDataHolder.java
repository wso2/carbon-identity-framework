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

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.ScriptEngineModeResolver;

/**
 * Data holder for the remote script engine bundle.
 * <p>
 * Holds OSGi service references that the bundle's internal classes consume —
 * currently the optional {@link ScriptEngineModeResolver} used by HYBRID-mode
 * routing inside {@code JsGraalGraphEngineModeRouter}. Population is driven by
 * {@code RemoteScriptEngineComponent}'s {@code @Reference} bindings.
 */
public class RemoteScriptEngineDataHolder {

    private static final RemoteScriptEngineDataHolder INSTANCE = new RemoteScriptEngineDataHolder();

    private ScriptEngineModeResolver scriptEngineModeResolver;

    private RemoteScriptEngineDataHolder() {
    }

    public static RemoteScriptEngineDataHolder getInstance() {

        return INSTANCE;
    }

    public ScriptEngineModeResolver getScriptEngineModeResolver() {

        return scriptEngineModeResolver;
    }

    public void setScriptEngineModeResolver(ScriptEngineModeResolver scriptEngineModeResolver) {

        this.scriptEngineModeResolver = scriptEngineModeResolver;
    }
}
