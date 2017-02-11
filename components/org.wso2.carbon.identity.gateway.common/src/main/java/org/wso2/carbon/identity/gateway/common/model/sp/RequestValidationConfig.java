package org.wso2.carbon.identity.gateway.common.model.sp;


import java.util.ArrayList;
import java.util.List;

public class RequestValidationConfig {

    private List<RequestValidatorConfig> requestValidatorConfigs = new ArrayList<>();

    public List<RequestValidatorConfig> getRequestValidatorConfigs() {
        return requestValidatorConfigs;
    }

    public void setRequestValidatorConfigs(List<RequestValidatorConfig> requestValidatorConfigs) {
        this.requestValidatorConfigs = requestValidatorConfigs;
    }
}
