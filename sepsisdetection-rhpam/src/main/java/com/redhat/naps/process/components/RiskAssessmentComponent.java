package com.redhat.naps.process.components;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RiskAssessment;
import org.hl7.fhir.r4.model.RiskAssessment.RiskAssessmentPredictionComponent;
import org.hl7.fhir.r4.model.RiskAssessment.RiskAssessmentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.context.FhirContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class RiskAssessmentComponent { 
    
    private static final Logger log = LoggerFactory.getLogger("RiskAssessmentComponent");

    private static FhirContext fhirCtx = FhirContext.forR4();

    @Value(value = "${fhir.server.url}")
    private String fhirURL;

    @Autowired
    RestTemplate template;

    public RiskAssessment createRiskAssessment(Patient patient,String sepsisResult,String observationId) {
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

    public RiskAssessment updateFhirServerwithRiskAssessment(RiskAssessment assessment) {
        String url = fhirURL+"/fhir/RiskAssessment";
        String riskAssessmentRequest = fhirCtx.newJsonParser().encodeResourceToString(assessment);
        log.info("updateFhirServerwithRiskAssessment riskAssessment = \n"+riskAssessmentRequest+"\n");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(riskAssessmentRequest, headers);
        String rAssessmentResponse = template.postForEntity(url, entity, String.class).getBody();
        log.info("updateFhirServerwithRiskAssessment() rAssessmentResponse = \n"+rAssessmentResponse+"\n");
        RiskAssessment rAssessmentResponseObj =  fhirCtx.newJsonParser().parseResource(RiskAssessment.class,rAssessmentResponse);
        return rAssessmentResponseObj;
    }

    
}
