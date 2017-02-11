package org.wso2.carbon.identity.gateway.model;

import org.wso2.carbon.identity.mgt.claim.Claim;

import java.util.HashSet;
import java.util.Set;

public class FederatedUser extends User {

    private String identifier;
    private Set<Claim> claims = new HashSet();

    public FederatedUser(String identifier) {
        this.identifier = identifier;
    }

    public FederatedUser(String identifier, Set<Claim> claims) {
        this.identifier = identifier;
        this.claims = claims;
    }

    @Override
    public String getUserIdentifier() {
        return identifier;
    }

    @Override
    public Set<Claim> getClaims() {
        return this.claims;
    }

    public void setUserClaims(Set<Claim> claims) {
        this.claims = claims;
    }
}
