package com.redhat.naps.process.commands;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.redhat.naps.process.JbpmDeploymentServiceConfiguration;
import com.redhat.naps.process.model.PatientVitals;
import com.redhat.naps.process.util.FHIRUtil;

import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;
import ca.uhn.fhir.context.FhirContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;

import org.kie.api.executor.Command;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetObservationsSignalEventCommand implements Command {

    private final static Logger log = LoggerFactory.getLogger(GetObservationsSignalEventCommand.class);

    private static FhirContext fhirCtx = FhirContext.forR4();

    private String fhirURL;
    private String fhirServerUserId = "admin";
    private String fhirServerUserPassword = "password";

    RestTemplate template;

    public GetObservationsSignalEventCommand(){
        template = JbpmDeploymentServiceConfiguration.ctx.getBean(RestTemplate.class);
        if(template == null)
          throw new RuntimeException("unable to obtain RestTemplate");


        fhirURL = System.getProperty(FHIRUtil.FHIR_SERVER_URL);
        if(StringUtils.isEmpty(fhirURL))
          throw new RuntimeException("Unable to obtain fhirURL with system property: "+FHIRUtil.FHIR_SERVER_URL);
        else {
            log.info("GetObservationsSignalCommand fhirURL = "+fhirURL);
        }

        if(System.getProperty(FHIRUtil.FHIR_SERVER_USER_ID) != null)
          this.fhirServerUserId = System.getProperty(FHIRUtil.FHIR_SERVER_USER_ID);
        
        if(System.getProperty(FHIRUtil.FHIR_SERVER_USER_PASSWORD) != null)
          this.fhirServerUserPassword = System.getProperty(FHIRUtil.FHIR_SERVER_USER_PASSWORD);

        BasicAuthenticationInterceptor basicAuthI = new BasicAuthenticationInterceptor(fhirServerUserId, fhirServerUserPassword);
        template.getInterceptors().add(basicAuthI);
    }

    @Override
    public ExecutionResults execute(CommandContext ctx) throws Exception {

        // NOTE:  Make sure this is the actual deploymentId and not an kieserver alias
        String deploymentId = (String) ctx.getData("deploymentId");
        if (deploymentId == null) {
            deploymentId = (String) ctx.getData("DeploymentId");
        }

        String signal = (String) ctx.getData("Signal");

        Long processInstanceId = (Long) ctx.getData("processInstanceId");
        if (processInstanceId == null) {
            processInstanceId = (Long) ctx.getData("ProcessInstanceId");
        }

        Patient patientObj = (Patient)ctx.getData(FHIRUtil.EVENT);
        if(patientObj == null)
          throw new RuntimeException("Context must include: "+FHIRUtil.PATIENT);

        List<Observation> obsList = getTimeBoxedObservation(patientObj);


        PatientVitals vitals = buildPatientVitals(patientObj, obsList);

        //TO-DO:  When persisting this list of Observations as part of process instance, upon retrieval of pInstanceVariables ..... the server thread is placed in an infinite loop of JSON processing
        //parameters.put("observationList", obsList);
        
        RuntimeManager runtimeManager = RuntimeManagerRegistry.get().getManager(deploymentId);
        if (runtimeManager == null) {
            throw new IllegalArgumentException("No runtime manager found for deployment id " + deploymentId);  
        }
        RuntimeEngine engine = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));        
        try {
            log.info("execute() signalling ... : deploymentId = "+deploymentId+" : pInstanceId = "+processInstanceId+" : signal = "+signal);
            engine.getKieSession().signalEvent(signal, vitals, processInstanceId);
            
            return new ExecutionResults();
        } finally {
            runtimeManager.disposeRuntimeEngine(engine);
        }
    }


    private List<Observation> getTimeBoxedObservation(Patient patient) {
        List<Observation> obsList = new ArrayList<>();
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE,-1);
        Date timeBoxDate = instance.getTime();
        String patientID = patient.getId();
        
        String url = fhirURL+"/Observation?patient="+patientID+"&_pretty=true";
        log.info("getTimeBoxedObservation() fhirUrl endpoint = "+url);

        String bundleStr = template.getForEntity(url,String.class).getBody();
        Bundle newBundle = fhirCtx.newJsonParser().parseResource(Bundle.class, bundleStr);
        for(BundleEntryComponent component : newBundle.getEntry()){
            if(component.getResource() instanceof Observation) {
                Observation obs = (Observation) component.getResource();
                if(obs.getMeta().getLastUpdatedElement().after(timeBoxDate)) // Adding Observation only if its 24 hrs
                obsList.add(obs);
            }
        }
        log.info("getTimedBoxedObservation() Patient Id : " + patientID+" # of Observations = "+obsList.size());
        return obsList;
    }

    private PatientVitals buildPatientVitals(Patient patient,List<Observation> timeBoxedObservations) {

        String obsId = timeBoxedObservations.get(0).getId().split("/")[timeBoxedObservations.get(0).getId().split("/").length - 1];
        PatientVitals vitals = new PatientVitals();
        vitals.setObservationId(obsId);
        
        //set Age
        /* Calendar currentDate = Calendar.getInstance();
        Calendar birthDate = Calendar.getInstance();

        log.info("********************   Birth Year : " + patient.getBirthDateElement().getYear() + " , " + currentDate.get(Calendar.YEAR) + " , " + birthDate.get(Calendar.YEAR));
        int years = currentDate.get(Calendar.YEAR) - patient.getBirthDateElement().getYear();
        vitals.setAge(years+"");
        vitals.setGender(patient.getGender().getDisplay()); */

        for(Observation observation : timeBoxedObservations) {
              for(Coding coding :  observation.getCode().getCoding()) {
                  log.info("Coding Display : " + coding.getDisplay());
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
