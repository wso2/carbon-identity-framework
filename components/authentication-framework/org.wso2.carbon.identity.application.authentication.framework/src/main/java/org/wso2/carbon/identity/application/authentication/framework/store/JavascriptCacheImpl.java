/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.store;

import java.util.HashMap;
import java.util.Map;
import javax.script.Bindings;
import javax.script.CompiledScript;

/**
 * Implementation for Javascript cache.
 * Hides the complexities of expiring, loading and purging on cached data.
 * 
 */
public class JavascriptCacheImpl implements JavascriptCache {
    private Map<String, CompiledScript> applicationVsScriptMap = new HashMap<>();
    private Map<String, Bindings> applicationVsBindings = new HashMap<>();

    public void putScript(String appName, CompiledScript script) {
        applicationVsScriptMap.put(appName, script);
    }

    public CompiledScript getScript(String appName) {
        return applicationVsScriptMap.get(appName);
    }

    public void removeScript(String appName) {
        applicationVsScriptMap.remove(appName);
    }

    public void putBindings(String appName, Bindings bindings) {
        applicationVsBindings.put(appName, bindings);
    }

    public Bindings getBindings(String appName) {
        return applicationVsBindings.get(appName);
    }

    public void removeBindings(String appName) {
        applicationVsBindings.remove(appName);
    }
}
