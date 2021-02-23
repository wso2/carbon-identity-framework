package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal;

import org.apache.commons.lang.StringUtils;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

/**
 * Represent the user's runtime claims for GraalJs Execution.
 */
public class GraalJsRuntimeClaims extends GraalJsClaims implements ProxyObject {

    public GraalJsRuntimeClaims(AuthenticationContext context, int step, String idp) {

        super(context, step, idp, false);
    }

    public GraalJsRuntimeClaims(AuthenticationContext context, AuthenticatedUser user) {

        super(context, user, false);
    }

    @Override
    public Object getMember(String claimUri) {

        if (authenticatedUser != null) {
            return getRuntimeClaim(claimUri);
        }
        return null;
    }

    @Override
    public boolean hasMember(String claimUri) {

        if (authenticatedUser != null) {
            return hasRuntimeClaim(claimUri);
        }
        return false;
    }

    @Override
    public void putMember(String claimUri, Value claimValue) {

        if (authenticatedUser != null) {
            setRuntimeClaim(claimUri, String.valueOf(claimValue));
        }
    }

    private Object getRuntimeClaim(String claimUri) {

        String runtimeClaimValue = getContext().getRuntimeClaim(claimUri);
        if (runtimeClaimValue != null) {
            return runtimeClaimValue;
        }
        if (isFederatedIdP()) {
            return getFederatedClaim(claimUri);
        }
        return getLocalClaim(claimUri);
    }

    private boolean hasRuntimeClaim(String claimUri) {

        String claim = getContext().getRuntimeClaim(claimUri);
        if (claim != null) {
            return true;
        }
        if (isFederatedIdP()) {
            return hasFederatedClaim(claimUri);
        }
        return hasLocalClaim(claimUri);
    }

    private void setRuntimeClaim(String claimUri, String claimValue) {
        String claimValueAsString = String.valueOf(claimValue);
        if (claimValueAsString == null) {
            claimValueAsString = StringUtils.EMPTY;
        }
        getContext().addRuntimeClaim(claimUri, String.valueOf(claimValueAsString));
    }
}
