/*
 *  Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.base;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used for creating checked exceptions that can be handled.
 */
public class IdentityException extends Exception {

    private static final long serialVersionUID = 725992116511551241L;

    protected IdentityException(String errorDescription) {
        super(errorDescription);
    }

    protected IdentityException(String errorDescription, Throwable cause) {
        super(errorDescription, cause);
    }

    // This method may be used for easily migrating existing usages of IdentityException creation.
    // However once we migrate all the usages of IdentityException to create using error(ErrorInfo) we can remove this
    @Deprecated
    public static IdentityException error(String errorDescription) {
        return new IdentityException(errorDescription);
    }

    // This method may be used for easily migrating existing usages of IdentityException creation.
    // However once we migrate all the usages of IdentityException to create using error(ErrorInfo) we can remove this
    @Deprecated
    public static IdentityException error(String errorDescription, Throwable cause) {
        return new IdentityException(errorDescription, cause);
    }
}
