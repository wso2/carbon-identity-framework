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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.AbstractJSContextMemberObject;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a authentication step.
 * This wrapper uses GraalJS polyglot context.
 */
public class JsGraalStep extends AbstractJSContextMemberObject implements ProxyObject {

    protected static final Log LOG = LogFactory.getLog(JsGraalStep.class);

    protected int step;
    protected String authenticatedIdp;

    public JsGraalStep(int step, String authenticatedIdp) {

        this.step = step;
        this.authenticatedIdp = authenticatedIdp;
    }

    public JsGraalStep(AuthenticationContext context, int step, String authenticatedIdp) {

        this(step, authenticatedIdp);
        initializeContext(context);
    }

    @Override
    public Object getMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_AUTHENTICATED_SUBJECT:
                return new JsGraalAuthenticatedUser(getContext(), getSubject(), step, authenticatedIdp);
            case FrameworkConstants.JSAttributes.JS_AUTHENTICATED_IDP:
                return authenticatedIdp;
            case FrameworkConstants.JSAttributes.JS_AUTHENTICATION_OPTIONS:
                return getOptions();
            default:
                return null;
        }
    }

    @Override
    public Object getMemberKeys() {

        return ProxyArray.fromArray(FrameworkConstants.JSAttributes.JS_AUTHENTICATED_SUBJECT,
                FrameworkConstants.JSAttributes.JS_AUTHENTICATION_OPTIONS,
                FrameworkConstants.JSAttributes.JS_AUTHENTICATED_IDP);
    }

    @Override
    public void putMember(String key, Value value) {

        LOG.warn("Step is readonly, hence the put member is ignored.");
    }

    @Override
    public boolean removeMember(String name) {

        LOG.warn("Step is readonly, hence the can't remove the member.");
        return false;
    }

    public boolean hasMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_AUTHENTICATED_SUBJECT:
            case FrameworkConstants.JSAttributes.JS_AUTHENTICATION_OPTIONS:
            case FrameworkConstants.JSAttributes.JS_AUTHENTICATED_IDP:
                return true;
            default:
                return super.hasMember(name);
        }
    }

    protected AuthenticatedUser getSubject() {

        if (authenticatedIdp != null) {
            AuthenticatedIdPData idPData = getContext().getCurrentAuthenticatedIdPs().get(authenticatedIdp);
            if (idPData == null) {
                idPData = getContext().getPreviousAuthenticatedIdPs().get(authenticatedIdp);
            }
            if (idPData != null) {
                return idPData.getUser();
            }
        }
        return null;
    }

    protected List<Map<String, String>> getOptions() {

        List<Map<String, String>> optionsList = new ArrayList<>();
        Optional<StepConfig> optionalStepConfig = getContext().getSequenceConfig().getStepMap().values().stream()
                .filter(stepConfig -> stepConfig.getOrder() == step).findFirst();
        optionalStepConfig.ifPresent(stepConfig -> stepConfig.getAuthenticatorList()
                .forEach(authConfig -> authConfig.getIdpNames().forEach(name -> {
                    Map<String, String> option = new HashMap<>();
                    option.put(FrameworkConstants.JSAttributes.IDP, name);
                    option.put(FrameworkConstants.JSAttributes.AUTHENTICATOR,
                            authConfig.getApplicationAuthenticator().getName());
                    optionsList.add(option);
                })));
        return optionsList;
    }
}
