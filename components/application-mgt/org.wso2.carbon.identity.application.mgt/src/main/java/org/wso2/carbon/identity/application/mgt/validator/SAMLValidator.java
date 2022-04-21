package org.wso2.carbon.identity.application.mgt.validator;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.util.ArrayList;
import java.util.List;

public class SAMLValidator implements ApplicationValidator {
    @Override
    public int getOrderId() {
        return 0;
    }

    @Override
    public List<String> validateApplication(ServiceProvider serviceProvider, String tenantDomain, String username) throws IdentityApplicationManagementException {
        List<String> validationErrors = new ArrayList<>();
        for(InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig:
                serviceProvider.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs()){
            if(inboundAuthenticationRequestConfig.getInboundAuthKey().equals("samlsso")){
                validateSAMLProperties(validationErrors, inboundAuthenticationRequestConfig);
            }
        }
        return validationErrors;
    }

    private void validateSAMLProperties(List<String> validationErrors, InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig) {
        //TODO: add validations
    }


}
