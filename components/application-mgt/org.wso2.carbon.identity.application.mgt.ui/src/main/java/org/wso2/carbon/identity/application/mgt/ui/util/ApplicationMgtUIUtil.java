/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.mgt.ui.util;

import org.wso2.carbon.identity.application.mgt.ui.ApplicationBean;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpSession;

public class ApplicationMgtUIUtil {

    private static final String SP_UNIQUE_ID_MAP = "spUniqueIdMap";
    public static final String JWKS_URI = "jwksURI";
    public static final String JWKS_DISPLAYNAME = "JWKS Endpoint";

    /**
     * Get related application bean from the session.
     *
     * @param session HTTP Session.
     * @param spName  Service provider name.
     * @return ApplicationBean
     */
    public static ApplicationBean getApplicationBeanFromSession(HttpSession session, String spName) {

        Map<String, UUID> spUniqueIdMap;

        if (session.getAttribute(SP_UNIQUE_ID_MAP) == null) {
            spUniqueIdMap = new HashMap<>();
            session.setAttribute(SP_UNIQUE_ID_MAP, spUniqueIdMap);
        } else {
            spUniqueIdMap = (HashMap<String, UUID>)session.getAttribute(SP_UNIQUE_ID_MAP);
        }

        if (spUniqueIdMap.get(spName) == null) {
            ApplicationBean applicationBean = new ApplicationBean();
            UUID uuid = UUID.randomUUID();
            spUniqueIdMap.put(spName, uuid);
            session.setAttribute(uuid.toString(), applicationBean);
        }
        return (ApplicationBean) session.getAttribute(spUniqueIdMap.get(spName).toString());
    }

    /**
     * Remove related application bean from the session.
     *
     * @param session Http Session.
     * @param spName  Service provider name.
     */
    public static void removeApplicationBeanFromSession(HttpSession session, String spName) {

        if (session.getAttribute(SP_UNIQUE_ID_MAP) == null) {
            return;
        }
        Map<String, UUID> spUniqueIdMap = (HashMap<String, UUID>)session.getAttribute(SP_UNIQUE_ID_MAP);

        if (spUniqueIdMap.get(spName) == null) {
            return;
        }
        session.removeAttribute(spUniqueIdMap.get(spName).toString());
        spUniqueIdMap.remove(spName);
    }

    // Will be supported with 'Advance Consent Management Feature'.
    /*
    public static ApplicationPurposes getApplicationSpecificPurposes(ServiceProvider serviceProvider)
            throws ConsentManagementException {

        ConsentConfig consentConfig = serviceProvider.getConsentConfig();
        ConsentManagementServiceClient consentServiceClient = new ConsentManagementServiceClient();
        Purpose[] spPurposes;
        List<ApplicationPurpose> appPurposes = new ArrayList<>();
        List<ApplicationPurpose> appSharedPurposes = new ArrayList<>();
        ConsentPurpose[] consentPurposes = new ConsentPurpose[0];
        if (isConsentPurposesAvailable(consentConfig)) {
            consentPurposes = consentConfig.getConsentPurposeConfigs().getConsentPurpose();
        }

        // Get application specific purposes associated with the service provider.
        spPurposes = consentServiceClient.listPurposes(serviceProvider.getApplicationName(), PURPOSE_GROUP_TYPE_SP);
        if (nonNull(spPurposes)) {
            for (Purpose spPurpose : spPurposes) {
                boolean isPurposeAssociated = false;
                for (ConsentPurpose consentPurpose : consentPurposes) {
                    if (consentPurpose.getPurposeId() == spPurpose.getId()) {
                        ApplicationPurpose appPurpose = buildApplicationPurpose(consentPurpose, spPurpose);
                        isPurposeAssociated = true;
                        appPurposes.add(appPurpose);
                        break;
                    }
                }
                if (!isPurposeAssociated) {
                    ApplicationPurpose appPurpose = new ApplicationPurpose();
                    appPurpose.setId(spPurpose.getId());
                    appPurpose.setName(spPurpose.getName());
                    appPurpose.setDescription(spPurpose.getDescription());
                    appPurpose.setDisplayOrder(DEFAULT_DISPLAY_ORDER);
                    appPurpose.setSelected(false);
                    appPurposes.add(appPurpose);
                }
            }
        }

        // Add shared purposes associated with the service provider.
        Purpose[] sharedPurposes;
        sharedPurposes = consentServiceClient.listPurposes(PURPOSE_GROUP_SHARED, PURPOSE_GROUP_TYPE_SYSTEM);
        if (nonNull(sharedPurposes)) {
            for (ConsentPurpose consentPurpose : consentPurposes) {
                for (Purpose sharedPurpose : sharedPurposes) {
                    if (consentPurpose.getPurposeId() == sharedPurpose.getId()) {
                        ApplicationPurpose appPurpose = buildApplicationPurpose(consentPurpose, sharedPurpose);
                        appSharedPurposes.add(appPurpose);
                        break;
                    }
                }
            }
        }

        ApplicationPurposes applicationPurposes = new ApplicationPurposes();
        applicationPurposes.setAppPurposes(appPurposes);
        applicationPurposes.setAppSharedPurposes(appSharedPurposes);

        return applicationPurposes;
    }

    public static Purpose[] getSharedPurposes() throws ConsentManagementException {

        ConsentManagementServiceClient consentServiceClient = new ConsentManagementServiceClient();
        return consentServiceClient.listPurposes(PURPOSE_GROUP_SHARED, PURPOSE_GROUP_TYPE_SYSTEM);
    }

    private static ApplicationPurpose buildApplicationPurpose(ConsentPurpose consentPurpose, Purpose sharedPurpose) {

        ApplicationPurpose appPurpose = new ApplicationPurpose();
        appPurpose.setId(sharedPurpose.getId());
        appPurpose.setName(sharedPurpose.getName());
        appPurpose.setDescription(sharedPurpose.getDescription());
        appPurpose.setDisplayOrder(consentPurpose.getDisplayOrder());
        appPurpose.setSelected(true);
        return appPurpose;
    }

    private static boolean isConsentPurposesAvailable(ConsentConfig consentConfig) {

        return consentConfig != null && consentConfig.getConsentPurposeConfigs() != null
            && consentConfig.getConsentPurposeConfigs().getConsentPurpose() != null;
    }
    */
}
