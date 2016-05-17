package org.wso2.carbon.identity.application.authentication.framework.processor.authenticator;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication.impl
        .AuthenticationResponse;
import org.wso2.carbon.identity.application.common.model.Property;

import java.util.List;

public interface ApplicationAuthenticator {


    public boolean canHandle(AuthenticationContext authenticationContext);


    public AuthenticationResponse process(AuthenticationContext authenticationContext);

    public String getContextIdentifier(AuthenticationContext authenticationContext);

    public String getName();


    public String getFriendlyName();


    public String getClaimDialectURI();

    public List<Property> getConfigurationProperties();
}
