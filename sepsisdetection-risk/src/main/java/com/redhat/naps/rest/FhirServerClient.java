package com.redhat.naps.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.Consumes;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

// As per:  https://www.hl7.org/fhir/resourcelist.html
@Path("/fhir")
@RegisterRestClient
public interface FhirServerClient {

    @POST
    @Path("/RiskAssessment")
    @Consumes("application/fhir+json")
    Response postRiskAssessment( String raJson);

    
}
