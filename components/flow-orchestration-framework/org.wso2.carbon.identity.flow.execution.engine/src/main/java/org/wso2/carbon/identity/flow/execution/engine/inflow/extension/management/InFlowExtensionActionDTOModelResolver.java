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

package org.wso2.carbon.identity.flow.execution.engine.inflow.extension.management;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverClientException;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionProperty;
import org.wso2.carbon.identity.action.management.api.model.BinaryObject;
import org.wso2.carbon.identity.action.management.api.service.ActionDTOModelResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.wso2.carbon.identity.flow.execution.engine.inflow.extension.management.InFlowExtensionActionConstants.ACCESS_CONFIG_ALLOWED_OPERATIONS;
import static org.wso2.carbon.identity.flow.execution.engine.inflow.extension.management.InFlowExtensionActionConstants.ACCESS_CONFIG_ALLOWED_OPERATIONS_PREFIX;
import static org.wso2.carbon.identity.flow.execution.engine.inflow.extension.management.InFlowExtensionActionConstants.ACCESS_CONFIG_EXPOSE;
import static org.wso2.carbon.identity.flow.execution.engine.inflow.extension.management.InFlowExtensionActionConstants.ACCESS_CONFIG_EXPOSE_PREFIX;
import static org.wso2.carbon.identity.flow.execution.engine.inflow.extension.management.InFlowExtensionActionConstants.MAX_EXPOSE_PATHS;

/**
 * ActionDTOModelResolver implementation for In-Flow Extension actions.
 * <p>
 * Responsible for validating and transforming access config properties (expose paths and
 * allowed operations) between the service layer representation and the DAO layer BLOB format.
 * </p>
 *
 * <ul>
 *   <li><b>Add operation</b>: Validates expose paths and allowed operations, then serializes
 *       them to JSON {@link BinaryObject}s for BLOB storage in IDN_ACTION_PROPERTIES.</li>
 *   <li><b>Get operation</b>: Deserializes BLOBs back to service-layer list objects.</li>
 *   <li><b>Update operation</b>: Validates updated values or preserves existing ones (PUT semantics).</li>
 *   <li><b>Delete operation</b>: No-op (properties are cascade-deleted with the action).</li>
 * </ul>
 */
public class InFlowExtensionActionDTOModelResolver implements ActionDTOModelResolver {

    private static final Log LOG = LogFactory.getLog(InFlowExtensionActionDTOModelResolver.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<String>> STRING_LIST_TYPE_REF =
            new TypeReference<List<String>>() { };
    private static final TypeReference<List<Map<String, Object>>> MAP_LIST_TYPE_REF =
            new TypeReference<List<Map<String, Object>>>() { };

    @Override
    public Action.ActionTypes getSupportedActionType() {

        return Action.ActionTypes.IN_FLOW_EXTENSION;
    }

    @Override
    public ActionDTO resolveForAddOperation(ActionDTO actionDTO, String tenantDomain)
            throws ActionDTOModelResolverException {

        Map<String, ActionProperty> properties = new HashMap<>();

        Object exposeValue = actionDTO.getPropertyValue(ACCESS_CONFIG_EXPOSE);
        // Expose is an optional field.
        if (exposeValue != null) {
            List<String> validatedExpose = validateExpose(exposeValue);
            properties.put(ACCESS_CONFIG_EXPOSE, createBlobProperty(validatedExpose));
        }

        Object allowedOpsValue = actionDTO.getPropertyValue(ACCESS_CONFIG_ALLOWED_OPERATIONS);
        // Allowed operations is an optional field.
        if (allowedOpsValue != null) {
            List<Map<String, Object>> validatedOps = validateAllowedOperations(allowedOpsValue);
            properties.put(ACCESS_CONFIG_ALLOWED_OPERATIONS, createBlobProperty(validatedOps));
        }

        // Handle per-flow-type override properties (prefixed keys).
        if (actionDTO.getProperties() != null) {
            for (Map.Entry<String, ActionProperty> entry : actionDTO.getProperties().entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(ACCESS_CONFIG_EXPOSE_PREFIX)) {
                    Object overrideExpose = actionDTO.getPropertyValue(key);
                    if (overrideExpose != null) {
                        List<String> validatedOverrideExpose = validateExpose(overrideExpose);
                        properties.put(key, createBlobProperty(validatedOverrideExpose));
                    }
                } else if (key.startsWith(ACCESS_CONFIG_ALLOWED_OPERATIONS_PREFIX)) {
                    Object overrideOps = actionDTO.getPropertyValue(key);
                    if (overrideOps != null) {
                        List<Map<String, Object>> validatedOverrideOps = validateAllowedOperations(overrideOps);
                        properties.put(key, createBlobProperty(validatedOverrideOps));
                    }
                }
            }
        }

        return new ActionDTO.Builder(actionDTO)
                .properties(properties)
                .build();
    }

    @Override
    public ActionDTO resolveForGetOperation(ActionDTO actionDTO, String tenantDomain)
            throws ActionDTOModelResolverException {

        Map<String, ActionProperty> properties = new HashMap<>();

        // Default access config properties.
        if (actionDTO.getPropertyValue(ACCESS_CONFIG_EXPOSE) != null) {
            properties.put(ACCESS_CONFIG_EXPOSE, deserializeExposeProperty(
                    ((BinaryObject) actionDTO.getPropertyValue(ACCESS_CONFIG_EXPOSE)).getJSONString()));
        }

        if (actionDTO.getPropertyValue(ACCESS_CONFIG_ALLOWED_OPERATIONS) != null) {
            properties.put(ACCESS_CONFIG_ALLOWED_OPERATIONS, deserializeAllowedOpsProperty(
                    ((BinaryObject) actionDTO.getPropertyValue(ACCESS_CONFIG_ALLOWED_OPERATIONS)).getJSONString()));
        }

        // Deserialize per-flow-type override properties (prefixed keys).
        if (actionDTO.getProperties() != null) {
            for (Map.Entry<String, ActionProperty> entry : actionDTO.getProperties().entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(ACCESS_CONFIG_EXPOSE_PREFIX)
                        && actionDTO.getPropertyValue(key) != null) {
                    properties.put(key, deserializeExposeProperty(
                            ((BinaryObject) actionDTO.getPropertyValue(key)).getJSONString()));
                } else if (key.startsWith(ACCESS_CONFIG_ALLOWED_OPERATIONS_PREFIX)
                        && actionDTO.getPropertyValue(key) != null) {
                    properties.put(key, deserializeAllowedOpsProperty(
                            ((BinaryObject) actionDTO.getPropertyValue(key)).getJSONString()));
                }
            }
        }

        return new ActionDTO.Builder(actionDTO)
                .properties(properties)
                .build();
    }

    @Override
    public List<ActionDTO> resolveForGetOperation(List<ActionDTO> actionDTOList, String tenantDomain)
            throws ActionDTOModelResolverException {

        List<ActionDTO> resolvedList = new ArrayList<>();
        for (ActionDTO actionDTO : actionDTOList) {
            resolvedList.add(resolveForGetOperation(actionDTO, tenantDomain));
        }
        return resolvedList;
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

        // Action Properties updating operation is treated as a PUT in DAO layer. Therefore if no properties are
        // updated the existing properties should be sent to the DAO layer.

        // Handle default access config properties.
        List<String> expose = getResolvedUpdatingExpose(updatingActionDTO, existingActionDTO);
        if (!expose.isEmpty()) {
            properties.put(ACCESS_CONFIG_EXPOSE, createBlobProperty(expose));
        }

        List<Map<String, Object>> allowedOps =
                getResolvedUpdatingAllowedOps(updatingActionDTO, existingActionDTO);
        if (!allowedOps.isEmpty()) {
            properties.put(ACCESS_CONFIG_ALLOWED_OPERATIONS, createBlobProperty(allowedOps));
        }

        // Handle per-flow-type override properties. Since DAO treats update as PUT (full replace),
        // we must carry forward all existing overrides that are not explicitly being updated.
        // First, carry forward all existing per-flow-type overrides.
        if (existingActionDTO.getProperties() != null) {
            for (Map.Entry<String, ActionProperty> entry : existingActionDTO.getProperties().entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(ACCESS_CONFIG_EXPOSE_PREFIX)
                        || key.startsWith(ACCESS_CONFIG_ALLOWED_OPERATIONS_PREFIX)) {
                    properties.put(key, createBlobProperty(existingActionDTO.getPropertyValue(key)));
                }
            }
        }
        // Then, overlay with any explicitly updated per-flow-type overrides.
        if (updatingActionDTO.getProperties() != null) {
            for (Map.Entry<String, ActionProperty> entry : updatingActionDTO.getProperties().entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(ACCESS_CONFIG_EXPOSE_PREFIX)) {
                    Object overrideExpose = updatingActionDTO.getPropertyValue(key);
                    if (overrideExpose != null) {
                        List<String> validatedOverrideExpose = validateExpose(overrideExpose);
                        properties.put(key, createBlobProperty(validatedOverrideExpose));
                    }
                } else if (key.startsWith(ACCESS_CONFIG_ALLOWED_OPERATIONS_PREFIX)) {
                    Object overrideOps = updatingActionDTO.getPropertyValue(key);
                    if (overrideOps != null) {
                        List<Map<String, Object>> validatedOverrideOps = validateAllowedOperations(overrideOps);
                        properties.put(key, createBlobProperty(validatedOverrideOps));
                    }
                }
            }
        }

        return new ActionDTO.Builder(updatingActionDTO)
                .properties(properties)
                .build();
    }

    @Override
    public void resolveForDeleteOperation(ActionDTO deletingActionDTO, String tenantDomain)
            throws ActionDTOModelResolverException {
        // No-op: properties are cascade-deleted with the action in IDN_ACTION_PROPERTIES.
    }

    // ---- Update helpers ----

    @SuppressWarnings("unchecked")
    private List<String> getResolvedUpdatingExpose(ActionDTO updatingActionDTO, ActionDTO existingActionDTO)
            throws ActionDTOModelResolverException {

        if (updatingActionDTO.getPropertyValue(ACCESS_CONFIG_EXPOSE) != null) {
            return validateExpose(updatingActionDTO.getPropertyValue(ACCESS_CONFIG_EXPOSE));
        } else if (existingActionDTO.getPropertyValue(ACCESS_CONFIG_EXPOSE) != null) {
            return (List<String>) existingActionDTO.getPropertyValue(ACCESS_CONFIG_EXPOSE);
        }
        return emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getResolvedUpdatingAllowedOps(ActionDTO updatingActionDTO,
                                                                    ActionDTO existingActionDTO)
            throws ActionDTOModelResolverException {

        if (updatingActionDTO.getPropertyValue(ACCESS_CONFIG_ALLOWED_OPERATIONS) != null) {
            return validateAllowedOperations(updatingActionDTO.getPropertyValue(ACCESS_CONFIG_ALLOWED_OPERATIONS));
        } else if (existingActionDTO.getPropertyValue(ACCESS_CONFIG_ALLOWED_OPERATIONS) != null) {
            return (List<Map<String, Object>>) existingActionDTO.getPropertyValue(ACCESS_CONFIG_ALLOWED_OPERATIONS);
        }
        return emptyList();
    }

    // ---- Validation ----

    @SuppressWarnings("unchecked")
    private List<String> validateExpose(Object exposeValue) throws ActionDTOModelResolverException {

        if (!(exposeValue instanceof List<?>)) {
            throw new ActionDTOModelResolverClientException("Invalid expose format.",
                    "Expose should be provided as a list of strings.");
        }

        List<?> exposeList = (List<?>) exposeValue;
        for (Object item : exposeList) {
            if (!(item instanceof String)) {
                throw new ActionDTOModelResolverClientException("Invalid expose format.",
                        "Expose must contain only string values.");
            }
        }

        List<String> expose = (List<String>) exposeValue;
        validateExposeCount(expose);
        validateExposePathFormat(expose);
        return expose;
    }

    private void validateExposeCount(List<String> expose) throws ActionDTOModelResolverClientException {

        if (expose.size() > MAX_EXPOSE_PATHS) {
            throw new ActionDTOModelResolverClientException("Maximum expose paths limit exceeded.",
                    String.format("The number of configured expose paths: %d exceeds the maximum allowed limit: %d.",
                            expose.size(), MAX_EXPOSE_PATHS));
        }
    }

    private void validateExposePathFormat(List<String> expose) throws ActionDTOModelResolverClientException {

        Set<String> seen = new HashSet<>();
        for (String path : expose) {
            if (path == null || path.trim().isEmpty()) {
                throw new ActionDTOModelResolverClientException("Invalid expose path.",
                        "Expose paths must not be null or empty.");
            }
            if (!path.startsWith("/")) {
                throw new ActionDTOModelResolverClientException("Invalid expose path.",
                        String.format("Expose path '%s' must start with '/'.", path));
            }
            if (!seen.add(path)) {
                throw new ActionDTOModelResolverClientException("Duplicate expose path.",
                        String.format("The expose path '%s' is duplicated.", path));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> validateAllowedOperations(Object allowedOpsValue)
            throws ActionDTOModelResolverClientException {

        if (!(allowedOpsValue instanceof List<?>)) {
            throw new ActionDTOModelResolverClientException("Invalid allowed operations format.",
                    "Allowed operations should be provided as a list.");
        }

        List<?> opsList = (List<?>) allowedOpsValue;
        for (Object item : opsList) {
            if (!(item instanceof Map)) {
                throw new ActionDTOModelResolverClientException("Invalid allowed operations format.",
                        "Each allowed operation must be an object with 'op' and 'paths' keys.");
            }
            Map<?, ?> opMap = (Map<?, ?>) item;
            if (!opMap.containsKey("op") || !(opMap.get("op") instanceof String)) {
                throw new ActionDTOModelResolverClientException("Invalid allowed operation.",
                        "Each allowed operation must contain an 'op' field with a string value.");
            }
            if (!opMap.containsKey("paths") || !(opMap.get("paths") instanceof List)) {
                throw new ActionDTOModelResolverClientException("Invalid allowed operation.",
                        "Each allowed operation must contain a 'paths' field with a list of strings.");
            }
        }

        return (List<Map<String, Object>>) allowedOpsValue;
    }

    // ---- Serialization helpers ----

    private ActionProperty createBlobProperty(Object value) throws ActionDTOModelResolverException {

        try {
            BinaryObject binaryObject = BinaryObject.fromJsonString(OBJECT_MAPPER.writeValueAsString(value));
            return new ActionProperty.BuilderForDAO(binaryObject).build();
        } catch (JsonProcessingException e) {
            throw new ActionDTOModelResolverException("Failed to serialize access config property to JSON.", e);
        }
    }

    private ActionProperty deserializeExposeProperty(String jsonString) throws ActionDTOModelResolverException {

        try {
            List<String> expose = OBJECT_MAPPER.readValue(jsonString, STRING_LIST_TYPE_REF);
            return new ActionProperty.BuilderForService(expose).build();
        } catch (IOException e) {
            throw new ActionDTOModelResolverException("Error reading expose values from storage.", e);
        }
    }

    private ActionProperty deserializeAllowedOpsProperty(String jsonString)
            throws ActionDTOModelResolverException {

        try {
            List<Map<String, Object>> allowedOps = OBJECT_MAPPER.readValue(jsonString, MAP_LIST_TYPE_REF);
            return new ActionProperty.BuilderForService(allowedOps).build();
        } catch (IOException e) {
            throw new ActionDTOModelResolverException("Error reading allowed operations from storage.", e);
        }
    }
}
