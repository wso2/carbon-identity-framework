/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.extension.management;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.wso2.carbon.identity.flow.extension.model.ContextPath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ActionManagement.ACCESS_CONFIG_EXPOSE;
import static org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ActionManagement.ACCESS_CONFIG_MODIFY;
import static org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ActionManagement.CERTIFICATE;
import static org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ActionManagement.CERTIFICATE_NAME_PREFIX;
import static org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ActionManagement.ICON_URL;
import static org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ActionManagement.MAX_EXPOSE_PATHS;

/**
 * ActionDTOModelResolver implementation for Flow Extension actions.
 */
public class FlowExtensionActionDTOModelResolver implements ActionDTOModelResolver {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<ContextPath>> CONTEXT_PATH_LIST_TYPE_REF =
            new TypeReference<List<ContextPath>>() { };

    private final CertificateManagementService certificateManagementService;

    public FlowExtensionActionDTOModelResolver(CertificateManagementService certificateManagementService) {

        this.certificateManagementService = certificateManagementService;
    }

    @Override
    public Action.ActionTypes getSupportedActionType() {

        return Action.ActionTypes.FLOW_EXTENSION;
    }

    @Override
    public ActionDTO resolveForAddOperation(ActionDTO actionDTO, String tenantDomain)
            throws ActionDTOModelResolverException {

        Map<String, ActionProperty> properties = new HashMap<>();

        Object exposeValue = actionDTO.getPropertyValue(ACCESS_CONFIG_EXPOSE);
        if (exposeValue != null) {
            List<ContextPath> validatedExpose = validateExpose(exposeValue);
            properties.put(ACCESS_CONFIG_EXPOSE, createBlobProperty(validatedExpose));
        }

        Object modifyValue = actionDTO.getPropertyValue(ACCESS_CONFIG_MODIFY);
        if (modifyValue != null) {
            List<ContextPath> validatedModify = validateExpose(modifyValue);
            properties.put(ACCESS_CONFIG_MODIFY, createBlobProperty(validatedModify));
        }

        handleCertificateAdd(actionDTO, properties, tenantDomain);

        Object iconUrlValue = actionDTO.getPropertyValue(ICON_URL);
        if (iconUrlValue instanceof String iconUrlStr && !iconUrlStr.isEmpty()) {
            properties.put(ICON_URL, new ActionProperty.BuilderForDAO(iconUrlStr).build());
        }

        return new ActionDTO.Builder(actionDTO)
                .properties(properties)
                .build();
    }

    @Override
    public ActionDTO resolveForGetOperation(ActionDTO actionDTO, String tenantDomain)
            throws ActionDTOModelResolverException {

        Map<String, ActionProperty> properties = new HashMap<>();

        if (actionDTO.getPropertyValue(ACCESS_CONFIG_EXPOSE) != null) {
            properties.put(ACCESS_CONFIG_EXPOSE, deserializeExposeProperty(
                    ((BinaryObject) actionDTO.getPropertyValue(ACCESS_CONFIG_EXPOSE)).getJSONString()));
        }

        if (actionDTO.getPropertyValue(ACCESS_CONFIG_MODIFY) != null) {
            properties.put(ACCESS_CONFIG_MODIFY, deserializeExposeProperty(
                    ((BinaryObject) actionDTO.getPropertyValue(ACCESS_CONFIG_MODIFY)).getJSONString()));
        }

        handleCertificateGet(actionDTO, properties, tenantDomain);

        Object iconUrlValue = actionDTO.getPropertyValue(ICON_URL);
        if (iconUrlValue != null) {
            properties.put(ICON_URL, new ActionProperty.BuilderForService(iconUrlValue.toString()).build());
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

    @Override
    public ActionDTO resolveForUpdateOperation(ActionDTO updatingActionDTO, ActionDTO existingActionDTO,
                                               String tenantDomain) throws ActionDTOModelResolverException {

        Map<String, ActionProperty> properties = new HashMap<>();

        // DAO layer treats action property updates as PUT, so unchanged properties must be re-sent.
        List<ContextPath> expose = getResolvedUpdatingExpose(updatingActionDTO, existingActionDTO);
        if (!expose.isEmpty()) {
            properties.put(ACCESS_CONFIG_EXPOSE, createBlobProperty(expose));
        }

        List<ContextPath> modify = getResolvedUpdatingModify(updatingActionDTO, existingActionDTO);
        if (!modify.isEmpty()) {
            properties.put(ACCESS_CONFIG_MODIFY, createBlobProperty(modify));
        }

        handleCertificateUpdate(updatingActionDTO, existingActionDTO, properties, tenantDomain);
        resolveUpdateIconUrl(updatingActionDTO, existingActionDTO, properties);

        return new ActionDTO.Builder(updatingActionDTO)
                .properties(properties)
                .build();
    }

    private void resolveUpdateIconUrl(ActionDTO updatingActionDTO, ActionDTO existingActionDTO,
                                      Map<String, ActionProperty> properties)
            throws ActionDTOModelResolverException {

        Object updatingIconUrl = updatingActionDTO.getPropertyValue(ICON_URL);
        if (updatingIconUrl instanceof String updatingIconUrlStr && !updatingIconUrlStr.isEmpty()) {
            properties.put(ICON_URL, new ActionProperty.BuilderForDAO(updatingIconUrlStr).build());
        } else if (existingActionDTO.getPropertyValue(ICON_URL) != null) {
            properties.put(ICON_URL, new ActionProperty.BuilderForDAO(
                    existingActionDTO.getPropertyValue(ICON_URL).toString()).build());
        }
    }

    @Override
    public void resolveForDeleteOperation(ActionDTO deletingActionDTO, String tenantDomain)
            throws ActionDTOModelResolverException {

        handleCertificateDelete(deletingActionDTO, tenantDomain);
    }

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

    @SuppressWarnings("unchecked")
    private List<ContextPath> validateExpose(Object exposeValue) throws ActionDTOModelResolverException {

        if (!(exposeValue instanceof List<?>)) {
            throw new ActionDTOModelResolverClientException("Invalid expose format.",
                    "Expose should be provided as a list.");
        }

        List<?> exposeList = (List<?>) exposeValue;
        List<ContextPath> result = new ArrayList<>();

        for (Object item : exposeList) {
            if (item instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) item;
                if (!map.containsKey("path") || !(map.get("path") instanceof String)) {
                    throw new ActionDTOModelResolverClientException("Invalid expose format.",
                            "Each expose entry must be an object with a 'path' field.");
                }
                String path = (String) map.get("path");
                boolean encrypted = map.containsKey("encrypted") && toBooleanSafe(map.get("encrypted"));
                result.add(new ContextPath(path, encrypted));
            } else if (item instanceof ContextPath contextPath) {
                result.add(contextPath);
            } else {
                throw new ActionDTOModelResolverClientException("Invalid expose format.",
                        "Each expose entry must be an object with 'path' and optional 'encrypted' fields.");
            }
        }

        validateContextPathFormat(result);
        return result;
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
            if (path.endsWith("/")) {
                throw new ActionDTOModelResolverClientException("Invalid expose path.",
                        String.format("Expose path '%s' must not end with a trailing '/'.", path));
            }
            if (!seen.add(path)) {
                throw new ActionDTOModelResolverClientException("Duplicate expose path.",
                        String.format("The expose path '%s' is duplicated.", path));
            }
        }
    }

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

            properties.put(CERTIFICATE,
                    new ActionProperty.BuilderForDAO(certificateId).build());
        } catch (CertificateMgtException e) {
            throw new ActionDTOModelResolverException("Error storing certificate for action: "
                    + actionDTO.getId(), e);
        }
    }

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

    private void handleCertificateUpdate(ActionDTO updatingActionDTO, ActionDTO existingActionDTO,
                                         Map<String, ActionProperty> properties, String tenantDomain)
            throws ActionDTOModelResolverException {

        Object newCertValue = updatingActionDTO.getPropertyValue(CERTIFICATE);
        Object existingCertValue = existingActionDTO.getPropertyValue(CERTIFICATE);

        // Empty string signals explicit certificate removal.
        boolean isExplicitRemoval = newCertValue instanceof String s && s.isEmpty();

        if (isExplicitRemoval && existingCertValue != null) {
            try {
                String existingCertId = extractCertificateId(existingCertValue);
                certificateManagementService.deleteCertificate(existingCertId, tenantDomain);
            } catch (CertificateMgtException e) {
                throw new ActionDTOModelResolverException("Error deleting certificate for action: "
                        + updatingActionDTO.getId(), e);
            }
        } else if (newCertValue != null && !isExplicitRemoval && existingCertValue != null) {
            String certificatePEM = extractCertificatePEM(newCertValue);
            try {
                String existingCertId = extractCertificateId(existingCertValue);
                certificateManagementService.updateCertificateContent(
                        existingCertId, certificatePEM, tenantDomain);
                properties.put(CERTIFICATE,
                        new ActionProperty.BuilderForDAO(existingCertId).build());
            } catch (CertificateMgtException e) {
                throw new ActionDTOModelResolverException("Error updating certificate for action: "
                        + updatingActionDTO.getId(), e);
            }
        } else if (newCertValue != null && !isExplicitRemoval) {
            handleCertificateAdd(updatingActionDTO, properties, tenantDomain);
        } else if (existingCertValue != null) {
            // Carry forward existing certificate ID — PUT semantics.
            properties.put(CERTIFICATE,
                    new ActionProperty.BuilderForDAO(extractCertificateId(existingCertValue)).build());
        }
    }

    private void handleCertificateDelete(ActionDTO deletingActionDTO, String tenantDomain)
            throws ActionDTOModelResolverException {

        Object certIdValue = deletingActionDTO.getPropertyValue(CERTIFICATE);
        if (certIdValue == null) {
            return;
        }

        try {
            String certId = extractCertificateId(certIdValue);
            certificateManagementService.deleteCertificate(certId, tenantDomain);
        } catch (CertificateMgtException e) {
            throw new ActionDTOModelResolverException("Error deleting certificate for action: "
                    + deletingActionDTO.getId(), e);
        }
    }

    // Handles both raw UUID strings and Certificate objects, since the GET resolver replaces
    // the stored UUID with the full Certificate.
    private String extractCertificateId(Object certValue) {

        if (certValue instanceof Certificate certificate) {
            return certificate.getId();
        }
        return certValue.toString();
    }

    private String extractCertificatePEM(Object certValue) throws ActionDTOModelResolverClientException {

        if (certValue instanceof Certificate certificate) {
            return certificate.getCertificateContent();
        } else if (certValue instanceof Map) {
            Map<?, ?> certMap = (Map<?, ?>) certValue;
            Object content = certMap.get("certificateContent");
            if (content instanceof String pem) {
                return pem;
            }
            throw new ActionDTOModelResolverClientException("Invalid certificate format.",
                    "Certificate object must contain a 'certificateContent' field.");
        } else if (certValue instanceof String pem) {
            return pem;
        }
        throw new ActionDTOModelResolverClientException("Invalid certificate format.",
                "Certificate must be a PEM string, a Certificate object, or a map with 'certificateContent'.");
    }

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

    // Jackson deserializes JSON true as Boolean but JSON "true" as String — handle both.
    private static boolean toBooleanSafe(Object value) {

        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof String s) {
            return Boolean.parseBoolean(s);
        }
        return false;
    }
}
