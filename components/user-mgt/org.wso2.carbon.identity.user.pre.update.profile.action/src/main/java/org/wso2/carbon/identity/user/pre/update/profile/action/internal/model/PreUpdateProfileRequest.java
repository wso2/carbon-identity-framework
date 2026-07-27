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

package org.wso2.carbon.identity.user.pre.update.profile.action.internal.model;

import org.wso2.carbon.identity.action.execution.api.model.Request;
import org.wso2.carbon.identity.action.execution.api.model.UserClaim;

import java.util.ArrayList;
import java.util.List;

/**
 * Request to represent the pre update profile request in the pre update profile event.
 */
public class PreUpdateProfileRequest extends Request {

    private final List<UserClaim> claims;

    private PreUpdateProfileRequest(Builder builder) {

        this.claims = builder.claims;
    }

    public List<UserClaim> getClaims() {

        return claims;
    }

    /**
     * Builder for the PreUpdateProfileRequest.
     */
    public static class Builder {

        private final List<UserClaim> claims = new ArrayList<>();

        public Builder addClaim(String uri, String value) {

            this.claims.add(new UserClaim(uri, value));
            return this;
        }

        public Builder addClaim(String uri, String[] value) {

            this.claims.add(new UserClaim(uri, value));
            return this;
        }

        public PreUpdateProfileRequest build() {

            return new PreUpdateProfileRequest(this);
        }
    }
}
