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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.JAVA_REGEX;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_JAVA_REGEX_INVALID;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_VALIDATION_REGEX_MISMATCH;

/**
 * Java Regex Validator.
 */
public class JavaRegExValidator extends AbstractRegExValidator {

    @Override
    public boolean validateProps(ValidationContext context) throws InputValidationMgtClientException {

        Map<String, String> properties = context.getProperties();
        if (properties.get(JAVA_REGEX) != null) {
            try {
                Pattern.compile(properties.get(JAVA_REGEX));
            } catch (PatternSyntaxException exception) {
                throw new InputValidationMgtClientException(ERROR_JAVA_REGEX_INVALID.getCode(),
                        String.format(ERROR_JAVA_REGEX_INVALID.getDescription(), properties.get(JAVA_REGEX)));
            }
        }
        return true;
    }

    @Override
    public boolean validate(ValidationContext context) throws InputValidationMgtClientException {

        String field = context.getField();
        String value = context.getValue();
        Map<String, String> attributesMap = context.getProperties();

        if (attributesMap.containsKey(field + "." + JAVA_REGEX)) {
            String javaRegEx = attributesMap.get(field + "." + JAVA_REGEX);
            Pattern pattern = Pattern.compile(javaRegEx);
            Matcher m = pattern.matcher(value);
            if (!m.matches()) {
                throw new InputValidationMgtClientException(ERROR_VALIDATION_REGEX_MISMATCH.getCode(),
                        ERROR_VALIDATION_REGEX_MISMATCH.getMessage(),
                        String.format(ERROR_VALIDATION_REGEX_MISMATCH.getDescription(), field,
                                value, javaRegEx));
            }
        }
        return true;
    }

    @Override
    public List<Property> getConfigurationProperties() {

        List<Property> configProperties = new ArrayList<>();
        int parameterCount = 0;

        Property regEx = new Property();
        regEx.setName(JAVA_REGEX);
        regEx.setDisplayName("Java RegEx pattern");
        regEx.setDescription("The java regEx pattern.");
        regEx.setType("string");
        regEx.setDisplayOrder(parameterCount);
        configProperties.add(regEx);

        return configProperties;
    }
}
