package com.redhat.naps.rest;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;

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
