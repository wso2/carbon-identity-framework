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

package org.wso2.carbon.identity.mgt.endpoint.util.client;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementServiceUtil;

import java.nio.charset.Charset;

/**
 * Utility class for common client operations.
 */
public class ClientUtils {

    private static final Log LOG = LogFactory.getLog(ClientUtils.class);
    private static final String CLIENT = "Client ";

    /**
     * Sets the authorization header for the HTTP request.
     *
     * @param httpMethod The HTTP request method to set the header on.
     */
    public static void setAuthorizationHeader(HttpUriRequestBase httpMethod) {

        LOG.debug("Setting authorization header for HTTP request");
        String toEncode = IdentityManagementServiceUtil.getInstance().getAppName() + ":"
                + String.valueOf(IdentityManagementServiceUtil.getInstance().getAppPassword());
        byte[] encoding = Base64.encodeBase64(toEncode.getBytes());
        String authHeader = new String(encoding, Charset.defaultCharset());
        httpMethod.addHeader(HTTPConstants.HEADER_AUTHORIZATION, CLIENT + authHeader);
        LOG.debug("Authorization header added successfully");
    }
}
