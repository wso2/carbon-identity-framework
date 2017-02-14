package org.wso2.carbon.identity.gateway.common.model.idp;

import java.util.ArrayList;
import java.util.List;

public class ProvisioningConfig {

    private JITProvisioningConfig jitProvisioningConfig;
    private List<ProvisioningClaimConfig> provisioningClaimConfigs = new ArrayList<>();
    private List<String> provisioningRoles = new ArrayList<>();
    private List<ProvisionerConfig> provisionerConfigs = new ArrayList<>();

    public JITProvisioningConfig getJitProvisioningConfig() {
        return jitProvisioningConfig;
    }

    public void setJitProvisioningConfig(JITProvisioningConfig jitProvisioningConfig) {
        this.jitProvisioningConfig = jitProvisioningConfig;
    }

    public List<ProvisionerConfig> getProvisionerConfigs() {
        return provisionerConfigs;
    }

    public void setProvisionerConfigs(List<ProvisionerConfig> provisionerConfigs) {
        this.provisionerConfigs = provisionerConfigs;
    }

    public List<ProvisioningClaimConfig> getProvisioningClaimConfigs() {
        return provisioningClaimConfigs;
    }

    public void setProvisioningClaimConfigs(List<ProvisioningClaimConfig> provisioningClaimConfigs) {
        this.provisioningClaimConfigs = provisioningClaimConfigs;
    }

    public List<String> getProvisioningRoles() {
        return provisioningRoles;
    }

    public void setProvisioningRoles(List<String> provisioningRoles) {
        this.provisioningRoles = provisioningRoles;
    }
}
