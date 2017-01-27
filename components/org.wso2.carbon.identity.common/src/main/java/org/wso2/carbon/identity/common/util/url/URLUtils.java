///*
// * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.wso2.carbon.identity.common.util.url;
//
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.wso2.carbon.identity.common.base.exception.IdentityRuntimeException;
//
//import java.io.UnsupportedEncodingException;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.util.Map;
//
///**
// * Url utils.
// */
//public class URLUtils {
//
//    private static final Logger logger = LoggerFactory.getLogger(URLUtils.class);
//
//    private static volatile URLUtils instance = null;
//
//    private URLUtils() {
//
//    }
//
//    public static URLUtils getInstance() {
//        if (instance == null) {
//            synchronized (URLUtils.class) {
//                if (instance == null) {
//                    instance = new URLUtils();
//                }
//            }
//        }
//        return instance;
//    }
//
//    public String getServerURL(String endpoint, boolean addProxyContextPath, boolean addWebContextRoot)
//            throws IdentityRuntimeException {
//
//        // check implementation in C4
//        // Need to consider hostname, mgt-transport port, proxy context path and web context path
//        return null;
//    }
//
//    /**
//     * Replace the placeholders with the related values in the URL.
//     *
//     * @param urlWithPlaceholders URL with the placeholders.
//     * @return URL filled with the placeholder values.
//     */
//    public static String fillURLPlaceholders(String urlWithPlaceholders) {
//
//        // supported placeholders in C4 we host, carbon protocol, carbon https port, http port, proxy context path,
// web
//        // context root, carbon web context, carbon context
//        return null;
//    }
//
//    /**
//     * Get the host name of the server.
//     *
//     * @return Hostname
//     */
//    public String getHostName() {
//        return null;
//    }
//
//    public String buildQueryString(Map<String, String[]> parameterMap) throws UnsupportedEncodingException {
//
//        StringBuilder queryString = new StringBuilder("?");
//        boolean isFirst = true;
//        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
//            for (String paramValue : entry.getValue()) {
//                if (isFirst) {
//                    isFirst = false;
//                } else {
//                    queryString.append("&");
//                }
//                queryString.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()));
//                queryString.append("=");
//                queryString.append(URLEncoder.encode(paramValue, StandardCharsets.UTF_8.name()));
//
//            }
//        }
//        return queryString.toString();
//    }
//
//    /**
//     * Get client IP address from the http request
//     *
//     * @param request http servlet request
//     * @return IP address of the initial client
//     */
////    public static String getClientIpAddress(HttpServletRequest request) {
////        for (String header : IdentityConstants.HEADERS_WITH_IP) {
////            String ip = request.getHeader(header);
////            if (ip != null && ip.length() != 0 && !IdentityConstants.UNKNOWN.equalsIgnoreCase(ip)) {
////                return getFirstIP(ip);
////            }
////        }
////        return request.getRemoteAddr();
////    }
//
//    /**
//     * Get the first IP from a comma separated list of IPs.
//     *
//     * @param commaSeparatedIPs String which contains comma+space separated IPs
//     * @return First IP
//     */
//    public String getFirstIP(String commaSeparatedIPs) {
//        if (StringUtils.isNotEmpty(commaSeparatedIPs) && commaSeparatedIPs.contains(",")) {
//            return commaSeparatedIPs.split(",")[0];
//        }
//        return commaSeparatedIPs;
//    }
//}
