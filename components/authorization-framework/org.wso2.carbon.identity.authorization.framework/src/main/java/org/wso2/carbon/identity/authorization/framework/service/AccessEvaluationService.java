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
import org.wso2.carbon.identity.authorization.framework.model.SearchActionsRequest;
import org.wso2.carbon.identity.authorization.framework.model.SearchActionsResponse;
import org.wso2.carbon.identity.authorization.framework.model.SearchResourcesRequest;
import org.wso2.carbon.identity.authorization.framework.model.SearchResourcesResponse;
import org.wso2.carbon.identity.authorization.framework.model.SearchSubjectsRequest;
import org.wso2.carbon.identity.authorization.framework.model.SearchSubjectsResponse;

/**
 * The {@code AccessEvaluationService} interface provides method definitions for Access Evaluation.
 * <p>
 *     Implementations of this interface should provide the functionalities to evaluate access based on the request
 *     type, whether it is an evaluation request or a search request. Implementations can be specific to a particular
 *     Authorization Engine and use the engine's API to perform the functionalities. Note that the request models
 *     and response models given and returned are generic so if implementing, you may need to convert them to the
 *     specific models of the Authorization Engine you are using.
 * </p>
 */
public interface AccessEvaluationService {

    /**
     * Returns a unique id of an Authorization Engine used for evaluating authorization.
     * <p>
     *     This method should return the unique id of the Authorization Engine which is used to evaluate authorization,
     *     which will be used to identify the engine in a service-oriented architecture. The name should be unique to
     *     the engine.
     * </p>
     * @implNote Please ensure that the id returned is unique to the engine.
     * @return The unique id of the Authorization Engine.
     */
    String getEngine();

    /**
     * Evaluates authorization when a new single Access Evaluation request is received.
     * <p>
     *   This method should be used when a single Access Evaluation request is received. The method should check
     *   the authorization from the Authorization Engine and return the decision given by the engine. Exceptions should
     *   be thrown if an error occurs while checking authorization, whether it is a configuration error or an error from
     *   the Authorization Engine.
     * </p>
     * @param accessEvaluationRequest The request which contains the necessary information to evaluate authorization.
     * @return The evaluation decision from the Authorization Engine.
     * @throws AccessEvaluationException If an error occurs while evaluating authorization.
     * @see AccessEvaluationRequest
     * @see AccessEvaluationResponse
     */
    AccessEvaluationResponse evaluate(AccessEvaluationRequest accessEvaluationRequest) throws AccessEvaluationException;

    /**
     * Evaluates authorization when a new Bulk Access Evaluation request is received with multiple requests.
     * <p>
     *     This method should be used when a bulk Access Evaluation request is received. The method should evaluate
     *     authorization for each request in the bulk request and return the decisions given by the Authorization Engine
     *     for each request. Exceptions should be thrown if an error occurs while evaluating, whether it is a
     *     configuration error or an error from the Authorization Engine. Errors occurring for requests inside the bulk
     *     request should be returned in the response itself.
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

    /**
     * Searches and returns the resources based on the given criteria in the request.
     * <p>
     *     This method should be used when a search resource request is received. The resource here refers to an entity
     *     that is protected by the authorization rules. The method should search for resources a subject (such as a
     *     user or service) is authorized to access or perform specific actions on. The criteria provided in the
     *     request typically include resource type, subject attributes, the desired action, and optionally contextual
     *     or environmental conditions. Exceptions should be thrown if an error occurs while searching, whether it is a
     *     configuration error or an error from the Authorization Engine.
     * </p>
     * @param searchResourcesRequest The request which contains the necessary information to search for resources.
     * @return The search results containing the resources that match the criteria.
     * @throws AccessEvaluationException If an error occurs while searching for resources.
     * @see SearchResourcesRequest
     * @see SearchResourcesResponse
     */
    SearchResourcesResponse searchResources(SearchResourcesRequest searchResourcesRequest)
            throws AccessEvaluationException;

    /**
     * Searches and returns the subjects based on the given criteria in the request.
     * <p>
     *     This method should be used when a search subject request is received. The subject here refers to an entity
     *     that requests access to a resource to perform actions on it. The method should search for subjects that are
     *     authorized to perform specific actions on resources. The criteria provided in the request typically include
     *     subject type, resource attributes, the desired action, and optionally contextual or environmental
     *     conditions. Exceptions should be thrown if an error occurs while searching, whether it is a configuration
     *     error or an error from the Authorization Engine.
     * </p>
     * @param searchSubjectsRequest The request which contains the necessary information to search for subjects.
     * @return The search results containing the subjects that match the criteria.
     * @throws AccessEvaluationException If an error occurs while searching for subjects.
     * @see SearchSubjectsRequest
     * @see SearchSubjectsResponse
     */
    SearchSubjectsResponse searchSubjects(SearchSubjectsRequest searchSubjectsRequest)
            throws AccessEvaluationException;

    /**
     * Searches and returns the actions based on the given criteria in the request.
     * <p>
     *     This method should be used when a search action request is received. The action here refers to an operation
     *     that can be performed on a resource. The method should search for actions that are authorized for a specific
     *     subject on a specific resource. The criteria provided in the request typically include resource attributes,
     *     subject attributes and optionally contextual or environmental conditions. Exceptions should be thrown if an
     *     error occurs while searching, whether it is a configuration error or an error from the Authorization Engine.
     * @param searchActionsRequest The request which contains the necessary information to search for actions.
     * @return The search results containing the actions that match the criteria.
     * @throws AccessEvaluationException If an error occurs while searching for actions.
     * @see SearchActionsRequest
     * @see SearchResourcesResponse
     */
    SearchActionsResponse searchActions(SearchActionsRequest searchActionsRequest)
            throws AccessEvaluationException;
}
