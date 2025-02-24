/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.mgt.model;

import java.util.HashMap;
import java.util.Map;

public class StepDTO {

    private String id;
    private String type;
    private double coordinateX;
    private double coordinateY;
    private double width;
    private double height;
    private final Map<String, Object> data = new HashMap<>();

    private StepDTO(Builder builder) {

        this.id = builder.id;
        this.type = builder.type;
        this.coordinateX = builder.coordinateX;
        this.coordinateY = builder.coordinateY;
        this.width = builder.width;
        this.height = builder.height;
        if (builder.data != null) {
            this.data.putAll(builder.data);
        }
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public double getCoordinateX() {

        return coordinateX;
    }

    public void setCoordinateX(double coordinateX) {

        this.coordinateX = coordinateX;
    }

    public double getCoordinateY() {

        return coordinateY;
    }

    public void setCoordinateY(double coordinateY) {

        this.coordinateY = coordinateY;
    }

    public double getWidth() {

        return width;
    }

    public void setWidth(double width) {

        this.width = width;
    }

    public double getHeight() {

        return height;
    }

    public void setHeight(double height) {

        this.height = height;
    }

    public Map<String, Object> getData() {

        return data;
    }

    public void addData(String key, Object value) {

        this.data.put(key, value);
    }

    public static class Builder {

        private String id;
        private String type;
        private double coordinateX;
        private double coordinateY;
        private double width;
        private double height;
        private Map<String, Object> data;

        public Builder id(String id) {

            this.id = id;
            return this;
        }

        public Builder type(String type) {

            this.type = type;
            return this;
        }

        public Builder coordinateX(double coordinateX) {

            this.coordinateX = coordinateX;
            return this;
        }

        public Builder coordinateY(double coordinateY) {

            this.coordinateY = coordinateY;
            return this;
        }

        public Builder width(double width) {

            this.width = width;
            return this;
        }

        public Builder height(double height) {

            this.height = height;
            return this;
        }

        public Builder data(Map<String, Object> data) {

            this.data = data;
            return this;
        }

        public StepDTO build() {

            return new StepDTO(this);
        }
    }
}
