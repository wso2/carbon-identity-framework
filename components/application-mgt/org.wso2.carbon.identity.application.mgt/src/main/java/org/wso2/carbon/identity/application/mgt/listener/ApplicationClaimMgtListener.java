/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.mgt.listener;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationDAOImpl;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataClientException;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.listener.AbstractClaimMetadataMgtListener;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.CLAIM_PROFILE_PROPERTY_DELIMITER;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_LOCAL_CLAIM_REFERRED_BY_APPLICATION;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_LOCAL_CLAIM_REQUESTED_IN_APPLICATIONS;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.PROFILES_CLAIM_PROPERTY_PREFIX;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY;

/**
 * Internal implementation of {@link AbstractClaimMetadataMgtListener} to listen to claim CRUD events.
 * Changes the Application/Service Provider data according to claim changes.
 */
public class ApplicationClaimMgtListener extends AbstractClaimMetadataMgtListener {

    private static ApplicationDAOImpl applicationDAO = new ApplicationDAOImpl();

    @Override
    public int getDefaultOrderId() {

        return 21;
    }

    @Override
    public boolean doPreDeleteClaim(String claimUri, String tenantDomain) throws ClaimMetadataException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        try {
            if (applicationDAO.isClaimReferredByAnySp(null, claimUri, tenantId)) {
                throw new ClaimMetadataClientException(ERROR_CODE_LOCAL_CLAIM_REFERRED_BY_APPLICATION.getCode(),
                        ERROR_CODE_LOCAL_CLAIM_REFERRED_BY_APPLICATION.getMessage());
            }
        } catch (IdentityApplicationManagementException e) {
            throw new ClaimMetadataException("Error when deleting claim.", e);
        }
        return true;
    }

    @Override
    public boolean doPreUpdateLocalClaim(LocalClaim localClaim, String tenantDomain) throws ClaimMetadataException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        String isSignInAssertionSupportedByDefault = localClaim.getClaimProperty(PROFILES_CLAIM_PROPERTY_PREFIX +
                ClaimConstants.DefaultAllowedClaimProfile.SIGN_IN_ASSERTION.getProfileName() +
                CLAIM_PROFILE_PROPERTY_DELIMITER + SUPPORTED_BY_DEFAULT_PROPERTY);
        if (StringUtils.isBlank(isSignInAssertionSupportedByDefault)
                || Boolean.parseBoolean(isSignInAssertionSupportedByDefault)) {
            return true;
        }
        try {
            if (applicationDAO.isClaimRequestedByAnySp(null, localClaim.getClaimURI(), tenantId)) {
                throw new ClaimMetadataClientException(ERROR_CODE_LOCAL_CLAIM_REQUESTED_IN_APPLICATIONS.getCode(),
                        String.format(ERROR_CODE_LOCAL_CLAIM_REQUESTED_IN_APPLICATIONS.getMessage(),
                                localClaim.getClaimURI()));
            }
        } catch (IdentityApplicationManagementException e) {
            throw new ClaimMetadataException("Error when updating claim.", e);
        }
        return true;
    }
}
