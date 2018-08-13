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

package org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AsyncCaller;
import org.wso2.carbon.identity.application.authentication.framework.AsyncReturn;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.LongWaitStatus;
import org.wso2.carbon.identity.application.authentication.framework.store.LongWaitStatusStoreService;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncSequenceExecutor {

    private static final Log log = LogFactory.getLog(AsyncSequenceExecutor.class);

    private ExecutorService executorService;

    public void init() {

        String poolSizeString = IdentityUtil.getProperty("AdaptiveAuth.AsyncSequenceExecutorPoolSize");
        int poolSize;
        if (poolSizeString != null) {
            poolSize = Integer.parseInt(poolSizeString);
        } else {
            poolSize = 5;
        }

        executorService = Executors.newFixedThreadPool(poolSize);
    }

    public void exec(AsyncCaller caller, AsyncReturn returnFunction, AuthenticationContext authenticationContext) throws FrameworkException {

        if (returnFunction == null) {
            throw new FrameworkException("Can not execute the async process, as no callback function registered on " +
                    "returnFunction.");
        }

        AsyncReturn wrappedReturn = (ctx, m, r) -> {
            this.execReturn(returnFunction, ctx, m, r);
        };

        executorService.submit(
                new AsyncCallerTask(
                        new ObservingAsyncProcess(caller, wrappedReturn, authenticationContext)));
    }

    private void execReturn(AsyncReturn returnFunction,
                            AuthenticationContext authenticationContext, Map<String, Object> data, String result) {

        executorService.execute(new AsyncReturnWorker(returnFunction, authenticationContext, data, result));
    }

    private class AsyncCallerTask implements Runnable {

        private ObservingAsyncProcess asyncProcess;

        public AsyncCallerTask(ObservingAsyncProcess asyncProcess) {

            this.asyncProcess = asyncProcess;
        }

        @Override
        public void run() {

            try {
                asyncProcess.call();
            } catch (FrameworkException e) {
                log.error("Error while calling async process. ", e);
            }
        }
    }

    private class AsyncReturnWorker implements Runnable {

        private AsyncReturn returnFunction;
        private AuthenticationContext authenticationContext;
        private Map<String, Object> data;
        private String result;

        public AsyncReturnWorker(AsyncReturn returnFunction, AuthenticationContext authenticationContext, Map<String,
                Object> data, String result) {

            this.returnFunction = returnFunction;
            this.authenticationContext = authenticationContext;
            this.data = data;
            this.result = result;
        }

        @Override
        public void run() {
            LongWaitStatusStoreService longWaitStatusStoreService =
                    FrameworkServiceDataHolder.getInstance().getLongWaitStatusStoreService();
            try {
                LongWaitStatus longWaitStatus = longWaitStatusStoreService.getWait(authenticationContext
                        .getContextIdentifier());
                if (longWaitStatus == null) {
                    log.error("Unknown wait key: " + authenticationContext
                            .getContextIdentifier() + " found while trying to continue from long wait. ");
                    return;
                }
                longWaitStatus.setStatus(LongWaitStatus.Status.COMPLETED);
                returnFunction.accept(authenticationContext, data, result);
            } catch (FrameworkException e) {
                log.error("Error while resuming from the wait. ", e);
            }
        }
    }

    private class ObservingAsyncProcess {

        private AsyncCaller caller;
        private AsyncReturn returnFunction;
        private AuthenticationContext authenticationContext;

        public ObservingAsyncProcess(AsyncCaller caller, AsyncReturn returnFunction, AuthenticationContext authenticationContext) {

            this.caller = caller;
            this.returnFunction = returnFunction;
            this.authenticationContext = authenticationContext;
        }

        private void call() throws FrameworkException {

            caller.accept(authenticationContext, returnFunction);
        }
    }
}
