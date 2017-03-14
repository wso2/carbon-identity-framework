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
 */
package org.wso2.carbon.identity.gateway.authentication.authenticator;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.gateway.authentication.response.AuthenticationResponse;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.context.SequenceContext;
import org.wso2.carbon.identity.gateway.exception.AuthenticationHandlerException;

/**
 * AbstractApplicationAuthenticator provide some basic functionality to all the authenticators.
 */
public abstract class AbstractApplicationAuthenticator implements ApplicationAuthenticator {

    private static final long serialVersionUID = -4406878411547612129L;
    private static final Logger log = LoggerFactory.getLogger(AbstractApplicationAuthenticator.class);

    @Override
    public AuthenticationResponse process(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        AuthenticationResponse authenticationResponse;
        if (isInitialRequest(authenticationContext)) {
            authenticationResponse = processRequest(authenticationContext);
        } else {
            authenticationResponse = processResponse(authenticationContext);
        }
        return authenticationResponse;
    }

    /**
     * Check whether the request is initial/failed one or not.
     *
     * @param authenticationContext
     * @return
     * @throws AuthenticationHandlerException
     */
    protected boolean isInitialRequest(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        SequenceContext sequenceContext = authenticationContext.getSequenceContext();
        SequenceContext.StepContext currentStepContext = sequenceContext.getCurrentStepContext();
        if (currentStepContext.getStatus().equals(SequenceContext.Status.INITIAL) || currentStepContext.getStatus()
                .equals(SequenceContext.Status.FAILED)) {
            return true;
        }
        return false;
    }


    /**
     * If the request is initial/failed, then this method will call by the process method.
     *
     * @param context
     * @return
     * @throws AuthenticationHandlerException
     */
    protected abstract AuthenticationResponse processRequest(AuthenticationContext context)
            throws AuthenticationHandlerException;

    /**
     * If the request is not the initial/failed one , then this method will call by processor.
     *
     * @param context
     * @return
     * @throws AuthenticationHandlerException
     */
    protected abstract AuthenticationResponse processResponse(AuthenticationContext context)
            throws AuthenticationHandlerException;


}
