/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.core.context.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Header;
import org.wso2.carbon.identity.core.context.model.Request;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.List;

/**
 * Utility class for IdentityContext related operations.
 */
public class IdentityContextUtil {

    private static final Log LOG = LogFactory.getLog(IdentityContextUtil.class);

    /**
     * Resolves the client IP address from the given IdentityContext.
     *
     * @return The client IP address if found, else falls back to the request IP address.
     */
    public static String getClientIpAddress() {

        Request request = IdentityContext.getThreadLocalIdentityContext().getRequest();
        if (request == null) {
            LOG.debug("IdentityContext or Request object is null. Cannot resolve client IP address.");
            return null;
        }

        List<Header> headerList = request.getHeaders();
        if (CollectionUtils.isEmpty(headerList)) {
            LOG.debug("Request headers are empty. Falling back to request IP address.");
            return request.getIpAddress();
        }

        for (String ipHeader : IdentityConstants.HEADERS_WITH_IP) {
            for (Header header : headerList) {
                if (ipHeader.equalsIgnoreCase(header.getName())) {
                    List<String> values = header.getValue();
                    if (CollectionUtils.isNotEmpty(values)) {
                        if (LOG.isDebugEnabled() && values.size() > 1) {
                            LOG.debug("Multiple values found for header: " + ipHeader +
                                    ". Using the first value as the client IP address.");
                        }
                        String ip = values.get(0);
                        if (StringUtils.isNotEmpty(ip) && !IdentityConstants.UNKNOWN.equalsIgnoreCase(ip)) {
                            return IdentityUtil.getFirstIP(ip);
                        }
                    }
                }
            }
        }

        LOG.debug("Cannot resolve client IP address from the request headers. Falling back to request IP address.");
        return request.getIpAddress();
    }

    /**
     * Resolves the client User-Agent from the given Request.
     *
     * @return The client User-Agent if found, else null.
     */
    public static String getClientUserAgent() {

        Request request = IdentityContext.getThreadLocalIdentityContext().getRequest();
        if (request == null) {
            LOG.debug("IdentityContext or Request object is null. Cannot resolve User-Agent.");
            return null;
        }

        List<Header> headerList = request.getHeaders();
        if (CollectionUtils.isEmpty(headerList)) {
            LOG.debug("Request headers are empty. User-Agent cannot be resolved.");
            return null;
        }

        List<String> forwardedUserAgent = null;
        List<String> userAgent = null;
        for (Header header : headerList) {
            String name = header.getName();
            if (IdentityConstants.X_FORWARDED_USER_AGENT.equalsIgnoreCase(name)) {
                forwardedUserAgent = header.getValue();
            } else if (IdentityConstants.USER_AGENT.equalsIgnoreCase(name)) {
                userAgent = header.getValue();
            }
        }

        if (CollectionUtils.isNotEmpty(forwardedUserAgent)) {
            if (LOG.isDebugEnabled() && forwardedUserAgent.size() > 1) {
                LOG.debug("Multiple values found for header: " + IdentityConstants.X_FORWARDED_USER_AGENT +
                        ". Using the first value as the User-Agent.");
            }
            return forwardedUserAgent.get(0);
        } else if (CollectionUtils.isNotEmpty(userAgent)) {
            if (LOG.isDebugEnabled() && userAgent.size() > 1) {
                LOG.debug("Multiple values found for header: " + IdentityConstants.USER_AGENT +
                        ". Using the first value as the User-Agent.");
            }
            return userAgent.get(0);
        }

        LOG.debug("User-Agent header not found in the request.");
        return null;
    }
}
