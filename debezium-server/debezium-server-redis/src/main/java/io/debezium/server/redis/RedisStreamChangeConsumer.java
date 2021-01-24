/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.server.redis;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.DebeziumEngine.RecordCommitter;
import io.debezium.server.BaseChangeConsumer;
import io.debezium.server.CustomConsumerBuilder;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

/**
 * Implementation of the consumer that delivers the messages into Redis (stream) destination.
 *
 * @author M Sazzadul Hoque
 *
 */
@Named("redis")
@Dependent
public class RedisStreamChangeConsumer extends BaseChangeConsumer
        implements DebeziumEngine.ChangeConsumer<ChangeEvent<Object, Object>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisStreamChangeConsumer.class);

    private static final String PROP_PREFIX = "debezium.sink.redis.";

    private static final String PROP_ADDRESS = PROP_PREFIX + "address";
    private HostAndPort address;

    @ConfigProperty(name = PROP_PREFIX + "credentials.user")
    String credentialsUser;

    @ConfigProperty(name = PROP_PREFIX + "credentials.password")
    String credentialsPassword;

    @ConfigProperty(name = PROP_PREFIX + "null.key", defaultValue = "default")
    String nullKey;

    private Jedis client = null;

    @Inject
    @CustomConsumerBuilder
    Instance<Jedis> customClient;

    @PostConstruct
    void connect() {
        if (customClient.isResolvable()) {
            client = customClient.get();
            LOGGER.info("Obtained custom configured Jedis '{}'", client);
            return;
        }

        final Config config = ConfigProvider.getConfig();
        address = HostAndPort.from(config.getValue(PROP_ADDRESS, String.class));
        client = new Jedis(address);
        if (credentialsUser != null) {
            client.auth(credentialsUser, credentialsPassword);
        }
        else if (credentialsPassword != null) {
            client.auth(credentialsPassword);
        }
        LOGGER.info("Using default Jedis '{}'", client);
    }

    @PreDestroy
    void close() {
        try {
            client.close();
        }
        catch (Exception e) {
            LOGGER.warn("Exception while closing Jedis: {}", client, e);
        }
    }

    @Override
    public void handleBatch(List<ChangeEvent<Object, Object>> records,
                            RecordCommitter<ChangeEvent<Object, Object>> committer)
            throws InterruptedException {

        for (ChangeEvent<Object, Object> record : records) {
            LOGGER.trace("Received event '{}'", record);

            String destination = record.destination();
            String key = (record.key() != null) ? getString(record.key()) : nullKey;
            String value = getString(record.value());
            client.xadd(destination, null, Collections.singletonMap(key, value));

            committer.markProcessed(record);
        }
        committer.markBatchFinished();
    }
}
