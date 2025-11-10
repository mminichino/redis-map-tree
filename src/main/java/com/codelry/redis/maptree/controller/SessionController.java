package com.codelry.redis.maptree.controller;

import com.codelry.redis.maptree.model.Record;
import com.codelry.redis.maptree.service.MapService;
import com.codelry.redis.maptree.service.MapTreeService;
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
@RequestMapping("/v1/api")
public class SessionController {

  private static final Logger logger = LoggerFactory.getLogger(SessionController.class);

  private final SessionService sessionService;
  private final MapService mapService;
  private final MapTreeService mapTreeService;

  @Autowired
  public SessionController(SessionService sessionService, MapService mapService, MapTreeService mapTreeService) {
    this.sessionService = sessionService;
    this.mapService = mapService;
    this.mapTreeService = mapTreeService;
  }

  @PostMapping("/create/{key}")
  public ResponseEntity<Map<String, String>> createRecord(@PathVariable String key, @RequestBody byte[] requestBody) {
    Record record = sessionService.createRecord(key, requestBody);
    logger.info("Successfully created record: {}", record.getRecordId());
    Map<String, String> response = Map.of("key", record.getRecordId().toString());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/map/{key}")
  public ResponseEntity<Map<String, String>> createMapRecord(@PathVariable String key, @RequestBody byte[] requestBody) {
    Record record = mapService.createRecord(key, requestBody);
    logger.info("Successfully created map record: {}", record.getRecordId());
    Map<String, String> response = Map.of("key", record.getRecordId().toString());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/tree/{key}")
  public ResponseEntity<Map<String, String>> createTreeRecords(@PathVariable String key, @RequestBody byte[] requestBody) {
    Record record = mapTreeService.createRecord(key, requestBody);
    logger.info("Successfully created tree records: {}", record.getRecordId());
    Map<String, String> response = Map.of("key", record.getRecordId().toString());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
