package com.redhat.naps.process.util;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Property;
import org.hl7.fhir.r4.model.RiskAssessment;
import org.hl7.fhir.r4.model.RiskAssessment.RiskAssessmentPredictionComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RiskAssessmentUtil {
    
    private static Logger log = LoggerFactory.getLogger(RiskAssessmentUtil.class);

    public static String getCode(RiskAssessment rAssessment) {
        RiskAssessmentPredictionComponent raPredictionComponent = rAssessment.getPredictionFirstRep();
        Property cProp = raPredictionComponent.getOutcome().getChildByName("coding");
        Coding coding = (Coding) cProp.getValues().get(0);
        String code = coding.getCode();
        log.info("getCode() code = "+code);
        return code;
    }
    
}
