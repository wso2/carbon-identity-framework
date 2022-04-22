package org.wso2.carbon.identity.application.mgt.validator;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.util.*;
import java.util.stream.Collectors;

public class SAMLValidator implements ApplicationValidator {
    @Override
    public int getOrderId() {
        return 1;
    }

    @Override
    public List<String> validateApplication(ServiceProvider serviceProvider, String tenantDomain, String username) throws IdentityApplicationManagementException {
        List<String> validationErrors = new ArrayList<>();
        for(InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig:
                serviceProvider.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs()){
            if(inboundAuthenticationRequestConfig.getInboundAuthKey().equals("samlsso")){
                validateSAMLProperties(validationErrors, inboundAuthenticationRequestConfig);
                break;
            }
        }
        return validationErrors;
    }

    private void validateSAMLProperties(List<String> validationErrors, InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig) {
        //TODO: add validations
        Property[] properties = inboundAuthenticationRequestConfig.getProperties();
        Map<String,List<String>> propertyMap = Arrays.stream(properties).collect(Collectors.groupingBy(Property::getName,Collectors.mapping(Property::getValue, Collectors.toList())));
    }
    

}
