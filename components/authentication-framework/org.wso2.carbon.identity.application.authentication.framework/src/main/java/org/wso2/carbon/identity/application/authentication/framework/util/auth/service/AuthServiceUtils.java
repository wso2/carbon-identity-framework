/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.util.auth.service;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.authentication.framework.exception.auth.service.AuthServiceException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils.UTF_8;

/**
 * Utility class for Auth Service.
 */
public class AuthServiceUtils {

    /**
     * Extract query params from the provided url.
     *
     * @param url Url to extract query params.
     * @return Map of query params.
     * @throws AuthServiceException if error occurred while extracting query params.
     */
    public static Map<String, String> extractQueryParams(String url) throws AuthServiceException {

        Map<String, String> queryParams = new HashMap<>();

        if (StringUtils.isBlank(url)) {
            return queryParams;
        }
        try {
            URI uri = new URI(url);
            String query = uri.getQuery();
            if (StringUtils.isNotBlank(query)) {
                String[] pairs = query.split(FrameworkUtils.QUERY_SEPARATOR);
                for (String pair : pairs) {
                    int idx = pair.indexOf(FrameworkUtils.EQUAL);
                    queryParams.put(URLDecoder.decode(pair.substring(0, idx), UTF_8),
                            URLDecoder.decode(pair.substring(idx + 1), UTF_8));
                }
            }
        } catch (URISyntaxException | UnsupportedEncodingException e) {
            throw new AuthServiceException(AuthServiceConstants.ErrorMessage.ERROR_UNABLE_TO_PROCEED.code(),
                    "Error while extracting query params from provided url.", e);
        }
        return queryParams;
    }
}
