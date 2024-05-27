/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.openjdk.nashorn;

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsStep;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

/**
 * Represents a authentication step.
 * Since Nashorn is deprecated in JDK 11 and onwards. We are introducing OpenJDK Nashorn engine.
 */
public class JsOpenJdkNashornStep extends JsStep implements AbstractOpenJdkNashornJsObject {

    @Deprecated
    public JsOpenJdkNashornStep(int step, String authenticatedIdp) {

        super(step, authenticatedIdp);
    }

    public JsOpenJdkNashornStep(int step, String authenticatedIdp, String authenticatedAuthenticator) {

        super(step, authenticatedIdp, authenticatedAuthenticator);
    }

    @Deprecated
    public JsOpenJdkNashornStep(AuthenticationContext context, int step, String authenticatedIdp) {

        super(context, step, authenticatedIdp);
    }

    public JsOpenJdkNashornStep(AuthenticationContext context, int step, String authenticatedIdp,
                                String authenticatedAuthenticator) {

        super(context, step, authenticatedIdp, authenticatedAuthenticator);
    }

    @Override
    public Object getMember(String name) {

        Object member = super.getMember(name);
        return member != null ? member : AbstractOpenJdkNashornJsObject.super.getMember(name);
    }

    @Override
    public boolean hasMember(String name) {

        return super.hasMember(name) || AbstractOpenJdkNashornJsObject.super.hasMember(name);
    }

    public void removeMember(String name) {

        super.removeMemberObject(name);
    }
}
