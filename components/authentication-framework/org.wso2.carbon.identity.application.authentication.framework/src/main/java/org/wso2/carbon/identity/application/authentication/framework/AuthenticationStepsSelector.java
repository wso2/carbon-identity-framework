/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework;

import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.model.AuthenticationChainConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

/**
 * Definition for adaptive authentication extensions.
 * Evaluates the next step to be taken based on current status of the AuthenticationContext.
 * 
 */
public interface AuthenticationStepsSelector {

    /**
     * Checks if the authentication level is satisfied for the given authentication context.
     * Returning true will cause the actual authenticator not to be executed and treated as if it has been returned
     * success.
     *
     * @param authenticatorConfig
     * @param context
     * @return
     */
    boolean isAuthenticationSatisfied(AuthenticatorConfig authenticatorConfig, AuthenticationContext context);

    /**
     * Finds the sequence chain name from the given sequence config respective to the current authentication context
     * @param context
     * @param serviceProvider
     * @return the selected chain, or null if not selected.
     */
    AuthenticationChainConfig resolveSequenceChain(AuthenticationContext context, ServiceProvider serviceProvider);
}
