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
import org.wso2.carbon.identity.framework.exception.FrameworkException;
import org.wso2.carbon.identity.framework.exception.FrameworkRuntimeException;
import org.wso2.carbon.identity.framework.internal.DataHolder;
import org.wso2.carbon.identity.framework.request.IdentityRequest;
import org.wso2.carbon.identity.framework.response.IdentityResponse;

import java.util.Optional;

public class IdentityProcessCoordinator {

    private static final Logger log = LoggerFactory.getLogger(IdentityProcessCoordinator.class);
    private DataHolder dataHolder = DataHolder.getInstance();

    public IdentityResponse process(IdentityRequest identityRequest) throws FrameworkException {
        IdentityProcessor processor = getIdentityProcessor(identityRequest);
        if (processor != null) {
            if (log.isDebugEnabled()) {
                log.debug("Identity Request is being processed by : " + processor.getName());
            }
            return processor.process(identityRequest).build();
        } else {
            throw FrameworkRuntimeException.error("No IdentityProcessor found to process the request");
        }
    }

    private IdentityProcessor getIdentityProcessor(IdentityRequest identityRequest) {

        Optional<IdentityProcessor> identityProcessor = dataHolder.getIdentityProcessors().stream()
                .filter(x -> x.canHandle(identityRequest))
                .findFirst();

        return identityProcessor.orElse(null);

    }

}
