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

package org.wso2.carbon.identity.core.context.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * This class models the Request.
 * Request is the entity that represents headers and parameters sent in the request.
 */
public class Request {

    private final List<Header> headers = new ArrayList<>();
    private final String ipAddress;

    private Request(Builder builder) {

        this.headers.addAll(builder.headers);
        this.ipAddress = builder.ipAddress;
    }

    public List<Header> getHeaders() {

        return Collections.unmodifiableList(headers);
    }

    public String getIpAddress() {

        return ipAddress;
    }

    /**
     * Builder for the Request.
     */
    public static class Builder {

        private final List<Header> headers = new ArrayList<>();
        private String ipAddress;

        public Builder fromHttpRequest(HttpServletRequest request) {

            if (request != null) {
                resolveHeaders(request);
                this.ipAddress = request.getRemoteAddr();
            }

            return this;
        }

        public Request build() {

            return new Request(this);
        }

        private void resolveHeaders(HttpServletRequest request) {

            Enumeration headerNames = request.getHeaderNames();
            if (headerNames == null) {
                return;
            }

            while (headerNames.hasMoreElements()) {
                String headerName = (String) headerNames.nextElement();
                Enumeration<String> headerValues = request.getHeaders(headerName);
                if (headerValues == null) {
                    continue;
                }

                List<String> values = Collections.list(headerValues);
                this.headers.add(new Header(headerName, values));
            }
        }
    }
}
