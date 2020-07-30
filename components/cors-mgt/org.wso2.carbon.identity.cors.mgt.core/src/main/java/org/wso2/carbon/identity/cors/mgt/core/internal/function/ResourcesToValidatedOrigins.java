/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.cors.mgt.core.internal.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceClientException;
import org.wso2.carbon.identity.cors.mgt.core.model.ValidatedOrigin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_INVALID_STORED_ORIGIN;

/**
 * Converts a ConfigurationManagement Resources to a list of ValidatedOrigins.
 */
public class ResourcesToValidatedOrigins implements Function<Resources, List<ValidatedOrigin>> {

    private static final Log log = LogFactory.getLog(ResourcesToValidatedOrigins.class);

    @Override
    public List<ValidatedOrigin> apply(Resources resources) {

        ArrayList<ValidatedOrigin> validatedOrigins = new ArrayList<>();
        for (Resource resource : resources.getResources()) {
            try {
                ValidatedOrigin validatedOrigin = new ValidatedOrigin(resource.getResourceName());
                validatedOrigins.add(validatedOrigin);
            } catch (CORSManagementServiceClientException e) {
                /*
                  The program should never reach here as all the origins in the configuration management store are
                  properly validated.
                 */
                log.error(String.format(ERROR_CODE_INVALID_STORED_ORIGIN.getDescription(), resource.getResourceName()),
                        e);
            }
        }

        return validatedOrigins;
    }
}
