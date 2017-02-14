/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.util;


import org.wso2.carbon.identity.gateway.api.util.Constants;
import org.wso2.carbon.identity.gateway.common.model.sp.ServiceProviderConfig;
import org.wso2.carbon.identity.gateway.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.gateway.processor.authenticator.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.authenticator.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.authenticator.RequestPathApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandlerException;
import org.wso2.msf4j.Request;

import java.util.List;
import java.util.Map;

public class Utility {
    public static ServiceProviderConfig getServiceProvider(String requestType, String clientId)
            throws AuthenticationHandlerException {
        ServiceProviderConfig  serviceProvider = null;
        /*ApplicationManagementService applicationManagementService = ApplicationManagementService.getInstance();
        try {
            serviceProvider =
                    applicationManagementService.getServiceProviderByClientId(requestType, clientId, tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            String errorMessage =
                    "Error occured while trying to get service providers by calling admin service, " + e.getMessage();
            throw new AuthenticationHandlerException(errorMessage, e);
        }*/
        return serviceProvider;
    }

    public static LocalApplicationAuthenticator getLocalApplicationAuthenticator(String name) {
        LocalApplicationAuthenticator localApplicationAuthenticator = null;
        List<LocalApplicationAuthenticator> localApplicationAuthenticators =
                FrameworkServiceDataHolder.getInstance().getLocalApplicationAuthenticators();
        for (LocalApplicationAuthenticator tmpLocalApplicationAuthenticator : localApplicationAuthenticators) {
            if (tmpLocalApplicationAuthenticator.getName().equals(name)) {
                localApplicationAuthenticator = tmpLocalApplicationAuthenticator;
                break;
            }
        }
        return localApplicationAuthenticator;
    }

    public static FederatedApplicationAuthenticator getFederatedApplicationAuthenticator(String name) {
        FederatedApplicationAuthenticator federatedApplicationAuthenticator = null;
        List<FederatedApplicationAuthenticator> federatedApplicationAuthenticators =
                FrameworkServiceDataHolder.getInstance().getFederatedApplicationAuthenticators();
        for (FederatedApplicationAuthenticator tmpFederatedApplicationAuthenticator :
                federatedApplicationAuthenticators) {
            if (tmpFederatedApplicationAuthenticator.getName().equals(name)) {
                federatedApplicationAuthenticator = tmpFederatedApplicationAuthenticator;
                break;
            }
        }
        return federatedApplicationAuthenticator;
    }

    public static RequestPathApplicationAuthenticator getRequestPathApplicationAuthenticator(String name) {
        RequestPathApplicationAuthenticator requestPathApplicationAuthenticator = null;
        List<RequestPathApplicationAuthenticator> requestPathApplicationAuthenticators =
                FrameworkServiceDataHolder.getInstance().getRequestPathApplicationAuthenticators();
        for (RequestPathApplicationAuthenticator authenticator : requestPathApplicationAuthenticators) {
            if (authenticator.getName().equals(name)) {
                requestPathApplicationAuthenticator = authenticator;
                break;
            }
        }
        return requestPathApplicationAuthenticator;
    }

    public static String getParameter(Request request, String paramName) {
        Map<String, String> queryParams = (Map<String, String>) request.getProperty(Constants.QUERY_PARAMETERS);
        Map<String, String> bodyParams = (Map<String, String>) request.getProperty(Constants.BODY_PARAMETERS);
        if (queryParams.get(paramName) != null) {
            return queryParams.get(paramName);
        } else {
            return bodyParams.get(paramName);
        }
    }

}
