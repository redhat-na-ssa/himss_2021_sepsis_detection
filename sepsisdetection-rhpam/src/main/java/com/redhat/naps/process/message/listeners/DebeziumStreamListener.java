package com.redhat.naps.process.message.listeners;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;



import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

// https://github.com/hapifhir/hapi-fhir/blob/master/hapi-fhir-base/src/main/java/ca/uhn/fhir/context/FhirContext.java
import ca.uhn.fhir.context.FhirContext;
import io.smallrye.openapi.api.util.FilterUtil;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RiskAssessment;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.r4.model.RiskAssessment.RiskAssessmentPredictionComponent;
import org.hl7.fhir.r4.model.RiskAssessment.RiskAssessmentStatus;

import com.redhat.naps.process.FhirProcessMgmt;
import com.redhat.naps.process.util.FHIRUtil;

import com.redhat.naps.process.model.PatientVitals;
import com.redhat.naps.process.model.SepsisResponse;



@Component
public class DebeziumStreamListener {

    private final static Logger log = LoggerFactory.getLogger(DebeziumStreamListener.class);

    private static FhirContext fhirCtx = FhirContext.forR4();

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Value(value = "${fhir.server.url}")
    private String fhirURL;

    @Value(value = "${AIModel.server.url}")
    private String aimodelUrl;

    @Autowired
    FhirProcessMgmt fhirProcessMgmt;

    @Autowired
    RestTemplate template;

    private SepsisResponse invokeAIModel(PatientVitals vitals) {
        String url = aimodelUrl;
        SepsisResponse response = template.postForEntity(url, vitals, SepsisResponse.class).getBody();
        return response;
    }

    private Patient getPatientFromFHIRDB(String id) {
        String url = fhirURL+"/fhir/Patient/"+id;

        Patient patient = template.getForEntity(url, Patient.class).getBody();
        return patient;
    }

    private List<Observation> getTimeBoxedObservation(Patient patient) {
        List<Observation> obsList = new ArrayList<>();
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE,-1);
        Date timeBoxDate = instance.getTime();
        String patientID = patient.getId();
        System.out.println("Patient Id : " + patientID);

        String url = fhirURL+"/fhir/Observation?patient="+patientID+"&_pretty=true";
       // System.out.println("URL : " + url);
        String bundleStr = template.getForEntity(url,String.class).getBody();
       // System.out.println("Bundle Result : " + bundleStr);
        Bundle newBundle = fhirCtx.newJsonParser().parseResource(Bundle.class, bundleStr);
        for(BundleEntryComponent component : newBundle.getEntry()){
             if(component.getResource() instanceof Observation)
             {
                Observation obs = (Observation) component.getResource();
                if(obs.getMeta().getLastUpdatedElement().after(timeBoxDate)) // Adding Observation only if its 24 hrs
                  obsList.add(obs);
             }
         }
        return obsList;
    }

    private PatientVitals buildPatientVitals(Patient patient) {
        List<Observation> obsList = getTimeBoxedObservation(patient);
        return _buildPatientVitals(patient, obsList);
    }

    private PatientVitals _buildPatientVitals(Patient patient,List<Observation> timeBoxedObservations) {
        PatientVitals vitals = new PatientVitals();
        //set Age
        /* Calendar currentDate = Calendar.getInstance();
        Calendar birthDate = Calendar.getInstance();

        System.out.println("********************   Birth Year : " + patient.getBirthDateElement().getYear() + " , " + currentDate.get(Calendar.YEAR) + " , " + birthDate.get(Calendar.YEAR));
        int years = currentDate.get(Calendar.YEAR) - patient.getBirthDateElement().getYear();
        vitals.setAge(years+"");
        vitals.setGender(patient.getGender().getDisplay()); */

        for(Observation observation : timeBoxedObservations)
        {
              for(Coding coding :  observation.getCode().getCoding())
              {
                  System.out.println("Coding Display : " + coding.getDisplay());
                  if(coding.getDisplay().equals(FHIRUtil.TEMP_CODE_STRING))
                      vitals.setTemp(observation.getValueQuantity().getValue().doubleValue());
                  if(coding.getDisplay().equals(FHIRUtil.HR_CODE_STRING))
                      vitals.setHr(observation.getValueQuantity().getValue().doubleValue());
                  if(coding.getDisplay().equals(FHIRUtil.BLOOD_PRESSURE_STRING))
                  {
                         for(ObservationComponentComponent component :  observation.getComponent())
                         {
                               for(Coding code : component.getCode().getCoding())
                               {
                                   if(code.getDisplay().equals(FHIRUtil.SBP_CODE_STRING))
                                       vitals.setSbp(component.getValueQuantity().getValue().doubleValue());
                                   if(code.getDisplay().equals(FHIRUtil.DBP_CODE_STRING))
                                       vitals.setDbp(component.getValueQuantity().getValue().doubleValue());
                               }
                         }
                  }
                  if(coding.getDisplay().equals(FHIRUtil.RESPRATE_CODE_STRING))
                      vitals.setResp(observation.getValueQuantity().getValue().doubleValue());
                  if(coding.getDisplay().equals(FHIRUtil.O2SAT_CODE_STRING))
                      vitals.setO2Sat(observation.getValueQuantity().getValue().doubleValue());
              }
        }
        return vitals;
    }


    private String getPatientIdfromObservation(Observation observation) {
        if(observation.getSubject() != null) {
            String ref = observation.getSubject().getReference();
            return ref.split("/")[1];
         }
         return null;
    }

    private String getPatientId(Bundle bundle) {
        for(BundleEntryComponent component : bundle.getEntry())
        {
            if(component.getResource() instanceof Patient)
            {
                Patient patient = (Patient) component.getResource();
                return patient.getId();
            }
            if(component.getResource() instanceof Observation)
            {
                Observation observation = (Observation) component.getResource();
                if(observation.getSubject() != null)
                {
                    String ref = observation.getSubject().getReference();
                    return ref.split("/")[1];
                }
            }

        }
        return "0";
    }

    private RiskAssessment createRiskAssessment(Patient patient,String sepsisResult,String observationId) {
        RiskAssessment assessment = new RiskAssessment();

        Reference ref = new Reference();
        ref.setReference("Patient/"+patient.getId());
        assessment.setSubject(ref);
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


    private RiskAssessment updateFhirDBwithRiskAssessment(RiskAssessment assessment) {
        String url = fhirURL+"/fhir/RiskAssessment";
        System.out.println("**********************************************************");
        String riskAssessmentRequest = fhirCtx.newJsonParser().encodeResourceToString(assessment);
        System.out.println(url);
        System.out.println(riskAssessmentRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(riskAssessmentRequest, headers);
        String riskAssesmentStr = template.postForEntity(url, entity, String.class).getBody();
        System.out.println("**********************************************************");
        System.out.println(riskAssesmentStr);
        System.out.println("**********************************************************");
        RiskAssessment assessment2 =  fhirCtx.newJsonParser().parseResource(RiskAssessment.class,riskAssesmentStr);
        return assessment2;
    }


    @KafkaListener(topics = "${listener.destination.debezium-stream}", containerFactory = "debeziumListenerContainerFactory")
    public void processMessage(@Payload String cloudEvent, 
                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                               @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, 
                               Acknowledgment ack) throws IOException {

        GZIPInputStream is = null;
        try {
            JsonNode rootNode = objectMapper.readTree(cloudEvent);
            JsonNode after = rootNode.get("data").get("payload").get("after");
            JsonNode resType = after.get("res_type");
            log.debug("processMessage() topic = "+topic+" : resType = "+resType.asText());
            if(FHIRUtil.PATIENT.equals(resType.asText())) {
                JsonNode resId = after.get("res_id");
                JsonNode resText = after.get("res_text");
                byte[] bytes = resText.binaryValue();
                is = new GZIPInputStream(new ByteArrayInputStream(bytes));
                String fhirJson = IOUtils.toString(is, "UTF-8");
                Patient oEvent = fhirCtx.newJsonParser().parseResource(Patient.class, fhirJson);
                oEvent.setId(resId.asText());
                log.info("processMessage() bytes length = "+bytes.length+ " : fhir json = \n"+ fhirJson+"\n fhir resourceType: "+oEvent.getResourceType().name());

                fhirProcessMgmt.startProcess(oEvent);
            } else {
                log.warn("Will not process message with FHIR type: "+resType.asText());
            }
        }catch(Exception x) {
            log.error("Unable to process the following debezium stream event: \n"+cloudEvent);
            x.printStackTrace();
            
        }finally {
            if(is != null)
                is.close();
            ack.acknowledge();
        }

    }
}
