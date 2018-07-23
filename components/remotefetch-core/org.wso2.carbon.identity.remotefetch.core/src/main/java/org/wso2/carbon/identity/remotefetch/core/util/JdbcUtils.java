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

package org.wso2.carbon.identity.remotefetch.core.util;

import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.identity.remotefetch.core.internal.RemoteFetchServiceComponentHolder;

/**
 * Util class to aid Jdbc executions.
 */
public class JdbcUtils {

    private JdbcUtils() {

        throw new IllegalStateException("Utility class");
    }

    /**
     * Get a new Jdbc Template
     *
     * @return a new JdbcTemplate
     */
    public static JdbcTemplate getNewTemplate() {

        return new JdbcTemplate(RemoteFetchServiceComponentHolder.getInstance().getDataSource());
    }
}
