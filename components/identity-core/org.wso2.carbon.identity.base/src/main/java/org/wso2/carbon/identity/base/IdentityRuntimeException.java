/*
 *  Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.base;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used for creating checked exceptions that can be handled.
 */
public class IdentityRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -5872545821846152596L;

    private List<ErrorInfo> errorInfoList = new ArrayList<>();

    public static class ErrorInfo {

        private String contextId = null;
        private String errorCode  = null;
        private String errorDescription = null;
        private String userErrorDescription = null;
        private Throwable cause = null;
        private Map<String, Object> parameters = new HashMap<>();

        private ErrorInfo(ErrorInfoBuilder builder) {

            this.contextId = builder.contextId;
            this.errorCode = builder.errorCode;
            this.userErrorDescription = builder.userErrorDescription;
            this.errorDescription = builder.errorDescription;
            if(MapUtils.isNotEmpty(builder.parameters)){
                this.parameters = builder.parameters;
            }
            this.cause = builder.cause;
        }

        public String getContextId() {
            return contextId;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getErrorDescription() {
            return errorDescription;
        }

        public String getUserErrorDescription() {
            return userErrorDescription;
        }

        public Throwable getCause() {
            return cause;
        }

        public Map<String,Object> getParameters() {
            return this.parameters;
        }

        public Object getParameter(String key) {
            return this.parameters.get(key);
        }

        public static class ErrorInfoBuilder {

            private String contextId = null;
            private String errorCode  = null;
            private String errorDescription = null;
            private String userErrorDescription = null;
            private Throwable cause = null;
            private Map<String, Object> parameters = new HashMap<>();

            public ErrorInfoBuilder(String errorDescription) {
                this.errorDescription = errorDescription;
            }

            public ErrorInfoBuilder contextId(String contextId) {
                this.contextId = contextId;
                return this;
            }

            public ErrorInfoBuilder errorCode(String errorCode) {
                this.errorCode = errorCode;
                return this;
            }

            public ErrorInfoBuilder userErrorDescription(String userErrorDescription) {
                this.userErrorDescription = userErrorDescription;
                return this;
            }

            public ErrorInfoBuilder cause(Throwable cause) {
                this.cause = cause;
                return this;
            }

            public ErrorInfoBuilder parameters(Map<String,Object> parameters) {
                if(MapUtils.isNotEmpty(parameters)) {
                    this.parameters = parameters;
                }
                return this;
            }

            public ErrorInfoBuilder parameter(String key, Object value) {
                if(key != null){
                    this.parameters.put(key, value);
                }
                return this;
            }

            public ErrorInfo build(){
                return new ErrorInfo(this);
            }
        }
    }
    
    protected IdentityRuntimeException(String errorDescription) {
        super(errorDescription);
    }

    protected IdentityRuntimeException(String errorDescription, Throwable cause) {
        super(errorDescription, cause);
    }

    // This method may be used for easily migrating existing usages of IdentityRuntimeException creation.
    // However once we migrate all the usages of IdentityRuntimeException to create using error(ErrorInfo) we can remove this
    @Deprecated
    public static IdentityRuntimeException error(String errorDescription) {
        IdentityRuntimeException identityRuntimeException = new IdentityRuntimeException(errorDescription);
        ErrorInfo.ErrorInfoBuilder errorInfoBuilder = new ErrorInfo.ErrorInfoBuilder(errorDescription);
        identityRuntimeException.addErrorInfo(errorInfoBuilder.build());
        return identityRuntimeException;
    }

    // This method may be used for easily migrating existing usages of IdentityRuntimeException creation.
    // However once we migrate all the usages of IdentityRuntimeException to create using error(ErrorInfo) we can remove this
    @Deprecated
    public static IdentityRuntimeException error(String errorDescription, Throwable cause) {
        IdentityRuntimeException identityRuntimeException = new IdentityRuntimeException(errorDescription, cause);
        ErrorInfo.ErrorInfoBuilder errorInfoBuilder = new ErrorInfo.ErrorInfoBuilder(errorDescription);
        errorInfoBuilder.cause(cause);
        identityRuntimeException.addErrorInfo(errorInfoBuilder.build());
        return identityRuntimeException;
    }

    public static IdentityRuntimeException error(ErrorInfo errorInfo) {
        if(errorInfo == null || StringUtils.isBlank(errorInfo.errorDescription)){
            throw new IllegalArgumentException("ErrorInfo object is null or Error Description is blank");
        }
        IdentityRuntimeException identityRuntimeException = null;
        if(errorInfo.getCause() != null) {
            identityRuntimeException = new IdentityRuntimeException(errorInfo.getErrorDescription(),
                    errorInfo.getCause());
        } else {
            identityRuntimeException = new IdentityRuntimeException(errorInfo.getErrorDescription());
        }
        identityRuntimeException.addErrorInfo(errorInfo);
        return identityRuntimeException;
    }

    public void addErrorInfo(ErrorInfo errorInfo) {
        if(errorInfo == null || StringUtils.isBlank(errorInfo.errorDescription)){
            throw new IllegalArgumentException("ErrorInfo object is null or Error Description is blank");
        }
        this.errorInfoList.add(errorInfo);
    }

    public List<ErrorInfo> getErrorInfoList() {
        return errorInfoList;
    }

    public String getCode() {

        StringBuilder builder = new StringBuilder();
        for(int i = this.errorInfoList.size() - 1; i >= 0; i--) {
            ErrorInfo info = this.errorInfoList.get(i);
            builder.append('[');
            builder.append(info.contextId);
            builder.append(':');
            builder.append(info.errorCode);
            builder.append(']');
        }
        return builder.toString();
    }

//    public String toString() {
//        StringBuilder builder = new StringBuilder();
//
//        builder.append(getCode());
//        builder.append('\n');
//
//        //append additional context information.
//        for(int i = this.errorInfoList.size() - 1; i >= 0; i--) {
//            ErrorInfo info = this.errorInfoList.get(i);
//            builder.append('[');
//            builder.append(info.contextId);
//            builder.append(':');
//            builder.append(info.errorCode);
//            builder.append(']');
//            builder.append(info.errorDescription);
//            if(i > 0) {
//                builder.append('\n');
//            }
//        }
//
//        //append root causes and text from this exception first.
//        if(getMessage() != null) {
//            builder.append('\n');
//            if(getCause() == null) {
//                builder.append(getMessage());
//            } else if(!getMessage().equals(getCause().toString())) {
//                builder.append(getMessage());
//            }
//        }
//        appendException(builder, getCause());
//        return builder.toString();
//    }
//
//    private void appendException(StringBuilder builder, Throwable throwable) {
//        if(throwable == null) {
//            return;
//        }
//        appendException(builder, throwable.getCause());
//        builder.append(throwable.toString());
//        builder.append('\n');
//    }
}
