/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.endpoint.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.ext.ResponseHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.message.Message;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.entitlement.endpoint.auth.EntitlementAuthenticationHandler;
import org.wso2.carbon.identity.entitlement.endpoint.auth.EntitlementAuthenticatorRegistry;
import org.wso2.carbon.identity.entitlement.endpoint.exception.UnauthorizedException;
import org.wso2.carbon.identity.entitlement.endpoint.util.EntitlementEndpointConstants;

import javax.ws.rs.core.Response;

public class AuthenticationFilter implements RequestHandler, ResponseHandler {

    private static Log log = LogFactory.getLog(AuthenticationFilter.class);

    @Override
    public Response handleRequest(Message message, ClassResourceInfo classResourceInfo) {

        // reset anything set on provisioning thread local.
        IdentityApplicationManagementUtil.resetThreadLocalProvisioningServiceProvider();

        if (log.isDebugEnabled()) {
            log.debug("Authenticating Entitlement Endpoint request..");
        }
        EntitlementAuthenticatorRegistry entitlementAuthRegistry = EntitlementAuthenticatorRegistry.getInstance();

        if (entitlementAuthRegistry != null) {
            EntitlementAuthenticationHandler entitlementAuthHandler = entitlementAuthRegistry.getAuthenticator(
                    message, classResourceInfo);

            boolean isAuthenticated = false;
            if (entitlementAuthHandler != null) {
                isAuthenticated = entitlementAuthHandler.isAuthenticated(message, classResourceInfo);

                if (isAuthenticated) {
                    return null;
                }
            }
        }
        //if null response is not returned(i.e:message continues its way to the resource), return error & terminate.
        UnauthorizedException unauthorizedException = new UnauthorizedException(
                EntitlementEndpointConstants.ERROR_UNAUTHORIZED_MESSAGE);
        Response.ResponseBuilder responseBuilder = Response.status(unauthorizedException.getCode());
        responseBuilder.entity(unauthorizedException.getDescription());

        return responseBuilder.build();
    }

    // To clear the ThreadLocalProvisioningServiceProvider in a non faulty case
    @Override
    public Response handleResponse(Message message, OperationResourceInfo operationResourceInfo, Response response) {
        IdentityApplicationManagementUtil.resetThreadLocalProvisioningServiceProvider();
        return null;
    }

}
