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

package org.wso2.carbon.identity.user.pre.update.password.action.internal.management;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverClientException;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverException;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverServerException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionProperty;
import org.wso2.carbon.identity.action.management.api.service.ActionDTOModelResolver;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtClientException;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.certificate.management.service.CertificateManagementService;
import org.wso2.carbon.identity.user.pre.update.password.action.api.model.PasswordSharing;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.component.PreUpdatePasswordActionServiceComponentHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.user.pre.update.password.action.internal.constant.PreUpdatePasswordActionConstants.CERTIFICATE;
import static org.wso2.carbon.identity.user.pre.update.password.action.internal.constant.PreUpdatePasswordActionConstants.PASSWORD_SHARING_FORMAT;

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

        Map<String, ActionProperty> properties = new HashMap<>();

        Object passwordSharingFormat = actionDTO.getPropertyValue(PASSWORD_SHARING_FORMAT);
        // Password sharing format is a required field.
        if (passwordSharingFormat == null) {
            throw new ActionDTOModelResolverClientException("Invalid Request",
                    "Password sharing format is a required field.");
        }
        if (!(passwordSharingFormat instanceof PasswordSharing.Format)) {
            throw new ActionDTOModelResolverClientException("Invalid Password Sharing Format.",
                    "Provided Password sharing format is unsupported.");
        }
        properties.put(PASSWORD_SHARING_FORMAT, new ActionProperty.BuilderForDAO(((PasswordSharing.Format)
                passwordSharingFormat).name()).build());

        // Certificate is an optional field.
        Object certificate = actionDTO.getPropertyValue(CERTIFICATE);
        if (certificate != null) {
            if (!(certificate instanceof Certificate)) {
                throw new ActionDTOModelResolverClientException("Invalid Certificate.",
                        "Provided certificate is unsupported.");
            }

            Certificate certToBeAdded = buildCertificate(actionDTO.getId(), (Certificate) certificate);
            String certificateId = addCertificate(certToBeAdded, tenantDomain);
            properties.put(CERTIFICATE, new ActionProperty.BuilderForDAO(certificateId).build());
        }

        return new ActionDTO.Builder(actionDTO)
                .properties(properties)
                .build();
    }

    @Override
    public ActionDTO resolveForGetOperation(ActionDTO actionDTO, String tenantDomain)
            throws ActionDTOModelResolverException {

        Map<String, ActionProperty> properties = new HashMap<>();

        if (actionDTO.getPropertyValue(PASSWORD_SHARING_FORMAT) == null) {
            throw new ActionDTOModelResolverServerException("Error while retrieving the password sharing format.",
                    "Unable to retrieve the password sharing format from the system");
        }
        properties.put(PASSWORD_SHARING_FORMAT, new ActionProperty.BuilderForService(
                PasswordSharing.Format.valueOf(actionDTO.getPropertyValue(PASSWORD_SHARING_FORMAT).toString()))
                .build());

        // Certificate is an optional field.
        if (actionDTO.getPropertyValue(CERTIFICATE) != null) {
            Certificate certificate = getCertificate((String) actionDTO.getPropertyValue(CERTIFICATE), tenantDomain);
            properties.put(CERTIFICATE, new ActionProperty.BuilderForService(certificate).build());
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
     * @param updatingActionDTO  ActionDTO that needs to be updated.
     * @param existingActionDTO  Existing ActionDTO.
     * @param tenantDomain       Tenant domain.
     * @return Resolved ActionDTO.
     * @throws ActionDTOModelResolverException ActionDTOModelResolverException.
     */
    @Override
    public ActionDTO resolveForUpdateOperation(ActionDTO updatingActionDTO, ActionDTO existingActionDTO,
                                               String tenantDomain) throws ActionDTOModelResolverException {

        Map<String, ActionProperty> properties = new HashMap<>();
        resolveCertificateUpdate(updatingActionDTO, existingActionDTO, properties, tenantDomain);
        resolvePasswordSharingFormatUpdate(updatingActionDTO, existingActionDTO, properties);

        return new ActionDTO.Builder(updatingActionDTO)
                .properties(properties)
                .build();
    }

    @Override
    public void resolveForDeleteOperation(ActionDTO deletingActionDTO, String tenantDomain)
            throws ActionDTOModelResolverException {

        if (deletingActionDTO.getPropertyValue(CERTIFICATE) instanceof Certificate) {
            Certificate certificate = (Certificate) deletingActionDTO.getPropertyValue(CERTIFICATE);
            deleteCertificate(certificate.getId(), tenantDomain);
        }
    }

    private static Certificate buildCertificate(String actionId, Certificate updatingCertificate) {

        return new Certificate.Builder()
                .name("ACTIONS:" + actionId)
                .certificateContent(updatingCertificate.getCertificateContent())
                .build();
    }

    private void resolvePasswordSharingFormatUpdate(ActionDTO updatingActionDTO, ActionDTO existingActionDTO,
                                                    Map<String, ActionProperty> properties) {

        if (updatingActionDTO.getPropertyValue(PASSWORD_SHARING_FORMAT) != null) {
            properties.put(PASSWORD_SHARING_FORMAT, new ActionProperty.BuilderForDAO(
                    ((PasswordSharing.Format) updatingActionDTO.getPropertyValue(PASSWORD_SHARING_FORMAT)).name())
                    .build());
        } else {
            properties.put(PASSWORD_SHARING_FORMAT, new ActionProperty.BuilderForDAO(
                    ((PasswordSharing.Format) existingActionDTO.getPropertyValue(PASSWORD_SHARING_FORMAT)).name())
                    .build());
        }
    }

    private void resolveCertificateUpdate(ActionDTO updatingActionDTO, ActionDTO existingActionDTO,
                                          Map<String, ActionProperty> properties, String tenantDomain)
            throws ActionDTOModelResolverException {

        Certificate updatingCertificate = (Certificate) updatingActionDTO.getPropertyValue(CERTIFICATE);
        Certificate existingCertificate = (Certificate) existingActionDTO.getPropertyValue(CERTIFICATE);

        if (isAddingNewCertificate(updatingCertificate, existingCertificate)) {
            Certificate certToBeAdded = buildCertificate(updatingActionDTO.getId(), updatingCertificate);
            String certificateId = addCertificate(certToBeAdded, tenantDomain);
            properties.put(CERTIFICATE, new ActionProperty.BuilderForDAO(certificateId).build());
        } else if (isDeletingExistingCertificate(updatingCertificate, existingCertificate)) {
            deleteCertificate(existingCertificate.getId(), tenantDomain);
        } else if (isUpdatingExistingCertificate(updatingCertificate, existingCertificate)) {
            updateCertificate(existingCertificate.getId(), updatingCertificate.getCertificateContent(), tenantDomain);
            properties.put(CERTIFICATE, new ActionProperty.BuilderForDAO(existingCertificate.getId()).build());
        } else if (existingCertificate != null) {
            properties.put(CERTIFICATE, new ActionProperty.BuilderForDAO(existingCertificate.getId()).build());
        }
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

    private void deleteCertificate(String certificateId, String tenantDomain) throws ActionDTOModelResolverException {

        try {
            getCertificateManagementService().deleteCertificate(certificateId, tenantDomain);
        } catch (CertificateMgtException e) {
            throw new ActionDTOModelResolverServerException("Error while deleting the certificate.", e.getDescription(),
                    e);
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

    private boolean isAddingNewCertificate(Certificate updatingCertificate, Certificate existingCertificate) {

        return existingCertificate == null && updatingCertificate != null &&
                updatingCertificate.getCertificateContent() != null;
    }

    private boolean isDeletingExistingCertificate(Certificate updatingCertificate, Certificate existingCertificate) {

        return existingCertificate != null && updatingCertificate != null &&
                updatingCertificate.getCertificateContent() != null &&
                updatingCertificate.getCertificateContent().equals(StringUtils.EMPTY);
    }

    private boolean isUpdatingExistingCertificate(Certificate updatingCertificate, Certificate existingCertificate) {

        return existingCertificate != null && updatingCertificate != null &&
                updatingCertificate.getCertificateContent() != null &&
                !updatingCertificate.getCertificateContent().equals(StringUtils.EMPTY);
    }

    private CertificateManagementService getCertificateManagementService() {

        return PreUpdatePasswordActionServiceComponentHolder.getInstance().getCertificateManagementService();
    }
}
