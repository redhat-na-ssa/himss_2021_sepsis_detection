package com.redhat.naps.rest;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;

import java.util.Base64;
import java.util.Map;
import java.util.Properties;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

// As per:  https://www.hl7.org/fhir/resourcelist.html
@Path("/")
@ClientHeaderParam(name = "Authorization", value = "{lookupAuth}")
@RegisterRestClient
public interface FhirServerClient {
    
    @GET
    @Path("/Subscription")
    @Produces("application/fhir+json")
    Response getSubscriptions();

    @POST
    @Path("/Subscription")
    @Consumes("application/fhir+json")
    Response postSubscription( String raJson);

    @GET
    @Path("/RiskAssessment")
    @Produces("application/fhir+json")
    Uni<Response> getRiskAssessments();


    @POST
    @Path("/RiskAssessment")
    @Consumes("application/fhir+json")
    Uni<Response> postRiskAssessmentAsync( String raJson);

    @POST
    @Path("/RiskAssessment")
    @Consumes("application/fhir+json")
    Response postRiskAssessment( String raJson);

    default String lookupAuth() {
        /*
        Properties props = System.getProperties();
        for(Map.Entry<?,?> entry : props.entrySet()){
            System.out.println(entry.getKey()+" : "+entry.getValue());

        }*/
        String authNuserIdpasswd = System.getProperty("com.redhat.naps.rest.smilecdr.authNuserIdpasswd");
        System.out.println("lookupAuth() authNuserIdpasswd = "+authNuserIdpasswd);

    return "Basic " + 
         Base64.getEncoder().encodeToString(authNuserIdpasswd.getBytes());
    }

    
}
