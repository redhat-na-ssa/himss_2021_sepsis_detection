package com.redhat.naps.process.message.listeners;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.hamcrest.CoreMatchers;
import org.jbpm.services.api.ProcessService;
import org.junit.Before;
import org.junit.Test;
import org.kie.internal.process.CorrelationKey;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Encounter;

// https://github.com/hapifhir/hapi-fhir/blob/master/hapi-fhir-base/src/main/java/ca/uhn/fhir/context/FhirContext.java
import ca.uhn.fhir.context.FhirContext;

public class CloudEventsListenerTest {

    private static FhirContext fhirCtx = FhirContext.forR4();

    private CloudEventsListener messageListener;

    @Mock
    private PlatformTransactionManager ptm;

    @Mock
    private TransactionStatus transactionStatus;

    @Mock
    private ProcessService processService;

    @Mock
    private Acknowledgment ack;

    @Captor
    private ArgumentCaptor<String> processIdCaptor;

    @Captor
    private ArgumentCaptor<CorrelationKey> correlationKeyCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> parametersCaptor;

    private final String processId = "pneumonia_patient_processing.Pneumonia_Case";
    
    @Before
    public void init() {
        initMocks(this);
        messageListener = new CloudEventsListener();
        setField(messageListener, null, ptm, PlatformTransactionManager.class);
        setField(messageListener, null, processService, ProcessService.class);
        setField(messageListener, "processId", processId, String.class);

        when(ptm.getTransaction(any())).thenReturn(transactionStatus);
        when(processService.startProcess(any(), any(), any(), any())).thenReturn(100L);
    }

    @Test
    public void testProcessIncidentReportedEventMessage() {

        String obsId = "observation12345";
        Observation obs = createInitialObservation(obsId);
        String obsString = fhirCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(obs);

        CloudEvent event = CloudEventBuilder.v1()
                .withId(obsId)
                .withType("ObservationReportedEvent")
                .withSource(URI.create("http://example.com"))
                .withDataContentType("application/json")
                .withData(obsString.getBytes())
                .build();

        messageListener.processMessage(event, "topic1", 1, ack);
        
        verify(processService).startProcess(any(), processIdCaptor.capture(), correlationKeyCaptor.capture(), parametersCaptor.capture());
        assertThat(processIdCaptor.getValue(), equalTo(processId));

        CorrelationKey correlationKey = correlationKeyCaptor.getValue();
        assertThat(correlationKey.getName(), equalTo("Observation/"+obsId));

        Map<String, Object> parameters = parametersCaptor.getValue();
        assertThat(parameters.size(), equalTo(1));
        verify(ack).acknowledge();
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
