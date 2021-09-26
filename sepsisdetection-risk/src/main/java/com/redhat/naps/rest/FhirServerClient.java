package com.redhat.naps.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

// As per:  https://www.hl7.org/fhir/resourcelist.html
@Path("/fhir")
@RegisterRestClient
public interface FhirServerClient {

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

    
}
