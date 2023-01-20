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

import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtClientException;
import org.wso2.carbon.identity.input.validation.mgt.model.Property;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationContext;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MIN_UNIQUE_CHR;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.PERIOD;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_VALIDATION_UNIQUE_CHR_MISMATCH;

/**
 * Unique character validator.
 */
public class UniqueCharacterValidator extends AbstractRulesValidator {

    @Override
    public boolean validate(ValidationContext context) throws InputValidationMgtClientException {

        String value = context.getValue();
        String field = context.getField();
        Set<Character> distinctChars = new LinkedHashSet<>();
        Map<String, String> attributesMap = context.getProperties();

        for (int i = 0; i < value.length(); i++) {
            char chr = value.charAt(i);
            distinctChars.add(chr);
        }

        if (attributesMap.containsKey(field + PERIOD + MIN_UNIQUE_CHR)) {
            int minUniqueCharacters = Integer.parseInt(attributesMap.get(field + PERIOD + MIN_UNIQUE_CHR));
            if (minUniqueCharacters > distinctChars.size()) {
                throw new InputValidationMgtClientException(ERROR_VALIDATION_UNIQUE_CHR_MISMATCH.getCode(),
                        ERROR_VALIDATION_UNIQUE_CHR_MISMATCH.getMessage(),
                        String.format(ERROR_VALIDATION_UNIQUE_CHR_MISMATCH.getDescription(), field,
                                minUniqueCharacters));
            }
        }
        return true;
    }

    @Override
    public boolean validateProps(ValidationContext context) throws InputValidationMgtClientException {

        Map<String, String> properties = context.getProperties();
        validatePropertyName(properties, this.getClass().getSimpleName(), context.getTenantDomain());
        if (properties.containsKey(MIN_UNIQUE_CHR) && (properties.get(MIN_UNIQUE_CHR) != null &&
                !validatePositiveNumber(properties.get(MIN_UNIQUE_CHR), MIN_UNIQUE_CHR, context.getTenantDomain()))) {
            properties.remove(MIN_UNIQUE_CHR);
        }
        return true;
    }

    @Override
    public List<Property> getConfigurationProperties() {

        List<Property> configProperties = new ArrayList<>();
        int parameterCount = 0;

        Property max = new Property();
        max.setName(MIN_UNIQUE_CHR);
        max.setDisplayName("Minimum unique characters");
        max.setDescription("The minimum unique characters of the field value.");
        max.setType("int");
        max.setDisplayOrder(++parameterCount);
        configProperties.add(max);

        return configProperties;
    }
}
