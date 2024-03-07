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
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsSteps;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

/**
 * Returns when context.steps[<step_number] is called
 * This wrapper uses GraalJS polyglot context.
 */
public class JsGraalSteps extends JsSteps implements ProxyArray {

    public JsGraalSteps() {

        super();
    }

    public JsGraalSteps(AuthenticationContext context) {

        super(context);
    }

    @Override
    public void set(long index, Value value) {
        //Steps can not be set with script.
    }

    public boolean hasMember(String name) {

        return true;
    }
}
