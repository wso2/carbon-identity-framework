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

package org.wso2.carbon.identity.event.publisher.api.model.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Model Class Implementation for ComplexSubject.
 */
public class ComplexSubject extends Subject {

    private static final String COMPLEX = "complex";

    private ComplexSubject() {

    }

    private ComplexSubject(Builder builder) {

        setFormat(COMPLEX);
        setProperties(Collections.unmodifiableMap(builder.subjectMap));
    }

    public static Builder builder() {

        return new Builder();
    }

    /**
     * Builder class for ComplexSubject.
     */
    public static class Builder {

        private static final String TENANT = "tenant";
        private static final String USER = "user";
        private static final String SESSION = "session";
        private static final String APPLICATION = "application";
        private static final String ORG_UNIT = "org_unit";
        private static final String GROUP = "group";
        private final Map<String, SimpleSubject> subjectMap = new HashMap<>();

        private Builder() {

        }

        public Builder tenant(SimpleSubject subject) {

            subjectMap.put(TENANT, subject);
            return this;
        }

        public Builder user(SimpleSubject subject) {

            subjectMap.put(USER, subject);
            return this;
        }

        public Builder session(SimpleSubject subject) {

            subjectMap.put(SESSION, subject);
            return this;
        }

        public Builder application(SimpleSubject subject) {

            subjectMap.put(APPLICATION, subject);
            return this;
        }

        public Builder organization(SimpleSubject subject) {

            subjectMap.put(ORG_UNIT, subject);
            return this;
        }

        public Builder group(SimpleSubject subject) {

            subjectMap.put(GROUP, subject);
            return this;
        }

        public ComplexSubject build() {

            return new ComplexSubject(this);
        }
    }

}
