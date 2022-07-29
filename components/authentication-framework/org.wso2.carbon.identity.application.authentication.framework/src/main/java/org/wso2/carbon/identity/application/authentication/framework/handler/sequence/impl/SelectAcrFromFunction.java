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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Select the preferred acr value from the available list.
 */
public class SelectAcrFromFunction implements SelectOneFunction {

    private static final Log log = LogFactory.getLog(SelectAcrFromFunction.class);

    public String evaluate(JsAuthenticationContext context, Object possibleOutcomesObj) {

        String[] possibleOutcomes = extractPossibleOutcomes(context, possibleOutcomesObj);
        List<String> acrListRequested = context.getWrapped().getRequestedAcr();
        if (acrListRequested == null || acrListRequested.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("ACR values from context is empty. Selecting the default outcome as null.");
            }
            return null;
        }
        if (possibleOutcomes.length > 0) {
            return selectBestOutcome(acrListRequested, possibleOutcomes);
        }
        return null;
    }

    private String[] extractPossibleOutcomes(JsAuthenticationContext context, Object possibleOutcomesObj) {

        String[] possibleOutcomes;
        if (possibleOutcomesObj instanceof String[]) {
            possibleOutcomes = (String[]) possibleOutcomesObj;
        } else if (possibleOutcomesObj instanceof ScriptObjectMirror) {
            if (((ScriptObjectMirror) possibleOutcomesObj).isArray()) {
                possibleOutcomes = ((ScriptObjectMirror) possibleOutcomesObj).to(String[].class);
            } else {
                log.error("Invalid argument provided for possible outcomes for " + FrameworkConstants.JSAttributes
                        .JS_FUNC_SELECT_ACR_FROM + " function in service provider: " + context.getWrapped()
                        .getServiceProviderName() + ". Expected array of strings.");
                possibleOutcomes = new String[0];
            }
        } else {
            log.error("Invalid argument provided for possible outcomes for " + FrameworkConstants.JSAttributes
                    .JS_FUNC_SELECT_ACR_FROM + " function in service provider: " + context.getWrapped()
                    .getServiceProviderName() + ". Expected array of strings.");
            possibleOutcomes = new String[0];
        }
        return possibleOutcomes;
    }

    private String selectBestOutcome(List<String> acrListRequested, String[] possibleOutcomes) {

        Map<Integer, String> acrRequestedWithPriority = new TreeMap<>(Collections.reverseOrder(
                (Comparator<Integer>) (o1, o2) -> o2.compareTo(o1)));
        String acrSelected = null;

        for (String acrChecked : acrListRequested) {
            for (int x = 0; x < possibleOutcomes.length; x++) {
                String outcomeToTest = possibleOutcomes[x];
                if (outcomeToTest.equals(acrChecked)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Reassigning Best Match for the outcome : " + outcomeToTest + " with priority : " +
                                x + 1);
                    }
                    acrRequestedWithPriority.put(x + 1, acrChecked);
                    break;
                }
            }
        }
        if (!acrRequestedWithPriority.entrySet().isEmpty()) {
            acrSelected = acrRequestedWithPriority.entrySet().iterator().next().getValue();
        }
        return acrSelected;
    }
}
