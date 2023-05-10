package com.redhat.naps.sse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import jakarta.annotation.PostConstruct;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import io.smallrye.reactive.messaging.MutinyEmitter;
import io.smallrye.reactive.messaging.annotations.Broadcast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@ApplicationScoped
public class FHIREventConsumer {


    private static Logger log = Logger.getLogger(FHIREventConsumer.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    @Channel(Util.RISK_ASSESSMENT_CHANNEL)
    @Broadcast
    MutinyEmitter<String> riskAssessmentEmitter;

    @PostConstruct
    public void start() {
        log.info("start()");
    }

    @Incoming(Util.FHIR_EVENT)
    @Acknowledgment(Acknowledgment.Strategy.PRE_PROCESSING)  // Ack message prior to message processing
    @Outgoing(Util.RAW_FHIR_EVENT_STREAM)
    @Broadcast                                               // Events are dispatched to all streaming subscribers
    public String process(String event) throws JsonMappingException, JsonProcessingException {
        log.debugv("process() Received event: {0}", event);
        JsonNode rootNode = objectMapper.readTree(event);
        JsonNode after = rootNode.get("data").get("payload").get("after");
        JsonNode resType = after.get("res_type");
        if (Util.RISK_ASSESSMENT.equals(resType.asText())){
            JsonNode resId = after.get("res_id");
            log.info("process() resId = "+resId);
            riskAssessmentEmitter.sendAndForget(event);
        }
        return event;
    }

    // Forces vert.x io thread to begin consumption on message channel
    // Otherwise, initialization of message channel will not occur until ServerSentEvent class receives first request
    // Expect to see a log statement similar to the following:
    //   INFO  [org.apache.kafka.clients.consumer.KafkaConsumer] (vert.x-kafka-consumer-thread-0) [Consumer clientId=consumer-e-console-react-group-1, groupId=e-console-react-group] Subscribed to topic(s): topic-incident-command
    @Incoming(Util.RAW_FHIR_EVENT_STREAM)
    public void captureRaw(String x) {
    }

    // Just in case at start-up there are RiskAssessments, the following avoids this warning:
    //  SRMSG00200: The method com.redhat.naps.sse.FHIREventConsumer#process has thrown an exception: java.lang.IllegalStateException: SRMSG00027: No subscriber found for the channel riskAsses-event-stream
    @Incoming(Util.RISK_ASSESSMENT_CHANNEL)
    public void captureRiskAssessment(String x){
    }

}
