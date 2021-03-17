/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn;

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base.JsBaseStep;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

/**
 * Represents a authentication step for Nashorn Execution.
 */
public class NashornJsStep extends JsBaseStep implements AbstractJsObject {

    public NashornJsStep(AuthenticationContext context, int step, String authenticatedIdp) {

        super(context, step, authenticatedIdp);
    }

    @Override
    public Object getMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_AUTHENTICATED_SUBJECT:
                return new NashornJsAuthenticatedUser(getContext(), getSubject(), step, authenticatedIdp);
            case FrameworkConstants.JSAttributes.JS_AUTHENTICATED_IDP:
                return authenticatedIdp;
            case FrameworkConstants.JSAttributes.JS_AUTHENTICATION_OPTIONS:
                return getOptions();
            default:
                return AbstractJsObject.super.getMember(name);
        }
    }

    @Override
    public void removeMember(String name) {

        LOG.warn("Step is readonly, hence the can't remove the member.");
    }

    @Override
    public void setMember(String name, Object value) {

        LOG.warn("Step is readonly, hence the setter is ignored.");
    }
}
