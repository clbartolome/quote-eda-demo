package com.redhat.demo.kstreams;

import java.time.Duration;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;

import com.redhat.demo.kstreams.model.Quote;
import com.redhat.demo.kstreams.model.QuoteAggregate;
import com.redhat.demo.kstreams.model.QuoteAggregateSerde;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class TopologyProducer {

    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        var quoteSerde = new ObjectMapperSerde<>(Quote.class);
        var quoteAggregateSerde = new ObjectMapperSerde<>(QuoteAggregate.class);

        KStream<String, Quote> quoteStream = builder.stream("quotes",
                Consumed.with(Serdes.String(), quoteSerde));

        quoteStream
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(6)))
                .aggregate(
                        QuoteAggregate::new,
                        (k, quote, aggregator) -> {
                            aggregator.addPrice(quote.price);
                            return aggregator;
                        },
                        Materialized.with(Serdes.String(), new QuoteAggregateSerde()))
                .toStream()
                .map((key, aggregate) -> {
                    return new KeyValue<>("quoteAggregate", aggregate);
                })
                .to("quote-aggregates", Produced.with(Serdes.String(), quoteAggregateSerde));

        var topology = builder.build();

        System.out.println(topology.describe());

        return topology;
    }
}
