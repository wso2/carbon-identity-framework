/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.AbstractJSObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsParameters;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base.JsBaseParameters;

import java.util.Map;

/**
 * Javascript wrapper for Java level HashMap of HTTP headers/cookies for GraalJs Execution.
 * This provides controlled access to HTTPServletRequest object's headers and cookies via provided javascript native
 * syntax.
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime.
 */
public class GraalJsParameters extends JsBaseParameters implements ProxyObject {

    public GraalJsParameters(Map wrapped) {

        super(wrapped);
    }

    @Override
    public Object getMember(String name) {

        Object member = getWrapped().get(name);
        if (member instanceof Map) {
            return new GraalJsParameters((Map) member);
        }
        return member;
    }

    @Override
    public Object getMemberKeys() {

        return null;
    }

    public void putMember(String key, Value value) {
        LOG.warn("Unsupported operation. Parameters are read only. Can't set parameter " + key + " to value: " + value);
    }

    @Override
    public boolean removeMember(String name) {

        LOG.warn("Unsupported operation. Parameters are read only. Can't remove parameter " + name);
        return false;
    }
}
