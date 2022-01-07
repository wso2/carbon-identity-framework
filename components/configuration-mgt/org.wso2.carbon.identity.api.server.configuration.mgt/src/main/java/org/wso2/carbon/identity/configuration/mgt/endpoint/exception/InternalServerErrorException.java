/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.configuration.mgt.endpoint.exception;

import org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ErrorDTO;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class InternalServerErrorException extends WebApplicationException {

    public InternalServerErrorException(ErrorDTO errorDTO) {

        super(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorDTO)
                .header(ConfigurationConstants.HEADER_CONTENT_TYPE,
                        ConfigurationConstants.DEFAULT_RESPONSE_CONTENT_TYPE)
                .build());
    }
}
