package org.wso2.carbon.identity.gateway.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.gateway.api.FrameworkRuntimeException;
import org.wso2.carbon.identity.gateway.api.internal.GatewayResourceAPIComponent;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;

import java.util.HashSet;
import java.util.Set;

public class LocalUser extends User {

    private Logger log = LoggerFactory.getLogger(LocalUser.class);

    org.wso2.carbon.identity.mgt.User user;

    public LocalUser(org.wso2.carbon.identity.mgt.User user) {
        this.user = user;
    }

    @Override
    public String getUserIdentifier() {
        return user.getUniqueUserId();
    }

    @Override
    public Set<Claim> getClaims() {
        try {
            return new HashSet(user.getClaims());
        } catch (IdentityStoreException e) {
            log.error("Error while reading user claims from local identity store.", e);
            return new HashSet();
        } catch (UserNotFoundException e) {
            throw new FrameworkRuntimeException("User cannot be found in local identity store.", e);
        }
    }
}