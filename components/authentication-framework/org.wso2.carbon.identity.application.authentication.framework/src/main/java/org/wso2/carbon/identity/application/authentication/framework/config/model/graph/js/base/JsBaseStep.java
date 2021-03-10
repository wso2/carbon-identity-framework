package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.AbstractJSContextMemberObject;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn.AbstractJsObject;
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
 */
public abstract class JsBaseStep extends AbstractJSContextMemberObject {

    protected static final Log LOG = LogFactory.getLog(JsBaseStep.class);

    protected int step;
    protected String authenticatedIdp;

    public JsBaseStep(int step, String authenticatedIdp) {

        this.step = step;
        this.authenticatedIdp = authenticatedIdp;
    }

    public JsBaseStep(AuthenticationContext context, int step, String authenticatedIdp) {

        this(step, authenticatedIdp);
        initializeContext(context);
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
        optionalStepConfig.ifPresent(stepConfig -> stepConfig.getAuthenticatorList().forEach(
                authConfig -> authConfig.getIdpNames().forEach(name -> {
                    Map<String, String> option = new HashMap<>();
                    option.put(FrameworkConstants.JSAttributes.IDP, name);
                    option.put(FrameworkConstants.JSAttributes.AUTHENTICATOR, authConfig.getApplicationAuthenticator()
                            .getName());
                    optionsList.add(option);
                })));
        return optionsList;
    }

}
