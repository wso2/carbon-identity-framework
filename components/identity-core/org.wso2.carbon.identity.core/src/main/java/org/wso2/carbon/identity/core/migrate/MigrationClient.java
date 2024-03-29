/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.core.migrate;

/**
 * Migration client interface.
 *
 * When server is started with -Dmigrate option, Identity core bundle will wait for a implementation of this interface.
 */
public interface MigrationClient {

    /**
     * Execute migration procedure during the startup(before identity core is initialized).
     *
     * @throws MigrationClientException
     */
    void execute() throws MigrationClientException;

    /**
     * Execute migration procedure after the startup. Using this method makes sure that all the components
     * are activated before running the migration steps.
     *
     * @throws MigrationClientException Migration Client Exception.
     */
    default void executeAfterStartup() throws MigrationClientException {

    }
}
