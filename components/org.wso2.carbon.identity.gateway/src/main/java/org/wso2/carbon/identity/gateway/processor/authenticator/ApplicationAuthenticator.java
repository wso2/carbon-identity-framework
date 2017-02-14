package org.wso2.carbon.identity.gateway.processor.authenticator;

import org.wso2.carbon.identity.claim.exception.ClaimResolvingServiceException;
import org.wso2.carbon.identity.claim.exception.ProfileMgtServiceException;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.AuthenticationResponse;
import org.wso2.carbon.identity.mgt.claim.Claim;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

public interface ApplicationAuthenticator {


    public boolean canHandle(AuthenticationContext authenticationContext);


    public AuthenticationResponse process(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException;

    public String getContextIdentifier(AuthenticationContext authenticationContext);

    public String getName();


    public String getFriendlyName();


    public String getClaimDialectURI();

    public List<Properties> getConfigurationProperties();

    public Set<Claim> getMappedRootClaims(Set<Claim> claims, Optional<String> profile, Optional<String> dialect)
            throws AuthenticationHandlerException;
}
