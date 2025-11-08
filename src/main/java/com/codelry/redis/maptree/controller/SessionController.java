package com.codelry.redis.maptree.controller;

import com.codelry.redis.maptree.model.Record;
import com.codelry.redis.maptree.service.SessionService;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/api/create/{key}")
public class SessionController {

  private static final Logger logger = LoggerFactory.getLogger(SessionController.class);

  private final SessionService sessionService;

  @Autowired
  public SessionController(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  @PostMapping
  public ResponseEntity<Map<String, String>> createRecord(@PathVariable String key, @RequestBody byte[] requestBody) {
    Record record = sessionService.createRecord(key, requestBody);
    logger.info("Successfully created record: {}", record.getRecordId());
    Map<String, String> response = Map.of("key", record.getRecordId().toString());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
