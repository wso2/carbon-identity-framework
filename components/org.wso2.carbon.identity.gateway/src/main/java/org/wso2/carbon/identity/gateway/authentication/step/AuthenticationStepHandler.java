/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.carbon.identity.gateway.authentication.step;


import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.gateway.api.handler.AbstractGatewayHandler;
import org.wso2.carbon.identity.gateway.authentication.executer.AbstractExecutionHandler;
import org.wso2.carbon.identity.gateway.authentication.response.AuthenticationResponse;
import org.wso2.carbon.identity.gateway.authentication.sequence.Sequence;
import org.wso2.carbon.identity.gateway.common.model.sp.IdentityProvider;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.context.SequenceContext;
import org.wso2.carbon.identity.gateway.context.SessionContext;
import org.wso2.carbon.identity.gateway.exception.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.internal.GatewayServiceHolder;
import org.wso2.carbon.identity.gateway.model.User;

import java.util.Collection;
import java.util.Iterator;

/**
 * AuthenticationStepHandler is handling the steps in given authentication flow and triggered by
 * AuthenticationHandler implementation.
 * <p>
 * This will find the execution strategy of a given step and execute the relevant handler.
 * If we need to have a custom step execution, we can extend this and register by changing the priority in OSGi.
 */
public class AuthenticationStepHandler extends AbstractGatewayHandler {
    @Override
    public boolean canHandle(MessageContext messageContext) {
        return true;
    }

    @Override
    public String getName() {
        return "AuthenticationStepHandler";
    }

    /**
     * handleStepAuthentication method is called by AuthenticationHandler implementation and will execute the flow.
     *
     * @param authenticationContext
     * @return
     * @throws AuthenticationHandlerException
     */
    public AuthenticationResponse handleStepAuthentication(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        AuthenticationResponse authenticationResponse;
        Sequence sequence = authenticationContext.getSequence();
        SequenceContext sequenceContext = authenticationContext.getSequenceContext();

        if (!lookUpSessionValidity(authenticationContext)) {
            AbstractExecutionHandler executionHandler = getExecutionHandler(authenticationContext);
            authenticationResponse = executionHandler.execute(authenticationContext);
        } else {
            authenticationResponse = new AuthenticationResponse(AuthenticationResponse.Status.AUTHENTICATED);
        }

        if (AuthenticationResponse.Status.AUTHENTICATED.equals(authenticationResponse.status)) {
            if (sequence.hasNext(sequenceContext.getCurrentStep())) {
                sequenceContext.setCurrentStep(sequenceContext.getCurrentStep() + 1);
                authenticationResponse = handleStepAuthentication(authenticationContext);
            }
        }

        return authenticationResponse;
    }

    /**
     * Validating the session context for current step context in all the service providers. This behaviour can be
     * changed for different algorithm as well.
     * <p>
     * As default implementation, we check the current step idps vs same step idps in all the authenticated service
     * providers in session context.
     *
     * @param authenticationContext
     * @return
     * @throws AuthenticationHandlerException
     */
    protected boolean lookUpSessionValidity(AuthenticationContext authenticationContext) throws
            AuthenticationHandlerException {

        boolean isSessionValid = false;
        SessionContext sessionContext = authenticationContext.getSessionContext();
        Collection<SequenceContext> existingContexts = null;
        if (sessionContext != null) {
            existingContexts = sessionContext.getSequenceContexts();
            Iterator<SequenceContext> it = existingContexts.iterator();
            while (it.hasNext()) {
                SequenceContext sequenceContext = it.next();
                int currentStep = authenticationContext.getSequenceContext().getCurrentStep();
                String idPName = sequenceContext
                        .getStepContext(authenticationContext.getSequenceContext().getCurrentStep())
                        .getIdentityProviderName();
                IdentityProvider identityProvider = authenticationContext.getSequence().getIdentityProvider(currentStep,
                        idPName);
                String authenticatorName = sequenceContext.getStepContext(authenticationContext.getSequenceContext()
                        .getCurrentStep())
                        .getAuthenticatorName();
                User user = sequenceContext.getStepContext(authenticationContext.getSequenceContext().getCurrentStep())
                        .getUser();
                if (identityProvider != null) {
                    SequenceContext.StepContext currentStepContext = authenticationContext.getSequenceContext().getCurrentStepContext();
                    if (currentStepContext == null) {
                        currentStepContext = authenticationContext.getSequenceContext().addStepContext();
                    }
                    currentStepContext.setIdentityProviderName(idPName);
                    currentStepContext.setUser(user);
                    currentStepContext.setAuthenticatorName(authenticatorName);
                    currentStepContext.setStatus(SequenceContext.Status.AUTHENTICATED);
                    isSessionValid = true;
                    break;
                }
            }
        }
        return isSessionValid;
    }

    /**
     * Return ExecutionHandler based on context.
     *
     * @param authenticationContext
     * @return
     */
    private AbstractExecutionHandler getExecutionHandler(AuthenticationContext authenticationContext) {
        return (AbstractExecutionHandler) getHandler(GatewayServiceHolder.getInstance().getExecutionHandlers(), authenticationContext);
    }


}