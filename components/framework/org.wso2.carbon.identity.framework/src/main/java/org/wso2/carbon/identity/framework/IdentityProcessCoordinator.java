/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.framework;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.framework.internal.DataHolder;
import org.wso2.carbon.identity.framework.message.Request;
import org.wso2.carbon.identity.framework.message.Response;

public class IdentityProcessCoordinator {

    private static final Logger log = LoggerFactory.getLogger(IdentityProcessCoordinator.class);
    private DataHolder dataHolder = DataHolder.getInstance();

    public Response process(Request identityRequest) throws FrameworkException {
        IdentityProcessor processor = getIdentityProcessor(identityRequest);
        if (processor != null) {
            if (log.isDebugEnabled()) {
                log.debug("Identity Request is being processed by : " + processor.getName());
            }
            return processor.process(identityRequest);
        } else {
            throw FrameworkRuntimeException.error("No IdentityProcessor found to process the request");
        }
    }

    private IdentityProcessor getIdentityProcessor(Request identityRequest) {

        return dataHolder.getIdentityProcessors()
                .stream()
                .filter(x -> x.canHandle(identityRequest))
                .findFirst()
                .orElse(null);
    }

}
