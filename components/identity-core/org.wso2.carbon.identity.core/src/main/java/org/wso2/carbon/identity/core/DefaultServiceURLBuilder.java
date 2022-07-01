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

package org.wso2.carbon.identity.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceComponent;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.PROXY_CONTEXT_PATH;

/**
 * Implementation for {@link ServiceURLBuilder}.
 * Builder for Service URL instances.
 */
public class DefaultServiceURLBuilder implements ServiceURLBuilder {

    protected String fragment;
    protected String[] urlPaths;
    protected String tenant;
    protected boolean mandateTenantedPath = false;
    protected Map<String, String> parameters = new HashMap<>();
    protected Map<String, String> fragmentParams = new HashMap<>();

    /**
     * Returns {@link ServiceURLBuilder} appended the URL path.
     *
     * @param paths Context paths. Can provide multiple context paths with a comma separated string.
     * @return {@link ServiceURLBuilder}.
     */
    @Override
    public ServiceURLBuilder addPath(String... paths) {

        this.urlPaths = paths;
        return this;
    }

    /**
     * Returns a ServiceURL with the protocol, hostname, port, proxy context path, a web context
     * root and the tenant domain (appended if required).
     *
     * @return {@link ServiceURL}.
     * @throws URLBuilderException If error occurred while constructing the URL.
     */
    @Override
    public ServiceURL build() throws URLBuilderException {

        String protocol = fetchProtocol();
        String proxyHostName = fetchProxyHostName();
        String internalHostName = fetchInternalHostName();
        String authenticationEndpointHostName = fetchAuthenticationEndpointHostName();
        String authenticationEndpointPath = fetchAuthenticationEndpointPath();
        String recoveryEndpointHostName = fetchRecoveryEndpointHostName();
        String recoveryEndpointPath = fetchRecoveryEndpointPath();
        int proxyPort = fetchPort();
        int transportPort = fetchTransportPort();
        String tenantDomain = StringUtils.isNotBlank(tenant) ? tenant : resolveTenantDomain();
        String proxyContextPath = ServerConfiguration.getInstance().getFirstProperty(PROXY_CONTEXT_PATH);
        String resolvedFragment = buildFragment(fragment, fragmentParams);
        String urlPath = getResolvedUrlPath(tenantDomain);
        String relativePublicUrl = fetchRelativePublicUrl(proxyContextPath, urlPath, resolvedFragment);
        String relativeInternalUrl = fetchRelativeInternalUrl(urlPath, resolvedFragment);
        String absoluteInternalUrl = fetchAbsoluteInternalUrl(protocol, internalHostName, transportPort,
                relativeInternalUrl);
        String absolutePublicUrlWithoutURLPath = fetchAbsolutePublicUrlWithoutURLPath(protocol, proxyHostName,
                proxyPort);
        if (StringUtils.isNotBlank(urlPath)) {
            if (authenticationEndpointHostName != null && authenticationEndpointPath != null &&
                    urlPath.contains(authenticationEndpointPath)) {
                absolutePublicUrlWithoutURLPath = fetchAbsolutePublicUrlWithoutURLPath(protocol,
                        authenticationEndpointHostName, proxyPort);
            }
            if (recoveryEndpointHostName != null && recoveryEndpointPath != null &&
                    urlPath.contains(recoveryEndpointPath)) {
                absolutePublicUrlWithoutURLPath = fetchAbsolutePublicUrlWithoutURLPath(protocol,
                        recoveryEndpointHostName, proxyPort);
            }
        }
        String absolutePublicURL = fetchAbsolutePublicUrl(absolutePublicUrlWithoutURLPath, relativePublicUrl);
        return new ServiceURLImpl(protocol, proxyHostName, internalHostName, proxyPort, transportPort, tenantDomain,
                proxyContextPath, urlPath, parameters, resolvedFragment, absolutePublicURL,
                absoluteInternalUrl, relativePublicUrl, relativeInternalUrl, absolutePublicUrlWithoutURLPath);
    }

    protected String getResolvedUrlPath(String tenantDomain) {

        String resolvedUrlContext = buildUrlPath(urlPaths);
        StringBuilder resolvedUrlStringBuilder = new StringBuilder();

        if (IdentityTenantUtil.isTenantQualifiedUrlsEnabled() && !resolvedUrlContext.startsWith("t/")) {
            if (mandateTenantedPath || isNotSuperTenant(tenantDomain)) {
                resolvedUrlStringBuilder.append("/t/").append(tenantDomain);
            }
        }

        if (StringUtils.isNotBlank(resolvedUrlContext)) {
            if (resolvedUrlContext.trim().charAt(0) != '/') {
                resolvedUrlStringBuilder.append("/").append(resolvedUrlContext.trim());
            } else {
                resolvedUrlStringBuilder.append(resolvedUrlContext.trim());
            }
        }

        return resolvedUrlStringBuilder.toString();
    }

    protected boolean isNotSuperTenant(String tenantDomain) {

        return !StringUtils.equals(tenantDomain, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
    }

    /**
     * Returns {@link ServiceURLBuilder} appended with other parameters. Such parameters should be
     * entered as <k,v> pairs.
     *
     * @param key   Key.
     * @param value Value.
     * @return {@link ServiceURLBuilder}.
     */
    @Override
    public ServiceURLBuilder addParameter(String key, String value) {

        parameters.put(key, value);
        return this;
    }

    /**
     * Returns {@link ServiceURLBuilder} appended with a fragment.
     *
     * @param fragment Fragment.
     * @return {@link ServiceURLBuilder}.
     */
    @Override
    public ServiceURLBuilder setFragment(String fragment) {

        this.fragment = fragment;
        return this;
    }

    /**
     * Returns {@link ServiceURLBuilder} appended with parameters. Such parameters should be
     * entered as <k,v> pairs. These parameters will get appended with an "&".
     *
     * @param key   Key.
     * @param value Value.
     * @return {@link ServiceURLBuilder}.
     */
    @Override
    public ServiceURLBuilder addFragmentParameter(String key, String value) {

        fragmentParams.put(key, value);
        return this;
    }

    @Override
    public ServiceURLBuilder setTenant(String tenantDomain) {

        this.tenant = tenantDomain;
        return this;
    }

    @Override
    public ServiceURLBuilder setTenant(String tenantDomain, boolean mandateTenantedPath) {

        this.tenant = tenantDomain;
        this.mandateTenantedPath = mandateTenantedPath;
        return this;
    }

    protected String resolveTenantDomain() {

        String tenantDomain = IdentityTenantUtil.getTenantDomainFromContext();
        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        }
        return tenantDomain;
    }

    protected String buildFragment(String fragment, Map<String, String> fragmentParams) throws URLBuilderException {

        if (StringUtils.isNotBlank(fragment)) {
            return fragment;
        } else {
            return getResolvedParamString(fragmentParams);
        }
    }

    protected String getResolvedParamString(Map<String, String> parameters) throws URLBuilderException {

        StringJoiner joiner = new StringJoiner("&");
        if (MapUtils.isNotEmpty(parameters)) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                StringBuilder paramBuilder = new StringBuilder();
                try {
                    paramBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name())).append("=")
                            .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name()));
                } catch (UnsupportedEncodingException e) {
                    throw new URLBuilderException(String.format("Error while trying to build url. %s is not supported" +
                            ".", StandardCharsets.UTF_8.name()), e);
                }
                joiner.add(paramBuilder.toString());
            }
        }
        return joiner.toString();
    }

    protected String buildUrlPath(String[] urlPaths) {

        StringBuilder urlPathBuilder = new StringBuilder();
        if (ArrayUtils.isNotEmpty(urlPaths)) {
            for (String path : urlPaths) {
                if (StringUtils.isNotBlank(path)) {
                    if (path.endsWith("/")) {
                        path = path.substring(0, path.length() - 1);
                    }
                    if (path.startsWith("/")) {
                        path = path.substring(1);
                    }
                    urlPathBuilder.append(path).append("/");
                }
            }
            if (urlPathBuilder.length() > 0 && urlPathBuilder.charAt(urlPathBuilder.length() - 1) == '/') {
                urlPathBuilder.setLength(urlPathBuilder.length() - 1);
            }
        }
        return urlPathBuilder.toString();
    }

    protected String fetchProtocol() {

        return CarbonUtils.getManagementTransport();
    }

    protected String fetchProxyHostName() throws URLBuilderException {

        String proxyHostName = ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants.HOST_NAME);
        return resolveHostName(proxyHostName);
    }

    protected String fetchInternalHostName() throws URLBuilderException {

        String internalHostName = IdentityUtil.getProperty(IdentityCoreConstants.SERVER_HOST_NAME);
        return resolveHostName(internalHostName);
    }

    protected String resolveHostName(String hostName) throws URLBuilderException {

        try {
            if (StringUtils.isBlank(hostName)) {
                hostName = NetworkUtils.getLocalHostname();
            }
        } catch (SocketException e) {
            throw new URLBuilderException(String.format("Error while trying to resolve the hostname %s from the " +
                    "system", hostName), e);
        }

        if (StringUtils.isNotBlank(hostName) && hostName.endsWith("/")) {
            hostName = hostName.substring(0, hostName.length() - 1);
        }
        return hostName;
    }

    protected int fetchPort() {

        String mgtTransport = CarbonUtils.getManagementTransport();
        AxisConfiguration axisConfiguration = IdentityCoreServiceComponent.getConfigurationContextService().
                getServerConfigContext().getAxisConfiguration();
        int port = CarbonUtils.getTransportProxyPort(axisConfiguration, mgtTransport);
        if (port <= 0) {
            port = CarbonUtils.getTransportPort(axisConfiguration, mgtTransport);
        }
        return port;
    }

    protected int fetchTransportPort() {

        String mgtTransport = CarbonUtils.getManagementTransport();
        AxisConfiguration axisConfiguration = IdentityCoreServiceComponent.getConfigurationContextService().
                getServerConfigContext().getAxisConfiguration();
        return CarbonUtils.getTransportPort(axisConfiguration, mgtTransport);
    }

    protected String fetchAuthenticationEndpointHostName() throws URLBuilderException {

        String authenticationEndpointHostName = IdentityUtil.
                getProperty(IdentityCoreConstants.AUTHENTICATION_ENDPOINT_HOST_NAME);
        if (StringUtils.isNotBlank(authenticationEndpointHostName)) {
            return resolveHostName(authenticationEndpointHostName);
        }
        return null;
    }

    protected String fetchAuthenticationEndpointPath() {

        String authenticationEndpointPath = IdentityUtil
                .getProperty(IdentityCoreConstants.AUTHENTICATION_ENDPOINT_PATH);
        return preprocessEndpointPath(authenticationEndpointPath);
    }

    protected String fetchRecoveryEndpointHostName() throws URLBuilderException {

        String recoveryEndpointHostName = IdentityUtil.
                getProperty(IdentityCoreConstants.RECOVERY_ENDPOINT_HOST_NAME);
        return resolveHostName(recoveryEndpointHostName);
    }

    protected String fetchRecoveryEndpointPath() {

        String recoveryEndpointPath = IdentityUtil
                .getProperty(IdentityCoreConstants.RECOVERY_ENDPOINT_PATH);
        return preprocessEndpointPath(recoveryEndpointPath);
    }

    protected String preprocessEndpointPath(String endpointPath) {

        if (StringUtils.isNotBlank(endpointPath)) {
            if (!endpointPath.startsWith("/")) {
                endpointPath = "/" + endpointPath;
            }
            if (endpointPath.endsWith("/")) {
                endpointPath = endpointPath.
                        substring(0, endpointPath.length() - 1);
            }
            return endpointPath;
        }
        return null;
    }

    protected String fetchAbsolutePublicUrl(String absolutePublicURLWithoutURLPath, String relativePublicURL) {

        StringBuilder absolutePublicUrl = new StringBuilder();
        absolutePublicUrl.append(absolutePublicURLWithoutURLPath);
        absolutePublicUrl.append(relativePublicURL);
        return absolutePublicUrl.toString();
    }

    protected String fetchAbsoluteInternalUrl(String protocol, String internalHostName, int transportPort,
                                              String relativeInternalURL) throws URLBuilderException {

        StringBuilder absoluteInternalUrl = new StringBuilder();
        if (StringUtils.isBlank(protocol)) {
            throw new URLBuilderException("Protocol of service URL is not available.");
        }
        if (StringUtils.isBlank(internalHostName)) {
            throw new URLBuilderException("Internal hostname of service URL is not available.");
        }
        absoluteInternalUrl.append(protocol).append("://");
        absoluteInternalUrl.append(internalHostName.toLowerCase());
        // If it's well known HTTPS port, skip adding port.
        if (transportPort != IdentityCoreConstants.DEFAULT_HTTPS_PORT) {
            absoluteInternalUrl.append(":").append(transportPort);
        }
        absoluteInternalUrl.append(relativeInternalURL);
        return absoluteInternalUrl.toString();
    }

    protected String fetchRelativePublicUrl(String proxyContextPath, String urlPath, String fragment) throws URLBuilderException {

        StringBuilder relativeUrl = new StringBuilder();
        appendContextToUri(relativeUrl, proxyContextPath);
        appendContextToUri(relativeUrl, urlPath);
        String resolvedParamsString = getResolvedParamString(parameters);
        appendParamsToUri(relativeUrl, resolvedParamsString, "?");
        appendParamsToUri(relativeUrl, fragment, "#");
        return relativeUrl.toString();
    }

    protected String fetchAbsolutePublicUrlWithoutURLPath(String protocol, String proxyHostName, int proxyPort) throws URLBuilderException {

        StringBuilder absolutePublicUrlWithoutURLPath = new StringBuilder();
        if (StringUtils.isBlank(protocol)) {
            throw new URLBuilderException("Protocol of service URL is not available.");
        }
        if (StringUtils.isBlank(proxyHostName)) {
            throw new URLBuilderException("Hostname of service URL is not available.");
        }
        absolutePublicUrlWithoutURLPath.append(protocol).append("://");
        absolutePublicUrlWithoutURLPath.append(proxyHostName.toLowerCase());
        // If it's well known HTTPS port, skip adding port.
        if (proxyPort != IdentityCoreConstants.DEFAULT_HTTPS_PORT) {
            absolutePublicUrlWithoutURLPath.append(":").append(proxyPort);
        }
        return absolutePublicUrlWithoutURLPath.toString();
    }

    protected String fetchRelativeInternalUrl(String urlPath, String fragment) throws URLBuilderException {

        StringBuilder relativeUrl = new StringBuilder();
        appendContextToUri(relativeUrl, urlPath);
        String resolvedParamsString = getResolvedParamString(parameters);
        appendParamsToUri(relativeUrl, resolvedParamsString, "?");
        appendParamsToUri(relativeUrl, fragment, "#");
        return relativeUrl.toString();
    }

    protected void appendParamsToUri(StringBuilder serverUrl, String resolvedParamsString, String delimiter)
            throws URLBuilderException {

        if (serverUrl.length() > 0 && serverUrl.charAt(serverUrl.length() - 1) == '/') {
            serverUrl.setLength(serverUrl.length() - 1);
        }
        if (StringUtils.isNotBlank(resolvedParamsString)) {
            serverUrl.append(delimiter).append(resolvedParamsString);
        }
    }

    protected void appendContextToUri(StringBuilder serverUrl, String contextPath) {

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

    protected static class ServiceURLImpl implements ServiceURL {

        private String protocol;
        private String proxyHostName;
        private String internalHostName;
        private int proxyPort;
        private int transportPort;
        private String tenantDomain;
        private String proxyContextPath;
        private String urlPath;
        private Map<String, String> parameters;
        private String fragment;
        private String absolutePublicUrl;
        private String absoluteInternalUrl;
        private String relativePublicUrl;
        private String relativeInternalUrl;
        private String absolutePublicUrlWithoutURLPath;

        public ServiceURLImpl(String protocol, String proxyHostName, String internalHostName, int proxyPort,
                                 int transportPort, String tenantDomain, String proxyContextPath, String urlPath,
                                 Map<String, String> parameters, String fragment, String absolutePublicUrl,
                                 String absoluteInternalUrl, String relativePublicUrl, String relativeInternalUrl,
                                 String absolutePublicUrlWithoutURLPath) {

            this.protocol = protocol;
            this.proxyHostName = proxyHostName;
            this.internalHostName = internalHostName;
            this.proxyPort = proxyPort;
            this.transportPort = transportPort;
            this.tenantDomain = tenantDomain;
            this.proxyContextPath = proxyContextPath;
            this.urlPath = urlPath;
            this.parameters = parameters;
            this.fragment = fragment;
            this.absolutePublicUrl = absolutePublicUrl;
            this.absoluteInternalUrl = absoluteInternalUrl;
            this.relativePublicUrl = relativePublicUrl;
            this.relativeInternalUrl = relativeInternalUrl;
            this.absolutePublicUrlWithoutURLPath = absolutePublicUrlWithoutURLPath;
        }

        /**
         * Returns the protocol of Service URL.
         *
         * @return String of the protocol.
         */
        @Override
        public String getProtocol() {

            return protocol;
        }

        /**
         * Returns the host name of Service URL.
         *
         * @return String of the host name.
         */
        @Override
        public String getProxyHostName() {

            return proxyHostName;
        }

        /**
         * Returns the port of Service URL.
         *
         * @return value of the port.
         */
        @Override
        public int getPort() {

            return proxyPort;
        }

        /**
         * Returns the internal transport port.
         *
         * @return value of the port.
         */
        @Override
        public int getTransportPort() {

            return transportPort;
        }

        /**
         * Returns the Url path of Service URL.
         *
         * @return String of the url path.
         */
        @Override
        public String getPath() {

            return urlPath;
        }

        /**
         * Returns the parameter value when the key is provided.
         *
         * @param key Key of the parameter.
         * @return The value of the parameter.
         */
        @Override
        public String getParameter(String key) {

            return parameters.get(key);
        }

        /**
         * Returns a map of the parameters.
         *
         * @return The parameters.
         */
        @Override
        public Map<String, String> getParameters() {

            return Collections.unmodifiableMap(parameters);
        }

        /**
         * Returns decoded fragment from the url.
         *
         * @return The decoded fragment.
         */
        @Override
        public String getFragment() {

            return fragment;
        }

        /**
         * Returns the tenant domain of the service URL.
         *
         * @return The tenant domain.
         */
        @Override
        public String getTenantDomain() {

            return tenantDomain;
        }

        /**
         * Returns the absolute URL used for Identity Server internal calls.
         * Concatenate the protocol, host name, port, proxy context path, web context root, url context, query params
         * and the fragment to return the internal absolute URL.
         *
         * @return The internal absolute URL from the Service URL instance.
         */
        @Override
        public String getAbsoluteInternalURL() {

            return absoluteInternalUrl;
        }

        /**
         * Returns the proxy server url when the Identity Server is fronted with a proxy.
         * Concatenate the protocol, host name, port, proxy context path, web context root, url context, query params
         * and the fragment to return the public absolute URL.
         *
         * @return The public absolute URL from the Service URL instance.
         */
        @Override
        public String getAbsolutePublicURL() {

            return absolutePublicUrl;
        }

        /**
         * Returns the proxy server url when the Identity Server is fronted with a proxy.
         * Concatenate the protocol, host name, port and  return the public absolute URL.
         *
         * @return The public absolute URL without any url path.
         */
        @Override
        public String getAbsolutePublicUrlWithoutPath() {

            return absolutePublicUrlWithoutURLPath;
        }

        /**
         * Returns the relative url, relative to the proxy the server is fronted with.
         * Concatenate the url context, query params and the fragment to the url path to return the public relative URL.
         *
         * @return The public relative URL from the Service URL instance.
         */
        @Override
        public String getRelativePublicURL() {

            return relativePublicUrl;
        }

        /**
         * Returns the relative url, relative to the internal server host.
         * Concatenate the query params and the fragment to the url path to return the internal relative URL.
         *
         * @return The internal relative URL from the Service URL instance.
         */
        @Override
        public String getRelativeInternalURL() {

            return relativeInternalUrl;
        }
    }
}
