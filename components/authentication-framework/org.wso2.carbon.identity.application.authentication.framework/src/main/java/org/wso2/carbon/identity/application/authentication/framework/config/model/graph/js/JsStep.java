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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js;

import jdk.nashorn.api.scripting.AbstractJSObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

public class JsStep extends AbstractJSObject {

    private static final Log LOG = LogFactory.getLog(JsSteps.class);

    private int step;
    private AuthenticationContext wrappedContext;
    private String authenticatedIdp;

    public JsStep(AuthenticationContext wrappedContext, int step, String authenticatedIdp) {

        this.wrappedContext = wrappedContext;
        this.step = step;
        this.authenticatedIdp = authenticatedIdp;
    }

    @Override
    public Object getMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_AUTHENTICATED_SUBJECT:
                return new JsAuthenticatedUser(getSubject(), wrappedContext, step, authenticatedIdp);
            default:
                return super.getMember(name);
        }
    }

    @Override
    public boolean hasMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_AUTHENTICATED_SUBJECT:
                return true;
            case FrameworkConstants.JSAttributes.JS_LOCAL_CLAIMS:
                return true;
            case FrameworkConstants.JSAttributes.JS_REMOTE_CLAIMS:
                return true;
            default:
                return super.hasMember(name);
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

    private AuthenticatedUser getSubject() {

        if (authenticatedIdp != null) {
            AuthenticatedIdPData idPData = wrappedContext.getCurrentAuthenticatedIdPs().get(authenticatedIdp);
            return idPData.getUser();
        }
        return null;
    }

}
