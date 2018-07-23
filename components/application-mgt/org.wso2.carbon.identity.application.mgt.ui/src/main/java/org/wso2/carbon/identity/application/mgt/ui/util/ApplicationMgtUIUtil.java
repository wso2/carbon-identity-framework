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

import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.identity.application.common.model.xsd.ConsentConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ConsentPurpose;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ui.ApplicationBean;
import org.wso2.carbon.identity.application.mgt.ui.ApplicationPurpose;
import org.wso2.carbon.identity.application.mgt.ui.ApplicationPurposes;
import org.wso2.carbon.identity.application.mgt.ui.client.ConsentManagementServiceClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpSession;

import static org.wso2.carbon.identity.application.mgt.ui.util.ApplicationMgtUIConstants.DEFAULT_DISPLAY_ORDER;
import static org.wso2.carbon.identity.application.mgt.ui.util.ApplicationMgtUIConstants.PURPOSE_GROUP_GENERAL;
import static org.wso2.carbon.identity.application.mgt.ui.util.ApplicationMgtUIConstants.PURPOSE_GROUP_TYPE_SP;
import static org.wso2.carbon.identity.application.mgt.ui.util.ApplicationMgtUIConstants.PURPOSE_GROUP_TYPE_SYSTEM;

public class ApplicationMgtUIUtil {

    private static final String SP_UNIQUE_ID_MAP = "spUniqueIdMap";

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

    public static ApplicationPurposes getApplicationSpecificPurposes(ServiceProvider serviceProvider)
            throws ConsentManagementException {

        ConsentConfig consentConfig = serviceProvider.getConsentConfig();
        ConsentManagementServiceClient consentServiceClient = new ConsentManagementServiceClient();
        Purpose[] spPurposes;
        List<ApplicationPurpose> appPurposes = new ArrayList<>();
        List<ApplicationPurpose> appGeneralPurposes = new ArrayList<>();
        ConsentPurpose[] consentPurposes = new ConsentPurpose[0];
        if (isConsentPurposesAvailable(consentConfig)) {
            consentPurposes = consentConfig.getConsentPurposeConfigs().getConsentPurpose();
        }

        spPurposes = consentServiceClient.listPurposes(serviceProvider.getApplicationName(), PURPOSE_GROUP_TYPE_SP);
        if (spPurposes != null) {
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

        Purpose[] generalPurposes;
        generalPurposes = consentServiceClient.listPurposes(PURPOSE_GROUP_GENERAL, PURPOSE_GROUP_TYPE_SYSTEM);
        if (generalPurposes != null) {
            for (ConsentPurpose consentPurpose : consentPurposes) {
                for (Purpose generalPurpose : generalPurposes) {
                    if (consentPurpose.getPurposeId() == generalPurpose.getId()) {
                        ApplicationPurpose appPurpose = buildApplicationPurpose(consentPurpose, generalPurpose);
                        appGeneralPurposes.add(appPurpose);
                        break;
                    }
                }
            }
        }

        ApplicationPurposes applicationPurposes = new ApplicationPurposes();
        applicationPurposes.setAppPurposes(appPurposes);
        applicationPurposes.setAppGeneralPurposes(appGeneralPurposes);

        return applicationPurposes;
    }

    public static Purpose[] getGeneralPurposes() throws ConsentManagementException {

        ConsentManagementServiceClient consentServiceClient = new ConsentManagementServiceClient();
        return consentServiceClient.listPurposes(PURPOSE_GROUP_GENERAL, PURPOSE_GROUP_TYPE_SYSTEM);
    }

    private static ApplicationPurpose buildApplicationPurpose(ConsentPurpose consentPurpose, Purpose generalPurpose) {

        ApplicationPurpose appPurpose = new ApplicationPurpose();
        appPurpose.setId(generalPurpose.getId());
        appPurpose.setName(generalPurpose.getName());
        appPurpose.setDescription(generalPurpose.getDescription());
        appPurpose.setDisplayOrder(consentPurpose.getDisplayOrder());
        appPurpose.setSelected(true);
        return appPurpose;
    }

    private static boolean isConsentPurposesAvailable(ConsentConfig consentConfig) {

        return consentConfig != null && consentConfig.getConsentPurposeConfigs() != null
            && consentConfig.getConsentPurposeConfigs().getConsentPurpose() != null;
    }
}
