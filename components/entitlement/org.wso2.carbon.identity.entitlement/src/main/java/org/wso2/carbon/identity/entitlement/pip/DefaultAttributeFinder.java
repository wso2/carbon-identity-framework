/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.entitlement.pip;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.attr.BagAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.user.api.ClaimManager;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.net.URI;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * DefaultAttributeFinder talks to the underlying user store to read user attributes.
 * DefaultAttributeFinder is by default registered for all the claims defined under
 * http://wso2.org/claims dialect.
 */
public class DefaultAttributeFinder extends AbstractPIPAttributeFinder {

    private static final Log log = LogFactory.getLog(DefaultAttributeFinder.class);
    private Set<String> supportedAttrs = new HashSet<String>();
    private boolean mapFederatedUsersToLocal = false;
    private static final String MAP_FEDERATED_USERS_TO_LOCAL = "MapFederatedUsersToLocal";
    private static final String FEDERATED_USER_DOMAIN = "FEDERATED";

    /**
     * Loads all the claims defined under http://wso2.org/claims dialect.
     *
     * @throws Exception
     */
    public void init(Properties properties) throws Exception {

        mapFederatedUsersToLocal = Boolean.parseBoolean(properties.getProperty(MAP_FEDERATED_USERS_TO_LOCAL));
        if (log.isDebugEnabled()) {
            log.debug("DefaultAttributeFinder is initialized successfully");
        }
    }

    @Override
    public String getModuleName() {
        return "Default Attribute Finder";
    }

    /**
     * This method is introduced in order to check whether the user is local or federated. If it is a
     * federated user, obtaining user attributes from userstore will be prevented.
     *
     * @param attributeType The type of the required attribute.
     * @param attributeId   The unique id of the required attribute.
     * @param category      The category of the required attribute.
     * @param issuer        The attribute issuer.
     * @param evaluationCtx The evaluation context object.
     * @return return the set of values for the required attribute.
     * @throws Exception throws if fails.
     */
    @Override
    public Set<String> getAttributeValues(URI attributeType, URI attributeId, URI category,
                                          String issuer, EvaluationCtx evaluationCtx) throws Exception {

        Set<String> values = null;
        EvaluationResult userType = evaluationCtx.getAttribute(new URI(StringAttribute.identifier), new URI(
                PDPConstants.USER_TYPE_ID), issuer, new URI(PDPConstants.USER_CATEGORY));
        String userTypeId = null;
        if (userType != null && userType.getAttributeValue() != null && userType.getAttributeValue().isBag()) {
            BagAttribute bagAttribute = (BagAttribute) userType.getAttributeValue();
            if (bagAttribute.size() > 0) {
                userTypeId = ((AttributeValue) bagAttribute.iterator().next()).encode();
                if (log.isDebugEnabled()) {
                    log.debug(String.format("The user type of the user is %s", userTypeId));
                }
            }
        }

        if (!StringUtils.equalsIgnoreCase(userTypeId, FEDERATED_USER_DOMAIN)) {
            // If the user is not a federated user, user attributes should be be populated from local userstore.
            values = super.getAttributeValues(attributeType, attributeId, category, issuer, evaluationCtx);
        } else if (mapFederatedUsersToLocal) {
            // If the user is federated and the MapFederatedToLocal config is enabled, then populate user attributes
            // from userstore.
            values = super.getAttributeValues(attributeType, attributeId, category, issuer, evaluationCtx);
        }
        return values;
    }
        /*
     * (non-Javadoc)
	 * 
	 * @see
	 * org.wso2.carbon.identity.entitlement.pip.PIPAttributeFinder#getAttributeValues(java.lang.
	 * String, java.lang.String, java.lang.String)
	 */
    public Set<String> getAttributeValues(String subjectId, String resourceId, String actionId,
                                          String environmentId, String attributeId, String issuer) throws Exception {
        Set<String> values = new HashSet<String>();

        if (log.isDebugEnabled()) {
            log.debug("Retrieving attribute values of subjectId \'" + subjectId + "\'with attributeId \'" +
                    attributeId + "\'");
        }
        if (StringUtils.isEmpty(subjectId)) {
            if (log.isDebugEnabled()) {
                log.debug("subjectId value is null or empty. Returning empty attribute set");
            }
            return values;
        }
        subjectId = MultitenantUtils.getTenantAwareUsername(subjectId);
        if (UserCoreConstants.ClaimTypeURIs.ROLE.equals(attributeId)) {
            if (log.isDebugEnabled()) {
                log.debug("Looking for roles via DefaultAttributeFinder");
            }
            String[] roles = CarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager()
                    .getRoleListOfUser(subjectId);
            if (roles != null && roles.length > 0) {
                for (String role : roles) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("User %1$s belongs to the Role %2$s", subjectId,
                                role));
                    }
                    values.add(role);
                }
            }
        } else {
            String claimValue = null;
            try {
                claimValue = CarbonContext.getThreadLocalCarbonContext().getUserRealm().
                        getUserStoreManager().getUserClaimValue(subjectId, attributeId, null);
                if (log.isDebugEnabled()) {
                    log.debug("Claim \'" + claimValue + "\' retrieved for attributeId \'" + attributeId + "\' " +
                            "for subjectId \'" + subjectId + "\'");
                }
            } catch (UserStoreException e) {
                if(e.getMessage().startsWith(IdentityCoreConstants.USER_NOT_FOUND)){
                    if(log.isDebugEnabled()){
                        log.debug("User: " + subjectId + " not found in user store");
                    }
                } else {
                    throw e;
                }
            }
            if (claimValue == null && log.isDebugEnabled()) {
                log.debug(String.format("Request attribute %1$s not found", attributeId));
            }
            // Fix for multiple claim values
            if (claimValue != null) {
                String claimSeparator = CarbonContext.getThreadLocalCarbonContext().getUserRealm().
                        getRealmConfiguration().getUserStoreProperty(IdentityCoreConstants.MULTI_ATTRIBUTE_SEPARATOR);
                if (StringUtils.isBlank(claimSeparator)) {
                    claimSeparator = IdentityCoreConstants.MULTI_ATTRIBUTE_SEPARATOR_DEFAULT;
                }
                if (claimValue.contains(claimSeparator)) {
                    StringTokenizer st = new StringTokenizer(claimValue, claimSeparator);
                    while (st.hasMoreElements()) {
                        String attributeValue = st.nextElement().toString();
                        if (StringUtils.isNotBlank(attributeValue)) {
                            values.add(attributeValue);
                        }
                    }
                } else {
                    values.add(claimValue);
                }
            }
        }
        return values;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.identity.entitlement.pip.PIPAttributeFinder#getSupportedAttributes()
     */
    public Set<String> getSupportedAttributes() {
        try {
            ClaimManager claimManager = CarbonContext.getThreadLocalCarbonContext().getUserRealm().getClaimManager();
            ClaimMapping[] claims = claimManager
                    .getAllClaimMappings(UserCoreConstants.DEFAULT_CARBON_DIALECT);
            for (ClaimMapping claim : claims) {
                supportedAttrs.add(claim.getClaim().getClaimUri());
            }
        } catch (Exception e) {
            //ignore
        }
        return supportedAttrs;
    }
}
