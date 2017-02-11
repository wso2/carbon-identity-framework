package org.wso2.carbon.identity.gateway.common.model.sp;

import java.util.ArrayList;
import java.util.List;

public class ServiceProviderConfig {

    private String name;

    private List<RequestHandlerConfig> requestHandlerConfigs = new ArrayList<>();
    private AuthenticationHandlerConfig authenticationHandlerConfig ;
    private List<ResponseHandlerConfig> responseHandlerConfigs = new ArrayList<>();

    public List<RequestHandlerConfig> getRequestHandlerConfigs() {
        return requestHandlerConfigs;
    }

    public void setRequestHandlerConfigs(List<RequestHandlerConfig> requestHandlerConfigs) {
        this.requestHandlerConfigs = requestHandlerConfigs;
    }

    public AuthenticationHandlerConfig getAuthenticationHandlerConfig() {
        return authenticationHandlerConfig;
    }

    public void setAuthenticationHandlerConfig(AuthenticationHandlerConfig authenticationHandlerConfig) {
        this.authenticationHandlerConfig = authenticationHandlerConfig;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ResponseHandlerConfig> getResponseHandlerConfigs() {
        return responseHandlerConfigs;
    }

    public void setResponseHandlerConfigs(List<ResponseHandlerConfig> responseHandlerConfigs) {
        this.responseHandlerConfigs = responseHandlerConfigs;
    }
}
