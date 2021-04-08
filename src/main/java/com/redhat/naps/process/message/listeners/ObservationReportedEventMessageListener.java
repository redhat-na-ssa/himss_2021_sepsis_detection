package com.redhat.naps.process.message.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import io.cloudevents.CloudEvent;

// https://github.com/hapifhir/hapi-fhir/blob/master/hapi-fhir-base/src/main/java/ca/uhn/fhir/context/FhirContext.java
import ca.uhn.fhir.context.FhirContext;

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
    private static final String OBSERVATION_PROCESS_VARIABLE_NAME = "Observation";

    private static FhirContext fhirCtx = FhirContext.forR4();

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

            /* NOTE:  
                FHIR data object uses id convention of:   <FHIR data type>/id
                Will use just the latter substring (after the "/") as the process instance correlation key
            */
            String idBase = oEvent.getIdBase();
            String cKey = idBase.substring(idBase.indexOf("/")+1);
            //log.info("doProcessMessage() correlationKey = "+cKey);
            CorrelationKey correlationKey = correlationKeyFactory.newCorrelationKey(cKey);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put(OBSERVATION_PROCESS_VARIABLE_NAME, oEvent);

            TransactionTemplate template = new TransactionTemplate(transactionManager);
            template.execute((TransactionStatus s) -> {
                Long pi = processService.startProcess(deploymentId, processId, correlationKey, parameters);
                log.debug("Started process for observation " + oEvent.toString() + ". ProcessInstanceId = " + pi+" : correlationKey = "+correlationKey.getName());
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

    private void validate(Observation obs) {
        //TO_DO
    }
}
