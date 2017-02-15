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

package org.wso2.carbon.identity.gateway.processor.handler.session;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.gateway.api.response.FrameworkHandlerResponse;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.context.SequenceContext;
import org.wso2.carbon.identity.gateway.context.SessionContext;
import org.wso2.carbon.identity.gateway.dao.CacheBackedSessionDAO;
import org.wso2.carbon.identity.gateway.processor.handler.FrameworkHandlerException;
import org.wso2.carbon.identity.gateway.processor.request.AuthenticationRequest;

import java.util.UUID;

public class SessionHandler extends AbstractSessionHandler {

    @Override
    public FrameworkHandlerResponse updateSession(AuthenticationContext context) throws FrameworkHandlerException {

        // add cookie value to authentication context
        String sessionCookie = ((AuthenticationRequest)context.getIdentityRequest()).getSessionCookie();
        String cookieValue = null;
        String cookieHash = null;
        if(StringUtils.isBlank(sessionCookie)) {
            cookieValue = UUID.randomUUID().toString();
            cookieHash = DigestUtils.sha256Hex(cookieValue);
        }
        context.addParameter(AuthenticationRequest.AuthenticationRequestConstants.SESSION_COOKIE, cookieValue);

        String serviceProviderName = context.getServiceProvider().getName();
        SequenceContext existingSequenceContext = context.getSessionContext().getSequenceContext(serviceProviderName);
        SequenceContext currentSequenceContext = context.getSequenceContext();
        int currentStep = currentSequenceContext.getCurrentStep();
        boolean isLastStepAuthenticated = existingSequenceContext.getCurrentStepContext().isAuthenticated();
        boolean isSequenceCompleted = !context.getSequence().hasNext(currentStep) && isLastStepAuthenticated;

        context.getSessionContext().addSequenceContext(serviceProviderName, currentSequenceContext);
        CacheBackedSessionDAO.getInstance().put(cookieHash, context.getSessionContext());
        return FrameworkHandlerResponse.CONTINUE;

//        if(existingSequenceContext == null) { // if new service provider
//
//            // insert new service provider to DB. The last step may be authenticated or not based on if the sequence
//            // was completed or not
//            context.getSessionContext().addSequenceContext(serviceProviderName, currentSequenceContext);
//            CacheBackedSessionDAO.getInstance().put(cookieHash, context.getSessionContext());
//
//        } else { // if existing service provider
//
//            if(isSequenceCompleted) { // all steps are successful
//
//                // check for existing steps of service provider and if IDP is different in any of the steps update
//                // them persist
//                // add more steps if needed
//                context.getSessionContext().addSequenceContext(serviceProviderName, currentSequenceContext);
//                CacheBackedSessionDAO.getInstance().put(cookieHash, context.getSessionContext());
//
//                for(int i = 1; i == currentStep; i++) {
//                    SequenceContext.StepContext existingStepContext = existingSequenceContext.getStepContext(i);
//                    SequenceContext.StepContext currentStepContext = currentSequenceContext.getStepContext(i);
//                    if(currentStepContext.isAuthenticated() && existingStepContext != null) {
//                        if(!existingStepContext.getIdentityProviderName().equals(currentStepContext
//                                                                                     .getIdentityProviderName())) {
//
//                        }
//
//                    }
//                }
//
//            } else { // all steps are not successful
//
//                // do the above only for the successful steps
//
//            }
//
//        }
        
    }
}
