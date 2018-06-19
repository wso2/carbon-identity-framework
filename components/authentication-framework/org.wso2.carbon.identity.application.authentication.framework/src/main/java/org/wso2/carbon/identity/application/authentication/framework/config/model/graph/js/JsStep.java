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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a authentication step.
 */
public class JsStep extends AbstractJSContextMemberObject {

    private static final Log LOG = LogFactory.getLog(JsSteps.class);

    private int step;
    private String authenticatedIdp;

    public JsStep(int step, String authenticatedIdp) {

        this.step = step;
        this.authenticatedIdp = authenticatedIdp;
    }

    public JsStep(AuthenticationContext context, int step, String authenticatedIdp) {

        this(step, authenticatedIdp);
        initializeContext(context);
    }

    @Override
    public Object getMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_AUTHENTICATED_SUBJECT:
                return new JsAuthenticatedUser(getContext(), getSubject(), step, authenticatedIdp);
            case FrameworkConstants.JSAttributes.JS_AUTHENTICATED_IDP:
                return authenticatedIdp;
            case FrameworkConstants.JSAttributes.JS_AUTHENTICATION_OPTIONS:
                return getOptions();
            default:
                return super.getMember(name);
        }
    }

    @Override
    public boolean hasMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_AUTHENTICATED_SUBJECT:
                return true;
            case FrameworkConstants.JSAttributes.JS_AUTHENTICATED_IDP:
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
            AuthenticatedIdPData idPData = getContext().getCurrentAuthenticatedIdPs().get(authenticatedIdp);
            return idPData.getUser();
        }
        return null;
    }

    private Map<String, Set<String>> getOptions() {

        Map<String, Set<String>> optionsList = new HashMap<>();
        StepConfig stepConfig = getContext().getSequenceConfig().getAuthenticationGraph().getStepMap().get(step);
        Set<String> localAuthenticators = new HashSet<>();
        Set<String> federatedIdps = new HashSet<>();
        stepConfig.getAuthenticatorList().forEach(authConfig -> authConfig.getIdpNames().forEach(name -> {
            if (FrameworkConstants.LOCAL_IDP_NAME.equals(name)) {
                localAuthenticators.add(authConfig.getApplicationAuthenticator().getName());
            } else {
                federatedIdps.add(name);
            }
        }));
        if (!localAuthenticators.isEmpty()) {
            optionsList.put(FrameworkConstants.JSAttributes.LOCAL, localAuthenticators);
        }
        if (!federatedIdps.isEmpty()) {
            optionsList.put(FrameworkConstants.JSAttributes.FEDERATED, federatedIdps);
        }
        return optionsList;
    }
}
