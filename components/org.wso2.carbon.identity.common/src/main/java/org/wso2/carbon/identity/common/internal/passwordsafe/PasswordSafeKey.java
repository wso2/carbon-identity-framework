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

package org.wso2.carbon.identity.common.internal.passwordsafe;

import org.wso2.carbon.identity.common.base.cache.CacheKey;

/**
 * Password safe cache key.
 */
public class PasswordSafeKey extends CacheKey {

    private static final long serialVersionUID = -974407425837498934L;
    private String uniqueID = null;

    public PasswordSafeKey(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PasswordSafeKey)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        PasswordSafeKey that = (PasswordSafeKey) o;

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
