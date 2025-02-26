/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.rule.management.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * Represents a rule in Rule Management.
 * This class has an id, a condition and a status.
 */
public abstract class Rule {

    protected String id;
    protected Condition condition;
    protected boolean isActive;

    /**
     * @JsonIgnore annotation is used to ignore the id field when serializing and deserializing the object,
     * to and from JSON in order to store in the database.
     */
    @JsonIgnore
    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    /**
     * @JsonIgnore annotation is used to ignore the active field when serializing and deserializing the object,
     * to and from JSON in order to store in the database.
     */
    @JsonIgnore
    public boolean isActive() {

        return isActive;
    }


    public abstract List<Expression> getExpressions();
}
