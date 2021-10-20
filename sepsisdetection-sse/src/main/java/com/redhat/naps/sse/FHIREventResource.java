package com.redhat.naps.sse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.annotations.SseElementType;
import org.reactivestreams.Publisher;

import org.jboss.logging.Logger;

@Path("sse")
@ApplicationScoped
public class FHIREventResource {

    private static Logger log = Logger.getLogger(FHIREventResource.class);

    @Inject
    @Channel("raw-fhir-event-stream") Publisher<String> rawFhirEvents;

    @Inject
    @Channel("riskEval-event-stream") Publisher<String> riskEvalEvents;


    // Test:   curl -N http://localhost:4199/sse/event/fhir/raw
    @GET
    @Path("/event/fhir/raw")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType("text/plain")
    public Publisher<String> rawEventStream() {
        return rawFhirEvents;
    }

    // Test:   curl -N http://localhost:4199/sse/event/fhir/riskEval
    @GET
    @Path("/event/fhir/riskEval")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType("text/plain")
    public Publisher<String> riskEvalEventStream() {
        return riskEvalEvents;
    }

}