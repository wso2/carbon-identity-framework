package org.wso2.carbon.identity.gateway.processor.handler.request;

import org.wso2.carbon.identity.gateway.api.FrameworkHandlerResponse;
import org.wso2.carbon.identity.gateway.common.model.sp.RequestValidationConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.RequestValidatorConfig;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandlerException;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public abstract class AbstractRequestHandler extends FrameworkHandler {
    public abstract FrameworkHandlerResponse validate(AuthenticationContext authenticationContext)
            throws RequestHandlerException;

    protected abstract String getValidatorType();

    public Properties getValidatorConfig(AuthenticationContext authenticationContext) throws AuthenticationHandlerException {

        if (authenticationContext.getServiceProvider() == null) {
            throw new AuthenticationHandlerException("Error while getting validator configs : No service provider " +
                    "found with uniqueId : " + authenticationContext.getUniqueId());
        }
        RequestValidationConfig validatorConfig = authenticationContext.getServiceProvider()
                .getRequestValidationConfig();
        List<RequestValidatorConfig> validatorConfigs = validatorConfig.getRequestValidatorConfigs();

        Iterator<RequestValidatorConfig> validatorConfigIterator = validatorConfigs.iterator();
        while (validatorConfigIterator.hasNext()) {
            RequestValidatorConfig validationConfig = validatorConfigIterator.next();
            if (getValidatorType().equalsIgnoreCase(validationConfig.getType())) {
                return validationConfig.getProperties();
            }
        }

        return new Properties();
    }
}
