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

package org.wso2.carbon.identity.user.pre.update.password.action.management;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.action.management.exception.ActionDTOModelResolverClientException;
import org.wso2.carbon.identity.action.management.exception.ActionDTOModelResolverException;
import org.wso2.carbon.identity.action.management.exception.ActionDTOModelResolverServerException;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.ActionDTO;
import org.wso2.carbon.identity.action.management.service.ActionDTOModelResolver;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtClientException;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.certificate.management.service.CertificateManagementService;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.PreUpdatePasswordActionServiceComponentHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.user.pre.update.password.action.constant.PreUpdatePasswordActionConstants.CERTIFICATE;
import static org.wso2.carbon.identity.user.pre.update.password.action.constant.PreUpdatePasswordActionConstants.PASSWORD_SHARING_FORMAT;

/**
 * This class implements the methods required to resolve ActionDTO objects in Pre Update Password extension.
 */
public class PreUpdatePasswordActionDTOModelResolver implements ActionDTOModelResolver {

    @Override
    public Action.ActionTypes getSupportedActionType() {

        return Action.ActionTypes.PRE_UPDATE_PASSWORD;
    }

    @Override
    public ActionDTO resolveForAddOperation(ActionDTO actionDTO, String tenantDomain)
            throws ActionDTOModelResolverException {

        Map<String, Object> properties = new HashMap<>();
        Certificate certificate = (Certificate) actionDTO.getProperty(CERTIFICATE);
        if (certificate != null && certificate.getCertificateContent() != null) {
            Certificate certToBeAdded = buildCertificate(actionDTO.getId(), certificate);
            // Certificate is an optional attribute.
            properties.put(CERTIFICATE, addCertificate(certToBeAdded, tenantDomain));
        }

        if (actionDTO.getProperty(PASSWORD_SHARING_FORMAT) == null) {
            throw new ActionDTOModelResolverClientException("Invalid Request",
                    "Password sharing format is a required field.");
        }
        properties.put(PASSWORD_SHARING_FORMAT, actionDTO.getProperty(PASSWORD_SHARING_FORMAT));

        return new ActionDTO.Builder(actionDTO)
                .properties(properties)
                .build();
    }

    @Override
    public ActionDTO resolveForGetOperation(ActionDTO actionDTO, String tenantDomain)
            throws ActionDTOModelResolverException {

        Map<String, Object> properties = new HashMap<>();
        String certificateId = (String) actionDTO.getProperty(CERTIFICATE);
        if (certificateId != null) {
            // Certificate is an optional attribute.
            properties.put(CERTIFICATE, getCertificate(certificateId, tenantDomain));
        }
        properties.put(PASSWORD_SHARING_FORMAT, actionDTO.getProperty(PASSWORD_SHARING_FORMAT));

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
     * @param updatingActionDTO  ActionDTO that needs to be updated.
     * @param existingActionDTO  Existing ActionDTO.
     * @param tenantDomain       Tenant domain.
     * @return Resolved ActionDTO.
     * @throws ActionDTOModelResolverException ActionDTOModelResolverException.
     */
    @Override
    public ActionDTO resolveForUpdateOperation(ActionDTO updatingActionDTO, ActionDTO existingActionDTO,
                                               String tenantDomain) throws ActionDTOModelResolverException {

        Map<String, Object> properties = new HashMap<>();
        resolveCertificateUpdate(updatingActionDTO, existingActionDTO, properties, tenantDomain);
        resolvePasswordSharingFormatUpdate(updatingActionDTO, existingActionDTO, properties);

        return new ActionDTO.Builder(updatingActionDTO)
                .properties(properties)
                .build();
    }

    @Override
    public void resolveForDeleteOperation(ActionDTO deletingActionDTO, String tenantDomain)
            throws ActionDTOModelResolverException {

        Certificate certificate = (Certificate) deletingActionDTO.getProperty(CERTIFICATE);
        if (certificate != null && certificate.getId() != null) {
            deleteCertificate(certificate.getId(), tenantDomain);
        }
    }

    private static Certificate buildCertificate(String actionId, Certificate updatingCertificate) {

        return new Certificate.Builder()
                .name("ACTIONS:" + actionId)
                .certificateContent(updatingCertificate.getCertificateContent())
                .build();
    }

    private String addCertificate(Certificate certificate, String tenantDomain) throws ActionDTOModelResolverException {

        try {
            return getCertificateManagementService().addCertificate(certificate, tenantDomain);
        } catch (CertificateMgtClientException e) {
            throw new ActionDTOModelResolverClientException("Error while adding the certificate.", e.getDescription());
        } catch (CertificateMgtException e) {
            throw new ActionDTOModelResolverServerException("Error while adding the certificate.", e.getDescription(),
                    e);
        }
    }

    private Certificate getCertificate(String certificateId, String tenantDomain)
            throws ActionDTOModelResolverException {

        try {
            return getCertificateManagementService().getCertificate(certificateId, tenantDomain);
        } catch (CertificateMgtException e) {
            throw new ActionDTOModelResolverServerException("Error while retrieving the certificate.",
                    e.getDescription(), e);
        }
    }

    private void resolveCertificateUpdate(ActionDTO updatingActionDTO, ActionDTO existingActionDTO,
                                          Map<String, Object> properties, String tenantDomain)
            throws ActionDTOModelResolverException {

        Certificate updatingCertificate = (Certificate) updatingActionDTO.getProperty(CERTIFICATE);
        Certificate existingCertificate = (Certificate) existingActionDTO.getProperty(CERTIFICATE);

        if (updatingCertificate != null && updatingCertificate.getCertificateContent() != null) {
            updateActionCertificate(updatingCertificate, existingCertificate, properties, tenantDomain);
        } else if (existingCertificate != null && existingCertificate.getId() != null) {
            // If a certificate is configured, and it is not updated; use the existing certificateId.
            properties.put(CERTIFICATE, existingCertificate.getId());
        }
    }

    private void updateActionCertificate(Certificate updatingCertificate, Certificate existingCertificate,
                                         Map<String, Object> properties, String tenantDomain)
            throws ActionDTOModelResolverException {

        if (existingCertificate != null && existingCertificate.getId() != null) {
            if (updatingCertificate.getCertificateContent().equals(StringUtils.EMPTY)) {
                deleteCertificate(existingCertificate.getId(), tenantDomain);
            } else {
                updateCertificate(existingCertificate.getId(), updatingCertificate.getCertificateContent(),
                        tenantDomain);

                properties.put(CERTIFICATE, existingCertificate.getId());
            }
        } else {
            Certificate certToBeAdded = buildCertificate(updatingCertificate.getId(), updatingCertificate);
            properties.put(CERTIFICATE, addCertificate(certToBeAdded, tenantDomain));
        }
    }

    private void updateCertificate(String certificateId, String updatingContent, String tenantDomain)
            throws ActionDTOModelResolverException {

        try {
             getCertificateManagementService().updateCertificateContent(certificateId, updatingContent, tenantDomain);
        } catch (CertificateMgtClientException e) {
            throw new ActionDTOModelResolverClientException("Error while updating the certificate.",
                    e.getDescription());
        } catch (CertificateMgtException e) {
            throw new ActionDTOModelResolverServerException("Error while updating the certificate.", e.getDescription(),
                    e);
        }
    }

    private void deleteCertificate(String certificateId, String tenantDomain) throws ActionDTOModelResolverException {

        try {
            getCertificateManagementService().deleteCertificate(certificateId, tenantDomain);
        } catch (CertificateMgtException e) {
            throw new ActionDTOModelResolverServerException("Error while deleting the certificate.", e.getDescription(),
                    e);
        }
    }

    private CertificateManagementService getCertificateManagementService() {

        return PreUpdatePasswordActionServiceComponentHolder.getInstance().getCertificateManagementService();
    }

    private void resolvePasswordSharingFormatUpdate(ActionDTO updatingActionDTO, ActionDTO existingActionDTO,
                                                    Map<String, Object> properties) {

        if (updatingActionDTO.getProperty(PASSWORD_SHARING_FORMAT) != null) {
            properties.put(PASSWORD_SHARING_FORMAT, updatingActionDTO.getProperty(PASSWORD_SHARING_FORMAT));
        } else {
            properties.put(PASSWORD_SHARING_FORMAT, existingActionDTO.getProperty(PASSWORD_SHARING_FORMAT));
        }
    }
}
