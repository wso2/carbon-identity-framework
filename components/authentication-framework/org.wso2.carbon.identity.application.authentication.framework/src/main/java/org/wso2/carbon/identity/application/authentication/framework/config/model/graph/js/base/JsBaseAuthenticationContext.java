package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base;

import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.AbstractJSObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import java.util.Map;
import java.util.Optional;

/**
 * Javascript abstract wrapper for Java level AuthenticationContext for both execution Implementations.
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
public abstract class JsBaseAuthenticationContext extends AbstractJSObjectWrapper<AuthenticationContext>
        implements JsAuthenticationContext {

    public JsBaseAuthenticationContext(AuthenticationContext wrapped) {
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

    protected boolean hasTransientValueInParameters(String key) {

        TransientObjectWrapper transientObjectWrapper = (TransientObjectWrapper) getWrapped().getParameter(key);
        return transientObjectWrapper != null && transientObjectWrapper.getWrapped() != null;
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
}
