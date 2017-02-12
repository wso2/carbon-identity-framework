package org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.util;


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
        Map<String, String> queryParams = (Map<String, String>) request.getProperty(org.wso2.carbon.identity.gateway
                .api.Constants.QUERY_PARAMETERS);
        Map<String, String> bodyParams = (Map<String, String>) request.getProperty(org.wso2.carbon.identity.gateway.api
                .Constants.BODY_PARAMETERS);
        if (queryParams.get(paramName) != null) {
            return queryParams.get(paramName);
        } else {
            return bodyParams.get(paramName);
        }
    }

}
