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

package org.wso2.carbon.identity.core.internal;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.model.ServiceURL;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import static org.wso2.carbon.identity.core.util.IdentityTenantUtil.isTenantQualifiedUrlsEnabled;

/**
 * Implementation for {@link ServiceURLBuilder}.
 * Builder for {@link ServiceURL} instances.
 */
public class DefaultServiceURLBuilder implements ServiceURLBuilder {

    private String[] urlPaths;
    private String fragment;
    private Map<String, String> parameters = new HashMap<>();
    private Map<String, String> fragmentParams = new HashMap<>();

    /**
     * This method is called to add the URL path to the builder.
     *
     * @param paths Context path. Can provide multiple context paths with a comma separated string.
     * @return {@link ServiceURLBuilder}.
     */
    @Override
    public ServiceURLBuilder addPath(String ... paths) {

        this.urlPaths = paths;
        return this;
    }

    /**
     * This method is called when the URL needs to be appended with other parameters. Such parameters should be
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
     * This method is called when the URL needs to be appended with a fragment.
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
     * This method is called when the URL fragment needs to be appended with parameters. Such parameters should be
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
    public ServiceURL build() throws URLBuilderException {

        String protocol = fetchProtocol();
        String hostName = fetchHostName();
        int port = fetchPort();
        String resolvedUrlContext = buildUrlPath(urlPaths);
        String resolvedFragment = buildFragment(fragment, fragmentParams);
        String tenantDomain = resolveTenantDomain();

        StringBuilder resolvedUrlStringBuilder = new StringBuilder();
        if (isTenantQualifiedUrlsEnabled()) {
            if (StringUtils.isNotBlank(tenantDomain)) {
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

        return new ServiceURL(protocol, hostName, port, resolvedUrlStringBuilder.toString(), parameters,
                resolvedFragment);
    }

    private String resolveTenantDomain() {

        String tenantDomain = IdentityTenantUtil.getTenantDomainFromContext();
        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        }
        return tenantDomain;
    }

    private String buildFragment(String fragment, Map<String, String> fragmentParams) {

        if (StringUtils.isNotBlank(fragment)) {
            return fragment;
        } else {
            return getResolvedParamString(fragmentParams);
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

    private String buildUrlPath(String[] urlPaths) {

        StringBuilder urlPathBuilder = new StringBuilder();
        if (ArrayUtils.isNotEmpty(urlPaths)) {
            for (String path : urlPaths) {
                urlPathBuilder.append(path).append("/");
            }
            if (urlPathBuilder.length() > 0 && urlPathBuilder.charAt(urlPathBuilder.length() - 1) == '/') {
                urlPathBuilder.setLength(urlPathBuilder.length() - 1);
            }
        }
        return urlPathBuilder.toString();
    }

    private String fetchProtocol() {

        return CarbonUtils.getManagementTransport();
    }

    private String fetchHostName() throws URLBuilderException {

        String hostName = ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants.HOST_NAME);
        try {
            if (StringUtils.isBlank(hostName)) {
                hostName = NetworkUtils.getLocalHostname();
            }
        } catch (SocketException e) {
            throw new URLBuilderException(String.format("Error while trying to resolve the hostname %s from the " +
                    "system", hostName), e);
        }
        return hostName;
    }

    private Integer fetchPort() {

        String mgtTransport = CarbonUtils.getManagementTransport();
        AxisConfiguration axisConfiguration = IdentityCoreServiceComponent.getConfigurationContextService().
                getServerConfigContext().getAxisConfiguration();
        int port = CarbonUtils.getTransportProxyPort(axisConfiguration, mgtTransport);
        if (port <= 0) {
            port = CarbonUtils.getTransportPort(axisConfiguration, mgtTransport);
        }
        return port;
    }
}
