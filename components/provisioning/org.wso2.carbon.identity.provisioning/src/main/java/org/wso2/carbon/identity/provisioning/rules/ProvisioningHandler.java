package org.wso2.carbon.identity.provisioning.rules;

import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.provisioning.ProvisioningEntity;

public interface ProvisioningHandler {
    default boolean isAllowedToProvision(String tenantDomainName, ProvisioningEntity provisioningEntity,
                                         ServiceProvider serviceProvider,
                                         String idPName,
                                         String connectorType){
        throw new UnsupportedOperationException("Method not implemented");
    };
}