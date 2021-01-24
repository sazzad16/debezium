/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.server.redis;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class RedisTestResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {

    public static final String REDIS_HOST = "redis";
    public static final int REDIS_PORT = 6379;
    public static final String REDIS_IMAGE = "redis:alpine";

    private static final GenericContainer<?> container = new GenericContainer<>(REDIS_IMAGE)
            .withStartupTimeout(Duration.ofMinutes(5))
            .waitingFor(Wait.forLogMessage(".*Ready to accept connections*", 1))
            .withExposedPorts(REDIS_PORT);

    @Override
    public Map<String, String> start() {
        container.start();

        Map<String, String> params = new ConcurrentHashMap<>();

        return params;
    }

    @Override
    public void stop() {
        try {
            if (container != null) {
                container.stop();
            }
        }
        catch (Exception e) {
            // ignored
        }
    }

    public static String getRedisServiceAddress() {
        return String.format("redis://%s:%d", REDIS_HOST, REDIS_PORT);
    }
}
