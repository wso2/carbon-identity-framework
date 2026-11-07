/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.flow.execution.engine.dao;

import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.poll.FlowPollingEntry;

/**
 * DAO for flow polling entries. They are kept in the flow context store with their own type, and these methods only
 * reach entries of that type.
 */
public interface FlowPollingDAO {

    /**
     * Add a polling entry, which expires at the entry's expiry time.
     *
     * @param entryId  Id of the entry.
     * @param tenantId Tenant id.
     * @param entry    Polling entry.
     * @throws FlowEngineException If the entry cannot be added.
     */
    void addPollingEntry(String entryId, int tenantId, FlowPollingEntry entry) throws FlowEngineException;

    /**
     * Get an unexpired polling entry.
     *
     * @param entryId  Id of the entry.
     * @param tenantId Tenant id.
     * @return Polling entry, or null if there is none.
     * @throws FlowEngineException If the entry cannot be read.
     */
    FlowPollingEntry getPollingEntry(String entryId, int tenantId) throws FlowEngineException;

    /**
     * Update an existing polling entry. A deleted entry is not recreated.
     *
     * @param entryId  Id of the entry.
     * @param tenantId Tenant id.
     * @param entry    Polling entry.
     * @return True if the entry was updated, false if it does not exist.
     * @throws FlowEngineException If the entry cannot be updated.
     */
    boolean updatePollingEntry(String entryId, int tenantId, FlowPollingEntry entry) throws FlowEngineException;

    /**
     * Delete a polling entry.
     *
     * @param entryId  Id of the entry.
     * @param tenantId Tenant id.
     * @throws FlowEngineException If the entry cannot be deleted.
     */
    void deletePollingEntry(String entryId, int tenantId) throws FlowEngineException;
}
