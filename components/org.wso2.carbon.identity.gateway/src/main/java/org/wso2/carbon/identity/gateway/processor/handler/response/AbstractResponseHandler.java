package org.wso2.carbon.identity.gateway.processor.handler.response;


import org.wso2.carbon.identity.gateway.api.FrameworkHandlerResponse;
import org.wso2.carbon.identity.gateway.common.model.sp.ResponseBuilderConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.ResponseBuildingConfig;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandlerException;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public abstract class AbstractResponseHandler extends FrameworkHandler {

    public abstract FrameworkHandlerResponse buildErrorResponse(AuthenticationContext authenticationContext)
            throws ResponseException;

    public abstract FrameworkHandlerResponse buildResponse(AuthenticationContext authenticationContext)
            throws ResponseException;

    protected abstract String getValidatorType();


    public Properties getResponseBuilderConfigs(AuthenticationContext authenticationContext) throws
            AuthenticationHandlerException {

        if (authenticationContext.getServiceProvider() == null) {
            throw new AuthenticationHandlerException("Error while getting validator configs : No service provider " +
                    "found with uniqueId : " + authenticationContext.getUniqueId());
        }

        ResponseBuildingConfig responseBuildingConfig = authenticationContext.getServiceProvider()
                .getResponseBuildingConfig();
        List<ResponseBuilderConfig> responseBuilderConfigs = responseBuildingConfig.getResponseBuilderConfigs();

        Iterator<ResponseBuilderConfig> responseBuilderConfigIterator = responseBuilderConfigs.iterator();
        while (responseBuilderConfigIterator.hasNext()) {
            ResponseBuilderConfig responseBuilderConfig = responseBuilderConfigIterator.next();
            if (getValidatorType().equalsIgnoreCase(responseBuilderConfig.getType())) {
                return responseBuilderConfig.getProperties();
            }
        }
        return new Properties();
    }

}
