/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.List;
import java.util.Map;

/**
 * Parameters that can be modified from the authentication script.
 * This wrapper uses GraalJS polyglot context.
 */
public class JsGraalWritableParameters extends JsGraalParameters implements ProxyObject {

    public JsGraalWritableParameters(Map wrapped) {

        super(wrapped);
    }

    public boolean removeMember(String name) {

        super.removeMemberObject(name);
        return true;
    }

    public void putMember(String key, Value value) {

        setMember(key, value.as(List.class));
    }

    public boolean hasMember(String name) {

        return true;
    }

    public void setMember(String name, Object value) {

        getWrapped().put(name, value);
    }
}
