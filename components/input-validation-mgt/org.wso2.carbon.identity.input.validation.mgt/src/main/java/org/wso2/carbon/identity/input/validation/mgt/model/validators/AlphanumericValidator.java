/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtClientException;
import org.wso2.carbon.identity.input.validation.mgt.model.Property;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.ALPHANUMERIC_REGEX_PATTERN_WITH_SPECIAL_CHARACTERS;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.DEFAULT_ALPHANUMERIC_REGEX_PATTERN;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.ENABLE_SPECIAL_CHARACTERS;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.ENABLE_VALIDATOR;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.USERNAME;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_INPUT_VALUE_NULL;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_INVALID_VALIDATOR_PROPERTY_VALUE;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_VALIDATION_ALPHANUMERIC_FORMAT_MISMATCH;

/**
 * Alphanumeric validator.
 */
public class AlphanumericValidator extends AbstractRulesValidator {

    private final List<String> allowedFields = new ArrayList<String>() {{
        add(USERNAME);
    }};

    @Override
    public boolean isAllowedField(String field) {

        return allowedFields.contains(field);
    }

    /**
     * Validate the string against the validation criteria.
     *
     * @param context   Validation Context.
     * @return boolean
     * @throws InputValidationMgtClientException Error when string does not satisfy the validation criteria
     */
    @Override
    public boolean validate(ValidationContext context) throws InputValidationMgtClientException {

        String value = context.getValue();
        String field = context.getField();
        Map<String, String> attributesMap = context.getProperties();
        String alphanumericRegEx = DEFAULT_ALPHANUMERIC_REGEX_PATTERN;
        // Check whether special characters are allowed.
        if (attributesMap.containsKey(ENABLE_SPECIAL_CHARACTERS)) {
            alphanumericRegEx = ALPHANUMERIC_REGEX_PATTERN_WITH_SPECIAL_CHARACTERS;
        }

        // Check whether value satisfies the alphanumeric criteria.
        if (attributesMap.containsKey(ENABLE_VALIDATOR)) {
            if (StringUtils.isBlank(value)) {
                throw new InputValidationMgtClientException(ERROR_INPUT_VALUE_NULL.getCode(),
                        ERROR_INPUT_VALUE_NULL.getMessage(), ERROR_INPUT_VALUE_NULL.getDescription());
            }
            if (Boolean.parseBoolean(attributesMap.get(ENABLE_VALIDATOR)) && !value.matches(alphanumericRegEx)) {
                throw new InputValidationMgtClientException(ERROR_VALIDATION_ALPHANUMERIC_FORMAT_MISMATCH.getCode(),
                    ERROR_VALIDATION_ALPHANUMERIC_FORMAT_MISMATCH.getMessage(), String.format(
                    ERROR_VALIDATION_ALPHANUMERIC_FORMAT_MISMATCH.getDescription(), field, alphanumericRegEx));
            }
        }

        return true;
    }

    /**
     * Get list of supported properties for the validator.
     *
     * @return  List<Property>
     */
    @Override
    public List<Property> getConfigurationProperties() {

        List<Property> configProperties = new ArrayList<>();
        int parameterCount = 0;

        Property enableValidator = new Property();
        enableValidator.setName(ENABLE_VALIDATOR);
        enableValidator.setDisplayName("Alphanumeric field value");
        enableValidator.setDescription("Validate whether the field value is alphanumeric.");
        enableValidator.setType("boolean");
        enableValidator.setDisplayOrder(++parameterCount);
        configProperties.add(enableValidator);

        Property allowSpecialChars = new Property();
        allowSpecialChars.setName(ENABLE_SPECIAL_CHARACTERS);
        allowSpecialChars.setDisplayName("Alphanumeric field value with special characters");
        allowSpecialChars.setDescription("Validate for allowed set of special characters along with alphanumeric" +
                " in the field value.");
        allowSpecialChars.setType("boolean");
        allowSpecialChars.setDisplayOrder(++parameterCount);
        configProperties.add(allowSpecialChars);

        return configProperties;
    }


    /**
     * Validate the configuration values of the properties for the validator.
     *
     * @param context   Validation Context.
     * @return  boolean
     */
    @Override
    public boolean validateProps(ValidationContext context) throws InputValidationMgtClientException {

        Map<String, String> properties = context.getProperties();
        validatePropertyName(properties, this.getClass().getSimpleName(), context.getTenantDomain());
        if (properties.get(ENABLE_VALIDATOR) != null && !validateBoolean(properties.get(ENABLE_VALIDATOR),
                ENABLE_VALIDATOR, context.getTenantDomain())) {
            throw new InputValidationMgtClientException(ERROR_INVALID_VALIDATOR_PROPERTY_VALUE.getCode(),
                    String.format(ERROR_INVALID_VALIDATOR_PROPERTY_VALUE.getDescription(),
                    properties.get(ENABLE_VALIDATOR), ENABLE_VALIDATOR));
        }
        return true;
    }
}
