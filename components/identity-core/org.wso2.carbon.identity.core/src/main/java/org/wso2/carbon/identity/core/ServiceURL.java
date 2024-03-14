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

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Service URL interface.
 */
public interface ServiceURL {

    /**
     * Returns the protocol of Service URL.
     *
     * @return String of the protocol.
     */
    String getProtocol();

    /**
     * Returns the host name of Service URL.
     *
     * @return String of the host name.
     */
    String getProxyHostName();

    /**
     * Returns the port of Service URL.
     *
     * @return value of the port.
     */
    int getPort();

    /**
     * Returns the internal transport portL.
     *
     * @return value of the port.
     */
    int getTransportPort();

    /**
     * Returns the Url path of Service URL.
     *
     * @return String of the url path.
     */
    String getPath();

    /**
     * Returns the parameter value when the key is provided.
     *
     * @param key Key of the parameter.
     * @return The value of the parameter.
     */
    String getParameter(String key);

    /**
     * Returns a map of the parameters.
     *
     * @return The parameters.
     */
    Map<String, String> getParameters();

    /**
     * Returns decoded fragment from the url.
     *
     * @return The decoded fragment.
     */
    String getFragment();

    /**
     * Returns the tenant domain of the service URL.
     *
     * @return The tenant domain.
     */
    String getTenantDomain();

    /**
     * Returns the absolute URL used for Identity Server internal calls.
     * Concatenate the protocol, host name, port, proxy context path, web context root, url context, query params and
     * the fragment to return the internal absolute URL.
     *
     * @return The internal absolute URL from the Service URL instance.
     */
    String getAbsoluteInternalURL();

    /**
     * Returns the proxy server url when the Identity Server is fronted with a proxy.
     * Concatenate the protocol, host name, port, proxy context path, web context root, url context, query params and
     * the fragment to return the public absolute URL.
     *
     * @return The public absolute URL from the Service URL instance.
     */
    String getAbsolutePublicURL();

    /**
     * Returns the relative url, relative to the proxy the server is fronted with.
     * Concatenate the url context, query params and the fragment to the url path to return the public relative URL.
     *
     * @return The public relative URL from the Service URL instance.
     */
    String getRelativePublicURL();

    /**
     * Returns the relative url, relative to the internal server host.
     * Concatenate the query params and the fragment to the url path to return the internal relative URL.
     *
     * @return The internal relative URL from the Service URL instance.
     */
    String getRelativeInternalURL();

    /**
     * Returns the proxy server url when the Identity Server is fronted with a proxy.
     * Concatenate the protocol, host name, port and  return the public absolute URL.
     *
     * @return The public absolute URL without any url path.
     */
    default String getAbsolutePublicUrlWithoutPath() {

        return StringUtils.EMPTY;
    }

    default String getAbsoluteRegionalURL() {

        return StringUtils.EMPTY;
    }
}
