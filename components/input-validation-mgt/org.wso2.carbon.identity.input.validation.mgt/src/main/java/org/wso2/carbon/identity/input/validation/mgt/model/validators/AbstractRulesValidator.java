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

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationContext;
import org.wso2.carbon.identity.input.validation.mgt.model.Validator;
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtClientException;
import org.wso2.carbon.identity.input.validation.mgt.model.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MAX_LENGTH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MIN_LENGTH;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.*;

public abstract class AbstractRulesValidator implements Validator {

    private static final Log log = LogFactory.getLog(AbstractRulesValidator.class);

    @Override
    public boolean validate(ValidationContext context) throws InputValidationMgtClientException {

        return true;
    }

    @Override
    public boolean validateProps(ValidationContext context) throws InputValidationMgtClientException {

        Map<String, String> properties = context.getProperties();
        // Validated supported properties.
        checkValidProperty(properties, this.getClass().getSimpleName(), context.getTenantDomain());

        if (properties.get(MIN_LENGTH) != null && validatePositiveNumber(properties.get(MIN_LENGTH), MIN_LENGTH,
                context.getTenantDomain())) {
            properties.remove(MIN_LENGTH);
        }
        if (properties.get(MAX_LENGTH) != null && validatePositiveNumber(properties.get(MAX_LENGTH), MAX_LENGTH,
                context.getTenantDomain())) {
            properties.remove(MIN_LENGTH);
        }
        // Ensure maximum limit is not less than minimum limit.
        if ((properties.get(MAX_LENGTH) != null) && (properties.get(MIN_LENGTH) != null) &&
                Integer.parseInt(properties.get(MIN_LENGTH)) > Integer.parseInt(properties.get(MAX_LENGTH))) {
            throw new InputValidationMgtClientException(ERROR_DEFAULT_MIN_MAX_MISMATCH.getCode(),
                    String.format(ERROR_DEFAULT_MIN_MAX_MISMATCH.getDescription(), context.getField(),
                            properties.get(MIN_LENGTH), properties.get(MAX_LENGTH)));
        }
        return true;
    }


    @Override
    public List<Property> getConfigurationProperties() {

        List<Property> configProperties = new ArrayList<>();
        int parameterCount = 0;

        Property min = new Property();
        min.setName(MIN_LENGTH);
        min.setDisplayName("Minimum");
        min.setDescription("The minimum length of the field value.");
        min.setType("int");
        min.setDisplayOrder(++parameterCount);
        configProperties.add(min);

        Property max = new Property();
        max.setName(MAX_LENGTH);
        max.setDisplayName("Maximum");
        max.setDescription("The maximum length of the field value.");
        max.setType("int");
        max.setDisplayOrder(++parameterCount);
        configProperties.add(max);

        return configProperties;
    }

    protected boolean validatePositiveNumber(String value, String property, String tenantDomain) throws InputValidationMgtClientException {

        if (!NumberUtils.isNumber(value)) {
            if (log.isDebugEnabled()) {
                log.error(String.format(ERROR_PROPERTY_TYPE_MISMATCH.getDescription(), property, "integer",
                        tenantDomain));
            }
            throw new InputValidationMgtClientException(ERROR_PROPERTY_TYPE_MISMATCH.getCode(),
                    String.format(ERROR_PROPERTY_TYPE_MISMATCH.getDescription(), property, "integer", tenantDomain));
        }
        return Integer.parseInt(value) < 0;
    }

    protected void checkValidProperty(Map<String, String> properties, String validator, String tenantDomain)
            throws InputValidationMgtClientException {

        List<String> supportedProperties = getConfigurationProperties().stream()
                .map(Property::getName).collect(Collectors.toList());
        for (String key: properties.keySet()) {
            if (!supportedProperties.contains(key)) {
                throw new InputValidationMgtClientException(ERROR_PROPERTY_NOT_SUPPORTED.getCode(),
                        String.format(ERROR_PROPERTY_NOT_SUPPORTED.getDescription(), key, validator, tenantDomain));
            }
        }
    }
}
