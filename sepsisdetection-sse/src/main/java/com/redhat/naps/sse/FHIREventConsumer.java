package com.redhat.naps.sse;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import io.smallrye.reactive.messaging.annotations.Broadcast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@ApplicationScoped
public class FHIREventConsumer {

    private static final String RISK_ASSESSMENT = "RiskAssessment";
    private static final String RISK_ASSESSMENT_CHANNEL = "riskEval-event-stream";
    private static Logger log = Logger.getLogger(FHIREventConsumer.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    @Channel(RISK_ASSESSMENT_CHANNEL)
    Emitter<String> riskAssessmentEmitter;

    @PostConstruct
    public void start() {
        log.info("start()");
    }

    @Incoming("fhir-event")
    @Acknowledgment(Acknowledgment.Strategy.PRE_PROCESSING)  // Ack message prior to message processing
    @Outgoing("raw-fhir-event-stream")
    @Broadcast                                               // Events are dispatched to all streaming subscribers
    public String process(String event) throws JsonMappingException, JsonProcessingException {
        log.debugv("Received event: {0}", event);
        JsonNode rootNode = objectMapper.readTree(event);
        JsonNode after = rootNode.get("data").get("payload").get("after");
        JsonNode resType = after.get("res_type");
        if (RISK_ASSESSMENT.equals(resType.asText())){
            JsonNode resId = after.get("res_id");
            log.info("broadcastRiskEval() resId = "+resId);
            riskAssessmentEmitter.send(event);
        }
        return event;
    }

    // Forces vert.x io thread to begin consumption on message channel
    // Otherwise, initialization of message channel will not occur until ServerSideEvent class receives first request
    // Expect to see a log statement similar to the following:
    //   INFO  [org.apache.kafka.clients.consumer.KafkaConsumer] (vert.x-kafka-consumer-thread-0) [Consumer clientId=consumer-e-console-react-group-1, groupId=e-console-react-group] Subscribed to topic(s): topic-incident-command
    @Incoming("raw-fhir-event-stream")
    public void capture(String x) {
    }

}
