package com.codelry.redis.maptree.service;

import com.codelry.redis.maptree.model.Record;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class MapService {

  private static final Logger logger = LoggerFactory.getLogger(MapService.class);
  private static final String RECORD_KEY_PREFIX = "record:";
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  private static final String FIELD_SESSION_ID = "sessionId";
  private static final String FIELD_CREATED_AT = "createdAt";
  private static final String FIELD_LAST_ACCESSED_AT = "lastAccessedAt";

  private final RetryTemplate retryTemplate;
  private final RedisTemplate<String, String> redisTemplate;
  private final MeterRegistry meterRegistry;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private final Timer createRecordTimer;
  private final Timer getRecordPathTimer;

  @Autowired
  public MapService(RedisTemplate<String, String> redisTemplate,
                    RetryTemplate retryTemplate,
                    MeterRegistry meterRegistry) {
    this.redisTemplate = redisTemplate;
    this.retryTemplate = retryTemplate;
    this.meterRegistry = meterRegistry;

    this.createRecordTimer = Timer.builder("record.create.duration")
        .description("Time taken to create a record")
        .tag("record", "create")
        .register(meterRegistry);

    this.getRecordPathTimer = Timer.builder("record.get.path.duration")
        .description("Time taken to get a record by path")
        .tag("record", "get")
        .publishPercentiles(0.95, 0.99)
        .serviceLevelObjectives(
            Duration.ofMillis(1),
            Duration.ofMillis(5),
            Duration.ofMillis(10)
        )
        .register(meterRegistry);
  }

  public Record createRecord(String key, byte[] requestBody) {
    try {
      Timer.Sample retryTimerSample = Timer.start(meterRegistry);

      return retryTemplate.execute(context -> {
        logger.debug("Attempting to insert record into Redis (attempt {})", context.getRetryCount() + 1);

        meterRegistry.counter("record.operation.retries",
                Tags.of("record", "create", "attempt", String.valueOf(context.getRetryCount() + 1)))
            .increment();

        Record record = new Record(key);

        Map<String, String> map = JsonFlattener.mapPaths(requestBody);
        createRecordTimer.record(() -> redisTemplate.opsForHash().putAll(key, map));

        logger.info("Successfully created record with ID: {}", record.getRecordId());

        meterRegistry.counter("record.create.success", Tags.of("record", "create")).increment();

        subDocGet(key, requestBody);

        return record;
      }, context -> {
        logger.error("Failed to create record after all retry attempts", context.getLastThrowable());

        retryTimerSample.stop(Timer.builder("record.create.retry.total.duration")
            .description("Total time including all retry attempts")
            .tag("record", "create")
            .register(meterRegistry));

        meterRegistry.counter("record.create.failure", Tags.of("record", "create")).increment();

        throw new RuntimeException("Unable to create record - Redis service unavailable", context.getLastThrowable());
      });
    } catch (Exception e) {
      logger.error("Error recording metrics for createRecord", e);
      throw new RuntimeException("Error recording metrics for createRecord", e);
    }
  }

  public void subDocGet(String key, byte[] document) {
    try {
      logger.info("Processing paths in document: {}", key);
      Set<String> paths = JsonFlattener.flattenPaths(document);
      FileWriter.create(paths);
      logger.info("Found {} paths in document", paths.size());

      Set<String> nullPaths = new HashSet<>();

      for (String path : paths) {
        if (path.contains("@")) {
          continue;
        }

        Object data = getRecordPathTimer.recordCallable(() -> redisTemplate.opsForHash().get(key, path));

        if (data == null) {
          meterRegistry.counter("record.get.null", Tags.of("record", "get")).increment();
          nullPaths.add(path);
          continue;
        }

        if (data instanceof String) {
          meterRegistry.counter("record.get.string", Tags.of("record", "get")).increment();
        } else {
          meterRegistry.counter("record.get.object", Tags.of("record", "get")).increment();
        }
      }
      FileWriter.create(nullPaths, "null_output.txt");
    } catch (Exception e) {
      logger.error("Error processing map", e);
    }
  }
}
