/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.inbound;

import java.io.Serializable;

/**
 * Identity response.
 */
public class IdentityResponse implements Serializable {

    private static final long serialVersionUID = 1348704275109461974L;

    protected IdentityMessageContext context;

    protected IdentityResponse(IdentityResponseBuilder builder) {
        this.context = builder.context;
    }

    /**
     * Identity response builder.
     */
    public static class IdentityResponseBuilder {

        protected IdentityMessageContext context;

        public IdentityResponseBuilder(IdentityMessageContext context) {
            this.context = context;
        }

        public IdentityResponseBuilder() {

        }

        public IdentityResponse build() {
            return new IdentityResponse(this);
        }
    }

    /**
     * Identity response constants.
     */
    public static class IdentityResponseConstants {

    }
}
