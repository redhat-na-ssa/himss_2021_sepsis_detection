package com.redhat.naps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.jackson.JsonFormat;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.ce.OutgoingCloudEventMetadata;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySink;
import io.smallrye.reactive.messaging.connectors.InMemorySource;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;

import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Message;

import ca.uhn.fhir.context.FhirContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.naps.utils.RiskAssessmentUtils;

import static org.awaitility.Awaitility.await;

@QuarkusTest
@QuarkusTestResource(KafkaTestResourceLifecycleManager.class)
public class CloudEventTest {

    private static final Logger log = LoggerFactory.getLogger("CloudEventTest");
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static FhirContext fhirCtx = FhirContext.forR4();


    @ConfigProperty(name="com.redhat.naps.test.work.dir")
    String workDir;

    @Inject @Any
    InMemoryConnector connector;

    @BeforeAll
    public static void setup() {
        // The following jackson configs are critical to ensure CloudEvents are (un)marshalled appropriately


        // Prevents the following: 
        //   com.fasterxml.jackson.databind.exc.MismatchedInputException: Cannot construct instance of `com.redhat.cajun.navy.rules.model.Mission` \
        //   (although at least one Creator exists): no String-argument constructor/factory method to deserialize from String value \
        //   ('eyJpiOjEsImxhc3RVcGRhdGUiOjE2MTExNzU2NjI3Njd9')
        // objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        // Prevents the following:
        //   com.fasterxml.jackson.databind.exc.InvalidDefinitionException: \
        //   No serializer found for class io.cloudevents.core.data.BytesCloudEventData and no properties discovered to create BeanSerializer \
        //   (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS) (through reference chain: io.cloudevents.core.v1.CloudEventV1["data"])
        objectMapper.registerModule(JsonFormat.getCloudEventJacksonModule());

    }

    @Test
    public void kafkaCloudEventTest() throws JsonProcessingException {
        
        // Send Patient as CloudEvent
        Patient patient = createPatient();
        String uid = UUID.randomUUID().toString();
        
        String cEventString = generateCloudEventJson(uid, patient, RiskAssessmentUtils.MESSAGE_TYPE_COMMAND);

        InMemorySource<Message<String>> generateRACommandSource = connector.source(RiskAssessmentUtils.COMMAND_CHANNEL);
        OutgoingCloudEventMetadata<String> cloudEventMetadata = OutgoingCloudEventMetadata.<String>builder()
        .withType(RiskAssessmentUtils.MESSAGE_TYPE_COMMAND)
        .withTimestamp(OffsetDateTime.now().toZonedDateTime())
        .build();
        Message<String> record = KafkaRecord.of(uid, cEventString).addMetadata(cloudEventMetadata);
        generateRACommandSource.send(record);

        // Consume CloudEvent with RiskAssessment
        InMemorySink consumeRASink = connector.sink(RiskAssessmentUtils.EVENT_CHANNEL);
        await()
          .atMost(Duration.ofSeconds(15))
          .<List<? extends Message<String>>>until(
              consumeRASink::received, 
              t -> t.size() == 1
            );
    }
    
    @Test
    public void writeCloudEventToFileTest() throws IOException {

        File workDirFile = new File(workDir);
        if(!workDirFile.exists()){
            workDirFile.mkdirs();
            log.info("writeCloudEventToFileTest() just created workDir = "+workDir);
        }

        Patient patient = createPatient();
        String uid = UUID.randomUUID().toString();

        String cEventString = generateCloudEventJson(uid, patient, RiskAssessmentUtils.MESSAGE_TYPE_COMMAND);

        String fileName = workDir + "/cEvent_"+uid;
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
        writer.append(cEventString);
        writer.close();
        log.info("generateCloudEvent() just wrote to: "+fileName);
    }

    private String generateCloudEventJson(String uid, Patient patient, String messageType ) throws JsonProcessingException {

        String patientPayload = fhirCtx.newJsonParser().setPrettyPrint(false).encodeResourceToString(patient);

        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put(RiskAssessmentUtils.PATIENT, patientPayload);
        rootNode.put(RiskAssessmentUtils.SEPSIS_RESULT, "1");
        rootNode.put(RiskAssessmentUtils.OBSERVATION_ID,"Observation/obs12345");
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

    private Patient createPatient() {

        Patient patient =  new Patient();
        patient.setId("#1");
        patient.addName().setFamily("FAM");
        return patient;

    }
}
