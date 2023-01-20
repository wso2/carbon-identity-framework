/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.input.validation.mgt.model.validators;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtClientException;
import org.wso2.carbon.identity.input.validation.mgt.model.Property;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationContext;
import org.wso2.carbon.identity.input.validation.mgt.model.Validator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_PROPERTY_NOT_SUPPORTED;

/**
 * Abstract regex validator.
 */
public abstract class AbstractRegExValidator implements Validator {

    private static final Log log = LogFactory.getLog(AbstractRegExValidator.class);

    @Override
    public boolean canHandle(String validatorName) {

        return StringUtils.equalsIgnoreCase(validatorName, this.getClass().getSimpleName());
    }

    @Override
    public boolean validateProps(ValidationContext context) throws InputValidationMgtClientException {

        Map<String, String> properties = context.getProperties();
        // Validated supported properties.
        return validatePropertyName(properties, this.getClass().getSimpleName(), context.getTenantDomain());
    }

    protected boolean validatePropertyName(Map<String, String> properties, String validator, String tenantDomain)
            throws InputValidationMgtClientException {

        List<String> supportedProperties = getConfigurationProperties().stream()
                .map(Property::getName).collect(Collectors.toList());
        for (String key: properties.keySet()) {
            if (!supportedProperties.contains(key)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("The property : %s is not supported for validator: %s in tenant: %s", key,
                            validator, tenantDomain));
                }
                throw new InputValidationMgtClientException(ERROR_PROPERTY_NOT_SUPPORTED.getCode(),
                        String.format(ERROR_PROPERTY_NOT_SUPPORTED.getDescription(), key, validator, tenantDomain));
            }
        }
        return true;
    }
}
