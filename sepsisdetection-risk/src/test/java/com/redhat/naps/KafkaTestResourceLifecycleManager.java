package com.redhat.naps;

import java.util.HashMap;
import java.util.Map;

import com.redhat.naps.utils.RiskAssessmentUtils;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;

public class KafkaTestResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {

    @Override
    public Map<String, String> start() {
        Map<String, String> env = new HashMap<>();
        Map<String, String> props1 = InMemoryConnector.switchIncomingChannelsToInMemory(RiskAssessmentUtils.COMMAND_CHANNEL);  
        Map<String, String> props2 = InMemoryConnector.switchOutgoingChannelsToInMemory(RiskAssessmentUtils.EVENT_CHANNEL);   
        env.putAll(props1);
        env.putAll(props2);
        return env;  
    }

    @Override
    public void stop() {
        InMemoryConnector.clear();  
    }
    
}
