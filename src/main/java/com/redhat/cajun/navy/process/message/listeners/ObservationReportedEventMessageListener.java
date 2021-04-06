package com.redhat.cajun.navy.process.message.listeners;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.fhir.r4.model.Observation;
import io.cloudevents.CloudEvent;
import org.jbpm.services.api.ProcessService;
import org.kie.internal.KieInternalServices;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class ObservationReportedEventMessageListener {

    private final static Logger log = LoggerFactory.getLogger(ObservationReportedEventMessageListener.class);

    private static final String TYPE_OBSERVATION_REPORTED_EVENT = "ObservationReportedEvent";

    private CorrelationKeyFactory correlationKeyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();

    @Autowired
    private ProcessService processService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Value("${observation.deployment.id}")
    private String deploymentId;

    @Value("${observation.process.id}")
    private String processId;

    @KafkaListener(topics = "${listener.destination.observation-reported-event}")
    public void processMessage(@Payload CloudEvent cloudEvent, @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                               @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, Acknowledgment ack) {

        if (!accept(cloudEvent)) {
            ack.acknowledge();
            return;
        }
        log.debug("Processing 'ObservationReportedEvent' message for observation " + key + " from topic:partition " + topic + ":" + partition);
        doProcessMessage(cloudEvent, ack);
    }

    private void doProcessMessage(CloudEvent cloudEvent, Acknowledgment ack) {
        Observation oEvent;
        try {

            oEvent = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(cloudEvent.getData().toBytes(), new TypeReference<Observation>() {});

            validate(oEvent);


            Map<String, Object> parameters = new HashMap<>();
            parameters.put("observation", oEvent);

            CorrelationKey correlationKey = correlationKeyFactory.newCorrelationKey(oEvent.toString());

            TransactionTemplate template = new TransactionTemplate(transactionManager);
            template.execute((TransactionStatus s) -> {
                Long pi = processService.startProcess(deploymentId, processId, correlationKey, parameters);
                log.debug("Started process for observation " + oEvent.toString() + ". ProcessInstanceId = " + pi);
                return null;
            });
        } catch (Exception e) {
            log.error("Error processing CloudEvent " + cloudEvent, e);
        }
        ack.acknowledge();
    }

    private boolean accept(CloudEvent cloudEvent) {
        if (cloudEvent == null) {
            log.warn("Message is not a CloudEvent. Message is ignored");
            //TODO: exception logging
            return false;
        }
        String messageType = cloudEvent.getType();
        if (!(TYPE_OBSERVATION_REPORTED_EVENT.equalsIgnoreCase(messageType))) {
            log.debug("Message with type '" + messageType + "' is ignored");
            return false;
        }
        String contentType = cloudEvent.getDataContentType();
        if (contentType == null || !(contentType.equalsIgnoreCase("application/json"))) {
            log.warn("CloudEvent data content type is not specified or not 'application/json'. Message is ignored");
            return false;
        }
        return true;
    }

    private void validate(Observation ire) {
        if (ire.getId() == null || ire.getId().isEmpty() ) {
            throw new IllegalStateException("Missing fields in ObservationReportedEvent: " + ire.toString());
        }
    }
}
