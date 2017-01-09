/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.gateway.artifact.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class HandlerConfig {
    private String id ;
    private String  name ;
    private String reference ;
    private HandlerConfig nextHandler ;

    private Map config = new HashMap() ;

    public HandlerConfig getNextHandler() {
        return nextHandler;
    }

    public void setNextHandler(HandlerConfig nextHandler) {
        this.nextHandler = nextHandler;
    }

    public Map getConfig() {
        return config;
    }

    public void setConfig(Map config) {
        this.config = config;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void setConfig(Properties config) {
        this.config = config;
    }

    private List<HandlerConfig> handlers = new ArrayList<>();


    public List<HandlerConfig> getHandlers() {
        return handlers;
    }

    public void setHandlers(List<HandlerConfig> handlers) {
        this.handlers = handlers;
    }

}
