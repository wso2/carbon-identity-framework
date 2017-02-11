package org.wso2.carbon.identity.gateway.context;


import org.wso2.carbon.identity.gateway.common.model.AuthenticationStep;
import org.wso2.carbon.identity.gateway.api.IdentityMessageContext;
import org.wso2.carbon.identity.gateway.cache.SessionContextCache;
import org.wso2.carbon.identity.gateway.common.model.sp.ServiceProviderConfig;
import org.wso2.carbon.identity.gateway.model.User;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.model.AbstractSequence;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.util.Utility;
import org.wso2.carbon.identity.gateway.processor.request.AuthenticationRequest;
import org.wso2.carbon.identity.gateway.processor.request.ClientAuthenticationRequest;
import org.wso2.carbon.identity.mgt.claim.Claim;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class AuthenticationContext<T1 extends Serializable, T2 extends Serializable, T3 extends ClientAuthenticationRequest> extends
                                                                                                   IdentityMessageContext<T1, T2, T3> {

    private static final long serialVersionUID = 6821167819709907062L;

    protected ClientAuthenticationRequest initialAuthenticationRequest;

    private AbstractSequence sequence = null;
    private SequenceContext sequenceContext = new SequenceContext();

    public AuthenticationContext(T3 authenticationRequest, Map<T1, T2> parameters) {
        super(authenticationRequest, parameters);
        this.initialAuthenticationRequest = authenticationRequest;
    }

    public AuthenticationContext(T3 authenticationRequest) {
        super(authenticationRequest);
        this.initialAuthenticationRequest = authenticationRequest;
    }

    public ClientAuthenticationRequest getInitialAuthenticationRequest() {
        return initialAuthenticationRequest;
    }

    public SessionContext getSessionContext() {
        AuthenticationRequest authenticationRequest = (AuthenticationRequest) getIdentityRequest();
        String browserCookieValue = "" ; //authenticationRequest.getBrowserCookieValue();
        return SessionContextCache.getInstance().getValueFromCache(browserCookieValue);
    }


    public SequenceContext getSequenceContext() {
        return sequenceContext;
    }

    public void setSequenceContext(
            SequenceContext sequenceContext) {
        this.sequenceContext = sequenceContext;
    }

    public AbstractSequence getSequence() {
        return sequence;
    }

    public void setSequence(
            AbstractSequence sequence) {
        this.sequence = sequence;
    }

    public ServiceProviderConfig getServiceProvider() throws AuthenticationHandlerException {
        ClientAuthenticationRequest clientAuthenticationRequest = getInitialAuthenticationRequest();
        ServiceProviderConfig serviceProvider = Utility.getServiceProvider(clientAuthenticationRequest.getType(),
                                                                           clientAuthenticationRequest.getUniqueId());
        return serviceProvider;
    }

    public User getSubjectUser() {
        SequenceContext sequenceContext = getSequenceContext();
        User subjectStepUser = null;
        AbstractSequence sequence = getSequence();
        AuthenticationStep[] stepAuthenticatorConfig = sequence.getStepAuthenticatorConfig();
        for (AuthenticationStep authenticationStep : stepAuthenticatorConfig) {
            boolean subjectUser = authenticationStep.isSubjectStep();
            if (subjectUser) {
                SequenceContext.StepContext stepContext =
                        sequenceContext.getStepContext(authenticationStep.getStepOrder());
                subjectStepUser = stepContext.getUser();
            }
        }
        return subjectStepUser;
    }

    public Set<Claim> getClaims() {
        SequenceContext sequenceContext = getSequenceContext();
        User attributeStepUser = null;
        AbstractSequence sequence = getSequence();
        AuthenticationStep[] stepAuthenticatorConfig = sequence.getStepAuthenticatorConfig();
        for (AuthenticationStep authenticationStep : stepAuthenticatorConfig) {
            boolean attributeStep = authenticationStep.isAttributeStep();
            if (attributeStep) {
                SequenceContext.StepContext stepContext =
                        sequenceContext.getStepContext(authenticationStep.getStepOrder());
                attributeStepUser = stepContext.getUser();
            }
        }
        return attributeStepUser.getClaims();
    }

}
