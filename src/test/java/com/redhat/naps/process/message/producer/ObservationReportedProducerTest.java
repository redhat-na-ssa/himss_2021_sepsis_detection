package com.redhat.naps.process.message.producer;

import io.cloudevents.core.builder.CloudEventBuilder;
import java.net.URI;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Encounter;
import io.cloudevents.CloudEvent;

// https://github.com/hapifhir/hapi-fhir/blob/master/hapi-fhir-base/src/main/java/ca/uhn/fhir/context/FhirContext.java
import ca.uhn.fhir.context.FhirContext;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {CloudEventProducer.class})
public class ObservationReportedProducerTest {

    private static FhirContext fhirCtx = FhirContext.forR4();

    @Autowired
    private CloudEventProducer producer;

    //@Test
    public void sendObservationReportToKafkaTest() {
        String topic = "observation-topic";
        String obsId = "observation12345";
        Observation obs = createInitialObservation(obsId);
        String obsString = fhirCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(obs);

        CloudEvent cEvent = CloudEventBuilder.v1()
                .withId("000")
                .withType("ObservationReportedEvent")
                .withSource(URI.create("http://example.com"))
                .withDataContentType("application/json")
                .withData(obsString.getBytes())
                .build();

        producer.send(topic, cEvent);

    }

    private Observation createInitialObservation(String obsId) {
        Observation obs = new Observation();

        Patient pt = new Patient();
        pt.setId("#1");
        pt.addName().setFamily("FAM");
        obs.getSubject().setReference("#1");
        obs.getContained().add(pt);

        Encounter enc = new Encounter();
        enc.setStatus(Encounter.EncounterStatus.ARRIVED);
        obs.getEncounter().setResource(enc);

        obs.setStatus(ObservationStatus.PRELIMINARY);
        obs.setId(obsId);

        return obs;
    }

}
