/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.data.provider.dfdp.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DFDP Timeline Entry DTO.
 * Part 7: Response Generation - Represents a single event in the DFDP execution timeline.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DFDPTimelineEntry {

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("relativeTime")
    private long relativeTimeMs;

    @JsonProperty("stage")
    private String stage;

    @JsonProperty("event")
    private String event;

    @JsonProperty("description")
    private String description;

    @JsonProperty("status")
    private String status;

    @JsonProperty("duration")
    private Long durationMs;

    @JsonProperty("claimCount")
    private Integer claimCount;

    @JsonProperty("details")
    private String details;

    /**
     * Default constructor.
     */
    public DFDPTimelineEntry() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Constructor with basic information.
     * 
     * @param stage Processing stage
     * @param event Event type
     * @param description Event description
     */
    public DFDPTimelineEntry(String stage, String event, String description) {
        this();
        this.stage = stage;
        this.event = event;
        this.description = description;
    }

    /**
     * Constructor with full information.
     * 
     * @param timestamp Event timestamp
     * @param stage Processing stage
     * @param event Event type
     * @param description Event description
     * @param status Event status
     */
    public DFDPTimelineEntry(long timestamp, String stage, String event, String description, String status) {
        this.timestamp = timestamp;
        this.stage = stage;
        this.event = event;
        this.description = description;
        this.status = status;
    }

    // Getters and setters

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getRelativeTimeMs() {
        return relativeTimeMs;
    }

    public void setRelativeTimeMs(long relativeTimeMs) {
        this.relativeTimeMs = relativeTimeMs;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public Integer getClaimCount() {
        return claimCount;
    }

    public void setClaimCount(Integer claimCount) {
        this.claimCount = claimCount;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    // Utility methods

    public double getRelativeTimeSeconds() {
        return relativeTimeMs / 1000.0;
    }

    public double getDurationSeconds() {
        return durationMs != null ? durationMs / 1000.0 : 0.0;
    }

    public String getFormattedTimestamp() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new java.util.Date(timestamp));
    }
}
