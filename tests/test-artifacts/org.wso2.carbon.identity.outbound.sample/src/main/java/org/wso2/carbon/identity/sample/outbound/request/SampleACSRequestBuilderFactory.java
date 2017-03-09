/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.sample.outbound.request;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.gateway.api.exception.GatewayClientException;
import org.wso2.carbon.identity.gateway.api.exception.GatewayException;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequestBuilderFactory;
import org.wso2.carbon.identity.gateway.util.GatewayUtil;
import org.wso2.msf4j.Request;

public class SampleACSRequestBuilderFactory extends GatewayRequestBuilderFactory {

    @Override
    public boolean canHandle(Request request) throws GatewayException {
        super.canHandle(request);
        String assertion = GatewayUtil.getParameter(request, "Assertion");
        if (StringUtils.isNotBlank(assertion)) {
            return true;
        }
        return false;
    }

    @Override
    public int getPriority() {
        return 75;
    }


    public SampleACSRequest.SampleACSRequestBuilder create(Request request) throws GatewayClientException {
        super.create(request);
        SampleACSRequest.SampleACSRequestBuilder builder = new SampleACSRequest.SampleACSRequestBuilder();
        this.create(builder, request);
        return builder;
    }

    public void create(SampleACSRequest.SampleACSRequestBuilder builder, Request request) throws GatewayClientException {
        super.create(builder, request);
        builder.setRequestDataKey(GatewayUtil.getParameter(request, "RelayState"));
    }

}
