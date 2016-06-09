/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.entitlement.endpoint.resources;

import org.wso2.carbon.identity.entitlement.endpoint.util.EntitlementEndpointConstants;

public class AbstractResource {
    public String identifyOutputFormat(String format) {
        if (format == null || ("*/*").equals(format) || ((format != null) && (format.startsWith(EntitlementEndpointConstants.APPLICATION_JSON)))) {
            return EntitlementEndpointConstants.APPLICATION_JSON;
        } else {
            return format;
        }
    }

    public String identifyInputFormat(String format) {
        if (format == null || ("*/*").equals(format) ||
                ((format != null) && (format.startsWith(EntitlementEndpointConstants.APPLICATION_JSON)))) {
            return EntitlementEndpointConstants.APPLICATION_JSON;
        } else {
            return format;
        }
    }
}
