/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.server.redis;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.connect.runtime.standalone.StandaloneConfig;

import io.debezium.server.TestConfigSource;

public class RedisTestConfigSource extends TestConfigSource {

    public static final String REDIS_ADDRESS = RedisTestResourceLifecycleManager.getRedisServiceAddress();

    public RedisTestConfigSource() {
        Map<String, String> redisTest = new HashMap<>();

        redisTest.put("debezium.sink.type", "redis");
        redisTest.put("debezium.sink.redis.address", REDIS_ADDRESS);
        redisTest.put("debezium.source.connector.class", "io.debezium.connector.postgresql.PostgresConnector");
        redisTest.put("debezium.source." + StandaloneConfig.OFFSET_STORAGE_FILE_FILENAME_CONFIG, OFFSET_STORE_PATH.toAbsolutePath().toString());
        redisTest.put("debezium.source.offset.flush.interval.ms", "0");
        redisTest.put("debezium.source.database.server.name", "testc");
        redisTest.put("debezium.source.schema.include.list", "inventory");
        redisTest.put("debezium.source.table.include.list", "inventory.customers");

        config = redisTest;
    }
}
