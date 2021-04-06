package com.redhat.cajun.navy.process;

import org.jbpm.kie.services.impl.query.SqlQueryDefinition;
import org.jbpm.services.api.query.QueryNotFoundException;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.api.query.model.QueryDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JbpmQueryConfiguration {

    @Autowired
    private QueryService queryService;

    private String signalsByCorrelationKey = "SELECT e.instanceid, c.name, e.element " +
            "FROM eventtypes e " +
            "INNER JOIN correlationkeyinfo c ON (c.processinstanceid = e.instanceid)";

    @Bean
    public QueryDefinition signalsByCorrelationKeyQuery() {
        QueryDefinition signalsByCorrelationKeyQuery;
        try {
            signalsByCorrelationKeyQuery = queryService.getQuery("signalsByCorrelationKey");
        } catch (QueryNotFoundException e) {
            signalsByCorrelationKeyQuery = new SqlQueryDefinition("signalsByCorrelationKey", "source");
            signalsByCorrelationKeyQuery.setExpression(signalsByCorrelationKey);
            queryService.registerQuery(signalsByCorrelationKeyQuery);
        }
        return signalsByCorrelationKeyQuery;
    }
}
