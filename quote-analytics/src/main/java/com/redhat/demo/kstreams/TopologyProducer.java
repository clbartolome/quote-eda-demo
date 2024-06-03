package com.redhat.demo.kstreams;

import java.util.Arrays;
import java.util.Locale;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class TopologyProducer {

    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        builder.<String, String>stream("streams-plaintext-input")
               .flatMapValues(value -> Arrays.asList(value.toLowerCase(Locale.getDefault()).split("\\W+")))
               .groupBy((key, value) -> value)
               .count(Materialized.as("counts-store"))
               .toStream()
               .to("streams-wordcount-output", Produced.with(Serdes.String(), Serdes.Long()));

        var topology = builder.build();

        System.out.println(topology.describe());

        return topology;
    }
}
