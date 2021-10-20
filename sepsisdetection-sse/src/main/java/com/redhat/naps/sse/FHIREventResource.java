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

@Path("sse")
@ApplicationScoped
public class FHIREventResource {

    @Inject
    @Channel(Util.RAW_FHIR_EVENT_STREAM) Publisher<String> rawFhirEvents;

    @Inject
    @Channel(Util.RISK_ASSESSMENT_CHANNEL) Publisher<String> riskAssessEvents;


    // Test:   curl -N http://localhost:4199/sse/event/fhir/raw
    @GET
    @Path("/event/fhir/raw")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType("text/plain")
    public Publisher<String> rawEventStream() {
        return rawFhirEvents;
    }

    // Test:   curl -N http://localhost:4199/sse/event/fhir/riskAsses
    @GET
    @Path("/event/fhir/riskAsses")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType("text/plain")
    public Publisher<String> riskAssessEventStream() {
        return riskAssessEvents;
    }

}