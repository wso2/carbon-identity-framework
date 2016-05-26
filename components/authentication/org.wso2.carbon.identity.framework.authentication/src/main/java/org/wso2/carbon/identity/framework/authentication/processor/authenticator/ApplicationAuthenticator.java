package org.wso2.carbon.identity.framework.authentication.processor.authenticator;

import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.framework.authentication.context.AuthenticationContext;
import org.wso2.carbon.identity.framework.authentication.processor.handler.authentication
        .AuthenticationHandlerException;
import org.wso2.carbon.identity.framework.authentication.processor.handler.authentication.impl.AuthenticationResponse;

import java.util.List;

public interface ApplicationAuthenticator {


    public boolean canHandle(AuthenticationContext authenticationContext);


    public AuthenticationResponse process(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException;

    public String getContextIdentifier(AuthenticationContext authenticationContext);

    public String getName();


    public String getFriendlyName();


    public String getClaimDialectURI();

    public List<Property> getConfigurationProperties();
}
