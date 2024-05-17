package org.acme.amqp.producer;

import java.util.UUID;

import org.acme.amqp.model.Quote;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import io.smallrye.mutiny.Multi;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/quotes")
public class QuotesResource {

    private static final Logger LOG = Logger.getLogger(QuotesResource.class);

    // Inject a Reactive Messaging Emitter to send messages to the quote-requests channel
    @Channel("quote-requests")
    Emitter<String> quoteRequestEmitter;

    // Injects the quotes stream which is fed by the quote channel
    @Channel("quotes")
    Multi<Quote> quotes;

    /**
     * Endpoint retrieving the "quotes" queue and sending the items to a server sent
     * event.
     */
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS) // Indicates that the content is sent using Server Sent Events
    public Multi<Quote> stream() {
        LOG.info("stream for SSE");
        return quotes; // Returns the (reactive) stream which is fed by the quote channel
    }

    /**
     * Endpoint to generate a new quote request id and send it to "quote-requests"
     * AMQP queue using the emitter.
     */
    @POST
    @Path("/request")
    @Produces(MediaType.TEXT_PLAIN)
    public String createRequest() {
        LOG.info("new quote request");
        UUID uuid = UUID.randomUUID();                 // On a post request, generate a random UUID and
        quoteRequestEmitter.send(uuid.toString()); // send it to the quote request queue using the emitter
        return uuid.toString();
    }
}