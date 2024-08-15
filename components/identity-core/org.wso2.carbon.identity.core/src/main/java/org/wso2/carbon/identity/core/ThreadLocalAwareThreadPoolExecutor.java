/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.identity.core;

import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Thread local aware thread pool executor. This will wrap overridden methods in an
 * MDC aware manner to propagate parent MDC thread local values to newly created thread.
 */
public class ThreadLocalAwareThreadPoolExecutor extends ThreadPoolExecutor {

    public ThreadLocalAwareThreadPoolExecutor(int corePoolSize,
                                              int maximumPoolSize,
                                              long keepAliveTime,
                                              TimeUnit unit,
                                              BlockingQueue<Runnable> workQueue) {

        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public ThreadLocalAwareThreadPoolExecutor(int corePoolSize,
                                              int maximumPoolSize,
                                              long keepAliveTime,
                                              TimeUnit unit,
                                              BlockingQueue<Runnable> workQueue,
                                              ThreadFactory threadFactory) {

        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public ThreadLocalAwareThreadPoolExecutor(int corePoolSize,
                                              int maximumPoolSize,
                                              long keepAliveTime,
                                              TimeUnit unit,
                                              BlockingQueue<Runnable> workQueue,
                                              RejectedExecutionHandler handler) {

        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public ThreadLocalAwareThreadPoolExecutor(int corePoolSize,
                                              int maximumPoolSize,
                                              long keepAliveTime,
                                              TimeUnit unit,
                                              BlockingQueue<Runnable> workQueue,
                                              ThreadFactory threadFactory, RejectedExecutionHandler handler) {

        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public void execute(Runnable command) {

        super.execute(wrapWithMdcContext(command));
    }

    private Runnable wrapWithMdcContext(Runnable command) {

        // Save the current MDC context
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return () -> {
            setMDCContext(contextMap);
            try {
                command.run();
            } finally {
                // Once the command is complete, clear MDC
                MDC.clear();
            }
        };
    }

    private void setMDCContext(Map<String, String> contextMap) {

        MDC.clear();
        if (contextMap != null) {
            MDC.setContextMap(contextMap);
        }
    }
}
