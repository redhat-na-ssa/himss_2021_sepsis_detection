package com.redhat.naps;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.naps.rest.FhirServerClient;
import com.redhat.naps.utils.RiskAssessmentUtils;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RiskAssessment;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.RiskAssessment.RiskAssessmentPredictionComponent;
import org.hl7.fhir.r4.model.RiskAssessment.RiskAssessmentStatus;
import org.jboss.logging.Logger;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.jackson.JsonFormat;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.vertx.ConsumeEvent;
import io.quarkus.vertx.core.runtime.VertxCoreRecorder;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import ca.uhn.fhir.context.FhirContext;

@ApplicationScoped
@Path("/")
public class RiskAssessmentService {

    private static final Logger log = Logger.getLogger("RiskAssessmentService");
    private static final String SUBSCRIPTION_JSON_FILE_NAME = "fhir_subscription.json";
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static FhirContext fhirCtx = FhirContext.forR4();

    @Inject
    @Channel(RiskAssessmentUtils.EVENT_CHANNEL)
    Emitter<String> eventChannel;

    @Inject
    @RestClient
    FhirServerClient fhirServerClient;

    @ConfigProperty(name="com.redhat.naps.rest.FhirServerClient/mp-rest/url")
    String fhirServerClientUrl;

    @ConfigProperty(name=RiskAssessmentUtils.POST_TO_FHIR_SERVER, defaultValue = "true")
    boolean postToFhirServer;

    @ConfigProperty(name="com.redhat.naps.rest.smilecdr.authNuserIdpasswd")
    String authNuserIdpasswd;

    @ConfigProperty(name="com.redhat.naps.rest.smilecdr.millisSleepForSmileCDRcall", defaultValue = "30000")
    private long millisSleepForSmileCDRcall;

    @Inject
    EventBus eventBus;

    public void start(@Observes StartupEvent ev) {
        // Prevents the following:
        //   com.fasterxml.jackson.databind.exc.InvalidDefinitionException: \
        //   No serializer found for class io.cloudevents.core.data.BytesCloudEventData and no properties discovered to create BeanSerializer \
        //   (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS) (through reference chain: io.cloudevents.core.v1.CloudEventV1["data"])
        objectMapper.registerModule(JsonFormat.getCloudEventJacksonModule());

        System.setProperty("com.redhat.naps.rest.smilecdr.authNuserIdpasswd", authNuserIdpasswd);
        int sNum = 0;
        for(int t=0; t<5; t++){
            try{
                Thread.sleep(this.millisSleepForSmileCDRcall);
                sNum = this.getSubscriptionListSizeFromSmileCDR();
                if(sNum < 1){
                    postSubscriptionToSmileCDR();
                    sNum++;
                }else {
                    log.info("start() no need to post subscription to SmileCDR. # of subs = "+sNum);
                }
                break;
            }catch(Throwable x){
                log.error("Following thrown when attempting to get subscription list from SmileCDR: "+x.getMessage());
            }
        }
        if(sNum == 0)
          throw new RuntimeException("unable to properly start due to problems managing subscriptions on SmileCDR server");
    }

    @POST
    @Path("/sanityCheck")
    public String sanityCheck(){
        return "good to go!";
    };

    private int getSubscriptionListSizeFromSmileCDR(){
        Response responseObj = null;
        try{
            responseObj = fhirServerClient.getSubscriptions();
            String sJson = responseObj.readEntity(String.class);
            Bundle bObj = this.fhirCtx.newJsonParser().parseResource(Bundle.class, sJson);
            List<BundleEntryComponent> becs = bObj.getEntry();
            log.info("start() # of Subscriptions = "+becs.size());
            return becs.size();
        }finally {
            if(responseObj != null)
              responseObj.close();
        }
    }

    private String postSubscriptionToSmileCDR(){
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Response responseObj = null;
        try {
            InputStream sResource = loader.getResourceAsStream(SUBSCRIPTION_JSON_FILE_NAME);
            String sJson = IOUtils.toString(sResource, "UTF-8");
            responseObj = fhirServerClient.postSubscription(sJson);
            String smileJson = responseObj.readEntity(String.class);
            Subscription sObj = this.fhirCtx.newJsonParser().parseResource(Subscription.class, smileJson);
            log.infov("postSubscriptionToSmileCDR() just posted subscription with id {0}, Response status = {1} ", sObj.getId(), responseObj.getStatus());
            return sObj.getId();
        }catch(Exception x){
            x.printStackTrace();
            throw new RuntimeException("Unable to POST subscription to SmileCDR server: "+SUBSCRIPTION_JSON_FILE_NAME);
        }finally {
            if(responseObj != null)
              responseObj.close();
        }
    }

    public void publishRiskAssessment(Patient patient, String sepsisResponse, String observationId, String correlationKey) throws JsonProcessingException {
        log.info("createRiskAssessment() patient = "+patient.getId()+" : sepsisResponse = "+sepsisResponse+" : obsId = "+observationId);
        String uid = UUID.randomUUID().toString();
        RiskAssessment assessment = createRiskAssessment(patient, sepsisResponse, observationId, correlationKey);

        String cEventString = generateCloudEventJson(uid, assessment, RiskAssessmentUtils.MESSAGE_TYPE_EVENT);
        
        Message<String> record = KafkaRecord.of(uid, cEventString);

        eventChannel.send(record);

        eventBus.send(RiskAssessmentUtils.POST_TO_FHIR_SERVER, assessment);
        //postRiskAssessmentToFhirServer(assessment);
    }

    private String generateCloudEventJson(String uid, RiskAssessment assessment, String messageType ) throws JsonProcessingException {

        String assessmentPayload = fhirCtx.newJsonParser().setPrettyPrint(false).encodeResourceToString(assessment);

        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put(RiskAssessmentUtils.RISK_ASSESSMENT, assessmentPayload);
        String cloudEventPayload = objectMapper.writeValueAsString(rootNode);

        CloudEvent cloudEvent = CloudEventBuilder.v1()
            .withId(uid)
            .withSource(URI.create("http://riskassessment"))
            .withType(messageType)
            .withTime(OffsetDateTime.now())
            .withData(cloudEventPayload.getBytes())
            .build();

        return objectMapper.writeValueAsString(cloudEvent);
    }

    private RiskAssessment createRiskAssessment(Patient patient, String sepsisResponse, String observationId, String correlationKey) {

        RiskAssessment assessment = new RiskAssessment();

        Reference ref = new Reference();
        ref.setReference(patient.getId());
        assessment.setSubject(ref);

        assessment.addIdentifier().setValue(correlationKey);

        assessment.setBasedOn(new Reference("Observation/"+observationId));
        assessment.setStatus(RiskAssessmentStatus.PRELIMINARY);
        Coding coding = new Coding("http://browser.ihtsdotools.org/","AIMODEL", "Inference of Sepsis");
        CodeableConcept concept = new CodeableConcept(coding);
        assessment.setCode(concept);
        RiskAssessmentPredictionComponent component = new RiskAssessmentPredictionComponent();
        String display = "Not Detected";
        if(sepsisResponse.equals("1"))
          display = "Detected";
        component.setOutcome(new CodeableConcept(new Coding("http://browser.ihtsdotools.org/","1",display)));
        List<RiskAssessmentPredictionComponent> predictionsList = new ArrayList<RiskAssessmentPredictionComponent>();
        predictionsList.add(component);
        assessment.setPrediction(predictionsList);
        return assessment;
    }

    @ConsumeEvent(RiskAssessmentUtils.POST_TO_FHIR_SERVER)
    @Blocking
    public void postRiskAssessmentToFhirServer(RiskAssessment assessment) throws IOException {

        if(!this.postToFhirServer){
            log.warn("postRiskAssessment() will not post");
            return;
        }
    
        String riskAssessmentRequest = fhirCtx.newJsonParser().encodeResourceToString(assessment);
        log.info("\n\n"+riskAssessmentRequest+"\n\n");
        Response responseObj = null;
        try {
            responseObj = fhirServerClient.postRiskAssessment(riskAssessmentRequest);
        }catch(WebApplicationException x){
            responseObj = x.getResponse();
            log.error("postRiskAssessmentToFhirServer() Error with following status when posting to fhir server: "+responseObj.getStatus() );
            String rEntity = responseObj.readEntity(String.class);
            log.error("postRiskAssessmentToFhirServer() error message = "+rEntity);

        }catch(ProcessingException x){
            log.error("postRiskAssessmentToFhirServer() The following error thrown: "+x.getMessage());
            x.printStackTrace();
        }finally {
            if(responseObj != null)
              responseObj.close();
        }
        /*s
        Uni<Response> response = fhirServerClient.postRiskAssessmentAsync(riskAssessmentRequest);
        response
          .onItem().invoke(r -> {
            log.info("response status = "+r.getStatus());
            if(r.getStatus() != 201)
              throw new RuntimeException("postRiskAssessment() fhir server response code = "+r.getStatus());

              String rAssessmentResponse = r.getEntity().toString();
              r.close();
              
              log.info("postRiskAssessmentToFhirServer() rAssessmentResponse = \n"+rAssessmentResponse+"\n");
              RiskAssessment rAssessmentResponseObj =  fhirCtx.newJsonParser().parseResource(RiskAssessment.class,rAssessmentResponse);
          })
          .onFailure().invoke(x -> {
                x.printStackTrace();
          });
          */

        
    }

}
