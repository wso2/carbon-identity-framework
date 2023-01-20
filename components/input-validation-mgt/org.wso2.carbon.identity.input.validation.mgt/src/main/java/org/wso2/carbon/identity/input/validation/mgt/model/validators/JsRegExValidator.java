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
import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtClientException;
import org.wso2.carbon.identity.input.validation.mgt.model.Property;
import org.wso2.carbon.identity.input.validation.mgt.model.ValidationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.JS_REGEX;
import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.ErrorMessages.ERROR_CODE_REGEX_MISMATCH;

/**
 * JavaScript regex validator.
 */
public class JsRegExValidator extends AbstractRegExValidator {

    @Override
    public boolean validate(ValidationContext context) throws InputValidationMgtClientException {

        // Validate against the Java regex in the BE.
        String value = context.getValue();
        boolean valid = false;
        Map<String, String> attributesMap = context.getProperties();
        String javaRegex = StringUtils.EMPTY;

        if (attributesMap.containsKey(JS_REGEX)) {
            String jsRegex = attributesMap.get(JS_REGEX);
            // Convert to Java regex.
            javaRegex = jsRegex.replaceAll("//", "/");

            Pattern pattern = Pattern.compile(javaRegex);
            Matcher matcher = pattern.matcher(value);
            valid = matcher.matches();
        }
        if (!valid) {
            throw new InputValidationMgtClientException(ERROR_CODE_REGEX_MISMATCH.getCode(),
                    ERROR_CODE_REGEX_MISMATCH.getMessage(),
                    String.format(ERROR_CODE_REGEX_MISMATCH.getDescription(), context.getField(), javaRegex));
        }
        return true;
    }

    @Override
    public List<Property> getConfigurationProperties() {

        List<Property> configProperties = new ArrayList<>();
        int parameterCount = 0;

        Property regEx = new Property();
        regEx.setName(JS_REGEX);
        regEx.setDisplayName("JavaScript RegEx pattern");
        regEx.setDescription("The javaScript regEx pattern.");
        regEx.setType("string");
        regEx.setDisplayOrder(parameterCount);
        configProperties.add(regEx);

        return configProperties;
    }
}
