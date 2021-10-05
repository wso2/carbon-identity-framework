/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.mgt.validator;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementValidationException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationMgtListenerServiceComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Manager which loop through the registered validators and validate before adding or updating an application.
 */
public class ApplicationValidatorManager {

    /**
     * Loop through the registered validators and validate the Application configuration.
     *
     * @param serviceProvider   Service provider.
     * @param tenantDomain      Tenant domain name corresponding to the tenant.
     * @param username          The user who was invoking the application action.
     *
     * @throws IdentityApplicationManagementException IdentityApplicationManagementException
     */
    public void validateSPConfigurations(ServiceProvider serviceProvider, String tenantDomain,
                                         String username) throws IdentityApplicationManagementException {

        List<String> validationErrors = new ArrayList<>();

        Collection<ApplicationValidator> validators =
                ApplicationMgtListenerServiceComponent.getApplicationValidators();
        for (ApplicationValidator validator : validators) {
            validationErrors.addAll(validator.validateApplication(serviceProvider, tenantDomain, username));
        }

        if (!validationErrors.isEmpty()) {
            String code = IdentityApplicationConstants.Error.INVALID_REQUEST.getCode();
            throw new IdentityApplicationManagementValidationException(code, validationErrors.toArray(new String[0]));
        }
    }
}
