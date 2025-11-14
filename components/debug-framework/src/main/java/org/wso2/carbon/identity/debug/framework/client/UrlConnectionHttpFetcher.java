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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Default HttpFetcher implementation using HttpURLConnection.
 */
public class UrlConnectionHttpFetcher implements HttpFetcher {

    private static final Log LOG = LogFactory.getLog(UrlConnectionHttpFetcher.class);

    @Override
    public Map<String, Object> getJson(String urlStr, Map<String, String> headers) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Accept", "application/json");
            if (headers != null) {
                for (Map.Entry<String, String> e : headers.entrySet()) {
                    connection.setRequestProperty(e.getKey(), e.getValue());
                }
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                ObjectMapper mapper = new ObjectMapper();
                try (InputStream in = connection.getInputStream()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> claims = mapper.readValue(in, Map.class);
                    return claims != null ? claims : new HashMap<>();
                }
            } else {
                LOG.debug("Non-200 response from " + urlStr + ": " + responseCode);
            }
        } catch (Exception e) {
            LOG.debug("Error fetching JSON from " + urlStr, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new HashMap<>();
    }
}
