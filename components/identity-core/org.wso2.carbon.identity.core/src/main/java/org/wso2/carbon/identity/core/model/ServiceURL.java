/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.core.model;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Service URL representation.
 */
public class ServiceURL {

    private String protocol;
    private String hostName;
    private int port;
    private String urlPath;
    private Map<String, String> parameters;
    private String fragment;
    private String resolvedParamsString;
    private String absoluteUrl;
    private String relativeUrl;

    public ServiceURL(String protocol, String hostName, int port, String urlPath, Map<String, String> parameters,
                      String fragment) throws URLBuilderException {

        this.protocol = protocol;
        this.hostName = hostName;
        this.port = port;
        this.urlPath = urlPath;
        this.parameters = parameters;
        this.fragment = fragment;
        setParamsString();
        this.absoluteUrl = getAbsoluteUrl();
        this.relativeUrl = getRelativeUrl();
    }

    /**
     * This method is called to get the protocol of {@link ServiceURL}.
     *
     * @return String of the protocol.
     */
    public String getProtocol() {

        return protocol;
    }

    /**
     * This method is called to get the host name of {@link ServiceURL}.
     *
     * @return String of the host name.
     */
    public String getHostName() {

        if (hostName.endsWith("/")) {
            hostName = hostName.substring(0, hostName.length() - 1);
        }
        return hostName;
    }

    /**
     * This method is called to get the port of {@link ServiceURL}.
     *
     * @return value of the port.
     */
    public int getPort() {

        return port;
    }

    /**
     * This method is called to get the Url path of {@link ServiceURL}.
     *
     * @return String of the url path.
     */
    public String getUrlPath() {

        return urlPath;
    }

    /**
     * This method is called to get the parameter value when the key is provided.
     *
     * @param key Key of the parameter.
     * @return The value of the parameter.
     */
    public String getParameter(String key) {

        return parameters.get(key);
    }

    /**
     * This method is called to get a list of the parameter names.
     *
     * @return ArrayList of parameter keys.
     */
    public String[] getParameterKeys() {

        ArrayList<String> parameterArrayList = new ArrayList<>();

        for (Map.Entry<String, String> entry : this.parameters.entrySet()) {
            parameterArrayList.add(entry.getKey());
        }
        return parameterArrayList.toArray(new String[0]);
    }

    /**
     * This method is called to get the decoded fragment from the url.
     *
     * @return The decoded fragment.
     * @throws URLBuilderException
     */
    public String getFragment() {

        return fragment;
    }

    /**
     * Concatenate the protocol, host name, port, proxy context path, web context root and url context to return the
     * absolute URL.
     *
     * @return The absolute URL from the {@link ServiceURL} instance.
     */
    public String getAbsoluteURL() {

        return this.absoluteUrl;
    }

    /**
     * This method is called to get the relative url from the url context.
     *
     * @return The relative URL from the {@link ServiceURL} instance.
     */
    public String getRelativeURL() {

        return this.relativeUrl;
    }

    private void appendContextToUri(StringBuilder serverUrl, String contextPath) {

        if (StringUtils.isNotBlank(contextPath)) {
            if (contextPath.endsWith("/")) {
                contextPath = contextPath.substring(0, contextPath.length() - 1);
            }
            if (StringUtils.isNotBlank(contextPath) && contextPath.trim().charAt(0) != '/') {
                serverUrl.append("/").append(contextPath.trim());
            } else {
                serverUrl.append(contextPath.trim());
            }
        }
    }

    private void appendParamsToUri(StringBuilder serverUrl, String resolvedParamsString, String delimiter)
            throws URLBuilderException {

        if (serverUrl.length() > 0 && serverUrl.charAt(serverUrl.length() - 1) == '/') {
            serverUrl.setLength(serverUrl.length() - 1);
        }
        if (StringUtils.isNotBlank(resolvedParamsString)) {
            try {
                serverUrl.append(delimiter).append(URLEncoder.encode(resolvedParamsString,
                        StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new URLBuilderException("Error while trying to build the url", e);
            }
        }
    }

    private String getResolvedParamString(Map<String, String> parameters) {

        StringJoiner joiner = new StringJoiner("&");
        if (MapUtils.isNotEmpty(parameters)) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                StringBuilder paramBuilder = new StringBuilder();
                paramBuilder.append(entry.getKey()).append("=").append(entry.getValue());
                joiner.add(paramBuilder.toString());
            }
        }
        return joiner.toString();
    }

    private Map<String, String> getParameters() {

        return parameters;
    }

    private void setParamsString() {

        resolvedParamsString = getResolvedParamString(parameters);
    }

    private String getAbsoluteUrl() throws URLBuilderException {

        StringBuilder absoluteUrl = new StringBuilder();
        absoluteUrl.append(getProtocol()).append("://");

        String hostName = getHostName();
        absoluteUrl.append(hostName.toLowerCase());

        int port = getPort();
        // If it's well known HTTPS port, skip adding port.
        if (port != IdentityCoreConstants.DEFAULT_HTTPS_PORT) {
            absoluteUrl.append(":").append(port);
        }

        String urlContext = getUrlPath();
        if (StringUtils.isNotBlank(urlContext)) {
            if (urlContext.trim().charAt(0) != '/' && urlContext.trim().charAt(0) != '?') {
                absoluteUrl.append("/").append(urlContext.trim());
            } else {
                absoluteUrl.append(urlContext.trim());
            }
        }
        appendParamsToUri(absoluteUrl, resolvedParamsString, "?");
        appendParamsToUri(absoluteUrl, fragment, "#");
        return absoluteUrl.toString();
    }

    private String getRelativeUrl() throws URLBuilderException {

        StringBuilder relativeUrl = new StringBuilder();
        String urlContext = getUrlPath();
        appendContextToUri(relativeUrl, urlContext);
        appendParamsToUri(relativeUrl, resolvedParamsString, "?");
        appendParamsToUri(relativeUrl, fragment, "#");
        return relativeUrl.toString();
    }
}
