package org.wso2.carbon.identity.gateway.common.model.sp;

public class ServiceProviderConfig {

    private String name;

    private RequestValidationConfig requestValidationConfig;
    private AuthenticationConfig authenticationConfig;
    private ResponseBuildingConfig responseBuildingConfig;


    public AuthenticationConfig getAuthenticationConfig() {
        return authenticationConfig;
    }

    public void setAuthenticationConfig(AuthenticationConfig authenticationConfig) {
        this.authenticationConfig = authenticationConfig;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RequestValidationConfig getRequestValidationConfig() {
        return requestValidationConfig;
    }

    public void setRequestValidationConfig(RequestValidationConfig requestValidationConfig) {
        this.requestValidationConfig = requestValidationConfig;
    }

    public ResponseBuildingConfig getResponseBuildingConfig() {
        return responseBuildingConfig;
    }

    public void setResponseBuildingConfig(ResponseBuildingConfig responseBuildingConfig) {
        this.responseBuildingConfig = responseBuildingConfig;
    }
}
