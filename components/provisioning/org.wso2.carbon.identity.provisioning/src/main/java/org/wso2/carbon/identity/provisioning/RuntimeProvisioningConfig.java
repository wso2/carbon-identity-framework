/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.provisioning;

import java.io.Serializable;
import java.util.Map.Entry;

public class RuntimeProvisioningConfig implements Serializable {

    private static final long serialVersionUID = -2629523092537958531L;

    private boolean blocking;
    private Entry<String, AbstractOutboundProvisioningConnector> provisioningConnectorEntry;

    /**
     * @return
     */
    public boolean isBlocking() {
        return blocking;
    }

    /**
     * @param blocking
     */
    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    /**
     * @return
     */
    public Entry<String, AbstractOutboundProvisioningConnector> getProvisioningConnectorEntry() {
        return provisioningConnectorEntry;
    }

    /**
     * @param provisioningConnectorEntry
     */
    public void setProvisioningConnectorEntry(
            Entry<String, AbstractOutboundProvisioningConnector> provisioningConnectorEntry) {
        this.provisioningConnectorEntry = provisioningConnectorEntry;
    }

}
