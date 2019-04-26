/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import java.util.Map;
import java.util.Optional;

/**
 * Javascript wrapper for Java level AuthenticationContext.
 * This provides controlled access to AuthenticationContext object via provided javascript native syntax.
 * e.g
 * var requestedAcr = context.requestedAcr
 * <p>
 * instead of
 * var requestedAcr = context.getRequestedAcr()
 * <p>
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime
 * AuthenticationContext.
 *
 * @see AuthenticationContext
 */
public class JsAuthenticationContext extends AbstractJSObjectWrapper<AuthenticationContext> {

    public JsAuthenticationContext(AuthenticationContext wrapped) {

        super(wrapped);
        initializeContext(wrapped);
    }

    @Override
    public Object getMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_REQUESTED_ACR:
                return getWrapped().getRequestedAcr();
            case FrameworkConstants.JSAttributes.JS_TENANT_DOMAIN:
                return getWrapped().getTenantDomain();
            case FrameworkConstants.JSAttributes.JS_SERVICE_PROVIDER_NAME:
                return getWrapped().getServiceProviderName();
            case FrameworkConstants.JSAttributes.JS_LAST_LOGIN_FAILED_USER:
                return getLastLoginFailedUserFromWrappedContext();
            case FrameworkConstants.JSAttributes.JS_REQUEST:
                return new JsServletRequest((TransientObjectWrapper) getWrapped()
                        .getParameter(FrameworkConstants.RequestAttribute.HTTP_REQUEST));
            case FrameworkConstants.JSAttributes.JS_RESPONSE:
                return new JsServletResponse((TransientObjectWrapper) getWrapped()
                        .getParameter(FrameworkConstants.RequestAttribute.HTTP_RESPONSE));
            case FrameworkConstants.JSAttributes.JS_STEPS:
                return new JsSteps(getWrapped());
            case FrameworkConstants.JSAttributes.JS_CURRENT_STEP:
                return new JsStep(getContext(), getContext().getCurrentStep(), getAuthenticatedIdPOfCurrentStep());
            case FrameworkConstants.JSAttributes.JS_CURRENT_KNOWN_SUBJECT:
                AuthenticatedUser currentSubject = getCurrentSubject();
                if (currentSubject != null) {
                    return new JsAuthenticatedUser(this.getContext(), currentSubject);
                } else {
                    return null;
                }
            case FrameworkConstants.JSAttributes.JS_RETRY_STEP:
                return getWrapped().isRetrying();
            default:
                return super.getMember(name);
        }
    }

    @Override
    public boolean hasMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_REQUESTED_ACR:
                return getWrapped().getRequestedAcr() != null;
            case FrameworkConstants.JSAttributes.JS_TENANT_DOMAIN:
                return getWrapped().getTenantDomain() != null;
            case FrameworkConstants.JSAttributes.JS_SERVICE_PROVIDER_NAME:
                return getWrapped().getServiceProviderName() != null;
            case FrameworkConstants.JSAttributes.JS_LAST_LOGIN_FAILED_USER:
                return getWrapped().getProperty(FrameworkConstants.JSAttributes.JS_LAST_LOGIN_FAILED_USER) != null;
            case FrameworkConstants.JSAttributes.JS_REQUEST:
                return hasTransientValueInParameters(FrameworkConstants.RequestAttribute.HTTP_REQUEST);
            case FrameworkConstants.JSAttributes.JS_RESPONSE:
                return hasTransientValueInParameters(FrameworkConstants.RequestAttribute.HTTP_RESPONSE);
            case FrameworkConstants.JSAttributes.JS_STEPS:
                return !getWrapped().getSequenceConfig().getStepMap().isEmpty();
            default:
                return super.hasMember(name);
        }
    }

    @Override
    public void removeMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_SELECTED_ACR:
                getWrapped().setSelectedAcr(null);
                break;
            default:
                super.removeMember(name);
        }
    }

    @Override
    public void setMember(String name, Object value) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_SELECTED_ACR:
                getWrapped().setSelectedAcr(String.valueOf(value));
                break;
            default:
                super.setMember(name, value);
        }
    }

    private boolean hasTransientValueInParameters(String key) {

        TransientObjectWrapper transientObjectWrapper = (TransientObjectWrapper) getWrapped().getParameter(key);
        return transientObjectWrapper != null && transientObjectWrapper.getWrapped() != null;
    }

    private JsAuthenticatedUser getLastLoginFailedUserFromWrappedContext() {

        Object lastLoginFailedUser = getWrapped().getProperty(FrameworkConstants.JSAttributes.JS_LAST_LOGIN_FAILED_USER);
        if (lastLoginFailedUser instanceof AuthenticatedUser) {
            return new JsAuthenticatedUser(getWrapped(), (AuthenticatedUser) lastLoginFailedUser);
        } else {
            return null;
        }
    }


    private String getAuthenticatedIdPOfCurrentStep() {

        if (getContext().getSequenceConfig() == null) {
            //Sequence config is not yet initialized
            return null;
        }

        StepConfig stepConfig = getContext().getSequenceConfig().getAuthenticationGraph().getStepMap()
                .get(getContext().getCurrentStep());
        if (stepConfig != null) {
            return stepConfig.getAuthenticatedIdP();
        }
        return null;

    }

    private AuthenticatedUser getCurrentSubject() {

        if (getContext().getSequenceConfig() == null) {
            //Sequence config is not yet initialized
            return null;
        }

        Map<Integer, StepConfig> stepConfigs = getContext().getSequenceConfig().getAuthenticationGraph().getStepMap();
        Optional<StepConfig> subjectIdentifierStep = stepConfigs.values().stream()
                .filter(stepConfig -> (stepConfig.isCompleted() && stepConfig.isSubjectIdentifierStep())).findFirst();

        return subjectIdentifierStep.map(StepConfig::getAuthenticatedUser).orElse(getWrapped().getLastAuthenticatedUser());
    }
}
