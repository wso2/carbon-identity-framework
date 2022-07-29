/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org).
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

package org.wso2.carbon.identity.core.migrate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.ServerStartupObserver;

public class MigrationClientStartupObserver implements ServerStartupObserver {

    private static final Log log = LogFactory.getLog(MigrationClientStartupObserver.class);
    private final MigrationClient migrationClient;

    public MigrationClientStartupObserver(MigrationClient migrationClient) {

        this.migrationClient = migrationClient;

    }

    @Override
    public void completingServerStartup() {
        // Do nothing.
    }

    @Override
    public void completedServerStartup() {

        try {
            log.info("Executing after startup steps of the migration client : "
                    + migrationClient.getClass().getName());
            migrationClient.executeAfterStartup();
            log.info("Executed after startup steps of the migration client : "
                    + migrationClient.getClass().getName());
        } catch (MigrationClientException e) {
            log.error("Error while performing migration after startup.", e);
        }
    }
}
