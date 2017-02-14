package org.wso2.carbon.identity.gateway.common.model.sp;

import java.util.ArrayList;
import java.util.List;

public class AuthenticationStepConfig {

    private String useLocalSubjectIdentifier;
    private String useDomainInLocalSubjectIdentifier;
    private String authStrategy;
    private List<IdentityProvider> identityProviders = new ArrayList<>();

    public String getAuthStrategy() {
        return authStrategy;
    }

    public void setAuthStrategy(String authStrategy) {
        this.authStrategy = authStrategy;
    }

    public List<IdentityProvider> getIdentityProviders() {
        return identityProviders;
    }

    public void setIdentityProviders(List<IdentityProvider> identityProviders) {
        this.identityProviders = identityProviders;
    }

    public String getUseDomainInLocalSubjectIdentifier() {
        return useDomainInLocalSubjectIdentifier;
    }

    public void setUseDomainInLocalSubjectIdentifier(String useDomainInLocalSubjectIdentifier) {
        this.useDomainInLocalSubjectIdentifier = useDomainInLocalSubjectIdentifier;
    }

    public String getUseLocalSubjectIdentifier() {
        return useLocalSubjectIdentifier;
    }

    public void setUseLocalSubjectIdentifier(String useLocalSubjectIdentifier) {
        this.useLocalSubjectIdentifier = useLocalSubjectIdentifier;
    }
}