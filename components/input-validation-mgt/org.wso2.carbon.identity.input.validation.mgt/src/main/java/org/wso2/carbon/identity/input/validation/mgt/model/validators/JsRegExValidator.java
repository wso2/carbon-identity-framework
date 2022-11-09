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

import static org.wso2.carbon.identity.input.validation.mgt.utils.Constants.Configs.JS_REGEX;

public class JsRegExValidator extends AbstractRegExValidator {

    @Override
    public boolean canHandle(String validatorName) {

        return false;
    }

    @Override
    public boolean validate(ValidationContext context) throws InputValidationMgtClientException {

        // This is a FE validator.
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
