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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsServletRequest;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;

import javax.servlet.http.HttpServletRequest;

/**
 * Javascript wrapper for Java level HTTPServletRequest.
 * This wrapper uses GraalJS polyglot context.
 * This provides controlled access to HTTPServletRequest object via provided javascript native syntax.
 * e.g
 * var redirect_uri = context.request.params.redirect_uri
 * <p>
 * instead of
 * var userName = context.getRequest().getParameter("redirect_uri)
 * <p>
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime
 * HTTPServletRequest.
 */
public class JsGraalServletRequest extends JsServletRequest implements ProxyObject {

    protected static final Log LOG = LogFactory.getLog(JsGraalServletRequest.class);

    public JsGraalServletRequest(TransientObjectWrapper<HttpServletRequest> wrapped) {

        super(wrapped);
    }

    @Override
    public Object getMemberKeys() {

        return ProxyArray.fromArray(super.getMemberKeys());
    }

    @Override
    public void putMember(String key, Value value) {

        String valueAsString = value.isString() ? value.asString() : String.valueOf(value);
        super.setMember(key, valueAsString);
    }

    @Override
    public boolean removeMember(String key) {

        return false;
    }
}
