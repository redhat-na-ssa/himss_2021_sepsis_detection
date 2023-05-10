package com.redhat.naps;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.naps.rest.FhirServerClient;
import com.redhat.naps.utils.RiskAssessmentUtils;

import org.hl7.fhir.r4.model.Property;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.RiskAssessment;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.RiskAssessment.RiskAssessmentPredictionComponent;

// https://github.com/hapifhir/hapi-fhir/blob/master/hapi-fhir-base/src/main/java/ca/uhn/fhir/context/FhirContext.java
import ca.uhn.fhir.context.FhirContext;


/*
  NOTE:  Requires the FHIR Server to be running.  Information about the FHIR server can be found at the following:

  1)  https://hapifhir.io/hapi-fhir/docs/server_jpa/get_started.html
  2)  https://github.com/hapifhir/hapi-fhir-jpaserver-starter
*/ 

@QuarkusTest
public class FhirServerTest {

    private static Logger log = LoggerFactory.getLogger(FhirServerTest.class);
    private static FhirContext fhirCtx = FhirContext.forR4();

    @Inject
    @RestClient
    FhirServerClient fhirClient;


    @Test
    public void riskAssessmentPredictionTest() throws IOException {

        String filePath = "/fhir/RiskAssessment.json";
        InputStream fStream = null;
        String oJson = null;
        try {
            fStream = this.getClass().getResourceAsStream(filePath);
            if(fStream != null){
                oJson = IOUtils.toString(fStream, "UTF-8");
                RiskAssessment rAssessment = (RiskAssessment) fhirCtx.newJsonParser().parseResource(oJson);
                RiskAssessmentPredictionComponent raPredictionComponent = rAssessment.getPredictionFirstRep();
                Property cProp = raPredictionComponent.getOutcome().getChildByName("coding");
                Coding coding = (Coding) cProp.getValues().get(0);
                String code = coding.getCode();
                log.info("riskAssessmentPredictionTest() code = "+code);
            }else {
                log.error("riskAssessmentTest() resource not found: "+filePath);
                return;
            }
        }finally {
            if(fStream != null)
                fStream.close();
        }
    }

    @Disabled
    @Test
    public void riskAssessmentTest() throws IOException {

        // POST
        String filePath = "/fhir/RiskAssessment.json";
        InputStream fStream = null;
        String oJson = null;
        try {
            fStream = this.getClass().getResourceAsStream(filePath);
            if(fStream != null){
                oJson = IOUtils.toString(fStream, "UTF-8");
                System.out.println("oJson = "+oJson);
            }else {
                log.error("riskAssessmentTest() resource not found: "+filePath);
                return;
            }
        }finally {
            if(fStream != null)
            fStream.close();
        }
        Response response = null;
        try {
            response = fhirClient.postRiskAssessment(oJson);
            log.info("Posted RiskAssessment response code = "+response.getStatus());
            
        }catch(WebApplicationException x){
            x.printStackTrace();
            response = x.getResponse();
            log.error("observationTest() error status = "+response.getStatus()+"  when posting the following file content to FhirServer: "+filePath);
            log.error("observationTest() error message = "+IOUtils.toString((InputStream)response.getEntity(), "UTF-8"));
        }
        response.close();

        // GET
        /*
        try {
            response = fhirClient.getObservations();
            assertTrue(response.getStatus() == 200 || response.getStatus() == 201);
            String obsString = IOUtils.toString((InputStream)response.getEntity(), "UTF-8");
            //log.info("observationTest() observations = "+obsString);
            response.close();
            
            Bundle bObj = fhirCtx.newJsonParser().parseResource(Bundle.class, obsString );
            List<BundleEntryComponent> becs = bObj.getEntry();
            log.info("observationTest() total # of observations = "+becs.size());
            assertTrue(becs.size() > 0);
        }catch(WebApplicationException x){
            response = x.getResponse();
            log.error("observationTest() error status = "+response.getStatus()+"  when getting Observation from FhirServer");
            log.error("observationTest() error message = "+IOUtils.toString((InputStream)response.getEntity(), "UTF-8"));
        }
        response.close();
        */
    }
    
}
