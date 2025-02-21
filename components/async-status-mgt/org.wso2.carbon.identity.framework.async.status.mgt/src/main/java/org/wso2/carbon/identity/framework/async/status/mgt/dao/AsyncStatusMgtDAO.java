/*
 * Copyright (c) 2023-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.framework.async.status.mgt.dao;

/**
 * DAO interface for Asynchronous Operation
 * Status Management.
 */
public interface AsyncStatusMgtDAO {

    /**
     *
     *
     * @param operationType Type of the share.
     * @param residentResourceId ID of the resource which is shared.
     * @param resourceType Type of the resource.
     * @param sharingPolicy  The sharing policy related with the share.
     * @param residentOrgId  The organization ID of the resource which is shared.
     * @param initiatorId  The ID of the user who is initiating the operation.
     * @param operationStatus  The status of the asynchronous operation.
     */
    void createB2BResourceSharingOperation(String operationType, String residentResourceId, String resourceType, String sharingPolicy, String residentOrgId, String initiatorId, String operationStatus);

    void get
}
