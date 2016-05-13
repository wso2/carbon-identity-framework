package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .model;


import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationHandlerException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;

import java.io.Serializable;

public class ServiceProviderConfig implements Serializable {

    private transient ServiceProvider serviceProvider = null;

    private String requestType;
    private String clientId;
    private String tenantDomain;

    public ServiceProviderConfig(String tenantDomain, String requestType, String clientId) {
        this.tenantDomain = tenantDomain;
        this.requestType = requestType;
        this.clientId = clientId;
    }

    public ServiceProvider getServiceProvider() throws AuthenticationHandlerException {
        if (this.serviceProvider == null) {
            synchronized (this) {
                ApplicationManagementService applicationManagementService = ApplicationManagementService.getInstance();
                try {
                    this.serviceProvider =
                            applicationManagementService.getServiceProviderByClientId(requestType, clientId, tenantDomain);
                } catch (IdentityApplicationManagementException e) {
                    String errorMessage = "Error occured while trying to get service providers by calling admin service, " + e.getMessage() ;
                    throw new AuthenticationHandlerException(errorMessage, e);
                }
            }
        }
        return this.serviceProvider;
    }
}
