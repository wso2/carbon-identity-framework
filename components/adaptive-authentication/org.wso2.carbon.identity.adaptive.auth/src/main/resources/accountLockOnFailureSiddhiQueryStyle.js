/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function onInitialRequest(context) {
    executeStep({
        id: '1',
        on: {
            success: function (context) {
                // DUMMY TEST
                var user = context.subject;
                var username = user.username;
                var isUserLocked = checkAccountLocked(username);
                if (isUserLocked) {
                    Log.info("---------------------- ACCOUNT LOCKED FOR USER: " + user.username + ' ---------------------------');
                    sendError({});
                }
            },

            fail: function (context) {
                // DUMMY TEST
                var appName = 'LockAccountOnFailureApp';
                var streamName = 'login_failure_stream';

                var user = context.lastAttemptedSubject;
                var username = context.lastAttemptedSubject.username;
                var sp = context.serviceProviderName;

                var payload = {'user': username, 'service_provider': sp};
                publishEvent(appName, streamName, payload);


                // Let's check whether user was locked
                var isUserLocked = checkAccountLocked(username);
                if (isUserLocked) {
                    Log.info("---------------------- ACCOUNT LOCKED FOR USER: " + user.username + ' ---------------------------');
                }
            }
        }
    });
}

function checkAccountLocked(username) {
    var x = querySiddhiRuntime("LockAccountOnFailureApp", "from AccountLockedTable on user == '" + username + "'");
    return x !== null && x.length > 0;
}
