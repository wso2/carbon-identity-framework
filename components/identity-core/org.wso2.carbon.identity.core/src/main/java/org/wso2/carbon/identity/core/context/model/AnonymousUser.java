/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.core.context.model;

/**
 * Holds the details of the anonymous user of the current flow, that is, the user before the server
 * knows who the user is. For example, the user of a self registration flow.
 */
public class AnonymousUser {

    private final String anonymousProfileTracker;

    private AnonymousUser(Builder builder) {

        this.anonymousProfileTracker = builder.anonymousProfileTracker;
    }

    /**
     * Returns the anonymous profile tracker of the user. It correlates the anonymous profile of the user
     * (for example, in an external profile store) with the user that the flow creates.
     * The client of the flow supplies this value.
     *
     * @return The anonymous profile tracker, or null if the flow has none.
     */
    public String getAnonymousProfileTracker() {

        return anonymousProfileTracker;
    }

    /**
     * Builder for the AnonymousUser.
     */
    public static class Builder {

        private String anonymousProfileTracker;

        public Builder anonymousProfileTracker(String anonymousProfileTracker) {

            this.anonymousProfileTracker = anonymousProfileTracker;
            return this;
        }

        public AnonymousUser build() {

            return new AnonymousUser(this);
        }
    }
}
