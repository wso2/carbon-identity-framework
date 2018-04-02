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
import org.wso2.carbon.identity.adaptive.auth.EmbeddedSiddhiEngine;
import org.wso2.carbon.identity.adaptive.auth.Utils;

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
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class SiddhiAppDeployer {

    public static final String SIDDHI_FILE_SUFFIX = ".siddhi";
    private Path rootPath;
    private boolean isFileWatcherRunning;
    private Thread serviceWatcherThread;
    private WatchKey fileWatcherKey;

    // Map to have a correlation between siddhi app name and file name
    private Map<String, String> fileNameToAppName = new ConcurrentHashMap<>();

    private static final Log log = LogFactory.getLog(SiddhiAppDeployer.class);

    public SiddhiAppDeployer(Path rootPath) {

        this.rootPath = rootPath;
    }

    public void start() {

        loadSiddhiApps();
        startWatching();
    }

    private void loadSiddhiApps() {

        try {
            Files.list(rootPath)
                    .filter(Files::isRegularFile)
                    .filter(x -> x.getFileName().toString().endsWith(SIDDHI_FILE_SUFFIX))
                    .forEach(this::deploySiddhiApp);
        } catch (IOException e) {
            log.error("Error while deploying siddhi app.", e);
        }
    }

    public void stop() {

        stopWatching();
    }

    private void startWatching() {

        serviceWatcherThread = new Thread(() -> {

            try {
                WatchService watcher = FileSystems.getDefault().newWatchService();
                fileWatcherKey = rootPath.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            } catch (IOException e) {
                log.error("Error registering watcher for path: " + rootPath.toAbsolutePath());
            }
            isFileWatcherRunning = true;
            while (isFileWatcherRunning) {
                try {
                    for (WatchEvent<?> event : fileWatcherKey.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == OVERFLOW) {
                            continue;
                        } else if (kind == ENTRY_CREATE) {
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            deploySiddhiApp(getResolvedPathRelativeToRoot(ev.context()));
                        } else if (kind == ENTRY_DELETE) {
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            undeploySiddhiApp(getResolvedPathRelativeToRoot(ev.context()));
                        } else if (kind == ENTRY_MODIFY) {
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            updateSiddhiApp(getResolvedPathRelativeToRoot(ev.context()));
                        }
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
        fileWatcherKey.cancel();
    }

    private void updateSiddhiApp(Path siddhiAppPath) {

        undeploySiddhiApp(siddhiAppPath);
        // TODO : fallback mechanism if newly deployment fails...????
        deploySiddhiApp(siddhiAppPath);
    }

    private void undeploySiddhiApp(Path siddhiAppPath) {

        String siddhiAppName = fileNameToAppName.get(siddhiAppPath.getFileName().toString());
        if (siddhiAppName != null) {
            EmbeddedSiddhiEngine.getInstance().undeployApp(siddhiAppName);
            log.info("Siddhi App : " + siddhiAppName + " undeployed Successfully.");
        }
    }

    private void deploySiddhiApp(Path siddhiAppPath) {

        String siddhiApp;
        try {
            siddhiApp = getSiddhiApp(siddhiAppPath);
            EmbeddedSiddhiEngine.getInstance().deployApp(siddhiApp);
            String siddhiAppName = Utils.getSiddhiAppName(siddhiApp);
            fileNameToAppName.put(siddhiAppPath.getFileName().toString(), siddhiAppName);
            log.info("Siddhi App : " + siddhiAppName + " deployed Successfully. \n" + siddhiApp);
        } catch (IOException e) {
            log.error("Error while deploying siddhiApp from path: " + siddhiAppPath.toAbsolutePath());
        }

    }

    private String getSiddhiApp(Path path) throws IOException {

        return new String(Files.readAllBytes(path.toAbsolutePath()));
    }

    private Path getResolvedPathRelativeToRoot(Path path) {

        return rootPath.resolve(path);
    }
}
