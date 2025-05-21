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
 * The {@code AuthorizationSubject} class represents a subject object in an Evaluation request. The subject here refers
 * to the entity that requires access to a resource in an Access Evaluation request.
 */
public class AuthorizationSubject {

    private String subjectType;
    private String subjectId;
    private Map<String, Object> properties;

    /**
     * Constructs an {@code AuthorizationSubject} object with the subject type and subject id.
     *
     * @param subjectType The type of the subject.
     * @param subjectId   The id of the subject.
     */
    public AuthorizationSubject(String subjectType, String subjectId) {

        this.subjectType = subjectType;
        this.subjectId = subjectId;
    }

    /**
     * Constructs an {@code AuthorizationSubject} object with the subject type for search requests.
     *
     * @param subjectType The type of the subject.
     */
    public AuthorizationSubject(String subjectType) {

        this.subjectType = subjectType;
    }

    /**
     * Returns the type of the subject.
     * @return The type of the subject.
     */
    public String getSubjectType() {
        return subjectType;
    }

    /**
     * Returns the id of the subject.
     * @return The id of the subject.
     */
    public String getSubjectId() {
        return subjectId;
    }

    /**
     * Returns the properties of the subject.
     * @return The properties of the subject.
     */
    public Map<String, Object> getProperties() {

        return properties;
    }

    /**
     * Sets the properties of the subject.
     * @param properties The properties of the subject.
     */
    public void setProperties(Map<String, Object> properties) {

        this.properties = properties;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AuthorizationSubject that = (AuthorizationSubject) obj;
        if (properties == null && that.properties == null) {
            return true;
        }
        if (properties == null || that.properties == null) {
            return false;
        }
        return subjectType.equals(that.subjectType) &&
                subjectId.equals(that.subjectId) &&
                properties.equals(that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subjectType, subjectId, properties);
    }
}
