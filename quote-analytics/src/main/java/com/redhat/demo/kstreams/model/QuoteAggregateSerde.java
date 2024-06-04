package com.redhat.demo.kstreams.model;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.ByteBuffer;
import java.util.Map;

public class QuoteAggregateSerde implements Serde<QuoteAggregate> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public void close() {
    }

    @Override
    public Serializer<QuoteAggregate> serializer() {
        return new Serializer<QuoteAggregate>() {
            @Override
            public byte[] serialize(String topic, QuoteAggregate data) {
                ByteBuffer buffer = ByteBuffer.allocate(16);
                buffer.putDouble(data.getSum());
                buffer.putLong(data.getCount());
                return buffer.array();
            }
        };
    }

    @Override
    public Deserializer<QuoteAggregate> deserializer() {
        return new Deserializer<QuoteAggregate>() {
            @Override
            public QuoteAggregate deserialize(String topic, byte[] data) {
                ByteBuffer buffer = ByteBuffer.wrap(data);
                double sum = buffer.getDouble();
                long count = buffer.getLong();
                return new QuoteAggregate(sum, count);
            }
        };
    }
}
