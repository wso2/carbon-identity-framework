/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Result of JavaScript evaluation from the engine.
 * This is used to transfer results between local/remote execution and the graph
 * builder.
 */
public class EvaluationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final Object result;
    private final Map<String, Object> updatedBindings;
    private final String errorMessage;
    private final String errorType;
    private final long elapsedMs;

    private EvaluationResult(Builder builder) {
        this.success = builder.success;
        this.result = builder.result;
        this.updatedBindings = builder.updatedBindings;
        this.errorMessage = builder.errorMessage;
        this.errorType = builder.errorType;
        this.elapsedMs = builder.elapsedMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public Object getResult() {
        return result;
    }

    public Map<String, Object> getUpdatedBindings() {
        return updatedBindings;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorType() {
        return errorType;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static EvaluationResult success(Object result, long elapsedMs) {
        return new Builder()
                .success(true)
                .result(result)
                .elapsedMs(elapsedMs)
                .build();
    }

    public static EvaluationResult failure(String errorMessage, String errorType, long elapsedMs) {
        return new Builder()
                .success(false)
                .errorMessage(errorMessage)
                .errorType(errorType)
                .elapsedMs(elapsedMs)
                .build();
    }

    /**
     * Builder for EvaluationResult.
     */
    public static class Builder {
        private boolean success;
        private Object result;
        private Map<String, Object> updatedBindings = new HashMap<>();
        private String errorMessage;
        private String errorType;
        private long elapsedMs;

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder result(Object result) {
            this.result = result;
            return this;
        }

        public Builder updatedBindings(Map<String, Object> updatedBindings) {
            this.updatedBindings = updatedBindings;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder errorType(String errorType) {
            this.errorType = errorType;
            return this;
        }

        public Builder elapsedMs(long elapsedMs) {
            this.elapsedMs = elapsedMs;
            return this;
        }

        public EvaluationResult build() {
            return new EvaluationResult(this);
        }
    }
}
