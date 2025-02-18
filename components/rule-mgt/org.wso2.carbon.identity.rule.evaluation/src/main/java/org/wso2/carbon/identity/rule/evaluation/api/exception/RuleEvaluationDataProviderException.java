/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.rule.evaluation.api.exception;

/**
 * Rule evaluation data provider exception.
 * This exception is thrown when an error occurs while retrieving evaluation data from rule data provider.
 */
public class RuleEvaluationDataProviderException extends RuleEvaluationException {

    public RuleEvaluationDataProviderException(String message) {

        super(message);
    }

    public RuleEvaluationDataProviderException(String message, Throwable cause) {

        super(message, cause);
    }
}
