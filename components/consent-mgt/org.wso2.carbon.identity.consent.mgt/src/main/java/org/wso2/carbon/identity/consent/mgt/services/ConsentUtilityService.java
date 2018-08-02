/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.consent.mgt.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.PIICategoryValidity;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposePIICategory;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptPurposeInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptServiceInput;
import org.wso2.carbon.identity.consent.mgt.exceptions.ConsentUtilityServiceException;
import org.wso2.carbon.identity.consent.mgt.internal.IdentityConsentDataHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This service contains utility services for consent related functionality.
 */
public class ConsentUtilityService {

    private static final Log log = LogFactory.getLog(ConsentUtilityService.class);

    /**
     * Validate a given receipt with with respective purposes.
     *
     * @param receiptInput User given receipt.
     * @param purposes     Configured purposes.
     * @throws ConsentUtilityServiceException ConsentUtilityServiceException.
     */
    public void validateReceiptPIIs(ReceiptInput receiptInput, List<Purpose> purposes) throws
            ConsentUtilityServiceException {

        if (purposes == null || receiptInput == null) {
            throw new IllegalArgumentException("Receipt Input and purposes should not be null");
        }
        if (log.isDebugEnabled()) {
            log.debug("Validating receipt against purposes.");
        }
        List<ReceiptServiceInput> services = receiptInput.getServices();
        for (Purpose purpose : purposes) {
            purpose = fillPurpose(purpose);
            boolean purposeConsented = false;
            Set<Integer> mandatoryPIIs = getMandatoryPIIs(purpose);
            if (log.isDebugEnabled()) {
                log.debug("Mandatory PIIs for purpose : " + purpose.getName() + " : " + Arrays.toString
                        (mandatoryPIIs.toArray()));
            }
            for (ReceiptServiceInput service : services) {
                List<ReceiptPurposeInput> consentPurposes = service.getPurposes();
                for (ReceiptPurposeInput consentPurpose : consentPurposes) {
                    if (consentPurpose.getPurposeId() == purpose.getId()) {
                        purposeConsented = true;
                        List<PIICategoryValidity> pIICategories = consentPurpose.getPiiCategory();
                        Set<Integer> consentedPIIs = getPIIs(pIICategories);

                        if (log.isDebugEnabled()) {
                            log.debug("Consented PIIs: " + Arrays.toString
                                    (consentedPIIs.toArray()));
                        }
                        if (!consentedPIIs.containsAll(mandatoryPIIs)) {
                            throw new ConsentUtilityServiceException("One or more mandatory attributes are missing in " +
                                    "the given receipt");
                        }
                    }
                }
                if (!purposeConsented && !mandatoryPIIs.isEmpty()) {
                    throw new ConsentUtilityServiceException("Consent receipt does not contain consent for " +
                            "purpose " + purpose.getName() + " with ID: " + purpose.getId() + ", which has " +
                            "mandatory PIIs");
                }
            }

        }
    }

    /**
     * If the consent is not given for a PII
     *
     * @param keySet
     * @param receipt
     * @return
     * @throws ConsentUtilityServiceException
     */
    public Set<String> filterPIIsFromReceipt(Set<String> keySet, ReceiptInput receipt) throws
            ConsentUtilityServiceException {

        if (keySet == null || receipt == null) {
            throw new ConsentUtilityServiceException("Key set and receipt should not be null");
        }
        List<ReceiptServiceInput> services = receipt.getServices();
        Set<String> consentedPIIs = new HashSet<>();
        for (ReceiptServiceInput service : services) {
            List<ReceiptPurposeInput> purposes = service.getPurposes();
            for (ReceiptPurposeInput consentPurpose : purposes) {
                List<PIICategoryValidity> piiCategories = consentPurpose.getPiiCategory();
                for (PIICategoryValidity piiCategory : piiCategories) {
                    consentedPIIs.add(getPIIName(consentPurpose.getPurposeId(), piiCategory.getId()));
                }

            }
        }
        keySet.retainAll(consentedPIIs);
        return keySet;
    }

    /**
     * Returns the set of mandatory PIIs of a given set of purposes.
     *
     * @param purposes List of purposes.
     * @return Set of Mandatory PIIs.
     * @throws ConsentUtilityServiceException
     */

    public Set<Integer> getMandatoryPIIs(List<Purpose> purposes) throws ConsentUtilityServiceException {

        if (purposes == null) {
            throw new ConsentUtilityServiceException("Purposes list should not be null");
        }
        Set<Integer> mandatoryPIIs = new HashSet<>();
        for (Purpose purpose : purposes) {
            Set<Integer> mandatoryPurposePIIs = getMandatoryPIIs(purpose);
            mandatoryPIIs.addAll(mandatoryPurposePIIs);

        }
        return mandatoryPIIs;
    }

    /**
     * Get mandatory PIIs of a purpose.
     *
     * @param purpose Purpose.
     * @return A set of mandatory PIIs in the given purpose.
     * @throws ConsentUtilityServiceException ConsentUtilityServiceException.
     */

    public Set<Integer> getMandatoryPIIs(Purpose purpose) throws
            ConsentUtilityServiceException {

        if (purpose == null) {
            throw new ConsentUtilityServiceException("Purposes List should not be null");
        }
        Set<Integer> mandatoryPIIs = new HashSet<>();
        purpose = fillPurpose(purpose);
        List<PurposePIICategory> purposePIICategories = purpose.getPurposePIICategories();
        for (PurposePIICategory purposePIICategory : purposePIICategories) {
            if (purposePIICategory.getMandatory()) {
                mandatoryPIIs.add(purposePIICategory.getId());
            }
        }
        return mandatoryPIIs;
    }

    /**
     * Get unique PIIs of a given set of purposes.
     *
     * @param purposes List of purposes.
     * @return Set of PIIs which contains in Map.
     * @throws ConsentUtilityServiceException ConsentUtilityServiceException.
     */
    public Set<Integer> getUniquePIIs(List<Purpose> purposes) throws ConsentUtilityServiceException {

        if (purposes == null) {
            throw new ConsentUtilityServiceException("Purposes List should not be null");
        }

        Set<Integer> uniquePIIs = new HashSet<>();
        for (Purpose purpose : purposes) {
            Set<Integer> piis = getPIIs(purpose);
            uniquePIIs.addAll(piis);
        }
        return uniquePIIs;
    }

    /**
     * Retrieve full purposes which are filled with all information.
     *
     * @param purposes Purposes list
     * @return Filled purposes list.
     * @throws ConsentUtilityServiceException ConsentUtilityServiceException.
     */
    public List<Purpose> getFilledPurposes(List<Purpose> purposes) throws ConsentUtilityServiceException {

        if (purposes == null) {
            throw new IllegalArgumentException("Purposes List should not be null");
        }
        List<Purpose> filledPurposes = new ArrayList<>();
        for (Purpose purpose : purposes) {
            filledPurposes.add(fillPurpose(purpose));
        }
        return filledPurposes;
    }

    private Set<Integer> getPIIs(Purpose purpose) throws ConsentUtilityServiceException {

        Set<Integer> uniquePIIs = new HashSet<>();
        Integer id = purpose.getId();
        purpose = fillPurpose(purpose);
        List<PurposePIICategory> purposePIICategories = purpose.getPurposePIICategories();
        for (PurposePIICategory purposePIICategory : purposePIICategories) {
            uniquePIIs.add(purposePIICategory.getId());
        }
        return uniquePIIs;
    }

    private Purpose fillPurpose(Purpose purpose) throws ConsentUtilityServiceException {

        ConsentManager consentManager = IdentityConsentDataHolder.getInstance().getConsentManager();
        if (purpose.getPurposePIICategories() != null && purpose.getPurposePIICategories().isEmpty()) {
            try {
                purpose = consentManager.getPurpose(purpose.getId());
            } catch (ConsentManagementException e) {
                throw new ConsentUtilityServiceException("Error while retrieving purpose with purpose ID: " + purpose
                        .getId(), e);
            }
        }
        return purpose;
    }

    private Set<Integer> getPIIs(List<PIICategoryValidity> piiCategoryValidities) {

        Set<Integer> piis = new HashSet<>();
        for (PIICategoryValidity piiCategoryValidity : piiCategoryValidities) {
            piis.add(piiCategoryValidity.getId());
        }
        return piis;
    }

    private String getPIIName(int purposeId, int PIIId) throws ConsentUtilityServiceException {

        ConsentManager consentManager = IdentityConsentDataHolder.getInstance().getConsentManager();
        try {
            Purpose purpose = consentManager.getPurpose(purposeId);
            List<PurposePIICategory> purposePIICategories = purpose.getPurposePIICategories();
            for (PurposePIICategory purposePIICategory : purposePIICategories) {
                if (purposePIICategory.getId() == PIIId) {
                    return purposePIICategory.getName();
                }
            }
        } catch (ConsentManagementException e) {
            throw new ConsentUtilityServiceException("Error while retrieving purpose with id:" + purposeId, e);
        }
        throw new ConsentUtilityServiceException("No PII can be found within given id: " + PIIId + "for purpose :" +
                purposeId);
    }
}

