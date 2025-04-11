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

package org.wso2.carbon.identity.user.pre.update.profile.action.internal.management;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverClientException;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverException;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverServerException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionProperty;
import org.wso2.carbon.identity.action.management.api.model.BinaryObject;
import org.wso2.carbon.identity.action.management.api.service.ActionDTOModelResolver;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.component.PreUpdateProfileActionServiceComponentHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.wso2.carbon.identity.user.pre.update.profile.action.internal.constant.PreUpdateProfileActionConstants.ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.profile.action.internal.constant.PreUpdateProfileActionConstants.MAX_ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.profile.action.internal.constant.PreUpdateProfileActionConstants.ROLE_CLAIM_URI;

/**
 * This class implements the methods required to resolve ActionDTO objects in Pre Update Profile extension.
 */
public class PreUpdateProfileActionDTOModelResolver implements ActionDTOModelResolver {

    private static final Log LOG = LogFactory.getLog(PreUpdateProfileActionDTOModelResolver.class);

    @Override
    public Action.ActionTypes getSupportedActionType() {

        return Action.ActionTypes.PRE_UPDATE_PROFILE;
    }

    @Override
    public ActionDTO resolveForAddOperation(ActionDTO actionDTO, String tenantDomain)
            throws ActionDTOModelResolverException {

        Map<String, ActionProperty> properties = new HashMap<>();
        Object attributes = actionDTO.getPropertyValue(ATTRIBUTES);
        // Attributes is an optional field.
        if (attributes != null) {
            List<String> validatedAttributes = validateAttributes(attributes, tenantDomain);
            ActionProperty attributesObject = createActionProperty(validatedAttributes);
            properties.put(ATTRIBUTES, attributesObject);
        }

        return new ActionDTO.Builder(actionDTO)
                .properties(properties)
                .build();
    }

    @Override
    public ActionDTO resolveForGetOperation(ActionDTO actionDTO, String tenantDomain)
            throws ActionDTOModelResolverException {

        Map<String, ActionProperty> properties = new HashMap<>();
        // Attributes is an optional field.
        if (actionDTO.getPropertyValue(ATTRIBUTES) != null) {

            properties.put(ATTRIBUTES, getAttributes(((BinaryObject) actionDTO
                    .getPropertyValue(ATTRIBUTES)).getJSONString()));
        }

        return new ActionDTO.Builder(actionDTO)
                .properties(properties)
                .build();
    }

    @Override
    public List<ActionDTO> resolveForGetOperation(List<ActionDTO> actionDTOList, String tenantDomain)
            throws ActionDTOModelResolverException {

        List<ActionDTO> actionDTOS = new ArrayList<>();
        for (ActionDTO actionDTO : actionDTOList) {
            actionDTOS.add(resolveForGetOperation(actionDTO, tenantDomain));
        }

        return actionDTOS;
    }

    /**
     * Resolves the actionDTO for the update operation.
     * When properties are updated, the existing properties are replaced with the new properties.
     * When properties are not updated, the existing properties should be sent to the upstream component.
     *
     * @param updatingActionDTO ActionDTO that needs to be updated.
     * @param existingActionDTO Existing ActionDTO.
     * @param tenantDomain      Tenant domain.
     * @return Resolved ActionDTO.
     * @throws ActionDTOModelResolverException ActionDTOModelResolverException.
     */
    @Override
    public ActionDTO resolveForUpdateOperation(ActionDTO updatingActionDTO, ActionDTO existingActionDTO,
                                               String tenantDomain) throws ActionDTOModelResolverException {

        Map<String, ActionProperty> properties = new HashMap<>();
        // Action Properties updating operation is treated as a PUT in DAO layer. Therefore if no properties are updated
        // the existing properties should be sent to the DAO layer.
        List<String> attributes = getResolvedUpdatingAttributes(updatingActionDTO, existingActionDTO,
                tenantDomain);
        if (!attributes.isEmpty()) {
            properties.put(ATTRIBUTES, createActionProperty(attributes));
        }
        return new ActionDTO.Builder(updatingActionDTO)
                .properties(properties)
                .build();
    }

    @Override
    public void resolveForDeleteOperation(ActionDTO deletingActionDTO, String tenantDomain)
            throws ActionDTOModelResolverException {

    }

    private List<String> getResolvedUpdatingAttributes(ActionDTO updatingActionDTO,
                                                       ActionDTO existingActionDTO, String tenantDomain)
            throws ActionDTOModelResolverException {

        if (updatingActionDTO.getPropertyValue(ATTRIBUTES) != null) {
            // return updating attributes after validation
            return validateAttributes(updatingActionDTO.getPropertyValue(ATTRIBUTES), tenantDomain);
        } else if (existingActionDTO.getPropertyValue(ATTRIBUTES) != null) {
            // return existing attributes
            return (List<String>) existingActionDTO.getPropertyValue(ATTRIBUTES);
        }
        // if attributes are not getting updated or not configured earlier, return empty list.
        return emptyList();
    }

    private ActionProperty createActionProperty(List<String> attributes) throws ActionDTOModelResolverException {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // Convert the attributes to a JSON string.
            BinaryObject attributesBinaryObject = BinaryObject.fromJsonString(objectMapper
                    .writeValueAsString(attributes));
            return new ActionProperty.BuilderForDAO(attributesBinaryObject).build();
        } catch (JsonProcessingException e) {
            throw new ActionDTOModelResolverException("Failed to convert object values to JSON string.", e);
        }
    }

    private ActionProperty getAttributes(String value) throws ActionDTOModelResolverException {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return new ActionProperty.BuilderForService(objectMapper.readValue(value,
                    new TypeReference<List<String>>() { })).build();
        } catch (IOException e) {
            throw new ActionDTOModelResolverException("Error while reading the attribute values from storage.", e);
        }
    }

    private List<String> validateAttributes(Object attributes, String tenantDomain)
            throws ActionDTOModelResolverException {

        List<String> validatedAttributes = getAttributesList(attributes);
        validateAttributesCount(validatedAttributes);
        validatedAttributes = filterValidSystemAttributes(validatedAttributes, tenantDomain);
        return validatedAttributes;
    }

    private List<String> getAttributesList(Object attributes) throws ActionDTOModelResolverClientException {

        if (!(attributes instanceof List<?>)) {
            throw new ActionDTOModelResolverClientException("Invalid attributes format.",
                    "Attributes should be provided as a list of Strings.");
        }

        List<?> attributesList = (List<?>) attributes;
        for (Object item : attributesList) {
            if (!(item instanceof String)) {
                throw new ActionDTOModelResolverClientException("Invalid attributes format.",
                        "Attributes must contain only String values.");
            }
        }

        return (List<String>) attributes;
    }

    private void validateAttributesCount(List<String> attributes) throws ActionDTOModelResolverClientException {

        if (attributes.size() > MAX_ATTRIBUTES) {
            throw new ActionDTOModelResolverClientException("Maximum attributes limit exceeded.",
                    String.format("The number of configured attributes: %d exceeds the maximum allowed limit: %d",
                            attributes.size(), MAX_ATTRIBUTES));
        }
    }

    private List<String> filterValidSystemAttributes(List<String> attributes, String tenantDomain)
            throws ActionDTOModelResolverClientException, ActionDTOModelResolverServerException {

        try {
            ClaimMetadataManagementService claimMetadataManagementService =
                    PreUpdateProfileActionServiceComponentHolder.getInstance().getClaimManagementService();
            List<LocalClaim> localClaims = claimMetadataManagementService.getLocalClaims(tenantDomain);
            Set<String> localClaimURIs = localClaims.stream()
                    .map(LocalClaim::getClaimURI)
                    .collect(Collectors.toSet());
            Set<String> uniqueAttributes = new HashSet<>();
            Set<String> duplicatedAttributes = new HashSet<>();
            for (String attribute : attributes) {
                if (!localClaimURIs.contains(attribute)) {
                    String invalidAttributeDescription = String.format("The provided %s attribute is not available " +
                            "in the system.", attribute);
                    throw new ActionDTOModelResolverClientException("Invalid attribute provided.",
                            invalidAttributeDescription);
                }
                if (attribute.equals(ROLE_CLAIM_URI)) {
                    throw new ActionDTOModelResolverClientException("Not supported.", String.format("%s attribute is " +
                            "not supported to be shared with extension.", ROLE_CLAIM_URI)
                    );
                }
                if (!uniqueAttributes.add(attribute)) {
                    duplicatedAttributes.add(attribute);
                }
            }
            if (LOG.isDebugEnabled() && !duplicatedAttributes.isEmpty()) {
                LOG.debug("Ignored duplicated attributes in pre profile action configuration : " +
                        String.join(", ", duplicatedAttributes));
            }
            return Collections.unmodifiableList(new ArrayList<>(uniqueAttributes));
        } catch (ClaimMetadataException e) {
            throw new ActionDTOModelResolverServerException("Error while retrieving local claims from claim meta " +
                    "data service.", e.getMessage());
        }
    }
}
