package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl.model;


import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;

import java.io.Serializable;

public class Sequence implements Serializable{

    private ServiceProviderConfig serviceProviderConfig = null ;

    public Sequence(ServiceProviderConfig serviceProviderConfig)
            throws IdentityApplicationManagementException {
        this.serviceProviderConfig = serviceProviderConfig;
    }

    public ServiceProviderConfig getServiceProviderConfig() {
        return serviceProviderConfig;
    }

    public void setServiceProviderConfig(
            ServiceProviderConfig serviceProviderConfig) {
        this.serviceProviderConfig = serviceProviderConfig;
    }
}
