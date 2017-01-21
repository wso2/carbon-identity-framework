package org.wso2.carbon.identity.gateway.processor.authenticator;

import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.AuthenticationResponse;

import java.util.List;
import java.util.Properties;

public interface ApplicationAuthenticator {


    public boolean canHandle(AuthenticationContext authenticationContext);


    public AuthenticationResponse process(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException;

    public String getContextIdentifier(AuthenticationContext authenticationContext);

    public String getName();


    public String getFriendlyName();


    public String getClaimDialectURI();

    public List<Properties> getConfigurationProperties();
}
