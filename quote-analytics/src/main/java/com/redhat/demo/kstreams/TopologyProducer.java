package com.redhat.demo.kstreams;

import java.util.Arrays;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class TopologyProducer {

    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> source = builder.stream("streams-plaintext-input", Consumed.with(Serdes.String(), Serdes.String()));

        KStream<String, String> words = source.flatMapValues(value -> Arrays.asList(value.split("\\W+")));

        words.to("streams-pipe-output", Produced.with(Serdes.String(), Serdes.String()));

        var topology = builder.build();

        System.out.println(topology.describe());

        return topology;
    }
}
