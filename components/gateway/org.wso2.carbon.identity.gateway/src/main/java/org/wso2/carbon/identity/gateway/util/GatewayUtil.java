/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.gateway.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.framework.FrameworkException;
import org.wso2.carbon.identity.gateway.context.GatewayMessageContext;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GatewayUtil {


    public static GatewayMessageContext mergeContext(GatewayMessageContext oldContext,
                                                     GatewayMessageContext newContext) {

        // Copy the data from old context
        newContext.setSessionDataKey(oldContext.getSessionDataKey());
        newContext.setInitialIdentityRequest(oldContext.getInitialIdentityRequest());
        newContext.addParameters(oldContext.getParameters());

        newContext.setFinalResponseBuilder(oldContext.getFinalResponseBuilder());
        // restore current state from the old context.
        newContext.setCurrentHandler(oldContext.getCurrentHandler());
        newContext.setHandlerResponseStatus(oldContext.getHandlerResponseStatus());

        return newContext;
    }


    public static Map<String, String> getQueryParamMap(String requestUri) {

        if (Optional.ofNullable(requestUri).isPresent()) {
            Map<String, String> queryMap = new HashMap<>();
            return splitQuery(
                    Arrays.stream(requestUri.split("\\?"))
                            .skip(1)
                            .findFirst()
                            .orElse(null)
            );
        }

        return Collections.emptyMap();
    }


    public static Map<String, String> splitQuery(String queryString) {

        if (Optional.ofNullable(queryString).isPresent()) {
            Map<String, String> queryMap = new HashMap<>();
            Arrays.stream(queryString.split("&"))
                    .map(GatewayUtil::splitQueryParameter)
                    .filter(x -> x.getKey() != null && x.getValue() != null)
                    .forEach(x -> queryMap.put(x.getKey(), x.getValue()));

            return queryMap;
        }

        return Collections.emptyMap();
    }

    private static SimpleEntry<String, String> splitQueryParameter(String queryPairString) {

        int idx = queryPairString.indexOf("=");
        String key = idx > 0 ? queryPairString.substring(0, idx) : queryPairString;
        String value = idx > 0 && queryPairString.length() > idx + 1 ? queryPairString.substring(idx + 1) : null;
        return new SimpleEntry<>(urlDecode(key), urlDecode(value));
    }


    public static String urlDecode(final String encoded) {

        try {
            return encoded == null ? null : URLDecoder.decode(encoded, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("Impossible: UTF-8 is a required encoding", e);
        }
    }

    public static String getSessionDataKeyFromContext(GatewayMessageContext gatewayMessageContext) throws
            FrameworkException {
        String sessionID = gatewayMessageContext.getSessionDataKey();
        if (StringUtils.isBlank(sessionID)) {
            throw new FrameworkException("SessionDataKey not found in the gateway message context.");
        }
        return sessionID;
    }
}
