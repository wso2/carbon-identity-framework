/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.client;

import java.util.Map;

/**
 * Abstraction for fetching JSON over HTTP. Implementations may use URLConnection, HttpClient, or be mocked in tests.
 */
public interface HttpFetcher {

    /**
     * Perform an HTTP GET and parse the response body as a JSON object into a Map.
     * Returns an empty map on non-200 responses or errors.
     */
    Map<String, Object> getJson(String url, Map<String, String> headers);
}
