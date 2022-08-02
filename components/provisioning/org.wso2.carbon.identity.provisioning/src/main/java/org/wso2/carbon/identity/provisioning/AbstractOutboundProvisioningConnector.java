/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.provisioning;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractOutboundProvisioningConnector implements Serializable {

    private static final long serialVersionUID = 8619915839101228583L;

    private static final String PROVISIONING_IDP = "IDP";
    private static final String PROVISIONING_TENANT = "TD";
    private static final String PROVISIONING_DOMAIN = "UD";
    private static final String PROVISIONING_USER = "UN";
    protected boolean jitProvisioningEnabled;

    /**
     * @param provisioningProperties
     * @throws IdentityProvisioningException
     */
    public abstract void init(Property[] provisioningProperties)
            throws IdentityProvisioningException;

    /**
     * @param provisioningEntity
     * @throws IdentityProvisioningException
     */
    public abstract ProvisionedIdentifier provision(ProvisioningEntity provisioningEntity)
            throws IdentityProvisioningException;

    /**
     * override only if needed - if claims are controlled by the identity provider, this will return
     * null. If it is connector specific this must return the corresponding claim dialect.
     *
     * @return
     * @throws IdentityProvisioningException
     */
    public String getClaimDialectUri() throws IdentityProvisioningException {
        return null;
    }

    /**
     * Override only if needed - if claims are controlled by the identity provider, this will return null.
     * If it is connector specific this must return the corresponding claim dialects.
     *
     * @return
     * @throws IdentityProvisioningException
     */
    public String[] getClaimDialectUris() throws IdentityProvisioningException {

        return null;
    }

    /**
     * @return
     * @throws IdentityProvisioningException
     */
    protected boolean isJitProvisioningEnabled() throws IdentityProvisioningException {
        return jitProvisioningEnabled;
    }

    /**
     * @param attributeMap
     * @return
     */
    protected List<String> getUserNames(Map<ClaimMapping, List<String>> attributeMap) {
        return ProvisioningUtil.getClaimValues(attributeMap,
                IdentityProvisioningConstants.USERNAME_CLAIM_URI, getUserStoreDomainName());
    }

    /**
     * @param attributeMap
     * @return
     */
    protected List<String> getGroupNames(Map<ClaimMapping, List<String>> attributeMap) {
        return ProvisioningUtil.getClaimValues(attributeMap,
                IdentityProvisioningConstants.GROUP_CLAIM_URI, getUserStoreDomainName());
    }

    /**
     * @param attributeMap
     * @return
     */
    protected String getPassword(Map<ClaimMapping, List<String>> attributeMap) {
        List<String> claimValue = ProvisioningUtil.getClaimValues(attributeMap,
                IdentityProvisioningConstants.PASSWORD_CLAIM_URI, null);

        if (CollectionUtils.isNotEmpty(claimValue) && claimValue.get(0) != null) {
            return claimValue.get(0);
        }

        return UUID.randomUUID().toString();

    }

    /**
     * @param attributeMap
     * @return claimValues
     */
    protected Map<String, String> getSingleValuedClaims(Map<ClaimMapping, List<String>> attributeMap) {

        Map<String, String> claimValues = new HashMap<>();

        for (Map.Entry<ClaimMapping, List<String>> entry : attributeMap.entrySet()) {
            ClaimMapping mapping = entry.getKey();
            if (mapping.getRemoteClaim() != null && mapping.getRemoteClaim().getClaimUri() != null) {
                String claimUri = mapping.getRemoteClaim().getClaimUri();

                if (!(IdentityProvisioningConstants.GROUP_CLAIM_URI.equals(claimUri)
                        || IdentityProvisioningConstants.PASSWORD_CLAIM_URI.equals(claimUri) || IdentityProvisioningConstants.USERNAME_CLAIM_URI
                        .equals(claimUri))) {
                    if (CollectionUtils.isNotEmpty(entry.getValue()) && entry.getValue().get(0) != null) {
                        claimValues.put(claimUri, entry.getValue().get(0));
                    } else {
                        claimValues.put(claimUri, mapping.getDefaultValue());
                    }
                }
            }
        }

        return claimValues;
    }

    /**
     * @return
     */
    protected String getUserStoreDomainName() {
        // return null by default. concrete implementations can override this value whenever
        // required.
        return null;
    }

    protected String buildUserId(ProvisioningEntity provisioningEntity, String provisioningPattern,
                                 String separator, String idpName) throws IdentityProvisioningException {

        Map<String, String> provValues = new HashMap<>();
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String username = provisioningEntity.getEntityName();
        String userStoreDomain = getDomainFromUserName(username);

        if (separator == null) {
            separator = "";
        }

        String provIdentifier = "";
        provValues.put(PROVISIONING_TENANT, tenantDomain.replaceAll(separator, ""));

        if (username != null) {
            provValues.put(PROVISIONING_USER, removeDomainFromUserName(username));
        }
        provValues.put(PROVISIONING_IDP, idpName.replaceAll(separator, ""));

        if (userStoreDomain != null) {
            provValues.put(PROVISIONING_DOMAIN, userStoreDomain.replaceAll(separator, ""));
        }

        String[] provisioningEntries = buildProvisioningEntries(provisioningPattern);

        for (int i = 0; i < provisioningEntries.length; i++) {
            if (StringUtils.isNotBlank(provisioningEntries[i])) {
                if (StringUtils.isBlank(provIdentifier)) {
                    provIdentifier = provValues.get(provisioningEntries[i].trim());
                } else {
                    provIdentifier = provIdentifier.concat(separator).concat(provValues.get(provisioningEntries[i].trim()));
                }
            }
        }

        return provIdentifier.toLowerCase();
    }

    private String[] buildProvisioningEntries(String provisioningPattern) throws IdentityProvisioningException {

        if (!provisioningPattern.contains("{") || !provisioningPattern.contains("}")) {
            throw new IdentityProvisioningException("Invalid Provisioning Pattern");
        }

        String provisioningPatternWithoutCurlBrace = provisioningPattern.replaceAll("\\{", "").replaceAll("\\}", "");
        return provisioningPatternWithoutCurlBrace.split(",");
    }

    private String getDomainFromUserName(String username) {
        int index;
        String domain = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
        if (StringUtils.isNotBlank(username)) {
            if ((index = username.indexOf("/")) > 0) {
                 domain = username.substring(0, index);
            }
            return domain;
        }
        return domain;
    }

    private String removeDomainFromUserName(String username) {
        int index;
        if ((index = username.indexOf(CarbonConstants.DOMAIN_SEPARATOR)) >= 0) {
            // remove domain name if exist
            username = username.substring(index + 1);
        }
        return username;
    }

}
