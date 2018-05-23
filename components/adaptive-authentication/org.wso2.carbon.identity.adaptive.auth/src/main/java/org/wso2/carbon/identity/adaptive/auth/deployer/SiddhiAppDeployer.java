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

package org.wso2.carbon.identity.adaptive.auth.deployer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.adaptive.auth.Utils;
import org.wso2.carbon.identity.adaptive.auth.internal.AdaptiveDataHolder;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Siddhi app deployer.
 */
public class SiddhiAppDeployer {

    private static final String SIDDHI_FILE_SUFFIX = ".siddhi";
    private Path rootPath;
    private boolean isFileWatcherRunning;
    WatchService watcher;

    // Map to have a correlation between siddhi app name and file name
    private Map<String, String> fileNameToAppName = new ConcurrentHashMap<>();

    private static final Log log = LogFactory.getLog(SiddhiAppDeployer.class);

    public SiddhiAppDeployer(Path rootPath) {

        this.rootPath = rootPath;
    }

    /**
     * Start siddhi app deployer.
     */
    public void start() {

        loadSiddhiApps();
        startWatching();
    }

    private void loadSiddhiApps() {

        try {
            if (Files.exists(rootPath)) {
                Files.list(rootPath)
                        .filter(Files::isRegularFile)
                        .filter(x -> x.getFileName().toString().endsWith(SIDDHI_FILE_SUFFIX))
                        .forEach(this::deploySiddhiApp);
            }
        } catch (IOException e) {
            log.error("Error while deploying siddhi apps in path: " + rootPath.toAbsolutePath(), e);
        }
    }

    /**
     * Stop siddhi app deployer.
     */
    public void stop() {

        stopWatching();
    }

    private void startWatching() {

        if (!Files.exists(rootPath)) {
            return;
        }

        Thread serviceWatcherThread = new Thread(() -> {
            WatchService watcher;
            try {
                watcher = FileSystems.getDefault().newWatchService();
                rootPath.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            } catch (IOException e) {
                log.error("Error registering watcher for path: " + rootPath.toAbsolutePath());
                return;
            }
            isFileWatcherRunning = true;
            while (isFileWatcherRunning) {
                // Wait for key to be signaled
                WatchKey fileWatcherKey;
                try {
                    fileWatcherKey = watcher.take();
                } catch (InterruptedException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Watching for siddhi apps deployment folder interrupted.", e);
                    }
                    return;
                }
                try {
                    for (WatchEvent<?> event : fileWatcherKey.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == ENTRY_CREATE) {
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path appPath = getResolvedPathRelativeToRoot(ev.context());
                            if (appPath.getFileName().toString().endsWith(SIDDHI_FILE_SUFFIX)) {
                                deploySiddhiApp(appPath);
                            }
                        } else if (kind == ENTRY_DELETE) {
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path appPath = getResolvedPathRelativeToRoot(ev.context());
                            if (appPath.getFileName().toString().endsWith(SIDDHI_FILE_SUFFIX)) {
                                undeploySiddhiApp(appPath);
                            }
                        } else if (kind == ENTRY_MODIFY) {
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path appPath = getResolvedPathRelativeToRoot(ev.context());
                            if (appPath.getFileName().toString().endsWith(SIDDHI_FILE_SUFFIX)) {
                                updateSiddhiApp(appPath);
                            }
                        }
                    }
                    //Reset the key -- this step is critical if you want to receive
                    //further watch events. If the key is no longer valid, the directory
                    //is inaccessible so exit the loop.
                    boolean valid = fileWatcherKey.reset();
                    if (!valid) {
                        break;
                    }
                } catch (Exception ex) {
                    log.error("Error while watching deployment folder for siddhiApps.", ex);
                }
            }
        });

        serviceWatcherThread.start();
    }

    private void stopWatching() {

        isFileWatcherRunning = false;
        try {
            watcher.close();
        } catch (IOException e) {
            log.error("Error while stop watching deployment folder for siddhiApps in path:"
                    + rootPath.toAbsolutePath(), e);
        }
    }

    private void updateSiddhiApp(Path siddhiAppPath) {

        undeploySiddhiApp(siddhiAppPath);
        // TODO : fallback mechanism if newly deployment fails...????
        deploySiddhiApp(siddhiAppPath);
    }

    private void undeploySiddhiApp(Path siddhiAppPath) {

        String siddhiAppName = fileNameToAppName.get(siddhiAppPath.getFileName().toString());
        if (siddhiAppName != null) {
            boolean result = AdaptiveDataHolder.getInstance().getSiddhiEngine().undeployApp(siddhiAppName);
            if (result) {
                log.info("Siddhi App : " + siddhiAppName + " undeployed successfully.");
            } else {
                log.error("Siddhi App : " + siddhiAppName + " could not be undeployed successfully.");
            }
        }
    }

    private void deploySiddhiApp(Path siddhiAppPath) {

        String siddhiApp;
        try {
            siddhiApp = getSiddhiApp(siddhiAppPath);
            boolean result = AdaptiveDataHolder.getInstance().getSiddhiEngine().deployApp(siddhiApp);
            if (result) {
                String siddhiAppName = Utils.getSiddhiAppName(siddhiApp);
                fileNameToAppName.put(siddhiAppPath.getFileName().toString(), siddhiAppName);
                log.info("Siddhi App : " + siddhiAppName + " deployed Successfully.");
                if (log.isDebugEnabled()) {
                    log.debug("Siddhi App deployed Successfully. \n" + siddhiApp);
                }
            } else {
                log.error("Error while deploying siddhiApp from path: " + siddhiAppPath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Error while deploying siddhiApp from path: " + siddhiAppPath.toAbsolutePath(), e);
        }

    }

    private String getSiddhiApp(Path path) throws IOException {

        return new String(Files.readAllBytes(path.toAbsolutePath()));
    }

    private Path getResolvedPathRelativeToRoot(Path path) {

        return rootPath.resolve(path);
    }
}
