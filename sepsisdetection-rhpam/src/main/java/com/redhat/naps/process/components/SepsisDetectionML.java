package com.redhat.naps.process.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.redhat.naps.process.model.PatientVitals;
import com.redhat.naps.process.model.SepsisResponse;

@Component
public class SepsisDetectionML {

    private final static Logger log = LoggerFactory.getLogger(SepsisDetectionML.class);

    @Value(value = "${AIModel.server.url}")
    private String aimodelUrl;

    @Autowired
    RestTemplate template;

    public SepsisResponse invokeAIModel(PatientVitals vitals) {
        SepsisResponse response = template.postForEntity(aimodelUrl, vitals, SepsisResponse.class).getBody();
        return response;
    }

}
