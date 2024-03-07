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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js;

import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsWrapperFactoryProvider;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base.JsBaseAuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base.JsBaseAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import java.util.Map;
import java.util.Optional;

/**
 * Abstract Javascript wrapper for Java level AuthenticationContext.
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
public abstract class JsAuthenticationContext extends AbstractJSObjectWrapper<AuthenticationContext>
        implements JsBaseAuthenticationContext {

    public JsAuthenticationContext(AuthenticationContext wrapped) {

        super(wrapped);
        initializeContext(wrapped);
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
            case FrameworkConstants.JSAttributes.JS_ENDPOINT_PARAMS:
                return getWrapped().getEndpointParams() != null;
            case FrameworkConstants.JSAttributes.JS_CURRENT_STEP:
            case FrameworkConstants.JSAttributes.JS_RETRY_STEP:
                return true;
            case FrameworkConstants.JSAttributes.JS_CURRENT_KNOWN_SUBJECT:
                return getCurrentSubjectIdentifierStep() != null;
            default:
                return super.hasMember(name);
        }
    }

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
                return JsWrapperFactoryProvider.getInstance().getWrapperFactory()
                        .createJsServletRequest((TransientObjectWrapper) getWrapped()
                                .getParameter(FrameworkConstants.RequestAttribute.HTTP_REQUEST));
            case FrameworkConstants.JSAttributes.JS_RESPONSE:
                return JsWrapperFactoryProvider.getInstance().getWrapperFactory()
                        .createJsServletResponse((TransientObjectWrapper) getWrapped()
                                .getParameter(FrameworkConstants.RequestAttribute.HTTP_RESPONSE));
            case FrameworkConstants.JSAttributes.JS_STEPS:
                return JsWrapperFactoryProvider.getInstance().getWrapperFactory().createJsSteps(getWrapped());
            case FrameworkConstants.JSAttributes.JS_CURRENT_STEP:
                return JsWrapperFactoryProvider.getInstance().getWrapperFactory()
                        .createJsStep(getContext(), getContext().getCurrentStep(), getAuthenticatedIdPOfCurrentStep(),
                                getAuthenticatedAuthenticatorOfCurrentStep());
            case FrameworkConstants.JSAttributes.JS_CURRENT_KNOWN_SUBJECT:
                StepConfig stepConfig = getCurrentSubjectIdentifierStep();
                if (stepConfig != null) {
                    return JsWrapperFactoryProvider.getInstance().getWrapperFactory()
                            .createJsAuthenticatedUser(this.getContext(), stepConfig.getAuthenticatedUser(),
                                    stepConfig.getOrder(), stepConfig.getAuthenticatedIdP());
                } else {
                    return null;
                }
            case FrameworkConstants.JSAttributes.JS_RETRY_STEP:
                return getWrapped().isRetrying();
            case FrameworkConstants.JSAttributes.JS_ENDPOINT_PARAMS:
                return JsWrapperFactoryProvider.getInstance().getWrapperFactory()
                        .createJsWritableParameters(getContext().getEndpointParams());
            default:
                return super.getMember(name);
        }
    }

    public Object getMemberKeys() {

        return new String[]{
                FrameworkConstants.JSAttributes.JS_REQUESTED_ACR,
                FrameworkConstants.JSAttributes.JS_TENANT_DOMAIN,
                FrameworkConstants.JSAttributes.JS_SERVICE_PROVIDER_NAME,
                FrameworkConstants.JSAttributes.JS_LAST_LOGIN_FAILED_USER,
                FrameworkConstants.JSAttributes.JS_REQUEST,
                FrameworkConstants.JSAttributes.JS_RESPONSE,
                FrameworkConstants.JSAttributes.JS_STEPS,
                FrameworkConstants.JSAttributes.JS_CURRENT_STEP,
                FrameworkConstants.JSAttributes.JS_CURRENT_KNOWN_SUBJECT,
                FrameworkConstants.JSAttributes.JS_RETRY_STEP};
    }

    public boolean setMemberObject(String name, Object value) {

        if (name.equals(FrameworkConstants.JSAttributes.JS_SELECTED_ACR)) {
            getWrapped().setSelectedAcr(String.valueOf(value));
        } else {
            super.setMember(name, value);
        }
        return true;
    }

    public boolean removeMemberObject(String name) {

        if (name.equals(FrameworkConstants.JSAttributes.JS_SELECTED_ACR)) {
            getWrapped().setSelectedAcr(null);
            return true;
        }
        return false;
    }

    private boolean hasTransientValueInParameters(String key) {

        TransientObjectWrapper transientObjectWrapper = (TransientObjectWrapper) getWrapped().getParameter(key);
        return transientObjectWrapper != null && transientObjectWrapper.getWrapped() != null;
    }

    protected String getAuthenticatedIdPOfCurrentStep() {

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

    protected String getAuthenticatedAuthenticatorOfCurrentStep() {

        if (getContext().getSequenceConfig() == null) {
            // Sequence config is not yet initialized.
            return null;
        }

        StepConfig stepConfig = getContext().getSequenceConfig().getStepMap()
                .get(getContext().getCurrentStep());

        return stepConfig != null ? stepConfig.getAuthenticatedAutenticator().getName() : null;
    }

    protected StepConfig getCurrentSubjectIdentifierStep() {

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

    protected JsBaseAuthenticatedUser getLastLoginFailedUserFromWrappedContext() {

        Object lastLoginFailedUser
                = getWrapped().getProperty(FrameworkConstants.JSAttributes.JS_LAST_LOGIN_FAILED_USER);
        if (lastLoginFailedUser instanceof AuthenticatedUser) {
            return JsWrapperFactoryProvider.getInstance().getWrapperFactory()
                    .createJsAuthenticatedUser(getWrapped(), (AuthenticatedUser) lastLoginFailedUser);
        } else {
            return null;
        }
    }
}
