/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.vc.config.management;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.vc.config.management.constant.VCConfigManagementConstants;
import org.wso2.carbon.identity.vc.config.management.dao.VCOfferDAO;
import org.wso2.carbon.identity.vc.config.management.dao.impl.VCOfferDAOImpl;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtClientException;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtException;
import org.wso2.carbon.identity.vc.config.management.model.VCOffer;

import java.util.List;

/**
 * Implementation of {@link VCOfferManager}.
 */
public class VCOfferManagerImpl implements VCOfferManager {

    private static final Log LOG = LogFactory.getLog(VCOfferManagerImpl.class);
    private static final VCOfferManager INSTANCE = new VCOfferManagerImpl();
    private final VCOfferDAO dao = new VCOfferDAOImpl();

    private VCOfferManagerImpl() {

    }

    /**
     * Get the singleton instance.
     *
     * @return VCOfferManager instance.
     */
    public static VCOfferManager getInstance() {

        return INSTANCE;
    }

    @Override
    public List<VCOffer> list(String tenantDomain) throws VCConfigMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Listing VC offers for tenant: " + tenantDomain);
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return dao.list(tenantId);
    }

    @Override
    public VCOffer get(String offerId, String tenantDomain) throws VCConfigMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting VC offer with ID: " + offerId + " for tenant: " + tenantDomain);
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return dao.get(offerId, tenantId);
    }

    @Override
    public VCOffer add(VCOffer offer, String tenantDomain) throws VCConfigMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Adding new VC offer for tenant: " + tenantDomain);
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        validateDisplayName(offer);
        validateCredentialConfigurationIds(offer);
        return dao.add(offer, tenantId);
    }

    @Override
    public VCOffer update(String offerId, VCOffer offer, String tenantDomain) throws VCConfigMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Updating VC offer with ID: " + offerId + " for tenant: " + tenantDomain);
        }
        if (offer.getOfferId() != null && !StringUtils.equals(offerId, offer.getOfferId())) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_OFFER_ID_MISMATCH.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_OFFER_ID_MISMATCH.getMessage());
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Check if offer exists.
        VCOffer existing = dao.get(offerId, tenantId);
        if (existing == null) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_OFFER_NOT_FOUND.getCode(),
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_OFFER_NOT_FOUND.getMessage());
        }

        // Normalize display name.
        String newDisplayName = offer.getDisplayName();
        if (StringUtils.isBlank(newDisplayName)) {
            offer.setDisplayName(existing.getDisplayName());
        } else {
            validateDisplayName(offer);
        }

        validateCredentialConfigurationIds(offer);
        return dao.update(offerId, offer, tenantId);
    }

    @Override
    public void delete(String offerId, String tenantDomain) throws VCConfigMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting VC offer with ID: " + offerId + " for tenant: " + tenantDomain);
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        dao.delete(offerId, tenantId);
    }

    /**
     * Validate display name.
     *
     * @param offer VC offer.
     * @throws VCConfigMgtClientException on validation errors.
     */
    private void validateDisplayName(VCOffer offer) throws VCConfigMgtClientException {

        if (StringUtils.isBlank(offer.getDisplayName())) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                    "Display name cannot be empty.");
        }
    }

    /**
     * Validate credential configuration IDs.
     *
     * @param offer VC offer.
     * @throws VCConfigMgtClientException on validation errors.
     */
    private void validateCredentialConfigurationIds(VCOffer offer) throws VCConfigMgtClientException {

        if (offer.getCredentialConfigurationIds() == null || offer.getCredentialConfigurationIds().isEmpty()) {
            throw new VCConfigMgtClientException(
                    VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                    "Credential configuration IDs cannot be empty.");
        }

        for (String configId : offer.getCredentialConfigurationIds()) {
            if (StringUtils.isBlank(configId)) {
                throw new VCConfigMgtClientException(
                        VCConfigManagementConstants.ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode(),
                        "Credential configuration ID cannot be empty.");
            }
        }
    }
}

