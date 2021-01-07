/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsServletResponse;
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
public class GraalJsAuthenticationContext extends AbstractJSObjectWrapper<AuthenticationContext>
        implements ProxyObject, JsAuthenticationContext {

    public GraalJsAuthenticationContext(AuthenticationContext wrapped) {
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
                return new GraalJsServletRequest((TransientObjectWrapper) getWrapped()
                        .getParameter(FrameworkConstants.RequestAttribute.HTTP_REQUEST));
            case FrameworkConstants.JSAttributes.JS_RESPONSE:
                return new JsServletResponse((TransientObjectWrapper) getWrapped()
                        .getParameter(FrameworkConstants.RequestAttribute.HTTP_RESPONSE));
            case FrameworkConstants.JSAttributes.JS_STEPS:
                return new GraalJsSteps(getWrapped());
            case FrameworkConstants.JSAttributes.JS_CURRENT_STEP:
                return new GraalJsStep(getContext(), getContext().getCurrentStep(), getAuthenticatedIdPOfCurrentStep());
            case FrameworkConstants.JSAttributes.JS_CURRENT_KNOWN_SUBJECT:
                StepConfig stepConfig = getCurrentSubjectIdentifierStep();
                if (stepConfig != null) {
                    return new GraalJsAuthenticatedUser(this.getContext(), stepConfig.getAuthenticatedUser(),
                            stepConfig.getOrder(), stepConfig.getAuthenticatedIdP());
                } else {
                    return null;
                }
            case FrameworkConstants.JSAttributes.JS_RETRY_STEP:
                return getWrapped().isRetrying();
        }

        return super.getMember(name);
    }

    @Override
    public Object getMemberKeys() {

        return ProxyArray.fromArray(new String[]{ FrameworkConstants.JSAttributes.JS_STEPS,
                FrameworkConstants.JSAttributes.JS_CURRENT_KNOWN_SUBJECT});
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
            case FrameworkConstants.JSAttributes.JS_CURRENT_STEP:
                return (getContext().getCurrentStep() > 0);
            case FrameworkConstants.JSAttributes.JS_CURRENT_KNOWN_SUBJECT:
                StepConfig stepConfig = getCurrentSubjectIdentifierStep();
                if (stepConfig == null) {
                    return false;
                }
                return  stepConfig.getAuthenticatedUser() != null;
            case FrameworkConstants.JSAttributes.JS_RETRY_STEP:
                return getWrapped().isRetrying();
        }
        return super.hasMember(name);
    }

    @Override
    public void putMember(String key, Value value) {
        switch (key) {
            case FrameworkConstants.JSAttributes.JS_SELECTED_ACR:
                getWrapped().setSelectedAcr(String.valueOf(value));
                break;
        }
    }

    @Override
    public boolean removeMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_SELECTED_ACR:
                getWrapped().setSelectedAcr(null);
                return true;
            default:

                return false;
        }
    }


    private boolean hasTransientValueInParameters(String key) {

        TransientObjectWrapper transientObjectWrapper = (TransientObjectWrapper) getWrapped().getParameter(key);
        return transientObjectWrapper != null && transientObjectWrapper.getWrapped() != null;
    }

    private GraalJsAuthenticatedUser getLastLoginFailedUserFromWrappedContext() {

        Object lastLoginFailedUser = getWrapped().getProperty(FrameworkConstants.JSAttributes.JS_LAST_LOGIN_FAILED_USER);
        if (lastLoginFailedUser instanceof AuthenticatedUser) {
            return new GraalJsAuthenticatedUser(getWrapped(), (AuthenticatedUser) lastLoginFailedUser);
        } else {
            return null;
        }
    }


    private String getAuthenticatedIdPOfCurrentStep() {

        if (getContext().getSequenceConfig() == null) {
            //Sequence config is not yet initialized
            return null;
        }

        StepConfig stepConfig = getContext().getSequenceConfig().getStepMap()
                .get(getContext().getCurrentStep());
        if (stepConfig != null) {
            return stepConfig.getAuthenticatedIdP();
        }
        return null;

    }

    private StepConfig getCurrentSubjectIdentifierStep() {

        if (getContext().getSequenceConfig() == null) {
            //Sequence config is not yet initialized
            return null;
        }

        Map<Integer, StepConfig> stepConfigs = getContext().getSequenceConfig().getStepMap();
        Optional<StepConfig> subjectIdentifierStep = stepConfigs.values().stream()
                .filter(stepConfig -> (stepConfig.isCompleted() && stepConfig.isSubjectIdentifierStep())).findFirst();

        if (subjectIdentifierStep.isPresent()) {
            return subjectIdentifierStep.get();
        } else if (getContext().getCurrentStep() > 0) {
            return stepConfigs.get(getContext().getCurrentStep());
        } else {
            return null;
        }
    }
}
