/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsParameters;

import java.util.Map;

public class JsGraalParameters extends JsParameters implements ProxyObject {

    public JsGraalParameters(Map wrapped) {

        super(wrapped);
    }

    @Override
    public Object getMember(String name) {

        Object member = getWrapped().get(name);
        if (member instanceof Map) {
            return new JsGraalParameters((Map) member);
        }
        return member;
    }

    @Override
    public Object getMemberKeys() {

        return ProxyArray.fromArray(getWrapped().keySet().toArray());
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
