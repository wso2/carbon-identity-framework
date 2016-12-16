/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.entitlement.common;

import org.wso2.balana.utils.Constants.PolicyConstants;

import java.util.Arrays;

/**
 *
 */
public class Utils {

    public static boolean isValidRuleAlgorithm(String algorithmUri, boolean isPolicy) {

        if (isPolicy) {
            return algorithmUri != null &&
                    Arrays.asList(PolicyConstants.PolicyCombiningAlog.algorithms).contains(algorithmUri);
        } else {
            return algorithmUri != null &&
                    Arrays.asList(PolicyConstants.RuleCombiningAlog.algorithms).contains(algorithmUri);
        }
    }

    public static boolean isValidCategory(String category) {

        return category != null &&
                Arrays.asList(EntitlementConstants.PolicyEditor.BASIC_CATEGORIES).contains(category);
    }

    public static boolean isValidFunction(String functionUri) {

        return functionUri != null &&
                Arrays.asList(PolicyConstants.Functions.functions).contains(functionUri);
    }

    public static boolean isValidDataType(String dataTypeUri) {

        return dataTypeUri != null &&
                Arrays.asList(PolicyConstants.DataType.dataTypes).contains(dataTypeUri);
    }

    public static boolean isValidEffect(String effectUri) {

        return effectUri != null &&
                Arrays.asList(PolicyConstants.RuleEffect.effect).contains(effectUri);
    }


    public static boolean isValidPreFunction(String preFunctionUri) {

        return preFunctionUri != null &&
                Arrays.asList(PolicyConstants.PreFunctions.preFunctions).contains(preFunctionUri);
    }
}
