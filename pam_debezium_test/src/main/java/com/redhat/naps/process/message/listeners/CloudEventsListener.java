package com.redhat.naps.process.message.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import io.cloudevents.CloudEvent;

// https://github.com/hapifhir/hapi-fhir/blob/master/hapi-fhir-base/src/main/java/ca/uhn/fhir/context/FhirContext.java
import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Observation;

import com.redhat.naps.process.FhirProcessMgmt;


@Component
public class CloudEventsListener {

    private final static Logger log = LoggerFactory.getLogger(CloudEventsListener.class);

    private static final String TYPE_OBSERVATION_REPORTED_EVENT = "ObservationReportedEvent";
    private static FhirContext fhirCtx = FhirContext.forR4();

    @Autowired
    FhirProcessMgmt fhirProcessMgmt;
    
    @KafkaListener(topics = "${listener.destination.observation-reported-event}")
    public void processMessage(@Payload CloudEvent cloudEvent, 
                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                               @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, 
                               Acknowledgment ack) {

        if (!accept(cloudEvent)) {
            ack.acknowledge();
            return;
        }
        doProcessMessage(cloudEvent, ack);
    }

    private void doProcessMessage(CloudEvent cloudEvent, Acknowledgment ack) {
        Observation oEvent;
        try {

            String obsString = new String(cloudEvent.getData().toBytes());
            //log.debug("doProcessMessage() obString = "+obsString);
            oEvent = fhirCtx.newJsonParser().parseResource(Observation.class, obsString );

            validate(oEvent);

            fhirProcessMgmt.startProcess(oEvent);
            
        } catch (Exception e) {
            log.error("Error processing CloudEvent " + cloudEvent, e);
            e.printStackTrace();
        } finally {
            ack.acknowledge();
        }
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

    private void validate(Observation obs) {
        //TO_DO
    }
}
