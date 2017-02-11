package org.wso2.carbon.identity.gateway.common.model.sp;

import java.util.ArrayList;
import java.util.List;

public class ResponseBuildingConfig {
    private List<ResponseBuilderConfig> responseBuilderConfigs = new ArrayList<>();

    public List<ResponseBuilderConfig> getResponseBuilderConfigs() {
        return responseBuilderConfigs;
    }

    public void setResponseBuilderConfigs(List<ResponseBuilderConfig> responseBuilderConfigs) {
        this.responseBuilderConfigs = responseBuilderConfigs;
    }
}
