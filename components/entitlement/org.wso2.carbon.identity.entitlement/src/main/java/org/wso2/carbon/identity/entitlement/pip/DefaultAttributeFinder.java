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
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.user.api.ClaimManager;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

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

    private static Log log = LogFactory.getLog(DefaultAttributeFinder.class);
    private Set<String> supportedAttrs = new HashSet<String>();

    /**
     * Loads all the claims defined under http://wso2.org/claims dialect.
     *
     * @throws Exception
     */
    public void init(Properties properties) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("DefaultAttributeFinder is initialized successfully");
        }
    }

    @Override
    public String getModuleName() {
        return "Default Attribute Finder";
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
