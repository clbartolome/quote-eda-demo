package org.acme.amqp.processor;

import java.util.Random;

import org.acme.amqp.model.Quote;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * A bean consuming data from the "request" AMQP queue and giving out a random quote.
 * The result is pushed to the "quotes" AMQP queue.
 */
@ApplicationScoped
public class QuoteProcessor {
    private static final Logger LOG = Logger.getLogger(QuoteProcessor.class);

    private Random random = new Random();

    @Incoming("requests")       // quotes-requests
    @Outgoing("quotes")         // quotes
    @Blocking(ordered = false)  // blocking method
    public Quote process(String quoteRequest) throws InterruptedException {
        LOG.info("quote serving");
        // simulate some hard working task
        Thread.sleep(200+random.nextInt(10)*200);
        return new Quote(quoteRequest, random.nextInt(100));
    }
}