package com.redhat.naps.utils;

public class RiskAssessmentUtils {
    public static final String MESSAGE_TYPE_COMMAND="generateRiskAssessment";
    public static final String MESSAGE_TYPE_EVENT="riskAssessmentEvent";
    public static final String PATIENT = "Patient";
    public static final String SEPSIS_RESPONSE = "SepsisResponse";
    public static final String OBSERVATION_ID = "ObservationId";
    public static final String RISK_ASSESSMENT = "RiskAssessment";
    public static final String CORRELATION_KEY = "CorrelationKey";

    public static final String CLOUD_EVENT_DATA = "data";

    public static final String COMMAND_CHANNEL = "generate-risk-assessment-command";
    public static final String EVENT_CHANNEL = "risk-assessment-event";

    public static final String POST_TO_FHIR_SERVER = "com.redhat.naps.postToFhirServer";

}
