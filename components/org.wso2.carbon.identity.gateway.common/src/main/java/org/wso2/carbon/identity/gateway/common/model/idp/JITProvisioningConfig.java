package org.wso2.carbon.identity.gateway.common.model.idp;


import java.util.ArrayList;
import java.util.List;

public class JITProvisioningConfig {
    private List<String> provisioningIdPs = new ArrayList<>();

    public List<String> getProvisioningIdPs() {
        return provisioningIdPs;
    }

    public void setProvisioningIdPs(List<String> provisioningIdPs) {
        this.provisioningIdPs = provisioningIdPs;
    }
}
