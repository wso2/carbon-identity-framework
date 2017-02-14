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

import org.wso2.carbon.identity.gateway.api.FrameworkServerException;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.context.SequenceContext;

public class SessionHandler extends AbstractSessionHandler {

    @Override
    public void updateSession(AuthenticationContext context) throws FrameworkServerException {

        // add cookie value to authentication context
        // hash the cookie value

        context.getSequenceContext().getCurrentStepContext().isAuthenticated();

        context.getSequence().hasNext(context.getSequenceContext().getCurrentStep());


        // if(all steps success) {

            //if(new service provider) {

                // insert new service provider to DB

            //} else {

                // update existing service provider in DB


            // }

        // } else {

            //if(new service provider) {

            //} else {

            //}

        // }
        SequenceContext sequenceContext = context.getSessionContext().getSequenceContext(context.getServiceProvider()
                                                                                               .getName());
        
    }
}
