package com.redhat.naps.components;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.redhat.naps.process.model.PatientVitals;
import com.redhat.naps.process.util.FHIRUtil;

import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import ca.uhn.fhir.context.FhirContext;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;

public class PatientVitalsComponent {
    
    private static FhirContext fhirCtx = FhirContext.forR4();

    @Value(value = "${fhir.server.url}")
    private String fhirURL;

    @Autowired
    RestTemplate template;

    public List<Observation> getTimeBoxedObservation(Patient patient) {
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
    
    public PatientVitals buildPatientVitals(Patient patient,List<Observation> timeBoxedObservations) {
        PatientVitals vitals = new PatientVitals();
        //set Age
        /* Calendar currentDate = Calendar.getInstance();
        Calendar birthDate = Calendar.getInstance();

        System.out.println("********************   Birth Year : " + patient.getBirthDateElement().getYear() + " , " + currentDate.get(Calendar.YEAR) + " , " + birthDate.get(Calendar.YEAR));
        int years = currentDate.get(Calendar.YEAR) - patient.getBirthDateElement().getYear();
        vitals.setAge(years+"");
        vitals.setGender(patient.getGender().getDisplay()); */

        for(Observation observation : timeBoxedObservations) {
              for(Coding coding :  observation.getCode().getCoding()) {
                  System.out.println("Coding Display : " + coding.getDisplay());
                  if(coding.getDisplay().equals(FHIRUtil.TEMP_CODE_STRING))
                      vitals.setTemp(observation.getValueQuantity().getValue().doubleValue());
                  if(coding.getDisplay().equals(FHIRUtil.HR_CODE_STRING))
                      vitals.setHr(observation.getValueQuantity().getValue().doubleValue());
                  if(coding.getDisplay().equals(FHIRUtil.BLOOD_PRESSURE_STRING)) {
                         for(ObservationComponentComponent component :  observation.getComponent()) {
                               for(Coding code : component.getCode().getCoding()) {
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
    
}
