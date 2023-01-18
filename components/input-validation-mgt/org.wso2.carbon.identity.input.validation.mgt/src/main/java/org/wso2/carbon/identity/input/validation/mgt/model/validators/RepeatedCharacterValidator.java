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
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.MAX_CONSECUTIVE_CHR;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_VALIDATION_REPETITIVE_CHR_MISMATCH;

/**
 * Repeated character validator.
 */
public class RepeatedCharacterValidator extends AbstractRulesValidator {

    @Override
    public boolean validate(ValidationContext context) throws InputValidationMgtClientException {

        int count = 1;
        int consecutiveLen = 1;
        String value = context.getValue();
        String field = context.getField();
        Map<String, String> attributesMap = context.getProperties();

        for (int i = 1; i < value.length(); i++) {
            if (value.charAt(i) == value.charAt(i - 1)) {
                count++;
                if (count > consecutiveLen) {
                    consecutiveLen = count;
                }
            } else {
                count = 1;
            }
        }

        if (attributesMap.containsKey(MAX_CONSECUTIVE_CHR)) {
            int maxConsecutiveLength = Integer.parseInt(attributesMap.get(MAX_CONSECUTIVE_CHR));
            if (maxConsecutiveLength > 0 && maxConsecutiveLength < consecutiveLen) {
                throw new InputValidationMgtClientException(ERROR_VALIDATION_REPETITIVE_CHR_MISMATCH.getCode(),
                        ERROR_VALIDATION_REPETITIVE_CHR_MISMATCH.getMessage(),
                        String.format(ERROR_VALIDATION_REPETITIVE_CHR_MISMATCH.getDescription(), field,
                                maxConsecutiveLength));
            }
        }
        return true;
    }

    @Override
    public boolean validateProps(ValidationContext context) throws InputValidationMgtClientException {

        Map<String, String> properties = context.getProperties();
        validatePropertyName(properties, this.getClass().getSimpleName(), context.getTenantDomain());
        if (properties.get(MAX_CONSECUTIVE_CHR) != null && !validatePositiveNumber(properties.get(MAX_CONSECUTIVE_CHR),
                MAX_CONSECUTIVE_CHR, context.getTenantDomain())) {
            properties.remove(MAX_CONSECUTIVE_CHR);
        }
        return true;
    }

    @Override
    public List<Property> getConfigurationProperties() {

        List<Property> configProperties = new ArrayList<>();
        int parameterCount = 0;

        Property max = new Property();
        max.setName(MAX_CONSECUTIVE_CHR);
        max.setDisplayName("Maximum Consecutive Character");
        max.setDescription("The maximum consecutive characters of the field value.");
        max.setType("int");
        max.setDisplayOrder(++parameterCount);
        configProperties.add(max);

        return configProperties;
    }
}
