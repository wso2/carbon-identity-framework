/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement;

import org.wso2.carbon.identity.entitlement.dto.StatusHolder;

import java.util.List;
import java.util.Properties;

/**
 * This listener would be fired after an admin action is done
 */
public interface PAPStatusDataHandler {

    /**
     * init entitlement status data handler module
     *
     * @param properties properties
     */
    public void init(Properties properties);

    /**
     * Handles
     *
     * @param about        indicates what is related with this admin status action
     * @param key          key value of the status
     * @param statusHolder <code>StatusHolder</code>
     * @throws EntitlementException throws, if fails to handle
     */
    public void handle(String about, String key, List<StatusHolder> statusHolder) throws EntitlementException;

    /**
     * @param about        indicates what is related with this admin status action
     * @param statusHolder <code>StatusHolder</code>
     * @throws EntitlementException if fails to handle
     */
    public void handle(String about, StatusHolder statusHolder) throws EntitlementException;

    /**
     * @param about        indicates what is related with this admin status action
     * @param key          key value of the status
     * @param type         admin action type
     * @param searchString search string for <code>StatusHolder</code>
     * @return An array of <code>StatusHolder</code>
     * @throws EntitlementException
     */
    public StatusHolder[] getStatusData(String about, String key, String type,
                                        String searchString) throws EntitlementException;
}
