package com.redhat.naps;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.naps.utils.RiskAssessmentUtils;


import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.hl7.fhir.r4.model.Patient;

import io.smallrye.reactive.messaging.ce.CloudEventMetadata;
import io.smallrye.reactive.messaging.ce.IncomingCloudEventMetadata;
import io.smallrye.reactive.messaging.ce.OutgoingCloudEventMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

@ApplicationScoped
public class CommandConsumer {

    private static final Logger log = LoggerFactory.getLogger("CommandConsumer");
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static FhirContext fhirCtx = FhirContext.forR4();

    @Inject
    RiskAssessmentService raService;

    @PostConstruct
    public void start() {
        log.info("start()");
    }

    @Incoming(RiskAssessmentUtils.COMMAND_CHANNEL)
    @Acknowledgment(Acknowledgment.Strategy.PRE_PROCESSING)  // Ack message prior to message processing
    public CompletionStage<Void> process(Message<String> message) throws JsonMappingException, JsonProcessingException {

        Optional<CloudEventMetadata> metadata = message.getMetadata(OutgoingCloudEventMetadata.class);
        //Optional<CloudEventMetadata> metadata = message.getMetadata(IncomingCloudEventMetadata.class);

        if (metadata.isEmpty()) {
            log.warn("process() Incoming message is not a CloudEvent");
            return CompletableFuture.completedFuture(null);
        }
        CloudEventMetadata<String> cloudEventMetadata = metadata.get();
        String type = cloudEventMetadata.getType();
        if(!RiskAssessmentUtils.MESSAGE_TYPE_COMMAND.equals(type)){
            log.warn("process() message data type != "+RiskAssessmentUtils.MESSAGE_TYPE_COMMAND+"  : "+ type);
            return CompletableFuture.completedFuture(null);
        }

        Map<String, Object> cloudEventMap = objectMapper.readValue(message.getPayload(), Map.class);
        Map<String, String> dataNode = (Map<String, String>)cloudEventMap.get(RiskAssessmentUtils.CLOUD_EVENT_DATA);

        String pNode = dataNode.get(RiskAssessmentUtils.PATIENT);
        if(pNode == null)
          throw new RuntimeException("CloudEvent payload does not include element: "+RiskAssessmentUtils.PATIENT);
        Patient patient = (Patient)fhirCtx.newJsonParser().parseResource(pNode);
        
        String srNode = dataNode.get(RiskAssessmentUtils.SEPSIS_RESULT);
        if(srNode == null)
          throw new RuntimeException("CloudEvent payload does not include element: "+RiskAssessmentUtils.SEPSIS_RESULT);
    
        String oNode = dataNode.get(RiskAssessmentUtils.OBSERVATION_ID);
        if(oNode == null)
          throw new RuntimeException("CloudEvent payload does not include element: "+RiskAssessmentUtils.OBSERVATION_ID);

        String cNode = dataNode.get(RiskAssessmentUtils.CORRELATION_KEY);
        if(cNode == null)
            throw new RuntimeException("CloudEvent payload does not include element: "+RiskAssessmentUtils.CORRELATION_KEY);
        
        raService.publishRiskAssessment(patient, srNode, oNode, cNode);
        return CompletableFuture.completedFuture(null);
    }
}
