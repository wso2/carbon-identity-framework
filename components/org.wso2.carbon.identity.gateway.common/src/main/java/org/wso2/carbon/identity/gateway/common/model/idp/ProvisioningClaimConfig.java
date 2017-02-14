package org.wso2.carbon.identity.gateway.common.model.idp;


public class ProvisioningClaimConfig {

    private String claimId;
    private String defaultValue;

    public String getClaimId() {
        return claimId;
    }

    public void setClaimId(String claimId) {
        this.claimId = claimId;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
