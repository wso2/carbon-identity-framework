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

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * Javascript wrapper for Java level HashMap of HTTP headers.
 * This provides controlled access to HTTPServletResponse object's headers via provided javascript native syntax.
 * Also, it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime.
 */
public class JsGraalHeaders implements ProxyObject {

    private final Map wrapped;
    private final HttpServletResponse response;

    public JsGraalHeaders(Map wrapped, HttpServletResponse response) {

        this.wrapped = wrapped;
        this.response = response;
    }

    @Override
    public Object getMember(String name) {

        if (wrapped == null) {
            return null;
        } else {
            return wrapped.get(name);
        }
    }

    @Override
    public Object getMemberKeys() {

        return ProxyArray.fromArray(wrapped.keySet().toArray());
    }

    @Override
    public boolean hasMember(String name) {

        if (wrapped == null) {
            return false;
        } else {
            return wrapped.get(name) != null;
        }
    }

    @Override
    public boolean removeMember(String name) {

        if (wrapped == null) {
            return false;
        } else {
            wrapped.remove(name);
        }
        return false;
    }

    @Override
    public void putMember(String name, Value value) {

        if (wrapped != null) {
            wrapped.put(name, value);
            //adds a new header to the response.
            response.addHeader(name, String.valueOf(value));
        }
    }
}
