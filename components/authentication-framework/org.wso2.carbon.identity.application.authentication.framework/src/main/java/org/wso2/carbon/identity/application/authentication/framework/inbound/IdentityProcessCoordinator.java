/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.inbound;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;

import java.util.List;

/**
 * Identity process coordinator.
 */
public class IdentityProcessCoordinator {

    private static final Log log = LogFactory.getLog(IdentityProcessCoordinator.class);

    public IdentityResponse process(IdentityRequest identityRequest) throws FrameworkException {

        IdentityProcessor processor = getIdentityProcessor(identityRequest);
        if (processor != null) {
            if (log.isDebugEnabled()) {
                log.debug("Starting to process IdentityProcessor : " + processor.getName());
            }
            return processor.process(identityRequest).build();
        } else {
            throw new FrameworkResourceNotFoundException("No IdentityProcessor found to process the request");
        }
    }

    private IdentityProcessor getIdentityProcessor(IdentityRequest identityRequest) {
        List<IdentityProcessor> processors = FrameworkServiceDataHolder.getInstance().getIdentityProcessors();
        for (IdentityProcessor requestProcessor : processors) {
            try {
                if (requestProcessor.canHandle(identityRequest)) {
                    return requestProcessor;
                }
            } catch (Exception e) {
                log.error("Error occurred while checking if " + requestProcessor.getName() + " can handle " +
                          identityRequest.toString());
            }
        }
        return null;
    }

}
