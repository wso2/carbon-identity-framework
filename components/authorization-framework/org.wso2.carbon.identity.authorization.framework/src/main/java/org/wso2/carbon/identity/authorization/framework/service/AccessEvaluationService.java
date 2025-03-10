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

package org.wso2.carbon.identity.authorization.framework.service;

import org.wso2.carbon.identity.authorization.framework.exception.AccessEvaluationException;
import org.wso2.carbon.identity.authorization.framework.model.AccessEvaluationRequest;
import org.wso2.carbon.identity.authorization.framework.model.AccessEvaluationResponse;
import org.wso2.carbon.identity.authorization.framework.model.BulkAccessEvaluationRequest;
import org.wso2.carbon.identity.authorization.framework.model.BulkAccessEvaluationResponse;

/**
 * The {@code AccessEvaluationService} interface provides method definitions for access evaluation.
 * <p>
 *     Implementations of this interface should provide the functionalities to evaluate access based on the request
 *     type, whether it is an evaluation request or a search request. Implementations can be specific to a particular
 *     authorization engine and use the engine's API to perform the functionalities. Note that the request models
 *     and response models given and returned are generic so if implementing, you may need to convert them to the
 *     specific models of the authorization engine you are using.
 * </p>
 */
public interface AccessEvaluationService {

    /**
     * Evaluates authorization when a new single access evaluation request is received.
     * <p>
     *   This method should be used when a single access evaluation request is received. The method should check
     *   the authorization from the authorization engine and return the decision given by the engine. Exceptions should
     *   be thrown if an error occurs while checking authorization, whether it is a configuration error or an error from
     *   the authorization engine.
     * </p>
     * @param accessEvaluationRequest The request which contains the necessary information to evaluate authorization.
     * @return The evaluation decision from the authorization engine.
     * @throws AccessEvaluationException If an error occurs while evaluating authorization.
     * @see AccessEvaluationRequest
     * @see AccessEvaluationResponse
     */
    AccessEvaluationResponse evaluate(AccessEvaluationRequest accessEvaluationRequest) throws AccessEvaluationException;

    /**
     * Evaluates authorization when a new bulk access evaluation request is received with multiple requests.
     * <p>
     *     This method should be used when a bulk access evaluation request is received. The method should evaluate
     *     authorization for each request in the bulk request and return the decisions given by the authorization engine
     *     for each request. Exceptions should be thrown if an error occurs while evaluating, whether it is a
     *     configuration error or an error from the authorization engine. Errors occurring for requests inside the bulk
     *     request should be returned in the response.
     * </p>
     * @param bulkAccessEvaluationRequest The list of requests which contains the necessary information to evaluate
     *                                    authorization.
     * @return Evaluation decisions for each request in the bulk request.
     * @throws AccessEvaluationException If an error occurs while evaluating authorization.
     * @see BulkAccessEvaluationRequest
     * @see BulkAccessEvaluationResponse
     */
    BulkAccessEvaluationResponse bulkEvaluate(BulkAccessEvaluationRequest bulkAccessEvaluationRequest)
            throws AccessEvaluationException;
}
