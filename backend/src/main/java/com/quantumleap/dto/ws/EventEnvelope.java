package com.quantumleap.dto.ws;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventEnvelope {

    private String id;
    private String boardId;
    private String userId;
    private String type;
    private OffsetDateTime ts;
    private Object data;
    private String tempId;

    // Constructors
    public EventEnvelope() {}

    public EventEnvelope(String id, String boardId, String userId, String type, OffsetDateTime ts, Object data) {
        this.id = id;
        this.boardId = boardId;
        this.userId = userId;
        this.type = type;
        this.ts = ts;
        this.data = data;
    }

    public EventEnvelope(String id, String boardId, String userId, String type, OffsetDateTime ts, Object data, String tempId) {
        this.id = id;
        this.boardId = boardId;
        this.userId = userId;
        this.type = type;
        this.ts = ts;
        this.data = data;
        this.tempId = tempId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public OffsetDateTime getTs() {
        return ts;
    }

    public void setTs(OffsetDateTime ts) {
        this.ts = ts;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getTempId() {
        return tempId;
    }

    public void setTempId(String tempId) {
        this.tempId = tempId;
    }

    @Override
    public String toString() {
        return "EventEnvelope{" +
                "id='" + id + '\'' +
                ", boardId='" + boardId + '\'' +
                ", userId='" + userId + '\'' +
                ", type='" + type + '\'' +
                ", ts=" + ts +
                ", data=" + data +
                ", tempId='" + tempId + '\'' +
                '}';
    }
}
