package org.wso2.carbon.identity.gateway.store;

import org.wso2.carbon.identity.gateway.common.model.idp.IdentityProvider;
import org.wso2.carbon.identity.gateway.common.model.sp.AuthenticationHandlerConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.AuthenticatorHandlerConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.RequestHandlerConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.ServiceProvider;
import org.wso2.carbon.identity.gateway.common.model.sp.ServiceProviderConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.StepHandlerConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceProviderConfigStore {
    private static ServiceProviderConfigStore serviceProviderConfigStore = new ServiceProviderConfigStore();

    private Map<String, String> spUniqueKeyMap = new HashMap<>();
    private Map<String, ServiceProvider> spEntityMap = new HashMap<>();

    private ServiceProviderConfigStore(){

    }

    public static ServiceProviderConfigStore getInstance(){
        return ServiceProviderConfigStore.serviceProviderConfigStore;
    }

    public void addServiceProvider(ServiceProvider serviceProvider){
        if(serviceProvider != null){
            ServiceProviderConfig serviceProviderConfig = serviceProvider.getServiceProviderConfig();
            List<RequestHandlerConfig> requestHandlerConfigs = serviceProviderConfig.getRequestHandlerConfigs();
            for(RequestHandlerConfig requestHandlerConfig: requestHandlerConfigs){
                String uniquePropertyName = requestHandlerConfig.getUniquePropertyName();
                String uniqueKey = requestHandlerConfig.getProperties().getProperty(uniquePropertyName);
                spUniqueKeyMap.put(uniqueKey, serviceProviderConfig.getName());
                spEntityMap.put(serviceProviderConfig.getName(), serviceProvider);
            }
        }

    }

    public ServiceProvider getServiceProvider(String uniqueKey){
        ServiceProvider serviceProvider = null ;
        String spName = spUniqueKeyMap.get(uniqueKey);
        if(spName != null){
            serviceProvider = spEntityMap.get(spName);
            if(serviceProvider != null){
                buildServiceProvider(serviceProvider);
            }
        }
        return serviceProvider;
    }


    private void buildServiceProvider(ServiceProvider serviceProvider) {

        ServiceProviderConfig serviceProviderConfig = serviceProvider.getServiceProviderConfig();
        AuthenticationHandlerConfig authenticationHandlerConfig = serviceProviderConfig
                .getAuthenticationHandlerConfig();
        if (authenticationHandlerConfig != null && authenticationHandlerConfig.getStepHandlerConfigs().size() > 0) {
            List<StepHandlerConfig> stepHandlerConfigs = authenticationHandlerConfig.getStepHandlerConfigs();

            stepHandlerConfigs.stream()
                    .forEach(stepHandlerConfig -> {
                                 AuthenticatorHandlerConfig authenticatorHandlerConfig = stepHandlerConfig
                                         .getAuthenticatorHandlerConfig();
                                 List<AuthenticatorHandlerConfig> authenticatorHandlerConfigs
                                         = stepHandlerConfig.getAuthenticatorHandlerConfigs();
                                 if (authenticatorHandlerConfig != null && authenticatorHandlerConfig
                                                                                   .getIdentityProviderConfig() ==
                                                                           null) {
                                     updateIdentityProvider(authenticatorHandlerConfig);
                                 } else {
                                     authenticatorHandlerConfigs.stream()
                                             .filter(tmpAuthenticatorHandlerConfig ->
                                                             tmpAuthenticatorHandlerConfig.getIdentityProviderConfig()
                                                                                         != null)
                                             .forEach(tmpAuthenticatorHandlerConfig ->
                                                              updateIdentityProvider(tmpAuthenticatorHandlerConfig));
                                 }
                             }
                    );
        }
    }
    private void updateIdentityProvider(AuthenticatorHandlerConfig authenticatorHandlerConfig){
        if(authenticatorHandlerConfig.getIdpName() != null) {
            IdentityProvider identityProvider = IdentityProviderConfigStore.getInstance()
                    .getIdentityProvider(authenticatorHandlerConfig.getIdpName());
            authenticatorHandlerConfig.setIdentityProviderConfig(identityProvider.getIdentityProviderConfig());
        }
    }


}
