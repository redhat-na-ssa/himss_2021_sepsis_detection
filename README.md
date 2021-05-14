1. Start kafka (using community strimzi containers)
   `````
   $ podman-compose -f etc/docker-compose.yaml up -d
   `````

2. Execute rhpam sql scripts on database:
   `````
   $ podman exec -it etc_db_1 /bin/bash
   $ cd /opt/sql \
     && psql -U rhpam -d rhpam -f postgresql-jbpm-schema.sql \
     && psql -U rhpam -d rhpam -f task_assigning_tables_postgresql.sql \
     && psql -U rhpam -d rhpam -f postgresql-jbpm-lo-trigger-clob.sql \
     && psql -U rhpam -d rhpam -f quartz_tables_postgres.sql \
     && psql rhpam -c "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO rhpam;" \
     && psql rhpam -c "GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO rhpam;"
   `````

   TO-DO:  Create a RH-PAM image seeded with the above tables as per:  https://github.com/sclorg/postgresql-container/tree/generated/12#extending-image

2. Tear down pod:
   `````
   $ $ podman-compose -f etc/docker-compose.yaml down
   `````

3. Clone, build and deploy pneumonia-patient-processing-kjar (WHY IS THIS NECESSARY ?)
   `````
   $ git clone https://github.com/redhat-naps-da/pneumonia-patient-processing-kjar.git
   $ cd pneumonia-patient-processing-kjar
   $ mvn clean install
   $ mvn deploy \
        -DaltDeploymentRepository="nexus::default::http://admin:admin123@$NEXUS_ROUTE/repository/redhat-naps/"
   `````

4. Build and Start app
   `````
   $ mvn clean package -DskipTests && \
         java -jar target/pneumonia-patient-processing-pam-0.0.1.jar
   `````

## Test

1. Set environment variables to support testing:
   `````
   $ export KJAR_VERSION=1.0.3
   $ export KIE_SERVER_CONTAINER_NAME=fhir-bpm
   `````

2. Health Check Report
   `````
   $ curl -X GET -H 'Accept:application/json' localhost:8080/rest/server/healthcheck?report=true
   `````

3. Create a container in kie-server:
   `````
   $ sed "s/{KIE_SERVER_CONTAINER_NAME}/$KIE_SERVER_CONTAINER_NAME/g" config/kie_container.json \
     | sed "s/{KJAR_VERSION}/$KJAR_VERSION/g" \
     > /tmp/kie_container.json && \
     curl -X PUT -H 'Content-type:application/json' localhost:8080/rest/server/containers/$KIE_SERVER_CONTAINER_ID-$KJAR_VERSION -d '@/tmp/kie_container.json'
   `````

4. List containers
   `````
   $ curl -X GET http://localhost:8080/rest/server/containers
   `````

5. Start a business process
   `````
   $ curl -X POST localhost:8080/fhir/processes/sendSampleCloudEvent/azra12350
   `````

6. List cases in JSON representation:
   `````
   $ curl -X GET -H 'Accept:application/json' localhost:8080/rest/server/queries/cases/
   `````

7. List process definitions in JSON representation:
   `````
   $ curl -X GET -H 'Accept:application/json' localhost:8080/rest/server/containers/$KIE_SERVER_CONTAINER_NAME-$KJAR_VERSION/processes/
   `````
