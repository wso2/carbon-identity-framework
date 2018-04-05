/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.loader;

import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.util.Map;

/**
 * Sequence Loader contract.
 */
public interface SequenceLoader {

    /**
     * Returns the sequence from the Loader/DAO/Repository, which matches to the context and parameter map
     * for the given Service provider.
     *
     * @param context the current Authentication Context.
     * @param parameterMap Name-Value pair with the incoming parameters.
     * @param serviceProvider
     * @return located SequenceConfig if matching one can be found or built. May return null.
     * @throws FrameworkException will be thrown upon any error accessing underlying repository.
     */
    SequenceConfig getSequenceConfig(AuthenticationContext context, Map<String, String[]> parameterMap,
                                     ServiceProvider serviceProvider) throws FrameworkException;

}
