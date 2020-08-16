/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.user.functionality.mgt.model;

/**
 * A data model class to define the functionality lock status element.
 */
public class FunctionalityLockStatus {

    public static final FunctionalityLockStatus UNLOCKED_STATUS = new FunctionalityLockStatus(false, 0, null, null);

    private boolean lockStatus;
    private long unlockTime;
    private String lockReasonCode;
    private String lockReason;

    public FunctionalityLockStatus(boolean lockStatus, long unlockTime, String lockReasonCode, String lockReason) {

        this.lockStatus = lockStatus;
        this.unlockTime = unlockTime;
        this.lockReasonCode = lockReasonCode;
        this.lockReason = lockReason;
    }

    /**
     * Checks the status of the functionality. Whether the functionality is locked or unlocked.
     *
     * @return The status for the functionality.
     */
    public boolean getLockStatus() {

        return lockStatus;
    }

    /**
     * Set the locked/unlocked status for the functionality.
     *
     * @param lockStatus Status for the functionality.
     */
    public void setLockStatus(boolean lockStatus) {

        this.lockStatus = lockStatus;
    }

    /**
     * Get the unlock time for the functionality.
     *
     * @return The unlock time for the functionality.
     */
    public long getUnlockTime() {

        return unlockTime;
    }

    /**
     * Set the unlock time for the functionality.
     *
     * @param unlockTime Unlock time for the functionality.
     */
    public void setUnlockTime(long unlockTime) {

        this.unlockTime = unlockTime;
    }

    /**
     * Get the lock reason code for the functionality.
     *
     * @return The lock reason code for the functionality.
     */
    public String getLockReasonCode() {

        return lockReasonCode;
    }

    /**
     * Set the lock reason code for the functionality.
     *
     * @param lockReasonCode Lock reason code of the functionality.
     */
    public void setLockReasonCode(String lockReasonCode) {

        this.lockReasonCode = lockReasonCode;
    }

    /**
     * Get the lock reason for the functionality.
     *
     * @return The lock reason for the functionality.
     */
    public String getLockReason() {

        return lockReason;
    }

    /**
     * Set the lock reason for the functionality.
     *
     * @param lockReason Lock reason for the functionality.
     */
    public void setLockReason(String lockReason) {

        this.lockReason = lockReason;
    }
}
