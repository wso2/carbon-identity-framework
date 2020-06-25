/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.template.mgt.handler.impl;

import org.wso2.carbon.identity.template.mgt.TemplateMgtConstants;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementException;
import org.wso2.carbon.identity.template.mgt.model.Template;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.security.sts.service.util.STSServiceValidationUtil.isWSTrustAvailable;

public class WSTrustConnectorValidator {

    // WS-Trust related constants.
    private static final String WS_TRUST_TEMPLATE_ID = "061a3de4-8c08-4878-84a6-24245f11bf0e";
    private static final String STS_TEMPLATE_NOT_FOUND_MESSAGE = "Request template with id: %s could " +
            "not be found since the WS-Trust connector has not been configured.";

    /**
     * Validates WS-Trust availability in application templates related operations.
     *
     * @param templateId Template Id of the template
     * @throws TemplateManagementException If WS-Trust connector is not available.
     */
    public static void validateWSTrustTemplateAvailability(String templateId)
            throws TemplateManagementException {

        if (templateId.equals(WS_TRUST_TEMPLATE_ID) && !isWSTrustAvailable()) {

            throw new TemplateManagementException(
                    String.format(STS_TEMPLATE_NOT_FOUND_MESSAGE, templateId),
                    TemplateMgtConstants.ErrorMessages.ERROR_CODE_TEMPLATE_NOT_FOUND.getCode());
        }
    }

    /**
     * Check if the application templates list contains template for WS-Trust when
     * WS-Trust functionality is not available and if it exists then remove it.
     *
     * @param templateList Default template list.
     * @return Modified template list if WS-Trust functionality is not available.
     */
    public static List<Template> removeWSTrustTemplate(List<Template> templateList) {

        List<Template> removableTemplates = new ArrayList<>();

        for (Template templateListItem : templateList) {
            if (templateListItem.getTemplateId().equals(WS_TRUST_TEMPLATE_ID) &&
                    !isWSTrustAvailable()) {
                removableTemplates.add(templateListItem);
            }
        }

        templateList.removeAll(removableTemplates);

        return templateList;
    }
}
