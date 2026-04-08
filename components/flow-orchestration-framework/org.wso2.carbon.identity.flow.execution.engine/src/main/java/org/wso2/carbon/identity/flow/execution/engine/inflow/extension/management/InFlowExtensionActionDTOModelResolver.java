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
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.certificate.management.service.CertificateManagementService;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model.ContextPath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.wso2.carbon.identity.flow.execution.engine.inflow.extension.management.InFlowExtensionActionConstants.ACCESS_CONFIG_MODIFY;
import static org.wso2.carbon.identity.flow.execution.engine.inflow.extension.management.InFlowExtensionActionConstants.ACCESS_CONFIG_MODIFY_PREFIX;
import static org.wso2.carbon.identity.flow.execution.engine.inflow.extension.management.InFlowExtensionActionConstants.CERTIFICATE;
import static org.wso2.carbon.identity.flow.execution.engine.inflow.extension.management.InFlowExtensionActionConstants.ICON_URL;
import static org.wso2.carbon.identity.flow.execution.engine.inflow.extension.management.InFlowExtensionActionConstants.ACCESS_CONFIG_EXPOSE;
import static org.wso2.carbon.identity.flow.execution.engine.inflow.extension.management.InFlowExtensionActionConstants.ACCESS_CONFIG_EXPOSE_PREFIX;
import static org.wso2.carbon.identity.flow.execution.engine.inflow.extension.management.InFlowExtensionActionConstants.CERTIFICATE_NAME_PREFIX;
import static org.wso2.carbon.identity.flow.execution.engine.inflow.extension.management.InFlowExtensionActionConstants.MAX_EXPOSE_PATHS;

/**
 * ActionDTOModelResolver implementation for In-Flow Extension actions.
 * <p>
 * Responsible for validating and transforming access config properties (expose paths and
 * modify paths) between the service layer representation and the DAO layer BLOB format.
 * </p>
 *
 * <ul>
 *   <li><b>Add operation</b>: Validates expose paths and modify paths, then serializes
 *       them to JSON {@link BinaryObject}s for BLOB storage in IDN_ACTION_PROPERTIES.</li>
 *   <li><b>Get operation</b>: Deserializes BLOBs back to service-layer list objects.</li>
 *   <li><b>Update operation</b>: Validates updated values or preserves existing ones (PUT semantics).</li>
 *   <li><b>Delete operation</b>: No-op (properties are cascade-deleted with the action).</li>
 * </ul>
 */
public class InFlowExtensionActionDTOModelResolver implements ActionDTOModelResolver {

    private static final Log LOG = LogFactory.getLog(InFlowExtensionActionDTOModelResolver.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<ContextPath>> CONTEXT_PATH_LIST_TYPE_REF =
            new TypeReference<List<ContextPath>>() { };

    private final CertificateManagementService certificateManagementService;

    public InFlowExtensionActionDTOModelResolver(CertificateManagementService certificateManagementService) {

        this.certificateManagementService = certificateManagementService;
    }

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
            List<ContextPath> validatedExpose = validateExpose(exposeValue);
            properties.put(ACCESS_CONFIG_EXPOSE, createBlobProperty(validatedExpose));
        }

        Object modifyValue = actionDTO.getPropertyValue(ACCESS_CONFIG_MODIFY);
        // Modify is an optional field.
        if (modifyValue != null) {
            List<ContextPath> validatedModify = validateExpose(modifyValue);
            properties.put(ACCESS_CONFIG_MODIFY, createBlobProperty(validatedModify));
        }

        // Handle certificate: store via CertificateManagementService and replace with ID.
        handleCertificateAdd(actionDTO, properties, tenantDomain);

        // Handle icon URL: pass through as a PRIMITIVE string.
        Object iconUrlValue = actionDTO.getPropertyValue(ICON_URL);
        if (iconUrlValue instanceof String && !((String) iconUrlValue).isEmpty()) {
            properties.put(ICON_URL, new ActionProperty.BuilderForDAO((String) iconUrlValue).build());
        }

        // Handle per-flow-type override properties (prefixed keys).
        if (actionDTO.getProperties() != null) {
            for (Map.Entry<String, ActionProperty> entry : actionDTO.getProperties().entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(ACCESS_CONFIG_EXPOSE_PREFIX)) {
                    Object overrideExpose = actionDTO.getPropertyValue(key);
                    if (overrideExpose != null) {
                        List<ContextPath> validatedOverrideExpose = validateExpose(overrideExpose);
                        properties.put(key, createBlobProperty(validatedOverrideExpose));
                    }
                } else if (key.startsWith(ACCESS_CONFIG_MODIFY_PREFIX)) {
                    Object overrideModify = actionDTO.getPropertyValue(key);
                    if (overrideModify != null) {
                        List<ContextPath> validatedOverrideModify = validateExpose(overrideModify);
                        properties.put(key, createBlobProperty(validatedOverrideModify));
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

        if (actionDTO.getPropertyValue(ACCESS_CONFIG_MODIFY) != null) {
            properties.put(ACCESS_CONFIG_MODIFY, deserializeExposeProperty(
                    ((BinaryObject) actionDTO.getPropertyValue(ACCESS_CONFIG_MODIFY)).getJSONString()));
        }

        // Retrieve certificate by stored ID.
        handleCertificateGet(actionDTO, properties, tenantDomain);

        // Icon URL: pass through as-is (already a PRIMITIVE string).
        Object iconUrlValue = actionDTO.getPropertyValue(ICON_URL);
        if (iconUrlValue != null) {
            properties.put(ICON_URL, new ActionProperty.BuilderForService(iconUrlValue.toString()).build());
        }

        // Deserialize per-flow-type override properties (prefixed keys).
        if (actionDTO.getProperties() != null) {
            for (Map.Entry<String, ActionProperty> entry : actionDTO.getProperties().entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(ACCESS_CONFIG_EXPOSE_PREFIX)
                        && actionDTO.getPropertyValue(key) != null) {
                    properties.put(key, deserializeExposeProperty(
                            ((BinaryObject) actionDTO.getPropertyValue(key)).getJSONString()));
                } else if (key.startsWith(ACCESS_CONFIG_MODIFY_PREFIX)
                        && actionDTO.getPropertyValue(key) != null) {
                    properties.put(key, deserializeExposeProperty(
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
        List<ContextPath> expose = getResolvedUpdatingExpose(updatingActionDTO, existingActionDTO);
        if (!expose.isEmpty()) {
            properties.put(ACCESS_CONFIG_EXPOSE, createBlobProperty(expose));
        }

        List<ContextPath> modify = getResolvedUpdatingModify(updatingActionDTO, existingActionDTO);
        if (!modify.isEmpty()) {
            properties.put(ACCESS_CONFIG_MODIFY, createBlobProperty(modify));
        }

        // Handle certificate update.
        handleCertificateUpdate(updatingActionDTO, existingActionDTO, properties, tenantDomain);

        // Handle icon URL update (PUT semantics: carry forward existing if not provided).
        Object updatingIconUrl = updatingActionDTO.getPropertyValue(ICON_URL);
        if (updatingIconUrl instanceof String && !((String) updatingIconUrl).isEmpty()) {
            properties.put(ICON_URL, new ActionProperty.BuilderForDAO((String) updatingIconUrl).build());
        } else if (existingActionDTO.getPropertyValue(ICON_URL) != null) {
            properties.put(ICON_URL, new ActionProperty.BuilderForDAO(
                    existingActionDTO.getPropertyValue(ICON_URL).toString()).build());
        }

        // Handle per-flow-type override properties. Since DAO treats update as PUT (full replace),
        // we must carry forward all existing overrides that are not explicitly being updated.
        // First, carry forward all existing per-flow-type overrides.
        if (existingActionDTO.getProperties() != null) {
            for (Map.Entry<String, ActionProperty> entry : existingActionDTO.getProperties().entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(ACCESS_CONFIG_EXPOSE_PREFIX)
                        || key.startsWith(ACCESS_CONFIG_MODIFY_PREFIX)) {
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
                        List<ContextPath> validatedOverrideExpose = validateExpose(overrideExpose);
                        properties.put(key, createBlobProperty(validatedOverrideExpose));
                    }
                } else if (key.startsWith(ACCESS_CONFIG_MODIFY_PREFIX)) {
                    Object overrideModify = updatingActionDTO.getPropertyValue(key);
                    if (overrideModify != null) {
                        List<ContextPath> validatedOverrideModify = validateExpose(overrideModify);
                        properties.put(key, createBlobProperty(validatedOverrideModify));
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

        // Delete the certificate if one was stored for this action.
        handleCertificateDelete(deletingActionDTO, tenantDomain);
    }

    // ---- Update helpers ----

    @SuppressWarnings("unchecked")
    private List<ContextPath> getResolvedUpdatingExpose(ActionDTO updatingActionDTO, ActionDTO existingActionDTO)
            throws ActionDTOModelResolverException {

        if (updatingActionDTO.getPropertyValue(ACCESS_CONFIG_EXPOSE) != null) {
            return validateExpose(updatingActionDTO.getPropertyValue(ACCESS_CONFIG_EXPOSE));
        } else if (existingActionDTO.getPropertyValue(ACCESS_CONFIG_EXPOSE) != null) {
            return (List<ContextPath>) existingActionDTO.getPropertyValue(ACCESS_CONFIG_EXPOSE);
        }
        return emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<ContextPath> getResolvedUpdatingModify(ActionDTO updatingActionDTO,
                                                       ActionDTO existingActionDTO)
            throws ActionDTOModelResolverException {

        if (updatingActionDTO.getPropertyValue(ACCESS_CONFIG_MODIFY) != null) {
            return validateExpose(updatingActionDTO.getPropertyValue(ACCESS_CONFIG_MODIFY));
        } else if (existingActionDTO.getPropertyValue(ACCESS_CONFIG_MODIFY) != null) {
            return (List<ContextPath>) existingActionDTO.getPropertyValue(ACCESS_CONFIG_MODIFY);
        }
        return emptyList();
    }

    // ---- Validation ----

    @SuppressWarnings("unchecked")
    private List<ContextPath> validateExpose(Object exposeValue) throws ActionDTOModelResolverException {

        if (!(exposeValue instanceof List<?>)) {
            throw new ActionDTOModelResolverClientException("Invalid expose format.",
                    "Expose should be provided as a list.");
        }

        List<?> exposeList = (List<?>) exposeValue;
        List<ContextPath> result = new ArrayList<>();

        for (Object item : exposeList) {
            if (item instanceof String) {
                // Simple string path — no encryption.
                result.add(new ContextPath((String) item, false));
            } else if (item instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) item;
                if (!map.containsKey("path") || !(map.get("path") instanceof String)) {
                    throw new ActionDTOModelResolverClientException("Invalid expose format.",
                            "Each expose entry must be a string or an object with a 'path' field.");
                }
                String path = (String) map.get("path");
                boolean encrypted = map.containsKey("encrypted") && toBooleanSafe(map.get("encrypted"));
                result.add(new ContextPath(path, encrypted));
            } else if (item instanceof ContextPath) {
                result.add((ContextPath) item);
            } else {
                throw new ActionDTOModelResolverClientException("Invalid expose format.",
                        "Each expose entry must be a string or an object with 'path' and optional 'encrypted' fields.");
            }
        }

        validateExposeCount(result);
        validateContextPathFormat(result);
        return result;
    }

    private void validateExposeCount(List<ContextPath> expose) throws ActionDTOModelResolverClientException {

        if (expose.size() > MAX_EXPOSE_PATHS) {
            throw new ActionDTOModelResolverClientException("Maximum expose paths limit exceeded.",
                    String.format("The number of configured expose paths: %d exceeds the maximum allowed limit: %d.",
                            expose.size(), MAX_EXPOSE_PATHS));
        }
    }

    private void validateContextPathFormat(List<ContextPath> expose) throws ActionDTOModelResolverClientException {

        Set<String> seen = new HashSet<>();
        for (ContextPath exposePath : expose) {
            String path = exposePath.getPath();
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

    // ---- Certificate lifecycle helpers ----

    /**
     * Stores the external service's certificate via CertificateManagementService during action creation.
     * The certificate PEM is replaced with the stored certificate's ID as a PRIMITIVE property.
     */
    private void handleCertificateAdd(ActionDTO actionDTO, Map<String, ActionProperty> properties,
                                      String tenantDomain) throws ActionDTOModelResolverException {

        Object certValue = actionDTO.getPropertyValue(CERTIFICATE);
        if (certValue == null) {
            return;
        }

        String certificatePEM = extractCertificatePEM(certValue);
        String certName = CERTIFICATE_NAME_PREFIX + actionDTO.getId();

        try {
            String certificateId = certificateManagementService.addCertificate(
                    new Certificate.Builder()
                            .name(certName)
                            .certificateContent(certificatePEM)
                            .build(),
                    tenantDomain);

            // Store the certificate ID as a primitive property so DAO persists just the ID.
            properties.put(CERTIFICATE,
                    new ActionProperty.BuilderForDAO(certificateId).build());
        } catch (CertificateMgtException e) {
            throw new ActionDTOModelResolverException("Error storing certificate for action: "
                    + actionDTO.getId(), e);
        }
    }

    /**
     * Retrieves the certificate by its stored ID during action get operations.
     * Replaces the stored ID with the full Certificate object as a service-layer property.
     */
    private void handleCertificateGet(ActionDTO actionDTO, Map<String, ActionProperty> properties,
                                      String tenantDomain) throws ActionDTOModelResolverException {

        Object certIdValue = actionDTO.getPropertyValue(CERTIFICATE);
        if (certIdValue == null) {
            return;
        }

        try {
            String certIdStr = certIdValue.toString();
            Certificate certificate = certificateManagementService.getCertificate(
                    certIdStr, tenantDomain);
            properties.put(CERTIFICATE,
                    new ActionProperty.BuilderForService(certificate).build());
        } catch (CertificateMgtException e) {
            throw new ActionDTOModelResolverException("Error retrieving certificate for action: "
                    + actionDTO.getId(), e);
        }
    }

    /**
     * Handles certificate lifecycle during action update: add new, update existing, delete, or carry forward.
     */
    private void handleCertificateUpdate(ActionDTO updatingActionDTO, ActionDTO existingActionDTO,
                                         Map<String, ActionProperty> properties, String tenantDomain)
            throws ActionDTOModelResolverException {

        Object newCertValue = updatingActionDTO.getPropertyValue(CERTIFICATE);
        Object existingCertValue = existingActionDTO.getPropertyValue(CERTIFICATE);

        if (newCertValue != null && existingCertValue != null) {
            // Update existing certificate.
            String certificatePEM = extractCertificatePEM(newCertValue);
            try {
                String existingCertId = extractCertificateId(existingCertValue);
                certificateManagementService.updateCertificateContent(
                        existingCertId, certificatePEM, tenantDomain);
                // Carry forward the existing certificate ID.
                properties.put(CERTIFICATE,
                        new ActionProperty.BuilderForDAO(existingCertId).build());
            } catch (CertificateMgtException e) {
                throw new ActionDTOModelResolverException("Error updating certificate for action: "
                        + updatingActionDTO.getId(), e);
            }
        } else if (newCertValue != null) {
            // Add new certificate (previously had none).
            handleCertificateAdd(updatingActionDTO, properties, tenantDomain);
        } else if (existingCertValue != null) {
            // No new certificate provided — carry forward the existing one (PUT semantics).
            properties.put(CERTIFICATE,
                    new ActionProperty.BuilderForDAO(extractCertificateId(existingCertValue)).build());
        }
        // else: both null — no certificate to handle.
    }

    /**
     * Deletes the certificate from IDN_CERTIFICATE during action deletion.
     */
    private void handleCertificateDelete(ActionDTO deletingActionDTO, String tenantDomain)
            throws ActionDTOModelResolverException {

        Object certIdValue = deletingActionDTO.getPropertyValue(CERTIFICATE);
        if (certIdValue == null) {
            return;
        }

        try {
            String certId = certIdValue.toString();
            certificateManagementService.deleteCertificate(certId, tenantDomain);
        } catch (CertificateMgtException e) {
            throw new ActionDTOModelResolverException("Error deleting certificate for action: "
                    + deletingActionDTO.getId(), e);
        }
    }

    /**
     * Extracts the certificate UUID from a certificate property value.
     * <p>
     * The existing action DTO may come from the GET resolver, which replaces the stored UUID
     * with the full {@link Certificate} object. This method handles both cases:
     * - {@link Certificate} object: extracts the ID via {@code getId()}.
     * - String: assumes it is already the UUID.
     * </p>
     */
    private String extractCertificateId(Object certValue) {

        if (certValue instanceof Certificate) {
            return ((Certificate) certValue).getId();
        }
        return certValue.toString();
    }

    /**
     * Extracts the PEM string from a certificate value, which may be a Certificate object,
     * a Map, or a plain string.
     */
    private String extractCertificatePEM(Object certValue) throws ActionDTOModelResolverClientException {

        if (certValue instanceof Certificate) {
            return ((Certificate) certValue).getCertificateContent();
        } else if (certValue instanceof Map) {
            Map<?, ?> certMap = (Map<?, ?>) certValue;
            Object content = certMap.get("certificateContent");
            if (content instanceof String) {
                return (String) content;
            }
            throw new ActionDTOModelResolverClientException("Invalid certificate format.",
                    "Certificate object must contain a 'certificateContent' field.");
        } else if (certValue instanceof String) {
            return (String) certValue;
        }
        throw new ActionDTOModelResolverClientException("Invalid certificate format.",
                "Certificate must be a PEM string, a Certificate object, or a map with 'certificateContent'.");
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
            List<ContextPath> expose = OBJECT_MAPPER.readValue(jsonString, CONTEXT_PATH_LIST_TYPE_REF);
            return new ActionProperty.BuilderForService(expose).build();
        } catch (IOException e) {
            throw new ActionDTOModelResolverException("Error reading expose values from storage.", e);
        }
    }

    /**
     * Safely converts a value to boolean, handling both {@link Boolean} and {@link String} types.
     * Jackson deserializes JSON {@code true} as {@link Boolean} but JSON {@code "true"} as {@link String}.
     *
     * @param value The value to convert.
     * @return {@code true} if the value is Boolean TRUE or the string "true" (case-insensitive).
     */
    private static boolean toBooleanSafe(Object value) {

        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return false;
    }
}
