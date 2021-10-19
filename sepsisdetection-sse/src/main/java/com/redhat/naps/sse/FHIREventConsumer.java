package com.redhat.naps.sse;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import io.smallrye.reactive.messaging.annotations.Broadcast;

import ca.uhn.fhir.context.FhirContext;

@ApplicationScoped
public class FHIREventConsumer {

    private static Logger log = Logger.getLogger(FHIREventConsumer.class);
    private static FhirContext fhirCtx = FhirContext.forR4();

    @PostConstruct
    public void start() {
        log.info("start()");
    }

    @Incoming("fhir-event")
    @Acknowledgment(Acknowledgment.Strategy.PRE_PROCESSING)  // Ack message prior to message processing
    @Outgoing("fhir-event-stream")
    @Broadcast                                               // Events are dispatched to all streaming subscribers
    public String process(String event) {
        log.debugv("Received event: {0}", event);
        return event;
    }

    // Forces vert.x io thread to begin consumption on message channel
    // Otherwise, initialization of message channel will not occur until ServerSideEvent class receives first request
    // Expect to see a log statement similar to the following:
    //   INFO  [org.apache.kafka.clients.consumer.KafkaConsumer] (vert.x-kafka-consumer-thread-0) [Consumer clientId=consumer-e-console-react-group-1, groupId=e-console-react-group] Subscribed to topic(s): topic-incident-command
    @Incoming("fhir-event-stream")
    public void capture(String x) {
    }

}
