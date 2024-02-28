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
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsRuntimeClaims;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

/**
 * Represent the user's runtime claims for GraalJs Execution.
 */
public class JsGraalRuntimeClaims extends JsRuntimeClaims implements ProxyObject {

    public JsGraalRuntimeClaims(AuthenticationContext context, int step, String idp) {

        super(context, step, idp);
    }

    public JsGraalRuntimeClaims(AuthenticationContext context, AuthenticatedUser user) {

        super(context, user);
    }

    @Override
    public Object getMemberKeys() {

        return ProxyArray.fromArray();
    }

    @Override
    public void putMember(String claimUri, Value value) {

        String valueAsString = value.isString() ? value.asString() : String.valueOf(value);
        setMember(claimUri, valueAsString);
    }
}
