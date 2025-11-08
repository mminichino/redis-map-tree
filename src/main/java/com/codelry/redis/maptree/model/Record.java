package com.codelry.redis.maptree.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.Set;

public class Record {
  private String recordId;
  private LocalDateTime createdAt;
  private LocalDateTime lastAccessedAt;
  private JsonNode record;
  private Set<String> recordSchema;

  public Record(String recordId) {
    this.recordId = recordId;
    this.createdAt = LocalDateTime.now();
    this.lastAccessedAt = LocalDateTime.now();
  }

  public Record() {
    this.createdAt = LocalDateTime.now();
    this.lastAccessedAt = LocalDateTime.now();
  }

  public String getRecordId() {
    return recordId;
  }

  public void setRecordId(String sessionId) {
    this.recordId = sessionId;
  }

  public JsonNode getRecord() {
    return record;
  }

  public void setRecord(JsonNode record) {
    this.record = record;
  }

  public Set<String> getRecordSchema() {
    return recordSchema;
  }

  public void setRecordSchema(Set<String> recordSchema) {
    this.recordSchema = recordSchema;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getLastAccessedAt() {
    return lastAccessedAt;
  }

  public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
    this.lastAccessedAt = lastAccessedAt;
  }

  public void updateLastAccessed() {
    this.lastAccessedAt = LocalDateTime.now();
  }
}
