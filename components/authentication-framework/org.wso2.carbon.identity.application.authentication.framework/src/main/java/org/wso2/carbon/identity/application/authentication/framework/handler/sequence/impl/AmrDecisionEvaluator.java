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

package org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDecisionEvaluator;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.graph.DecisionNode;
import org.wso2.carbon.identity.application.common.model.graph.Link;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Performs the authentication decision based on ACR in the context.
 */
public class AmrDecisionEvaluator implements AuthenticationDecisionEvaluator {

    private static final Log log = LogFactory.getLog(AmrDecisionEvaluator.class);

    @Override
    public String evaluate(AuthenticationContext context, ServiceProvider serviceProvider, DecisionNode config) {
        List<String> amrRequested = context.getAmrRequested();
        if (amrRequested == null || amrRequested.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("AMR values from context is empty. Selecting the default outcome as null.");
            }
            return null;
        }
        return null;
    }
}
