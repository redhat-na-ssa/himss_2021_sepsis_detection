package com.redhat.naps;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.naps.rest.FhirServerClient;
import com.redhat.naps.utils.RiskAssessmentUtils;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RiskAssessment;
import org.hl7.fhir.r4.model.RiskAssessment.RiskAssessmentPredictionComponent;
import org.hl7.fhir.r4.model.RiskAssessment.RiskAssessmentStatus;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.jackson.JsonFormat;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.reactive.messaging.ce.CloudEventMetadata;
import io.smallrye.reactive.messaging.ce.OutgoingCloudEventMetadata;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.vertx.mutiny.core.eventbus.EventBus;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;

@ApplicationScoped
public class RiskAssessmentService {

    private static final Logger log = LoggerFactory.getLogger("RiskAssessmentService");
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static FhirContext fhirCtx = FhirContext.forR4();

    @Inject
    @Channel(RiskAssessmentUtils.EVENT_CHANNEL)
    Emitter eventChannel;

    @Inject
    @RestClient
    FhirServerClient fhirServerClient;

    @ConfigProperty(name=RiskAssessmentUtils.POST_TO_FHIR_SERVER, defaultValue = "true")
    boolean postToFhirServer;

    @Inject
    EventBus eventBus;

    @PostConstruct
    public void start() {
        // Prevents the following:
        //   com.fasterxml.jackson.databind.exc.InvalidDefinitionException: \
        //   No serializer found for class io.cloudevents.core.data.BytesCloudEventData and no properties discovered to create BeanSerializer \
        //   (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS) (through reference chain: io.cloudevents.core.v1.CloudEventV1["data"])
        objectMapper.registerModule(JsonFormat.getCloudEventJacksonModule());
    }

    public void publishRiskAssessment(Patient patient, String sepsisResult, String observationId, String correlationKey) throws JsonProcessingException {
        log.info("createRiskAssessment() patient = "+patient.getId()+" : sepsisResult = "+sepsisResult+" : obsId = "+observationId);
        String uid = UUID.randomUUID().toString();
        RiskAssessment assessment = createRiskAssessment(patient, sepsisResult, observationId, correlationKey);

        String cEventString = generateCloudEventJson(uid, assessment, RiskAssessmentUtils.MESSAGE_TYPE_EVENT);
        
        CloudEventMetadata<String> cloudEventMetadata = OutgoingCloudEventMetadata.<String>builder()
        .withType(RiskAssessmentUtils.MESSAGE_TYPE_EVENT)
        .withTimestamp(OffsetDateTime.now().toZonedDateTime())
        .build();
        Message<String> record = KafkaRecord.of(uid, cEventString).addMetadata(cloudEventMetadata);

        eventChannel.send(record);

        eventBus.sendAndForget(RiskAssessmentUtils.POST_TO_FHIR_SERVER, assessment);
    }

    private String generateCloudEventJson(String uid, RiskAssessment assessment, String messageType ) throws JsonProcessingException {

        String assessmentPayload = fhirCtx.newJsonParser().setPrettyPrint(false).encodeResourceToString(assessment);

        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put(RiskAssessmentUtils.RISK_ASSESSMENT, assessmentPayload);
        String cloudEventPayload = objectMapper.writeValueAsString(rootNode);

        CloudEvent cloudEvent = CloudEventBuilder.v1()
            .withId(uid)
            .withSource(URI.create(""))
            .withType(messageType)
            .withTime(OffsetDateTime.now())
            .withData(cloudEventPayload.getBytes())
            .build();

        return objectMapper.writeValueAsString(cloudEvent);
    }

    private RiskAssessment createRiskAssessment(Patient patient, String sepsisResult, String observationId, String correlationKey) {

        RiskAssessment assessment = new RiskAssessment();

        Reference ref = new Reference();
        ref.setReference("Patient/"+patient.getId());
        assessment.setSubject(ref);

        assessment.addIdentifier().setValue(correlationKey);

        assessment.setBasedOn(new Reference("Observation/"+observationId));
        assessment.setStatus(RiskAssessmentStatus.PRELIMINARY);
        Coding coding = new Coding("http://browser.ihtsdotools.org/","AIMODEL", "Inference of Sepsis");
        CodeableConcept concept = new CodeableConcept(coding);
        assessment.setCode(concept);
        RiskAssessmentPredictionComponent component = new RiskAssessmentPredictionComponent();
        String display = "Not Detected";
        if(sepsisResult.equals("1"))
          display = "Detected";
        component.setOutcome(new CodeableConcept(new Coding("http://browser.ihtsdotools.org/","1",display)));
        List<RiskAssessmentPredictionComponent> predictionsList = new ArrayList<RiskAssessmentPredictionComponent>();
        predictionsList.add(component);
        assessment.setPrediction(predictionsList);
        return assessment;
    }

    @ConsumeEvent(RiskAssessmentUtils.POST_TO_FHIR_SERVER)
    public void postRiskAssessmentToFhirServer(RiskAssessment assessment) {

        if(!this.postToFhirServer){
            log.warn("postRiskAssessment() will not post");
            return;
        }
    
        String riskAssessmentRequest = fhirCtx.newJsonParser().encodeResourceToString(assessment);
        Response response = fhirServerClient.postRiskAssessment(riskAssessmentRequest);
        if(response.getStatus() != 201)
          throw new RuntimeException("postRiskAssessment() fhir server response code = "+response.getStatus());

        String rAssessmentResponse = response.getEntity().toString();

        
        log.info("updateFhirServerwithRiskAssessment() rAssessmentResponse = \n"+rAssessmentResponse+"\n");
        RiskAssessment rAssessmentResponseObj =  fhirCtx.newJsonParser().parseResource(RiskAssessment.class,rAssessmentResponse);
        
    }

}
