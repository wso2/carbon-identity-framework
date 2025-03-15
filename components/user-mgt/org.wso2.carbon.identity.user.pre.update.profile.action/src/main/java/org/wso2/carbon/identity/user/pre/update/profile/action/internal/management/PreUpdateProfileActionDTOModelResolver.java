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
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverClientException;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverException;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverServerException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionPropertyForDAO;
import org.wso2.carbon.identity.action.management.api.model.BinaryObject;
import org.wso2.carbon.identity.action.management.api.service.ActionDTOModelResolver;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.user.pre.update.profile.action.internal.constant.PreUpdateProfileActionConstants.ATTRIBUTES;

/**
 * This class implements the methods required to resolve ActionDTO objects in Pre Update Profile extension.
 */
public class PreUpdateProfileActionDTOModelResolver implements ActionDTOModelResolver {

    @Override
    public Action.ActionTypes getSupportedActionType() {

        return Action.ActionTypes.PRE_UPDATE_PROFILE;
    }

    @Override
    public ActionDTO resolveForAddOperation(ActionDTO actionDTO, String tenantDomain)
            throws ActionDTOModelResolverException {

        Map<String, ActionPropertyForDAO> properties = new HashMap<>();
        Object attributes = actionDTO.getProperty(ATTRIBUTES);
        // Attributes is an optional field.
        if (attributes != null) {
            List<String> validatedAttributes = validateAttributes(attributes);
            BinaryObject attributesBinaryObject = new BinaryObject(convertAttributesToInputStream(validatedAttributes));
            properties.put(ATTRIBUTES, new ActionPropertyForDAO(attributesBinaryObject));
        }

        return new ActionDTO.BuilderForData(actionDTO)
                .properties(properties)
                .build();
    }

    @Override
    public ActionDTO resolveForGetOperation(ActionDTO actionDTO, String tenantDomain)
            throws ActionDTOModelResolverException {

        Map<String, Object> properties = new HashMap<>();
        // Attributes is an optional field.
        if (actionDTO.getProperty(ATTRIBUTES) != null) {
            if (!(actionDTO.getProperty(ATTRIBUTES) instanceof ActionPropertyForDAO)) {
                throw new ActionDTOModelResolverServerException("Unable to retrieve attributes.",
                        "Invalid action property provided to retrieve attributes.");
            }
            properties.put(ATTRIBUTES, getAttributes(((BinaryObject) ((ActionPropertyForDAO) actionDTO
                    .getProperty(ATTRIBUTES)).getValue()).getInputStream()));
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

        Map<String, ActionPropertyForDAO> properties = new HashMap<>();
        // Action Properties updating operation is treated as a PUT in DAO layer. Therefore if no properties are updated
        // the existing properties should be sent to the DAO layer.
        if (updatingActionDTO.getProperty(ATTRIBUTES) != null) {
            List<String> validatedAttributes = validateAttributes(updatingActionDTO.getProperty(ATTRIBUTES));
            if (!validatedAttributes.isEmpty()) {
                BinaryObject attributesBinaryObject = new BinaryObject(convertAttributesToInputStream
                        (validatedAttributes));
                properties.put(ATTRIBUTES, new ActionPropertyForDAO(attributesBinaryObject));
            }
        } else {
            BinaryObject attributesBinaryObject = new BinaryObject(convertAttributesToInputStream((List<String>)
                    existingActionDTO.getProperty(ATTRIBUTES)));
            properties.put(ATTRIBUTES, new ActionPropertyForDAO(attributesBinaryObject));
        }
        return new ActionDTO.BuilderForData(updatingActionDTO)
                .properties(properties)
                .build();
    }

    @Override
    public void resolveForDeleteOperation(ActionDTO deletingActionDTO, String tenantDomain)
            throws ActionDTOModelResolverException {

    }

    private InputStream convertAttributesToInputStream(List<String> attributes) throws ActionDTOModelResolverException {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // Convert the attributes to a JSON array and generate the input stream.
            return new ByteArrayInputStream(objectMapper.writeValueAsString(attributes)
                    .getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new ActionDTOModelResolverException("Failed to convert attributes to JSON.", e);
        }
    }

    private List<String> getAttributes(InputStream stream) throws ActionDTOModelResolverException {

        StringBuilder sb = new StringBuilder();
        List<String> attributes;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            attributes = objectMapper.readValue(sb.toString(), new TypeReference<List<String>>() { });
        } catch (IOException e) {
            throw new ActionDTOModelResolverException("Error while reading the attribute values from storage.", e);
        }

        return attributes;
    }

    private List<String> validateAttributes(Object attributes) throws ActionDTOModelResolverClientException {

        if (!(attributes instanceof List<?>)) {
            throw new ActionDTOModelResolverClientException("Invalid attributes format.",
                    "Attributes should be provided as a list of Strings.");
        }

        // Validate that all elements are in String data type
        List<?> attributesList = (List<?>) attributes;
        for (Object item : attributesList) {
            if (!(item instanceof String)) {
                throw new ActionDTOModelResolverClientException("Invalid attributes format.",
                        "Attributes must contain only String values.");
            }
        }

        // If validation passes, cast it safely
        return (List<String>) attributes;
    }
}
