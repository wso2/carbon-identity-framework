/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.rule.metadata.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a link in the response of a field of options reference type.
 */
public class Link {

    private final String href;
    private final String method;
    private final String rel;

    @JsonCreator
    public Link(@JsonProperty("href") String href, @JsonProperty("method") String method,
                @JsonProperty("rel") String rel) {

        validate(href, method, rel);
        this.href = href;
        this.method = method;
        this.rel = rel;
    }

    public String getHref() {

        return href;
    }

    public String getMethod() {

        return method;
    }

    public String getRel() {

        return rel;
    }

    private void validate(String href, String method, String rel) {

        if (href == null || href.isEmpty()) {
            throw new IllegalArgumentException("Link 'href' cannot be null or empty.");
        }

        if (method == null || method.isEmpty()) {
            throw new IllegalArgumentException("Link 'method' cannot be null or empty.");
        }

        if (rel == null || rel.isEmpty()) {
            throw new IllegalArgumentException("Link 'rel' cannot be null or empty.");
        }
    }
}
