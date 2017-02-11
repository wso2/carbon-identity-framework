package org.wso2.carbon.identity.gateway.model;

import org.wso2.carbon.identity.mgt.claim.Claim;

import java.io.Serializable;
import java.util.Set;

public abstract class User implements Serializable {

    public abstract String getUserIdentifier();

    public abstract Set<Claim> getClaims();

}