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
public class AcrDecisionEvaluator implements AuthenticationDecisionEvaluator {

    private static final Log log = LogFactory.getLog(AcrDecisionEvaluator.class);

    @Override
    public String evaluate(AuthenticationContext context, ServiceProvider serviceProvider, DecisionNode config) {
        List<String> acrListRequested = context.getSequenceConfig().getRequestedAcr();
        if (acrListRequested == null || acrListRequested.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("ACR values from context is empty. Selecting the default outcome as null.");
            }
            return null;
        }
        if (config.getLinks() != null && !config.getLinks().isEmpty()) {
            Map<String, String[]> pathExpressionMap = new HashMap<>();
            for (Link link : config.getLinks()) {
                if (StringUtils.isNotEmpty(link.getExpression())) {
                    String[] acrValues = link.getExpression().split(" ");
                    pathExpressionMap.put(link.getName(), acrValues);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Evaluating ACR from the context: " + acrListRequested);
            }

            SelectedAcrAndOutcome result = bestMatch(acrListRequested, pathExpressionMap);
            if(result.selectedAcr != null) {
                context.setSelectedAcr(result.selectedAcr);
            }
            return result.outcome;
        }
        return null;
    }

    private SelectedAcrAndOutcome bestMatch(List<String> acrListRequested, Map<String, String[]> pathExpressions) {
        String bestMatch = null;
        String acrSelected = null;
        int matchLevelX = 0;
        int matchLevelY = 0;

        for (Map.Entry<String, String[]> entry : pathExpressions.entrySet()) {
            String[] acrExpressions = entry.getValue();
            String outcome = entry.getKey();
            int y = 0;
            for (String acrChecked : acrListRequested) {
                for (int x = 0; x < acrExpressions.length; x++) {
                    if ((bestMatch == null || distance(matchLevelX, matchLevelY) > distance(x, y)) && acrExpressions[x]
                            .equals(acrChecked)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Reassigning Best Match for the outcome : " + outcome);
                        }
                        acrSelected = acrChecked;
                        bestMatch = outcome;
                        matchLevelX = x;
                        matchLevelY = y;
                    }
                }
                y++;
            }
        }

        return new SelectedAcrAndOutcome(bestMatch, acrSelected);
    }

    /*
     * Calculate geometric distance of two coordinates in order to compare best match for ACR list comparison.
     */
    private long distance(int x, int y) {
        return x * x + y * y;
    }

    private class SelectedAcrAndOutcome {
        String outcome;
        String selectedAcr;

        public SelectedAcrAndOutcome(String outcome, String selectedAcr) {
            this.outcome = outcome;
            this.selectedAcr = selectedAcr;
        }
    }
}
