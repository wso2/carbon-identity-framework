package org.wso2.carbon.identity.gateway.common.model.sp;

public class ServiceProvider {
    private ServiceProviderConfig serviceProviderConfig;

    public ServiceProviderConfig getServiceProviderConfig() {
        return serviceProviderConfig;
    }

    public void setServiceProviderConfig(ServiceProviderConfig serviceProviderConfig) {
        this.serviceProviderConfig = serviceProviderConfig;
    }
}