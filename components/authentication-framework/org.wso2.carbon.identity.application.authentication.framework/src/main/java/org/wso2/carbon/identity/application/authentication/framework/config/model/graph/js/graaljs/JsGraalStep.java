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
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsStep;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

/**
 * Represents a authentication step.
 * This wrapper uses GraalJS polyglot context.
 */
public class JsGraalStep extends JsStep implements ProxyObject {

    public JsGraalStep(int step, String authenticatedIdp, String authenticatedAuthenticator) {

        super(step, authenticatedIdp, authenticatedAuthenticator);
    }

    public JsGraalStep(AuthenticationContext context, int step, String authenticatedIdp,
                       String authenticatedAuthenticator) {

        super(context, step, authenticatedIdp, authenticatedAuthenticator);
    }

    @Override
    public Object getMemberKeys() {

        return ProxyArray.fromArray(super.getMemberKeys());
    }

    @Override
    public void putMember(String key, Value value) {

        super.setMember(key, value);
    }

    @Override
    public boolean removeMember(String name) {

        super.removeMemberObject(name);
        return true;
    }
}
