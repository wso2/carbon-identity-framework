/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.gateway.resource.util;


import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.util.BufferUtil;

import javax.ws.rs.core.MediaType;

import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.io.UnsupportedEncodingException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Resource level Utility.
 */
public class Utils {


    public static void processParameters(Request request) {

        Map<String, String> queryParams = getRequestParameters(request);
        request.setProperty(org.wso2.carbon.identity.gateway.common.util.Constants.QUERY_PARAMETERS, queryParams);
        String body = readRequestBody(request);
        request.setProperty(org.wso2.carbon.identity.gateway.common.util.Constants.REQUEST_BODY, body);

        if (isFormParamRequest(request.getContentType())) {
            try {
                handleFormParams(body, request);
            } catch (UnsupportedEncodingException e) {
                throw new GatewayRuntimeException("Error while building request body");
            }
        } else {
            request.setProperty(org.wso2.carbon.identity.gateway.common.util.Constants.BODY_PARAMETERS,
                    new HashMap<>());
        }
    }

    private static String readRequestBody(Request msf4jRequest) {

        ByteBuffer merge = BufferUtil.merge(msf4jRequest.getFullMessageBody());
        return Charset.defaultCharset().decode(merge).toString();
    }

    private static Map<String, String> getRequestParameters(Request request) {
        return getQueryParamMap(request.getUri());
    }

    public static Map<String, String> getQueryParamMap(String requestUri) {

        if (Optional.ofNullable(requestUri).isPresent()) {
            return splitQuery(
                    Arrays.stream(requestUri.split("\\?"))
                            .skip(1)
                            .findFirst()
                            .orElse(null)
            );
        }

        return Collections.emptyMap();
    }

    private static void handleFormParams(String requestBody, Request request)
            throws UnsupportedEncodingException {

        Map<String, String> bodyParams = new HashMap<String, String>();
        splitQuery(requestBody).forEach(bodyParams::put);
        request.setProperty(org.wso2.carbon.identity.gateway.common.util.Constants.BODY_PARAMETERS, bodyParams);
    }

    private static Map<String, String> splitQuery(String queryString) {

        if (Optional.ofNullable(queryString).isPresent()) {
            Map<String, String> queryMap = new HashMap<>();
            Arrays.stream(queryString.split("&"))
                    .map(Utils::splitQueryParameter)
                    .filter(x -> x.getKey() != null && x.getValue() != null)
                    .forEach(x -> queryMap.put(x.getKey(), x.getValue()));

            return queryMap;
        }

        return Collections.emptyMap();
    }


    private static AbstractMap.SimpleEntry<String, String> splitQueryParameter(String queryPairString) {

        int idx = queryPairString.indexOf("=");
        String key = idx > 0 ? queryPairString.substring(0, idx) : queryPairString;
        String value = idx > 0 && queryPairString.length() > idx + 1 ? queryPairString.substring(idx + 1) : null;
        return new AbstractMap.SimpleEntry<String, String>(urlDecode(key), urlDecode(value));
    }

    private static String urlDecode(final String encoded) {

        try {
            return encoded == null ? null : URLDecoder.decode(encoded, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("Impossible: UTF-8 is a required encoding", e);
        }
    }

    private static boolean isFormParamRequest(String contentType) {
        return MediaType.APPLICATION_FORM_URLENCODED.equalsIgnoreCase(contentType);
    }

}
