/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.common.cache;

/**
 * Random password container cache key.
 */
public class RandomPasswordContainerCacheKey extends CacheKey {

    private static final long serialVersionUID = 8961005517638265366L;
    private String uniqueID;

    public RandomPasswordContainerCacheKey(String uniqueID) {

        this.uniqueID = uniqueID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RandomPasswordContainerCacheKey)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        RandomPasswordContainerCacheKey that = (RandomPasswordContainerCacheKey) o;

        if (!uniqueID.equals(that.uniqueID)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + uniqueID.hashCode();
        return result;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

}
