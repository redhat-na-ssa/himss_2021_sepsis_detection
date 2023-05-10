package com.redhat.naps.sse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import io.smallrye.mutiny.Multi;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestStreamElementType;

@Path("sse")
@ApplicationScoped
public class FHIREventResource {

    Logger log = Logger.getLogger("FHIREventResource");

    @Channel(Util.RAW_FHIR_EVENT_STREAM) 
    Multi<String> rawFhirEvents;

    @Channel(Util.RISK_ASSESSMENT_CHANNEL) 
    Multi<String> riskAssessEvents;


    // Test:   curl -N http://localhost:4199/sse/event/fhir/raw
    @GET
    @Path("/event/fhir/raw")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.TEXT_PLAIN)
    public Multi<String> rawEventStream() {
        log.info("rawEventStream()");
        return rawFhirEvents;
    }

    // Test:   curl -N http://localhost:4199/sse/event/fhir/riskAsses
    @GET
    @Path("/event/fhir/riskAsses")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.TEXT_PLAIN)
    public Multi<String> riskAssessEventStream() {
        log.info("riskAssessEventStream()");
        return riskAssessEvents;
    }

}
