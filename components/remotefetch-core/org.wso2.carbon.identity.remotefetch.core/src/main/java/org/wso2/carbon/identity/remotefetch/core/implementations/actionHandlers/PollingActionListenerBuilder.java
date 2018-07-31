/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.remotefetch.core.implementations.actionHandlers;

import org.wso2.carbon.identity.remotefetch.common.actionlistener.ActionListener;
import org.wso2.carbon.identity.remotefetch.common.actionlistener.ActionListenerBuilder;
import org.wso2.carbon.identity.remotefetch.common.actionlistener.ActionListenerBuilderException;

import java.io.File;
import java.util.Map;

public class PollingActionListenerBuilder extends ActionListenerBuilder {

    @Override
    public ActionListener build() throws ActionListenerBuilderException {

        Map<String, String> actionListenerAttributes = this.fetchConfig.getActionListenerAttributes();
        int frequency;

        File directory;
        if (actionListenerAttributes.containsKey("frequency")) {
            try {
                frequency = Integer.parseInt(actionListenerAttributes.get("frequency"));
            } catch (NumberFormatException e) {
                throw new ActionListenerBuilderException("Frequency not valid in configuration", e);
            }
        } else {
            throw new ActionListenerBuilderException("Frequency not available in configuration");
        }

        if (actionListenerAttributes.containsKey("directory")) {
            directory = new File(actionListenerAttributes.get("directory"));
        } else {
            throw new ActionListenerBuilderException("Directory not available in configuration");
        }

        return new PollingActionListener(this.repoConnector, directory,
                this.configDeployer, frequency, this.fetchConfig.getRemoteFetchConfigurationId());
    }
}
