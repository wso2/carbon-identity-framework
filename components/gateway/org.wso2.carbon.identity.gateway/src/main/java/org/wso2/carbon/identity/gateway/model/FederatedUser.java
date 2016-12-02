package org.wso2.carbon.identity.gateway.model;

import java.util.ArrayList;
import java.util.List;

public class FederatedUser extends User {

    private List<UserClaim> userClaims = new ArrayList<>();

    @Override
    public List<UserClaim> getUserClaims() {
        return this.userClaims;
    }

    public void setUserClaims(
            List<UserClaim> userClaims) {
        this.userClaims = userClaims;
    }
}
