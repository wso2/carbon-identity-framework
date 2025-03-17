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

package org.wso2.carbon.identity.authorization.framework.model;

import java.util.Map;
import java.util.Objects;

/**
 * The {@code AccessEvaluationRequest} class is a model for an Access Evaluation request body which contains the
 * information required to evaluate authorization from an Authorization Engine.
 * This follows the AuthZEN Evaluation request format.
 */
public class AccessEvaluationRequest {

    private final AuthorizationResource resource;
    private final AuthorizationAction action;
    private final AuthorizationSubject subject;
    private Map<String, Object> context;

    /**
     * Constructs an {@code AccessEvaluationRequest} object with the subject, action and resource.
     *
     * @param subject  The subject that needs access.
     * @param action   The action that subject requires to perform.
     * @param resource The resource that subject requires to perform the action on.
     * @see AuthorizationSubject
     * @see AuthorizationAction
     * @see AuthorizationResource
     */
    public AccessEvaluationRequest(AuthorizationSubject subject, AuthorizationAction action,
                                   AuthorizationResource resource) {

        this.resource = resource;
        this.action = action;
        this.subject = subject;
    }

    /**
     * returns the subject of the access evaluation request.
     * @return the {@link AuthorizationSubject} object.
     */
    public AuthorizationSubject getSubject() {

        return subject;
    }

    /**
     * returns the resource of the access evaluation request.
     * @return the {@link AuthorizationResource} object.
     */
    public AuthorizationResource getResource() {

        return resource;
    }

    /**
     * returns the action of the access evaluation request.
     * @return the {@link AuthorizationAction} object.
     */
    public AuthorizationAction getActionObject() {

        return action;
    }

    /**
     * returns additional context of the access evaluation request.
     * @return the additional context as a {@link Map} object.
     */
    public Map<String, Object> getContext() {

        return context;
    }

    /**
     * sets the additional context of the access evaluation request.
     * @param context the additional context as a {@link Map} object.
     */
    public void setContext(Map<String, Object> context) {

        this.context = context;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AccessEvaluationRequest that = (AccessEvaluationRequest) obj;
        if (context == null && that.context == null) {
            return true;
        }
        if (context == null || that.context == null) {
            return false;
        }
        return Objects.equals(resource, that.resource) &&
                Objects.equals(action, that.action) &&
                Objects.equals(subject, that.subject) &&
                Objects.equals(context, that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resource, action, subject, context);
    }
}
