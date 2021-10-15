package com.redhat.naps.sse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.annotations.SseElementType;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.reactivestreams.Publisher;

import io.smallrye.mutiny.Uni;

import org.jboss.logging.Logger;

@Path("sse")
@ApplicationScoped
public class FHIREventResource {

    private static Logger log = Logger.getLogger(FHIREventResource.class);

    @Inject
    @Channel("fhir-event-stream") Publisher<String> fhirEvents;

    @GET
    @Path("/event/fhir")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType("text/plain")
    public Publisher<String> eventStream() {
        return fhirEvents;
    }

}
