/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceComponent;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;

import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.Map;

/**
 * TODO: Comment
 */
public class DefaultURLResolverService implements URLResolverService {

    @Override
    public String resolveUrl(String url, boolean addProxyContextPath, boolean addWebContextRoot,
                             String tenantDomain, Map<String, Object> properties) throws URLResolverException {

        try {
            URL parsedUrl = new URL(url);
            StringBuilder urlBuilder = new StringBuilder(parsedUrl.getProtocol())
                    .append(parsedUrl.getHost())
                    .append(parsedUrl.getPort());
            appendContextToUri(parsedUrl.getPath(), addProxyContextPath, addWebContextRoot, urlBuilder);
            return urlBuilder.toString();

        } catch (MalformedURLException e) {
            throw new URLResolverException("Error while parsing the URL: " + url, e);
        }
    }

    @Override
    public String resolveUrlContext(String urlContext, boolean addProxyContextPath, boolean addWebContextRoot,
                                    String tenantDomain, Map<String, Object> properties) throws URLResolverException {

        String hostName = getHostName();
        String mgtTransport = CarbonUtils.getManagementTransport();
        int mgtTransportPort = getMgtTransportPort(mgtTransport);

        if (hostName.endsWith("/")) {
            hostName = hostName.substring(0, hostName.length() - 1);
        }
        StringBuilder serverUrl = new StringBuilder(mgtTransport).append("://").append(hostName.toLowerCase());
        // If it's well known HTTPS port, skip adding port
        if (mgtTransportPort != IdentityCoreConstants.DEFAULT_HTTPS_PORT) {
            serverUrl.append(":").append(mgtTransportPort);
        }

        appendContextToUri(urlContext, addProxyContextPath, addWebContextRoot, serverUrl);
        return serverUrl.toString();
    }

    private int getMgtTransportPort(String mgtTransport) {

        AxisConfiguration axisConfiguration = IdentityCoreServiceComponent.getConfigurationContextService().
                getServerConfigContext().getAxisConfiguration();
        int mgtTransportPort = CarbonUtils.getTransportProxyPort(axisConfiguration, mgtTransport);
        if (mgtTransportPort <= 0) {
            mgtTransportPort = CarbonUtils.getTransportPort(axisConfiguration, mgtTransport);
        }
        return mgtTransportPort;
    }

    private String getHostName() throws URLResolverException {

        String hostName = ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants.HOST_NAME);
        try {
            if (hostName == null) {
                hostName = NetworkUtils.getLocalHostname();
            }
        } catch (SocketException e) {
            throw new URLResolverException("Error while trying to read hostname.", e);
        }
        return hostName;
    }

    private static void appendContextToUri(String endpoint, boolean addProxyContextPath, boolean addWebContextRoot,
                                           StringBuilder serverUrl) {

        // If ProxyContextPath is defined then append it
        if (addProxyContextPath) {
            // If ProxyContextPath is defined then append it
            String proxyContextPath = ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants
                                                                                                 .PROXY_CONTEXT_PATH);
            if (StringUtils.isNotBlank(proxyContextPath)) {
                if (proxyContextPath.trim().charAt(0) != '/') {
                    serverUrl.append("/").append(proxyContextPath.trim());
                } else {
                    serverUrl.append(proxyContextPath.trim());
                }
            }
        }

        // If webContextRoot is defined then append it
        if (addWebContextRoot) {
            String webContextRoot = ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants
                                                                                               .WEB_CONTEXT_ROOT);
            if (StringUtils.isNotBlank(webContextRoot)) {
                if (webContextRoot.trim().charAt(0) != '/') {
                    serverUrl.append("/").append(webContextRoot.trim());
                } else {
                    serverUrl.append(webContextRoot.trim());
                }
            }
        }

        String enableTenantUrlSupport = IdentityUtil.getProperty("EnableTenantUrlSupport");
        boolean addTenantQualifier = Boolean.parseBoolean(enableTenantUrlSupport);

        if (addTenantQualifier) {

            String tenantNameFromContext = (String) IdentityUtil.threadLocalProperties.get().get
                    ("TenantNameFromContext");
            if (tenantNameFromContext != null) {
                if (serverUrl.toString().endsWith("/")) {
                    serverUrl.append("t/").append(tenantNameFromContext);
                } else {
                    serverUrl.append(tenantNameFromContext);
                }
            }
        }

        if (StringUtils.isNotBlank(endpoint)) {
            if (!serverUrl.toString().endsWith("/") && endpoint.trim().charAt(0) != '/') {
                serverUrl.append("/").append(endpoint.trim());
            } else if (serverUrl.toString().endsWith("/") && endpoint.trim().charAt(0) == '/') {
                serverUrl.append(endpoint.trim().substring(1));
            } else {
                serverUrl.append(endpoint.trim());
            }
        }
        if (serverUrl.toString().endsWith("/")) {
            serverUrl.setLength(serverUrl.length() - 1);
        }
    }
}
