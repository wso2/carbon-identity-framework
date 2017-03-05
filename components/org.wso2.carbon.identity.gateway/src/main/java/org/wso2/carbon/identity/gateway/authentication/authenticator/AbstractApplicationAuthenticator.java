/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.gateway.authentication.authenticator;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.claim.exception.ClaimResolvingServiceException;
import org.wso2.carbon.identity.claim.exception.ProfileMgtServiceException;
import org.wso2.carbon.identity.claim.mapping.profile.ClaimConfigEntry;
import org.wso2.carbon.identity.claim.mapping.profile.ProfileEntry;
import org.wso2.carbon.identity.claim.service.ClaimResolvingService;
import org.wso2.carbon.identity.claim.service.ProfileMgtService;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.internal.GatewayServiceHolder;
import org.wso2.carbon.identity.gateway.exception.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.authentication.AuthenticationResponse;
import org.wso2.carbon.identity.mgt.claim.Claim;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractApplicationAuthenticator implements ApplicationAuthenticator {
    private static final long serialVersionUID = -4406878411547612129L;
    private static final Logger log = LoggerFactory.getLogger(AbstractApplicationAuthenticator.class);


    @Override
    public AuthenticationResponse process(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        AuthenticationResponse authenticationResponse = AuthenticationResponse.INCOMPLETE;
        if (isInitialRequest(authenticationContext)) {
            authenticationResponse = processRequest(authenticationContext);
        } else {
            authenticationResponse = processResponse(authenticationContext);
        }
        return authenticationResponse;
    }

    protected abstract boolean isInitialRequest(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException;

    protected abstract AuthenticationResponse processRequest(AuthenticationContext context)
            throws AuthenticationHandlerException;

    protected abstract AuthenticationResponse processResponse(AuthenticationContext context)
            throws AuthenticationHandlerException;

    public Set<Claim> getMappedRootClaims(Set<Claim> claims, Optional<String> profile, Optional<String> dialect)
            throws AuthenticationHandlerException {

        try {
            final Set<Claim> transformedClaims = new HashSet<>();

            String claimDialect;
            if (dialect.isPresent()) {
                claimDialect = dialect.get();
            } else {
                claimDialect = claims.stream().findFirst().get().getDialectUri();
            }
            ClaimResolvingService claimResolvingService = GatewayServiceHolder.getInstance()
                    .getClaimResolvingService();
            Map<String, String> claimMapping = claimResolvingService.getClaimMapping(claimDialect);

            Set<Claim> transformedClaimsTmp = new HashSet<>();
            claims.stream().filter(claim -> claimMapping.containsKey(claim.getClaimUri()))
                    .forEach(claim -> {
                        //#TODO:replace root dialect URI
                        Claim tmpClaim = new Claim("rootdialect", claimMapping.get(claim.getClaimUri()), claim.getValue());
                        transformedClaimsTmp.add(tmpClaim);
                    });

            if (profile.isPresent()) {
                ProfileMgtService profileMgtService = GatewayServiceHolder.getInstance().getProfileMgtService();
                // fix null pointer for profile.get()
                ProfileEntry profileEntry = profileMgtService.getProfile(profile.get());
                List<ClaimConfigEntry> profileClaims = profileEntry.getClaims();
                Map<String, ClaimConfigEntry> profileClaimMap = new HashMap<>();
                profileClaims.forEach(profileClaim -> profileClaimMap.put(profileClaim.getClaimURI(), profileClaim));

                transformedClaimsTmp.stream().filter(claim -> profileClaimMap.containsKey(claim.getClaimUri()))
                        .forEach(transformedClaims::add);
                return transformedClaims;
            }
            return transformedClaimsTmp;
        } catch (ClaimResolvingServiceException | ProfileMgtServiceException e) {
            throw new AuthenticationHandlerException(e.getMessage(), e);
        }
    }



































    /*
    public AuthenticatorFlowStatus process(HttpServletRequest request,
                                           HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException, LogoutFailedException {

        if (!canHandle(request)
            || (request.getAttribute(FrameworkConstants.REQ_ATTR_HANDLED) != null && ((Boolean) request
                .getAttribute(FrameworkConstants.REQ_ATTR_HANDLED)))) {
            initiateAuthenticationRequest(request, response, context);
            context.setCurrentAuthenticator(getName());
            return AuthenticatorFlowStatus.INCOMPLETE;
        } else {
            try {
                processAuthenticationResponse(request, response, context);
                if (this instanceof LocalApplicationAuthenticator) {
                    if (!context.getSequenceConfig().getApplicationConfig().isSaaSApp()) {
                        String userDomain = context.getSubject().getTenantDomain();
                        String tenantDomain = context.getTenantDomain();
                        if (!StringUtils.equals(userDomain, tenantDomain)) {
                            context.setProperty("UserTenantDomainMismatch", true);
                            throw new AuthenticationFailedException("Service Provider tenant domain must be " +
                                                                    "equal to user tenant domain for non-SaaS " +
                                                                    "applications");
                        }
                    }
                }
                request.setAttribute(FrameworkConstants.REQ_ATTR_HANDLED, true);
                return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
            } catch (AuthenticationFailedException e) {
                Map<Integer, Step> stepMap = context.getSequenceConfig().getStepMap();
                boolean stepHasMultiOption = false;

                if (stepMap != null && !stepMap.isEmpty()) {
                    Step stepConfig = stepMap.get(context.getCurrentStep());

                    if (stepConfig != null) {
                        stepHasMultiOption = stepConfig.isMultiOption();
                    }
                }

                if (retryAuthenticationEnabled() && !stepHasMultiOption) {
                    context.setRetrying(true);
                    context.setCurrentAuthenticator(getName());
                    initiateAuthenticationRequest(request, response, context);
                    return AuthenticatorFlowStatus.INCOMPLETE;
                } else {
                    throw e;
                }
            }
        }

    }
*/

    /*
    protected void initiateAuthenticationRequest(HttpServletRequest request,
                                                 HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {
    }

    protected abstract void processAuthenticationResponse(HttpServletRequest request,
                                                          HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException;

    protected void initiateLogoutRequest(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationContext context) throws LogoutFailedException {
        throw new UnsupportedOperationException();
    }

    protected void processLogoutResponse(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationContext context) throws LogoutFailedException {
        throw new UnsupportedOperationException();
    }

    protected boolean retryAuthenticationEnabled() {
        return false;
    }

    @Override
    public String getClaimDialectURI() {
        return null;
    }

    @Override
    public List<Property> getConfigurationProperties() {
        return new ArrayList<Property>();
    }

    protected String getUserStoreAppendedName(String userName) {
        if (!userName.contains(CarbonConstants.DOMAIN_SEPARATOR) && UserCoreUtil.getDomainFromThreadLocal() != null
            && !"".equals(UserCoreUtil.getDomainFromThreadLocal())) {
            userName = UserCoreUtil.getDomainFromThreadLocal() + CarbonConstants.DOMAIN_SEPARATOR + userName;
        }
        return userName;
    }

    */
}
