/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.servlet.mgt;

import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationInInterceptor;
import org.apache.cxf.validation.BeanValidationProvider;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * Custom Bean Validation Interceptor.
 */
@SuppressWarnings("java:S110") // Disable "Inheritance tree of classes should not be too deep" rule
public class CustomBeanValidationInterceptor extends JAXRSBeanValidationInInterceptor {

    public CustomBeanValidationInterceptor() {

        super();
        this.setProvider(createBeanValidationProvider());
    }

    private BeanValidationProvider createBeanValidationProvider() {

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        return new BeanValidationProvider(validator);
    }
}
