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

package org.wso2.carbon.identity.input.validation.mgt.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.input.validation.mgt.internal.InputValidationDataHolder;
import org.wso2.carbon.identity.input.validation.mgt.model.LabelValue;
import org.wso2.carbon.user.core.UserStoreClientException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_INVALID_ATTRIBUTE_VALUE_TYPE;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_NOT_ALLOWED_ATTRIBUTE_VALUE;

/**
 * User operation event listener to validate data types of user claims.
 */
public class DataTypeValidationListener extends AbstractIdentityUserOperationEventListener {

    private static final Log LOG = LogFactory.getLog(DataTypeValidationListener.class);

    @Override
    public int getExecutionOrderId() {

        int orderId = getOrderId();
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }
        return 197;
    }

    @Override
    public boolean isEnable() {

        IdentityEventListenerConfig eventListenerConfig = IdentityUtil.readEventListenerProperty
                (UserOperationEventListener.class.getName(), this.getClass().getName());
        if (eventListenerConfig == null) {
            return false;
        }
        return Boolean.parseBoolean(eventListenerConfig.getEnable());
    }

    @Override
    public boolean doPreAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
                                String profile, UserStoreManager userStoreManager) throws UserStoreException {

        return doPreSetUserClaimValues(userName, claims, profile, userStoreManager);
    }

    @Override
    public boolean doPreSetUserClaimValue(String userName, String claimURI, String claimValue, String profile,
                                          UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            Optional<LocalClaim> localClaim = InputValidationDataHolder.getInstance()
                    .getClaimMetadataManagementService().getLocalClaim(claimURI, tenantDomain);
            if (localClaim.isPresent()) {
                String dataType = localClaim.get().getClaimProperty(ClaimConstants.DATA_TYPE_PROPERTY);
                validateDataType(claimURI, claimValue, dataType);

                // For the options data type, validate the canonical values if defined.
                String canonicalValues = localClaim.get().getClaimProperty(ClaimConstants.CANONICAL_VALUES_PROPERTY);
                validateCanonicalValues(claimURI, claimValue, canonicalValues);
            }
        } catch (ClaimMetadataException e) {
            throw new UserStoreException(e);
        }
        return true;
    }

    @Override
    public boolean doPreSetUserClaimValues(String userName, Map<String, String> claims, String profile,
                                           UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        Set<String> updatedClaims = claims.keySet();
        try {
            List<LocalClaim> localClaims = InputValidationDataHolder.getInstance().getClaimMetadataManagementService()
                    .getLocalClaims(tenantDomain);
            localClaims = localClaims.stream().filter(claim -> updatedClaims.contains(claim.getClaimURI()))
                    .collect(Collectors.toList());
            for (LocalClaim localClaim : localClaims) {
                String dataType = localClaim.getClaimProperty(ClaimConstants.DATA_TYPE_PROPERTY);
                String claimValue = claims.get(localClaim.getClaimURI());
                validateDataType(localClaim.getClaimURI(), claimValue, dataType);

                // For the options data type, validate the canonical values if defined.
                String canonicalValues = localClaim.getClaimProperty(ClaimConstants.CANONICAL_VALUES_PROPERTY);
                validateCanonicalValues(localClaim.getClaimURI(), claimValue, canonicalValues);
            }
        } catch (ClaimMetadataException e) {
            throw new UserStoreException(e);
        }
        return true;
    }

    private void validateDataType(String claim, String value, String dataType) throws UserStoreClientException {

        if (StringUtils.isEmpty(dataType)) {
            return;
        }

        if (ClaimConstants.ClaimDataType.INTEGER.name().equalsIgnoreCase(dataType)) {
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException e) {
                handleDataTypeMismatchClientException(claim, dataType);
            }
        } else if (ClaimConstants.ClaimDataType.DECIMAL.name().equalsIgnoreCase(dataType) &&
                !NumberUtils.isNumber(value)) {
            handleDataTypeMismatchClientException(claim, dataType);
        }
    }

    private void validateCanonicalValues(String claim, String value, String canonicalValues)
            throws UserStoreClientException {

        if (StringUtils.isEmpty(canonicalValues)) {
            return;
        }

        String allowedValues;
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<LabelValue> list = mapper.readValue(canonicalValues, mapper.getTypeFactory()
                    .constructCollectionType(List.class, LabelValue.class));
            for (LabelValue labelValue : list) {
                if (labelValue.getValue().equals(value)) {
                    return; // Valid value found.
                }
            }
            allowedValues = list.stream().map(LabelValue::getValue).collect(Collectors.joining(", "));
        } catch (JsonProcessingException e) {
            LOG.error("Error while parsing canonical values for claim: " + claim, e);
            return;
        }
        throw new UserStoreClientException(String.format(ERROR_NOT_ALLOWED_ATTRIBUTE_VALUE.getDescription(), value,
                claim, allowedValues), ERROR_NOT_ALLOWED_ATTRIBUTE_VALUE.getCode());
    }

    private void handleDataTypeMismatchClientException(String claim, String dataType) throws UserStoreClientException {

        throw new UserStoreClientException(String.format(ERROR_INVALID_ATTRIBUTE_VALUE_TYPE.getDescription(), claim,
                dataType), ERROR_INVALID_ATTRIBUTE_VALUE_TYPE.getCode());
    }
}
