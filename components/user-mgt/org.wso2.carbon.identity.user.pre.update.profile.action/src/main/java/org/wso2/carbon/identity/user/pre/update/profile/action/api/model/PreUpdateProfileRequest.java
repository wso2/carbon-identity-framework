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

package org.wso2.carbon.identity.user.pre.update.profile.action.api.model;

import org.wso2.carbon.identity.action.execution.api.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This class models the request at a pre update profile trigger.
 * PreUpdateProfileRequest is the entity that represents the request that is sent to Action
 * over {@link ActionExecutionRequest}.
 */
public class PreUpdateProfileRequest extends Request {

    private final List<UserClaim> claims;

    private PreUpdateProfileRequest(Builder builder) {

        this.claims = builder.claims;
        this.additionalHeaders = builder.additionalHeaders;
        this.additionalParams = builder.additionalParams;
    }

    public List<UserClaim> getClaims() {

        return claims;
    }

    /**
     * Builder for PreUpdateProfileRequest.
     */
    public static class Builder {

        private final List<Header> additionalHeaders = new ArrayList<>();
        private final List<Param> additionalParams = new ArrayList<>();
        private final List<UserClaim> claims = new ArrayList<>();

        public Builder addAdditionalHeader(String name, String[] value) {

            this.additionalHeaders.add(new Header(name, value));
            return this;
        }

        public Builder addAdditionalParam(String name, String[] value) {

            this.additionalParams.add(new Param(name, value));
            return this;
        }

        public Builder claims(List<UserClaim> claims) {

            this.claims = claims;
            return this;
        }

        public PreUpdateProfileRequest build() {

            return new PreUpdateProfileRequest(this);
        }
    }
}
