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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * Javascript wrapper for Java level HashMap of HTTP headers.
 * This provides controlled access to HTTPServletResponse object's headers via provided javascript native syntax.
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime.
 */
public class CommonJsHeaders {

    private final Map wrapped;
    private final HttpServletResponse response;

    public CommonJsHeaders(Map wrapped, HttpServletResponse response) {

        this.wrapped = wrapped;
        this.response = response;
    }

    public Object getMember(String name) {

        if (wrapped == null) {
            return null;
        }
        return wrapped.get(name);
    }

    public Object getMemberKeys() {

        return wrapped.keySet().toArray();
    }

    public boolean hasMember(String name) {

        if (wrapped == null) {
            return false;
        }
        return wrapped.get(name) != null;
    }

    public boolean removeMemberObject(String name) {

        if (wrapped != null) {
            wrapped.remove(name);
            return true;
        }
        return false;
    }

    public boolean setMemberObject(String name, Object value) {

        if (wrapped != null) {
            wrapped.put(name, value);
            // Adds a new header to the response.
            response.addHeader(name, String.valueOf(value));
            return true;
        }
        return false;
    }
}
